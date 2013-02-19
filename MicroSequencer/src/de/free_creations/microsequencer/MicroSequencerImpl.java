/*
 *  Copyright 2011 Harald Postner <Harald at H-Postner.de>.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package de.free_creations.microsequencer;

import de.free_creations.audioconfig.Audioconfig;
import de.free_creations.midiutil.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.*;
import rtaudio4java.AudioSystem.StreamParameters;
import rtaudio4java.*;

/**
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
class MicroSequencerImpl implements MicroSequencer {

  static final private Logger logger = Logger.getLogger(MicroSequencer.class.getName());
  /**
   * The number of times the function provideExecutor() has been called.
   */
  private int provideExecutorCount = 0;
  private boolean opened = false;
  private final MasterSequencer masterSequencer =
          new MasterSequencerImpl(MidiSubSequencer.getFactory());
  private final AudioMixer audioMixer = new AudioMixer(masterSequencer);
  private AudioSystem audioSystem;
  private final List<ExecutorService> executors = new ArrayList<ExecutorService>();
  private final ThreadFactory threadFactory = new ThreadFactory() {

    private int threadCount = 0;

    @Override
    public Thread newThread(Runnable r) {
      threadCount++;
      Thread thread = new Thread(r);
      thread.setPriority(Thread.MAX_PRIORITY - 1);
      thread.setName("AudioWorker_" + threadCount);
      return thread;
    }
  };

  /**
   * Sets the sequence that defines the timing of the tracks. To make tracks
   * play they must be added to a midi port that is obtained by createPort()
   *
   * @param sequence
   */
  @Override
  public void setSequence(Sequence sequence) {
    masterSequencer.setMasterTrack(new TempoTrack(sequence), new TimeSignatureTrack(sequence), sequence.getTickLength());
  }

  /**
   * Starts playback of the MIDI data in the currently loaded sequence. Playback
   * will begin from the current position. Loop handling is not implemented, so
   * playback will continue to play to the end of the sequence and then stopOut.
   */
  @Override
  public void start() {
    logger.log(Level.FINER, "started");
    masterSequencer.startMidi();

  }

  /**
   * Stops playback of the currently loaded sequence, if any.
   */
  @Override
  public void stop() {
    logger.log(Level.FINER, "stop()");
    masterSequencer.stopMidi();
  }

  /**
   * Indicates whether the Sequencer is currently running. The default is false.
   * The Sequencer starts running when either startOut() is called. isRunning then
   * returns true until playback of the sequence completes or stopOut() is called.
   *
   * @return true if the Sequencer is running, otherwise false
   */
  @Override
  public boolean isRunning() {
    return masterSequencer.isRunning();
  }

  /**
   * Scales the sequencer's actual playback tempo by the factor provided. The
   * default is 1.0. A value of 1.0 represents the natural rate (the tempo
   * specified in the sequence), 2.0 means twice as fast, etc. Changes of the
   * tempo during playback will take effect immediately.
   *
   * @param factor the requested tempo scalar
   */
  @Override
  public void setTempoFactor(float factor) {
    masterSequencer.setTempoFactor(factor);
  }

  /**
   * Scales the sequencer's actual playback tempo by the factor provided. The
   * default is 1.0. A value of 1.0 represents the natural rate (the tempo
   * specified in the sequence), 2.0 means twice as fast, etc.
   *
   * @param factor the requested tempo scalar
   */
  @Override
  public void setTempoFactor(double factor) {
    masterSequencer.setTempoFactor(factor);
  }

  /**
   * Returns the current tempo factor for the sequencer. The default is 1.0.
   *
   * @return tempo factor.
   */
  @Override
  public float getTempoFactor() {
    return (float) masterSequencer.getTempoFactor();
  }

  /**
   * Returns the current tempo factor as a double value. The default is 1.0.
   *
   * @return tempo factor.
   */
  @Override
  public double getTempoFactorEx() {
    return masterSequencer.getTempoFactor();
  }

  /**
   * {@inheritDoc }
   */
  @Override
  public long getTickLength() {
    return masterSequencer.getTickLength();
  }

  /**
   * @deprecated use getTickPosition(double offset)
   * @return
   */
  @Override
  @Deprecated
  public long getTickPosition() {
    return (long) getTickPosition(0D);
  }

  /**
   * Obtains the current position in the sequence, expressed in MIDI ticks. (The
   * duration of a tick in seconds is determined both by the tempo and by the
   * timing resolution stored in the Sequence.)
   *
   * @param offset an offset in seconds that is added to the current time so the
   * returned value is the position that will (probably) be reached in the given
   * time. ("Probably" - because if a sudden tempo-factor change occurs within
   * the given time interval, the estimation will be wrong.)
   * @return
   */
  @Override
  public double getTickPosition(double offset) {
    if (audioSystem != null) {
      if (audioSystem.isStreamRunning()) {
        try {
          return masterSequencer.getCurrentTickPosition(audioSystem.getStreamTime() + offset);
        } catch (RtError ex) {
          logger.log(Level.SEVERE, null, ex);
        }
      }
    }
    return masterSequencer.getCurrentTickPosition(0D);
  }

  /**
   * Obtains the current position in the sequence, as musical measure and beat.
   * (The duration of a measures and beats is determined by the tempo and the
   * time signature.
   *
   * @param offset
   * @return
   */
  //@Override
  public BeatPosition getBeatPosition(double offset) {
    if (audioSystem != null) {
      if (audioSystem.isStreamRunning()) {
        try {
          return masterSequencer.getCurrentBeatPosition(audioSystem.getStreamTime() + offset);
        } catch (RtError ex) {
          Logger.getLogger(MicroSequencerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }
    return masterSequencer.getCurrentBeatPosition(0D);
  }

  /**
   * Sets the sequencer position in MIDI ticks. At the next invocation of
   * startOut(), the sequencer will startOut from this position.
   *
   * @param tick the tick to startOut from.
   */
  @Override
  public void setTickPosition(long tick) {
    masterSequencer.setTickPosition(tick);
  }

  /**
   * Sets the sequencer position in MIDI ticks. At the next invocation of
   * startOut(), the sequencer will startOut from this position.
   *
   * @param tick the tick to startOut from.
   */
  @Override
  public void setTickPosition(double tick) {
    masterSequencer.setTickPosition(tick);
  }

  /**
   * Sets the first MIDI tick that will be played in the loop. If the loop count
   * is greater than 0, playback will jump to this point when reaching the loop
   * end point.
   *
   * A value of 0 for the starting point means the beginning of the loaded
   * sequence. The starting point must be lower than or equal to the ending
   * point, and it must fall within the size of the loaded sequence.
   *
   * A sequencer's loop startOut point defaults to startOut of the sequence.
   *
   * @param tick the loop's starting position, in MIDI ticks (zero-based)
   * @throws IllegalArgumentException if the requested loop startOut point cannot
   * be set, usually because it falls outside the sequence's duration or because
   * the startOut point is after the end point
   */
  @Override
  public void setLoopStartPoint(long tick) {
    masterSequencer.setLoopStartPoint(tick);
  }

  /**
   * Obtains the startOut position of the loop, in MIDI ticks.
   *
   * @return the startOut position of the loop, in MIDI ticks (zero-based)
   */
  @Override
  public long getLoopStartPoint() {
    return (long) masterSequencer.getLoopStartPoint();
  }

  /**
   * Sets the last MIDI tick that will be played in the loop. If the loop count
   * is 0, the loop end point has no effect and playback continues to play when
   * reaching the loop end point.
   *
   * A value of -1 for the ending point indicates the last tick of the sequence.
   * Otherwise, the ending point must be greater than or equal to the starting
   * point, and it must fall within the size of the loaded sequence.
   *
   * A sequencer's loop end point defaults to -1, meaning the end of the
   * sequence.
   *
   * @param tick the loop's ending position, in MIDI ticks (zero-based), or -1
   * to indicate the final tick
   * @throws IllegalArgumentException if the requested loop point cannot be set,
   * usually because it falls outside the sequence's duration or because the
   * ending point is before the starting point
   */
  @Override
  public void setLoopEndPoint(long tick) {
    masterSequencer.setLoopEndPoint(tick);
  }

  /**
   * Obtains the end position of the loop, in MIDI ticks.
   *
   * @return the end position of the loop, in MIDI ticks (zero-based), or -1 to
   * indicate the end of the sequence
   */
  @Override
  public long getLoopEndPoint() {
    return (long) masterSequencer.getLoopEndPoint();
  }

  /**
   * @deprecated use setSequence(Sequence sequence)
   * @param stream
   * @throws IOException
   * @throws InvalidMidiDataException
   */
  @Override
  @Deprecated
  public void setSequence(InputStream stream) throws IOException, InvalidMidiDataException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * @deprecated Not supported yet.
   */
  @Override
  @Deprecated
  public Sequence getSequence() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * @deprecated Not supported yet.
   */
  @Override
  @Deprecated
  public void startRecording() {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * @deprecated Not supported yet.
   */
  @Override
  @Deprecated
  public void stopRecording() {
    throw new UnsupportedOperationException("Not supported yet.");

  }

  /**
   * @deprecated Not supported yet.
   * @return
   */
  @Override
  @Deprecated
  public boolean isRecording() {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * @deprecated Not supported yet.
   * @param track
   * @param channel
   */
  @Override
  @Deprecated
  public void recordEnable(Track track, int channel) {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * @deprecated Not supported yet.
   * @param track
   */
  @Override
  @Deprecated
  public void recordDisable(Track track) {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * @deprecated Not supported yet.
   * @return
   */
  @Override
  @Deprecated
  public float getTempoInBPM() {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * @deprecated Not supported yet.
   * @param bpm
   */
  @Override
  @Deprecated
  public void setTempoInBPM(float bpm) {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * @deprecated Not supported yet.
   * @return
   */
  @Override
  @Deprecated
  public float getTempoInMPQ() {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * @deprecated Not supported yet.
   * @param mpq
   */
  @Override
  @Deprecated
  public void setTempoInMPQ(float mpq) {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * @deprecated Not supported yet.
   * @return
   */
  @Override
  @Deprecated
  public long getMicrosecondLength() {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * @deprecated Not supported yet
   * @return
   */
  @Override
  @Deprecated
  public long getMicrosecondPosition() {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * @deprecated Not supported yet.
   * @param microseconds
   */
  @Override
  @Deprecated
  public void setMicrosecondPosition(long microseconds) {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * @deprecated Not supported yet.
   * @param sync
   */
  @Override
  @Deprecated
  public void setMasterSyncMode(SyncMode sync) {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * @deprecated Not supported yet.
   * @return
   */
  @Override
  @Deprecated
  public SyncMode getMasterSyncMode() {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * @deprecated Not supported yet.
   * @return
   */
  @Override
  @Deprecated
  public SyncMode[] getMasterSyncModes() {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * @deprecated Not supported yet.
   * @param sync
   */
  @Override
  @Deprecated
  public void setSlaveSyncMode(SyncMode sync) {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * @deprecated Not supported yet.
   * @return
   */
  @Override
  @Deprecated
  public SyncMode getSlaveSyncMode() {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * @deprecated Not supported yet.
   * @return
   */
  @Override
  @Deprecated
  public SyncMode[] getSlaveSyncModes() {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * @deprecated Not supported yet.
   * @param track
   * @param mute
   */
  @Override
  @Deprecated
  public void setTrackMute(int track, boolean mute) {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * @deprecated Not supported yet.
   * @param track
   * @return
   */
  @Override
  @Deprecated
  public boolean getTrackMute(int track) {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * @deprecated Not supported yet.
   * @param track
   * @param solo
   */
  @Override
  @Deprecated
  public void setTrackSolo(int track, boolean solo) {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * @deprecated Not supported yet.
   * @param track
   * @return
   */
  @Override
  @Deprecated
  public boolean getTrackSolo(int track) {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * @deprecated Not supported yet.
   * @param listener
   * @return
   */
  @Override
  @Deprecated
  public boolean addMetaEventListener(MetaEventListener listener) {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * @deprecated Not supported yet.
   * @param listener
   */
  @Override
  @Deprecated
  public void removeMetaEventListener(MetaEventListener listener) {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * @deprecated Not supported yet.
   * @param listener
   * @param controllers
   * @return
   */
  @Override
  @Deprecated
  public int[] addControllerEventListener(ControllerEventListener listener, int[] controllers) {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * @deprecated Not supported yet.
   * @param listener
   * @param controllers
   * @return
   */
  @Override
  @Deprecated
  public int[] removeControllerEventListener(ControllerEventListener listener, int[] controllers) {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * Sets the number of repetitions of the loop for playback. When the playback
   * position reaches the loop end point, it will loop back to the loop startOut
   * point count times, after which playback will continue to play to the end of
   * the sequence.
   *
   * If the current position when this method is invoked is greater than the
   * loop end point, playback continues to the end of the sequence without
   * looping, unless the loop end point is changed subsequently.
   *
   * A count value of 0 disables looping: playback will continue at the loop end
   * point, and it will not loop back to the loop startOut point. This is a
   * sequencer's default.
   *
   * If playback is stopped during looping, the current loop status is cleared;
   * subsequent startOut requests are not affected by an interrupted loop
   * operation.
   *
   * @param count the number of times playback should loop back from the loop's
   * end position to the loop's startOut position, or {@link javax.sound.midi.Sequencer#LOOP_CONTINUOUSLY}
   * to indicate that looping should continue until interrupted
   */
  @Override
  public void setLoopCount(int count) {
    masterSequencer.setLoopCount(count);
  }

  /**
   * Obtains the number of repetitions for playback.
   *
   * @return the number of loops after which playback plays to the end of the
   * sequence
   */
  @Override
  public int getLoopCount() {
    return masterSequencer.getLoopCount();
  }

  /**
   * @deprecated Not supported yet.
   * @return
   */
  @Override
  @Deprecated
  public Info getDeviceInfo() {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * Opens the synthesiser and the the required audio resources. <p> Note that
   * once closed, it cannot be reopened. Attempts to reopen this device will
   * always result in a MidiUnavailableException. </p>
   *
   * @throws MidiUnavailableException
   */
  @Override
  public void open() throws MidiUnavailableException {
    try {
      if (!Audioconfig.isProbed()) {
        throw new MidiUnavailableException("Audioconfig not probed. Cause: " + Audioconfig.getProbeMessage());
      }
      audioSystem = AudioSystemFactory.getRtAudioInstance(Audioconfig.getSystemNumber());
      StreamParameters outParams = new StreamParameters(Audioconfig.getOutputDeviceNumber(), Audioconfig.getFirstChannel(), Audioconfig.getNumberOfChannels());
      audioSystem.openStream(outParams, null,
              Audioconfig.getSampleRate(),
              Audioconfig.getBufferSize(),
              audioMixer, null);
      if (!audioSystem.isStreamOpen()) {
        throw new MidiUnavailableException("Clould not open the stream.");
      }
      audioSystem.startStream();
      if (!audioSystem.isStreamRunning()) {
        throw new MidiUnavailableException("Clould not start the stream.");
      }
    } catch (RtErrorInvalidUse ex) {
      throw new MidiUnavailableException(ex.getMessage());
    } catch (RtErrorProcessError ex) {
      throw new MidiUnavailableException(ex.getMessage());
    } catch (RtErrorInavalidParameter ex) {
      throw new MidiUnavailableException(ex.getMessage());
    }
    opened = true;

  }

  /**
   * Closes the synthesiser and and the audio system. <p> Note that once closed,
   * it cannot be reopened. </p> Attempts to reopen this device will always
   * result in a MidiUnavailableException.
   */
  @Override
  public void close() {

    if (!opened) {
      return;
    }
    opened = false;
    try {
      audioSystem.stopStream().get();
      audioSystem.closeStream().get();
    } catch (InterruptedException ex) {
      throw new RuntimeException(ex);
    } catch (ExecutionException ex) {
      throw new RuntimeException(ex);
    } catch (RtErrorInvalidUse ex) {
      throw new RuntimeException(ex);
    }


  }

  /**
   * Reports whether the device is openOut.
   *
   * @return true if the device is openOut, otherwise false
   */
  @Override
  public boolean isOpen() {
    return opened;
  }

  /**
   * @deprecated Not supported yet. (use midiPort send)
   * @return
   */
  @Override
  @Deprecated
  public int getMaxReceivers() {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * @deprecated Not supported yet.
   * @return
   */
  @Override
  @Deprecated
  public int getMaxTransmitters() {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * @deprecated Not supported yet.
   * @return
   */
  @Override
  @Deprecated
  public Receiver getReceiver() throws MidiUnavailableException {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * @deprecated Not supported yet.
   * @return
   */
  @Override
  @Deprecated
  public List<Receiver> getReceivers() {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * @deprecated Not supported yet.
   * @return
   * @throws MidiUnavailableException
   */
  @Override
  @Deprecated
  public Transmitter getTransmitter() throws MidiUnavailableException {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * @deprecated Not supported yet.
   * @return
   */
  @Override
  @Deprecated
  public List<Transmitter> getTransmitters() {
    throw new UnsupportedOperationException("Not supported yet.");


  }

  /**
   * Create a port to attach midi tracks. The tracks will be rendered on an new
   * instance of the default synthesiser using the given sound-bank.
   *
   * @param name a name for the instance of the synthesiser.
   * @param soundbank the sound-bank to be used (may be null).
   * @return a sequencer port which allows to attach the track that shall be
   * rendered on this port.
   * @throws Exception
   */
  @Override
  public SequencerPort createDefaultSynthesizerPort(final String name, Soundbank soundbank) throws MidiUnavailableException {
    ExecutorService executorService = provideExecutor();
    MidiSubSequencer subsequncer = (MidiSubSequencer) masterSequencer.createMidiSubSequencer(name, soundbank);
    AudioPort audioPort = audioMixer.createPort(subsequncer, executorService);
    SequencerPortImpl sequencerPort = new SequencerPortImpl(audioPort, subsequncer);
    return sequencerPort;
  }

  /**
   * Closes all ports and removes them from the processOut loop.
   */
  @Override
  public void removeAllPorts() {
    audioMixer.removeAllPorts();
    masterSequencer.removeAllSubsequncers();
  }

  /**
   * {@inheritDoc }
   */
  @Override
  public void setLeadinTime(long ticks) {
    System.err.println("MicroSequencerImpl: Function setLeadinTime must be implemented");
  }

  /**
   * {@inheritDoc }
   */
  @Override
  public long getLeadinTime() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  /**
   * {@inheritDoc }
   */
  @Override
  public void setLeadoutTime(long ticks) {
    System.err.println("MicroSequencerImpl: Function setLeadoutTime must be implemented");
  }

  /**
   * {@inheritDoc }
   */
  @Override
  public long getLeadoutTime() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  @Deprecated
  public BeatPosition tickToBeatPosition(double tickPosition) {
    return masterSequencer.tickToBeatPosition(tickPosition);
  }

  @Override
  public RPositionEx tickToRPositionEx(double tickPosition) {
    return masterSequencer.tickToRPositionEx(tickPosition);
  }

  @Override
  public double tickToEffectiveBPM(double tickPosition) {
    return masterSequencer.tickToEffectiveBPM(tickPosition);
  }

  @Override
  public double beatPositionToTick(RPosition position) {
    return masterSequencer.beatPositionToTick(position);
  }

  @Override
  public double getMaxLoadAndClear() {
    return audioMixer.getMaxLoadAndClear();
  }

  /**
   * Provide an executor that can be attached to an audio port. In order to
   * balance the load onto all available CPU's, this function will create new
   * executors until
   *
   * @return
   */
  private ExecutorService provideExecutor() {
    int executorIdx = provideExecutorCount % Runtime.getRuntime().availableProcessors();
    while (executors.size() <= executorIdx) {
      executors.add(Executors.newSingleThreadExecutor(threadFactory));
    }
    provideExecutorCount++;
    return executors.get(executorIdx);
  }

  @Override
  public void addSequencerEventListener(SequencerEventListener listener) {
    masterSequencer.add(listener);
  }

  @Override
  public void removeSequencerEventListener(SequencerEventListener listener) {
    masterSequencer.remove(listener);
  }
}
