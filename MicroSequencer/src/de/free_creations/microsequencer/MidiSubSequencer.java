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

import com.sun.media.sound.AudioSynthesizer;
import com.sun.media.sound.SoftSynthesizer;
import de.free_creations.microsequencer.MasterSequencer.SubSequencer;
import de.free_creations.midiutil.InitializationList;
import de.free_creations.midiutil.MidiUtil;
import de.free_creations.midiutil.TempoTrack.TimeMap;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Track;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

/**
 * A sub-sequencer steers one synthesiser. The events of all the tracks attached
 * to one sub-sequencer are fed to the synthesiser. <h2>Implementation
 * assumptions</h2> open() is executed before series of process() are
 * invoked. When playing: every process() is preceded by prepareNormalCycle()
 * or prepareLoopEndCycle(). Once prepareSession() or prepareLoopEndCycle() has
 * executed the values of "thisCycleStartTick" and "nextCycleStartTick" passed
 * in "prepareNormalCycle()" are monotonically increasing. <h2>Threading</h2>
 * The functions open(), close(), prepareLoopEndCycle(), process() are
 * synchronised by the "processLock". Access to the tracks are synchronised by
 * the "trackLock" and by the fact that once started, the active-tracks cannot
 * (should not??) change anymore.
 *
 * @author Harald Postner <Harald at H-Postner.de>
 * @Note the prepareLoopEndCycle seems send events that are beyond the end of
 * the loop. To be investigated...
 */
class MidiSubSequencer implements MasterSequencer.MidiSubSequencer, AudioProcessor {

  static final private Logger logger = Logger.getLogger(MidiSubSequencer.class.getName());
  private final Object trackLock = new Object();
  private final Object processLock = new Object();
  private final String name;
  /**
   * changes in volume (in attenuation) should not be effectuated from one frame
   * to the next, because this would produce ugly cracking sounds. Instead the
   * current attenuation will exponentially reach its target value. The time
   * this takes is defined by the RELAXATIONTIME constant (in seconds).
   */
  private final double RELAXATIONTIME = 0.1; // in seconds
  private boolean stopping = false;
  private boolean loopEndProcessing;

  private class TimestampedMessage {

    public final MidiMessage message;
    public final double streamTime;

    TimestampedMessage(MidiMessage message, double streamTime) {
      this.message = message;
      this.streamTime = streamTime;
    }
  }
  /**
   * The synthesiser that renders Midi events into audio samples.
   */
  private final AudioSynthesizer synthesizer;
  /**
   * The sound bank to be used by the synthesiser.
   */
  private final Soundbank soundbank;
  /**
   * A synthMidiReceiver of the synthesiser that shall receive the midi
   * instructions.
   */
  private final Receiver synthMidiReceiver;
  /**
   * The Midi-tick position in the tracks at the start of the current cycle.
   */
  private double thisCycleStartTick = 0D;
  /**
   * The Midi-tick position in the tracks at the start of the next cycle.
   */
  private double nextCycleStartTick = 0D;
  /**
   * The Tracks that are currently played.
   */
  private Track[] activeTracks = new Track[]{};
  /**
   * The Tracks that should be played when the MidiSubSequencer starts for the
   * next time.
   */
  private Track[] tracks = new Track[]{};
  /**
   * Indicates which tracks (out of activeTracks) should stay mute.
   */
  private boolean[] activeMute = new boolean[]{};
  /**
   * Indicates which tracks (out of tracks) should stay mute when the
   * MidiSubSequencer starts for the next time.
   */
  private boolean[] mute = new boolean[]{};
  /**
   * the mapping between synthesiser-time and Midi ticks, that should be applied
   * at the beginning of the current cycle. If the end of the loop lies within
   * the current cycle we need also a second time-map, see timeMap_2 for more
   * detail.
   */
  private TimeMap timeMap_1;
  /**
   * This parameter is only relevant if the end of the loop lies within the
   * current cycle. In this case timeMap_2 gives the mapping between
   * synthesiser-time and Midi ticks, that should be applied after the cursor
   * has jumped back to the beginning of the loop. A null value indicates that
   * the midi sequence can be traversed sequentially without jumping backwards.
   */
  private TimeMap timeMap_2;
  /**
   * This parameter is only relevant if the end of the loop lies within the
   * current cycle. In this case it indicates where to restart once loopEndTick
   * has been reached.
   */
  private double loopStartTick;
  /**
   * This parameter is only relevant if the end of the loop lies within the
   * current cycle. In this case it indicates how far we shall go until we are
   * jumping back loopStartTick.
   */
  private double loopEndTick;
  private ArrayList<Integer> nextTrackEventToProcess = new ArrayList<>();
  /**
   * The current time of the synthesiser in seconds.
   */
  private double synthesizerTime;
  /**
   * The duration of one cycle in seconds.
   */
  private double cycleDuration;
  /**
   * The NIO-Byte-buffer will be used to cast a stream of bytes into a stream of
   * floats.
   */
  private ByteBuffer soundByteBuffer;
  /**
   * The NIO-Float-buffer will be mapped onto above Byte buffer and will be used
   * to retrieve the floats.
   */
  private FloatBuffer soundFloatBuffer;
  /**
   * The byte array will be used to write the stream of bytes.
   */
  private byte[] soundByteArray;
  /**
   * The float array will be used to read above bytes as floats.
   */
  private float[] soundFloatArray;
  /**
   * The stream where the synthesiser will write its output into.
   */
  private AudioInputStream synthesizerStream;
  /**
   * Flag indicating the next cycle is stating the rendering
   */
  private volatile boolean starting = false;
  /**
   * Flag indicating whether the attached tracks are being played or not.
   */
  private volatile boolean playing = false;
  /**
   * Flag indicating whether the subSequencer has been opened.
   */
  private volatile boolean opened = false;
  /**
   * A Queue of Midi messages that should be send to the sequencer (in addition
   * to those contained in the tracks).
   */
  private final BlockingQueue<TimestampedMessage> messageQueue =
          new LinkedBlockingQueue<>();

  /**
   * Create a new MidiSubSequencer. The Midi events will be rendered on a new
   * synthesiser using the given sound-bank.
   *
   * @param name a name for this MidiSubSequencer
   * @param soundbank the sound-bank that the synthesiser shall use (may be
   * null)
   * @throws MidiUnavailableException
   */
  public MidiSubSequencer(final String name, Soundbank soundbank) throws MidiUnavailableException {
    this.name = name;
    this.synthesizer = new SoftSynthesizer();
    this.soundbank = soundbank;
    this.synthMidiReceiver = synthesizer.getReceiver();
  }

  /**
   * Create a new MidiSubSequencer that will render Midi events on the given
   * synthesiser using the given sound-bank.
   *
   * @param name a name for this MidiSubSequencer.
   * @param synthesizer the synthesiser that shall render the Midi events.
   * @param soundbank the sound-bank that the synthesiser shall use (may be
   * null)
   * @throws MidiUnavailableException
   */
  MidiSubSequencer(final String name, AudioSynthesizer synthesizer, Soundbank soundbank) throws MidiUnavailableException {
    this.name = name;
    this.synthesizer = synthesizer;
    this.soundbank = soundbank;
    this.synthMidiReceiver = synthesizer.getReceiver();
  }

  /**
   * Builds a factory object that provides this implementation as sub-sequencer.
   *
   * @return a factory that can make sub-sequencer objects.
   */
  public static MasterSequencer.SubSequencerFactory getFactory() {
    MasterSequencer.SubSequencerFactory newFactory =
            new MasterSequencer.SubSequencerFactory() {
              @Override
              public MasterSequencer.MidiSubSequencer make(String name, Soundbank soundbank) throws MidiUnavailableException {
                return new MidiSubSequencer(name, soundbank);
              }

              @Override
              public MasterSequencer.AudioRecorderSubSequencerInt makeAudioRecorder(String name) {
                throw new UnsupportedOperationException("Cannot make an audio recorder.");
              }
            };
    return newFactory;
  }

  @Override
  public void start() {
  }

  @Override
  public void stop() {
  }

  /**
   * send an "all sounds off" message to all channels.
   *
   * @param synthesizerTime the point in time (in the synthesizers time space,
   * expressed in seconds) when the synthesizer should execute the message.
   */
  private void allSoundsOff(double synthesizerTime) {
    for (int channel = 0; channel < 16; channel++) {
      try {
        ShortMessage soundsOffMessage = new ShortMessage();
        soundsOffMessage.setMessage(ShortMessage.CONTROL_CHANGE, channel, MidiUtil.contAllSoundOff, 0);
        long timestamp = (long) (1E6 * synthesizerTime);
        synthMidiReceiver.send(soundsOffMessage, timestamp);
      } catch (InvalidMidiDataException ex) {
        Logger.getLogger(MidiSubSequencer.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  /**
   * Mute (or un-mute) an individual track. The muting will take place only at
   * the next start.
   *
   * @TODO this should be activable at any time...
   * @param trackIndex an index into the array given in {@link #setTracks(javax.sound.midi.Track[])
   * }
   * @param value true - the track should remain quiet, false the track produces
   * sound.
   */
  void setMute(int trackIndex, boolean value) {
    mute[trackIndex] = value;
  }

  /**
   * Mute (or un-mute) the whole sub-sequencer.
   *
   * @param value true - all tracks should remain quiet, false - the track may
   * produce sound (tracks that have been muted individually must stay mute).
   */
  void setMute(boolean value) {
    System.out.println("Not yet implemented SubSequencer.setAttenuation");
  }

  /**
   * Set the attenuation of an individual track.
   *
   * @param trackIndex an index into the array given in {@link #setTracks(javax.sound.midi.Track[])
   * }
   * @param value the value in decibels
   */
  void setAttenuation(int trackIndex, float value) {
    System.out.println("Not yet implemented SubSequencer.setAttenuation");
  }

  /**
   * Sends a MIDI message to the attached synthesiser. The timestamp is given
   * relative to the current time of the Audio-System. If the given streamTime
   * is negative, the sequencer will do the its best effort to send the message
   * as soon as possible.
   *
   * @param message the MIDI message to send.
   * @param streamTime the stream-time in seconds when the message should be
   * processed
   * @throws IllegalStateException if the subSequencer is closed.
   */
  public void send(MidiMessage message, double streamTime) {
    if (!opened) {
      throw new IllegalStateException("SubSequencer is closed.");
    }
    messageQueue.add(new TimestampedMessage(message, streamTime));
  }

  /**
   * Sets the tracks that will be played on the next start.
   *
   * @param tracks
   */
  public void setTracks(Track[] tracks) {
    if (tracks == null) {
      throw new IllegalArgumentException("Internal Error: null not allowed here, "
              + "use an empty array instead.");
    }
    synchronized (trackLock) {
      this.tracks = tracks;
      mute = new boolean[tracks.length];
      Arrays.fill(mute, false);

    }
  }

  /**
   * Activate the playback of the attached tracks. This function resets the
   * pointers to the beginning of the tracks. Thus once this function has
   * executed, the MidiSubSequencer assumes that the values of
   * "thisCycleStartTick" and "nextCycleStartTick" passed in
   * "prepareLoopEndCycle()" are monotonically increasing.
   */
  @Override
  public void prepareSession(double startTick, PlayingMode mode) {
    synchronized (trackLock) {
      logger.log(Level.FINER, "{0}:preparePlaying({1})", new Object[]{name, startTick});
      activeTracks = Arrays.copyOf(tracks, tracks.length);
      activeMute = Arrays.copyOf(mute, mute.length);
      resetTrackEventsToProcess();
      playing = true;
      stopping = false;
      starting = true;
    }
  }

  /**
   * This function shall be called before starting the rendering of the attached
   * tracks. The function goes through all attached tracks and searches for
   * controller events that lay before the starting position but should
   * nevertheless be send to the sequencer.
   *
   * @param startPosition the midi tick in the sequence where we are starting
   * rendering
   * @param synthesizerTime the time in seconds when the synthesizer should
   * excute the initialization.
   */
  private void initializeControllers(double startPosition, double synthesizerTime) {
    // first reset all controllers on all channels
    long timestamp = (long) (1E6 * synthesizerTime);
    for (int channel = 0; channel < 16; channel++) {
      synthMidiReceiver.send(createResetAllControllersMessage(channel), timestamp);
    }
    //now initialise the controllers
    for (Track track : activeTracks) {
      InitializationList initMessages = new InitializationList(track, (long) startPosition);
      long increment = 0;
      if (timestamp > 0) {
        increment = 1; //make sure that "ResetAllControllersMessage" is executed before set controller
      }
      for (MidiMessage message : initMessages) {
        synthMidiReceiver.send(message, timestamp + increment);
      }
    }
  }

  /**
   * De-activate the playback of the attached tracks.
   */
  @Override
  public void stopSession() {
    synchronized (trackLock) {
      stopping = true;

    }
  }

  /**
   * This function is called by the Processor for every cycle.
   *
   * @param streamTime the time in seconds of the audio stream at the start
   * of this cycle.
   * @param targetAttenuation the volume of the output signal (0.0 is silence,
   * 1.0 is full volume)
   * @return the audio output for the next cycle.
   * @throws IOException if an input or output error occurs in the synthesiser.
   * @throws IllegalStateException if the subSequencer is closed.
   */
  @Override
  public float[] process(double streamTime, float[] input) throws IOException, IllegalStateException {
    synchronized (processLock) {
      if (!opened) {
        throw new IllegalStateException("Cannot process in closed state.");
      }

      double offset = synthesizerTime - streamTime;
      double cycleEndStreamTime = streamTime + cycleDuration;

      // send pending Midi events to the synthesiser
      sendQueuedEvents(cycleEndStreamTime, offset);
      // send track events
      synchronized (trackLock) {
        if (starting) {
          initializeControllers(thisCycleStartTick, synthesizerTime);
          starting = false;
        }
        if (stopping) {
          playing = false;
          stopping = false;
          allSoundsOff(synthesizerTime + cycleDuration);
        }
        if (playing) {
          sendTrackEvents();
        }
      }

      //let the synthesizer render these events
      synthesizerStream.read(soundByteArray);

      //cast to float
      soundByteBuffer.clear();
      soundByteBuffer.put(soundByteArray);
      soundFloatBuffer.clear();
      soundFloatBuffer.get(soundFloatArray);


      // Note: Gervill's "getMicrosecondPosition()" seems to be buggy
      // the statement:   10L*synthesizer.getMicrosecondPosition()
      // seems to return a reasonable time, but why do we have to multiply by 10?
      // So we prefer to calculate our position (in seconds) by our own...
      synthesizerTime = synthesizerTime + cycleDuration;
      return soundFloatArray;
    }
  }

  @Override
  public void open(int samplingRate, int framesPerCycle, int inputChannelCount, int outputChannelCount, boolean noninterleaved) throws MidiUnavailableException {

    if (noninterleaved) {
      throw new RuntimeException("Oops..., this version is not able to handle noninterleaved channels.");
    }
    synchronized (processLock) {

      synthesizerTime = 0D;
      cycleDuration = (double) framesPerCycle / (double) samplingRate;

      int floatLength = outputChannelCount * framesPerCycle;
      int byteLenght = floatLength * (Float.SIZE / Byte.SIZE);

      // prepare a NIO Byte buffer to perform byte to float conversion.
      // Note: the direct buffer is more performant by a factor 10 than the array based version.
      soundByteBuffer = ByteBuffer.allocateDirect(byteLenght);
      soundByteBuffer.order(ByteOrder.nativeOrder());
      soundFloatBuffer = soundByteBuffer.asFloatBuffer();

      soundByteArray = new byte[byteLenght];
      Arrays.fill(soundByteArray, (byte) 0);

      soundFloatArray = new float[floatLength];
      Arrays.fill(soundFloatArray, 0F);


      //use big endian order if this is the native order.
      boolean useBigEndian = (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN);
      AudioFormat format = new AudioFormat(AudioFormat.Encoding.PCM_FLOAT, samplingRate, 32, outputChannelCount, 4 * outputChannelCount, samplingRate, useBigEndian); // ...............bigEndian        - indicates whether the data for a single sample is stored in big-endian byte order

      Map<String, Object> info = new HashMap<String, Object>();
      info.put("interpolation", "linear");
      info.put("max polyphony", "1024");

      synthesizerStream = synthesizer.openStream(format, info);
      if (!synthesizer.isOpen()) {
        throw new MidiUnavailableException("Could not open " + synthesizer + ".");
      }
      if (soundbank != null) {
        boolean success = synthesizer.loadAllInstruments(soundbank);
        if (!success) {
          throw new MidiUnavailableException("Could not load " + soundbank + ".");
        }
      }
      messageQueue.clear();
      opened = true;
    }
  }

  @Override
  public void close() {
    synchronized (processLock) {
      synthesizer.close();
      opened = false;
      playing = false;
    }
  }

  @Override
  public void prepareNormalCycle(TimeMap timeMap, double thisCycleStartTick, double nextCycleStartTick) {
    synchronized (processLock) {
      this.loopEndProcessing = false;
      this.timeMap_1 = timeMap;
      this.timeMap_2 = null;
      this.thisCycleStartTick = thisCycleStartTick;
      this.nextCycleStartTick = nextCycleStartTick;
      this.loopStartTick = -1;
      this.loopEndTick = -1;
    }
  }

  @Override
  public void prepareLoopEndCycle(TimeMap timeMap_1, TimeMap timeMap_2, double thisCycleStartTick, double nextCycleStartTick, double loopStartTick, double loopEndTick) {
    synchronized (processLock) {
      this.loopEndProcessing = true;
      this.timeMap_1 = timeMap_1;
      this.timeMap_2 = timeMap_2;
      this.thisCycleStartTick = thisCycleStartTick;
      this.nextCycleStartTick = nextCycleStartTick;
      this.loopStartTick = loopStartTick;
      this.loopEndTick = loopEndTick;
    }
  }

  public Track[] getTracks() {
    return tracks;
  }

  private void resetTrackEventsToProcess() {
    nextTrackEventToProcess.clear();
    for (int i = 0; i < activeTracks.length; i++) {
      nextTrackEventToProcess.add(0);
    }
  }

  private void setNextEventToProcess(int trackIdx, int eventIdx) {
    nextTrackEventToProcess.set(trackIdx, eventIdx);
  }

  private int getNextEventToProcess(int trackIdx) {
    return nextTrackEventToProcess.get(trackIdx);
  }

  /**
   * Inspect the attached tracks and send all events related to this cycle to
   * the synthesiser.
   */
  private void sendTrackEvents() {
    double upperLimit_1 = nextCycleStartTick;
    if (loopEndProcessing) {
      upperLimit_1 = loopEndTick;
    }
    sendTrackEvents(thisCycleStartTick, upperLimit_1, timeMap_1, synthesizerTime);


    if (loopEndProcessing) {
      double synthesizerTime_2 = synthesizerTime + timeMap_1.getTimeOffset(loopEndTick);
      resetTrackEventsToProcess();
      initializeControllers(loopStartTick, synthesizerTime_2);
      sendTrackEvents(loopStartTick, nextCycleStartTick, timeMap_2, synthesizerTime_2);
    }
  }

  /**
   * Inspect the attached tracks and send all events laying between the given
   * limits.
   *
   * @param lowerTickLimit events below this limit are not send.
   * @param upperTickLimit events below this limit are send.
   * @param timeMap the time-map that shall be used to convert midi ticks into
   * real seconds
   * @param lowerSynthesizerTime the synthesiser time-position corresponding to
   * the lower-limit-tick
   */
  private void sendTrackEvents(double lowerTickLimit, double upperTickLimit,
          TimeMap timeMap, double lowerSynthesizerTime) {

    for (int trackIdx = 0; trackIdx < this.activeTracks.length; trackIdx++) {
      int eventIdx = getNextEventToProcess(trackIdx);
      boolean toBeDoneLater = sendTrackEvent(eventIdx, trackIdx, lowerTickLimit,
              upperTickLimit, timeMap, lowerSynthesizerTime);
      while (!toBeDoneLater) {
        eventIdx++;
        toBeDoneLater = sendTrackEvent(eventIdx, trackIdx, lowerTickLimit,
                upperTickLimit, timeMap, lowerSynthesizerTime);
      }
      setNextEventToProcess(trackIdx, eventIdx);
    }
  }

  /**
   * Send a specific event to the synthesiser. The event is identified by its
   * index in a specific track. Before sending the event to the synthesiser,
   * this function verifies whether the event lays in between the lower and
   * upper tick limit. Only events that lay in this interval are send to the
   * synthesiser. If the event should be considered for a future cycle, this is
   * flagged by returning true.
   *
   * @param eventIdx the index of the event in the track.
   * @param trackIdx the index of the track.
   * @param lowerTickLimit events below this limit are not send.
   * @param upperTickLimit events below this limit are send.
   * @param timeMap the time-map that shall be used to convert midi ticks into
   * real seconds
   * @param lowerSynthesizerTime the synthesiser time-position corresponding to
   * the lower-limit-tick
   * @return true if the event should be considered in a later cycle.
   */
  private boolean sendTrackEvent(int eventIdx, int trackIdx,
          double lowerTickLimit, double upperTickLimit,
          TimeMap timeMap, double lowerSynthesizerTime) {

    if (eventIdx >= activeTracks[trackIdx].size()) {
      // although there are no more events to process,
      // we return true to indicate that we can stop to search for more 
      // events in *this* cycle.
      return true;
    }
    MidiEvent event = activeTracks[trackIdx].get(eventIdx);
    if (event.getTick() >= upperTickLimit) {
      return true;
    }
    if (event.getTick() >= lowerTickLimit) {
      if (!activeMute[trackIdx]) {
        double offset = timeMap.getTimeOffset(event.getTick());
        long timeStamp = (long) (1E6 * (lowerSynthesizerTime + offset));
        synthMidiReceiver.send(event.getMessage(), timeStamp);
      }
    }
    return false;
  }

  /**
   * Sends the queued messages to the synthesiser. Messages who's timestamp is
   * after the current cycle's end are not send but re-queued.
   *
   * @param cycleEndStreamTime the stream-time when the cycle ends (in seconds)
   * @param offsetTime the offset between stream-time and synthesiser-time.
   */
  private void sendQueuedEvents(double cycleEndStreamTime, double offsetTime) {
    ArrayList<TimestampedMessage> messages = new ArrayList<TimestampedMessage>();
    messageQueue.drainTo(messages);
    if (messages.isEmpty()) {
      return; //shortcut
    }
    ArrayList<TimestampedMessage> reQueuedMessages = new ArrayList<TimestampedMessage>();

    for (TimestampedMessage message : messages) {
      if (message.streamTime < cycleEndStreamTime) {
        // the message is timed for this cycle
        long timestamp = -1; // the default time stamp
        if (message.streamTime >= 0D) {
          // we can calculate a better time stamp than the default one.
          timestamp = (long) (1E6 * (message.streamTime + offsetTime));
        }
        synthMidiReceiver.send(message.message, timestamp);
      } else {
        // the message is timed for a future cycle, let's re-queue it.
        reQueuedMessages.add(message);
      }
    }
    // Put the re-queued message back into the queue.
    for (TimestampedMessage message : reQueuedMessages) {
      messageQueue.add(message);
    }
  }

  /**
   * Creates a "resest all controller" Midi message.
   *
   * @param channel the Midi channel
   * @return a "resest all controller" Midi message for the given Midi channel.
   */
  private MidiMessage createResetAllControllersMessage(int channel) {
    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(
              ShortMessage.CONTROL_CHANGE, //command,
              channel, //channel,
              //              MidiUtil.contAllControllersOff, //data2)
              MidiUtil.contAllNotesOff, //data2)
              0);//data2)
    } catch (InvalidMidiDataException ex) {
      logger.log(Level.SEVERE, null, ex);
    }
    return message;
  }

  @Override
  public String toString() {
    return "SubSequencer{" + "name=" + name + '}';
  }
}
