/*
 * Demo program which outputs a sinus waveform
 */
package de.free_creations.audioconfig;

import de.free_creations.audioconfig.StoredConfig.ConfigRecord;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import rtaudio4java.AudioProcessor_Float32;
import rtaudio4java.AudioSystem;
import rtaudio4java.AudioSystemFactory;
import rtaudio4java.RtError;

/**
 * A simple audio recorder that opens an RtAudio instance and records about one
 * second of audio input and plays it back through the output channel.
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
class Recorder {

  private static final Logger logger = Logger.getLogger(Recorder.class.getName());
  private static final float level_1_dB = -60;
  private static final float level_2_dB = -50;
  private static final float level_3_dB = -40;
  private static final float level_4_dB = -30;
  private static final float level_1_Volt = (float) (Math.pow(10, level_1_dB / 20));
  private static final float level_2_Volt = (float) (Math.pow(10, level_2_dB / 20));
  private static final float level_3_Volt = (float) (Math.pow(10, level_3_dB / 20));
  private static final float level_4_Volt = (float) (Math.pow(10, level_4_dB / 20));
  private volatile AudioSystem dac;
  private final Thread startingThread;
  private final Thread stoppingThread;
  private final ConfigRecord audioConfig;
  private final RecorderProcessor processor;

  /**
   *
   */
  private class RecorderProcessor extends AudioProcessor_Float32 {

    private int nFrames;
    private int outputChannelCount;
    private int inputChannelCount;
    private float[] audioOutArray;
    private volatile float peakVolt = 0.0F;
    private final Object peakLock = new Object();

    @Override
    public void onOpenStream(int samplingRate, int nFrames, int inputChannelCount,
            int outputChannelCount, boolean noninterleaved) {
      if (noninterleaved) {
        throw new RuntimeException("This version is not able to handle noninterleaved channels.");
      }

      logger.log(Level.INFO, "Stream Params = ({0}; {1}; {2}; {3})",
              new Object[]{samplingRate, nFrames, inputChannelCount, outputChannelCount});

      this.nFrames = nFrames;
      this.outputChannelCount = outputChannelCount;
      this.inputChannelCount = inputChannelCount;

      audioOutArray = new float[outputChannelCount * nFrames];
      Arrays.fill(audioOutArray, 0.0F);

    }

    @Override
    public float[] process(float[] inputBuffer, double streamTime, int status) {
      if (inputBuffer == null) {
        return audioOutArray;
      }
      if (inputBuffer.length == 0) {
        return audioOutArray;
      }
      float inSample = 0.0F;
      for (int frame = 0; frame < nFrames; frame++) {
        int outBase = frame * outputChannelCount;
        int inBase = frame * inputChannelCount;
        int inChannel = 0;
        for (int outChannel = 0; outChannel < outputChannelCount; outChannel++) {
          if (inChannel < inputChannelCount) {
            inSample = inputBuffer[inBase + inChannel];
            inChannel++;
          }
          audioOutArray[outBase + outChannel] = inSample;
        }
      }
      // logger.log(Level.INFO, "streamTime = {0}", streamTime);
      setPeakValue(inputBuffer);
      return audioOutArray;
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

    private void setPeakValue(float[] inputBuffer) {
      synchronized (peakLock) {
        // calculate the DC current on each channel
        float[] dcLevels = new float[inputChannelCount];
        Arrays.fill(dcLevels, 0.0F);
        int channel = 0;
        for (float sample : inputBuffer) {
          dcLevels[channel] += sample;
          channel++;
          if (channel == inputChannelCount) {
            channel = 0;
          }
        }
        for (int i = 0; i < inputChannelCount; i++) {
          dcLevels[i] = dcLevels[i] / nFrames;
        }

        channel = 0;
        for (float sample : inputBuffer) {
          peakVolt = Math.max(Math.abs(sample - dcLevels[channel]), peakVolt);
          channel++;
          if (channel == inputChannelCount) {
            channel = 0;
          }
        }
      }
    }

    protected float getPeakVoltAndClear() {
      synchronized (peakLock) {
        float result = peakVolt;
        peakVolt = 0.0F;
        return result;
      }
    }
  }

  /**
   *
   */
  private class Starter implements Runnable {

    @Override
    public void run() {
      if (audioConfig == null) {
        logger.log(Level.SEVERE, "audioConfig is null.");
        return;
      }
      try {
        logger.log(Level.INFO, "architecture = {0}",
                AudioSystemFactory.apiTypeToString(audioConfig.getArchitectureNumber()));

        dac = AudioSystemFactory.getRtAudioInstance(audioConfig.getArchitectureNumber());

        // print messages to stderr.
        dac.showWarnings(true);

        AudioSystem.StreamParameters oParams = audioConfig.getOutputParameters();
        if (oParams == null) {
          logger.log(Level.SEVERE, "Not a valid output device.");
          return;
        }
        logger.log(Level.INFO, "output device = {0}", oParams.deviceId);

        AudioSystem.StreamParameters iParams = audioConfig.getInputParameters();
        if (iParams == null) {
          logger.log(Level.SEVERE, "Not a valid input device.");
          return;
        }
        logger.log(Level.INFO, "input device = {0}", oParams.deviceId);


        AudioSystem.StreamOptions options = audioConfig.getOptions();


        dac.openStream(oParams, iParams, audioConfig.getSampleRate(), audioConfig.getBufferSize(), processor, options);

        if (!dac.isStreamOpen()) {
          logger.log(Level.SEVERE, "Could not open the stream.");
          return;

        }

        dac.startStream();

      } catch (RtError ex) {
        logger.log(Level.SEVERE, null, ex);
      }
    }
  }

  /**
   *
   */
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
        logger.log(Level.SEVERE, null, ex);
      }
    }
  }

  /**
   * Create a new recorder and start recording immediately the input stream,
   * after a predefined time the recording is played back on the output stream.
   *
   * @param audioConfig
   */
  public Recorder(StoredConfig.ConfigRecord audioConfig) {
    this.audioConfig = audioConfig;
    processor = new RecorderProcessor();
    startingThread = new Thread(new Starter());
    stoppingThread = new Thread(new Stopper());
    startRecording();
  }

  /**
   * Obtains the current time-stamp of the device, in microseconds. This value
   * gives an indication how many frames have been send over the sound-system
   * since it was opened. When this function is called during the sound-system
   * is about to open, a value of zero will be reported.
   *
   * @return the current time-stamp of the device in microseconds.
   */
  public long getMicrosecondPosition() {
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

  private void startRecording() {
    startingThread.start();
  }

  public void stopRecording() {
    try {
      startingThread.join(); // if the starting thread is still running, wait until it's finished
      stoppingThread.start();
    } catch (InterruptedException ex) {
      logger.log(Level.WARNING, null, ex);
    }
  }

  public int getPeakLevelAndClear() {
    float peakVolt = processor.getPeakVoltAndClear();
    if (peakVolt > level_4_Volt) {
      return 4;
    }
    if (peakVolt > level_3_Volt) {
      return 3;
    }
    if (peakVolt > level_2_Volt) {
      return 2;
    }
    if (peakVolt > level_1_Volt) {
      return 1;
    }
    return 0;
  }
}
