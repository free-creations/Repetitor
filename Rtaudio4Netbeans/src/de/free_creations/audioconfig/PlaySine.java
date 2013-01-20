/*
 * Demo program which outputs a sinus waveform
 */
package de.free_creations.audioconfig;

import java.util.logging.Level;
import java.util.logging.Logger;
import rtaudio4java.AudioProcessor_Float32;
import rtaudio4java.AudioSystem;
import rtaudio4java.AudioSystemFactory;
import rtaudio4java.RtError;

/**
 * A simple audio synthesiser that opens an RtAudio instance
 * and plays a sinus-sound through a specified device. This object
 * is used to start a test sound when the user presses the button
 * labeled "test" in the configuration dialog.
 * <br/>
 * Usage:
 * <pre>
 *{@code
 *
 *   // build a sine synthesizer and let it play
 *   PlaySine synt = new PlaySine(apiType,device, offset, nChannels, bufferFrames, sampleRate);
 *
 *   // wait for 2 seconds
 *   Thread.sleep(2000);
 *
 *   // stop playing
 *   synt.stopPlaying();
 *
 *   // ...once stopped, the synt object cannot be used anymore...
 *
 * }
 * </pre>
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
class PlaySine {

  /** frequency in cycles per second. */
  private static final float frequency = 440.0F;
  /** the amplitude of the output signal  (must be less than 1.0)*/
  private static final float amplitude = 0.1F;
  private volatile AudioSystem dac;
  private final Object dacLock = new Object();
  private final int apiType;
  private final int device;
  private final int offset;
  private final int nChannels;
  private final int bufferFrames;
  private final int sampleRate;
  private final Thread startingThread;
  private final Thread stoppingThread;

  private class SineSynthesizer extends AudioProcessor_Float32 {

    private float[] outputBuff;
    private float[] waveTable;
    private int waveTablePtr = 0;
    private int nFrames;
    private int channelCount;

    private void prepareWaveTable(int waveLength) {
      waveTable = new float[waveLength];
      for (int i = 0; i < waveLength; i++) {
        waveTable[i] = amplitude * (float) Math.sin((2D * (double) i * Math.PI) / (double) waveLength);
      }
    }

    @Override
    public void onOpenStream(int samplingRate, int nFrames, int inputChannelCount,
            int outputChannelCount, boolean noninterleaved) {
      if (noninterleaved) {
        throw new RuntimeException("This version is not able to handle noninterleaved channels.");
      }

      this.nFrames = nFrames;
      this.channelCount = outputChannelCount;
      waveTablePtr = 0;

      // calculate how long one cycle is, for the requested frequency
      // with the given sampling rate.
      int waveLength = (int) (samplingRate / frequency);
      if (waveLength < 2) {
        throw new RuntimeException("frequency too high.");
      }
      // prepare a wave table holding one cycle
      prepareWaveTable(waveLength);
      // determine how long the output buffer must be
      outputBuff = new float[nFrames * outputChannelCount];
    }

    @Override
    public float[] process(float[] inputBuffer, double streamTime, int status) {

      int buffPtr = 0;
      for (int frame = 0; frame < nFrames; frame++) {
        for (int channel = 0; channel < channelCount; channel++) {
          outputBuff[buffPtr] = waveTable[waveTablePtr];
          buffPtr++;
        }
        waveTablePtr++;
        if (waveTablePtr >= waveTable.length) {
          waveTablePtr = 0;
        }
      }
      return outputBuff;
    }

    @Override
    public void onCloseStream() {
    }

    @Override
    public void onStartStream() throws Throwable {
    }

    @Override
    public void onStopStream() throws Throwable {
    }
  }

  private class Starter implements Runnable {

    public void run() {
      try {
        synchronized (dacLock) {
          dac = AudioSystemFactory.getRtAudioInstance(apiType);
        }


        SineSynthesizer synthesizer = new SineSynthesizer();
        // print messages to stderr.
        dac.showWarnings(true);

        // Set our stream parameters for output only.
        AudioSystem.StreamParameters oParams = new AudioSystem.StreamParameters(
                device, //deviceId
                offset, //firstChannel
                nChannels //number of channels
                );
        AudioSystem.StreamOptions options = new AudioSystem.StreamOptions(
                false, //noninterleaved
                false, // minimizeLatency
                false,// hogDevice
                false,// scheduleRealtime
                3,//numberOfBuffers
                0,// priority
                "test"//String streamName
                );


        dac.openStream(oParams, null, sampleRate, bufferFrames, synthesizer, options);

        dac.startStream();

      } catch (RtError ex) {
        Logger.getLogger(PlaySine.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  private class Stopper implements Runnable {

    @Override
    public void run() {
      try {
        if (dac == null) {
          return;
        }
        dac.stopStream();
        dac.closeStream();
      } catch (RtError ex) {
        Logger.getLogger(PlaySine.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  /**
   * Create a new Sine synthesiser, open the specified audio device,
   * connect the synthesiser with the audio device and start playing immediately.
   * @param apiType the audio system that shall be opened.
   * @param device the device within the audio system.
   * @param offset the first channel of the device that shall be used.
   * @param nChannels the number of channels ( 2 is stereo).
   * @param bufferFrames the length of the buffer in frames.
   * @param sampleRate the number of frames per second.
   */
  public PlaySine(int apiType, int device, int offset, int nChannels, int bufferFrames, int sampleRate) {
    this.apiType = apiType;
    this.device = device;
    this.offset = offset;
    this.nChannels = nChannels;
    this.bufferFrames = bufferFrames;
    this.sampleRate = sampleRate;
    startingThread = new Thread(new Starter());
    stoppingThread = new Thread(new Stopper());
    startPlaying();
  }

  /**
   * Obtains the current time-stamp of the device, in microseconds.
   * This value gives an indication how many frames have been send
   * over the sound-system since it was opened. When this
   * function is called during the sound-system is about to open,
   * a value of zero will be reported.
   * @return the current time-stamp of the device in microseconds.
   */
  public long getMicrosecondPosition() {
    synchronized (dacLock) {
      if (dac == null) {
        return 0L;
      } else {
        try {
          return (long) (dac.getStreamTime() * 10E6);
        } catch (RtError ex) {
          return 0L;
        }
      }
    }
  }

  private void startPlaying() {
    startingThread.start();
  }

  public void stopPlaying() {
    try {
      startingThread.join(); // if the starting thread is still running, wait until it's finished
      stoppingThread.start();
    } catch (InterruptedException ex) {
      Logger.getLogger(PlaySine.class.getName()).log(Level.INFO, null, ex);
    } 

  }
}
