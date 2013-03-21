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
package de.free_creations.midisong;

import de.free_creations.microsequencer.AudioPort;
import de.free_creations.microsequencer.MicroSequencer;
import de.free_creations.microsequencer.PlayingMode;
import de.free_creations.microsequencer.SequencerEventListener;
import de.free_creations.microsequencer.SequencerMidiPort;
import de.free_creations.microsequencer.SequencerPort;
import de.free_creations.midiutil.BeatPosition;
import de.free_creations.midiutil.RPosition;
import de.free_creations.midiutil.RPositionEx;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.*;
import org.openide.util.Exceptions;

/**
 * The Song-session manages the dynamic aspects of a song when a song is being
 * played. Session relevant informations are all those settings that the user
 * can change without changing the song itself.
 *
 * <br/> Examples of such information are:
 *
 * <ul> <li>the position of the cursor</li>
 *
 * <li>the position and the length of the lead-in</li>
 *
 *  * <li>the position and the length of the lead-out</li>
 *
 * <li>the muting of individual tracks</li> </ul>
 *
 * A song session can be connected and disconnected to a sequencer. In order to
 * maintain a clean version of the song, the session will not operate on the
 * given song but rather on a clone of the given song.
 *
 * <p> There can be several open {@link Song songs} in one NetBeans instance. To
 * each open song there can be several song-sessions attached. But only one
 * song-session can have the sequencer attached. The only session that can be
 * heard (that is playing) is the one which currently has the sequencer.
 * Starting and stopping the reproduction of a song has to be done through such
 * a Song Session object. </p>
 *
 * <p> In order to be able to manage the set of all open song-sessions there is
 * the factory class {@link SongSessionManager SongSessionManager}. New
 * {@link SongSession SongSessions} shall be created by using the
 * {@link SongSessionManager SongSessionManagers} methods. </p>
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class SongSession {

  static final private Logger logger = Logger.getLogger(SongSession.class.getName());
  private MicroSequencer sequencer = null;
  private final Song song;
  private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
  /**
   * a session is active if it has access to the sound system *
   */
  private volatile boolean active = false;
  public static final String PROP_ACTIVE = "active";
  private long startPoint = 0;
  public static final String PROP_STARTPOINT = "cursor";
  private long loopStartPoint;
  public static final String PROP_LOOPSTARTPOINT = "loopStartPoint";
  private long loopEndPoint;
  public static final String PROP_LOOPENDPOINT = "loopEndPoint";
  private long leadinTime;
  public static final String PROP_LEADINTIME = "leadinTime";
  private long leadoutTime;
  public static final String PROP_LEADOUTTIME = "loeadoutTime";
  private boolean looping = false;
  public static final String PROP_LOOPING = "looping";
  private String name;
  public static final String PROP_NAME = "name";
  public static final String PROP_PLAYING = "playing";
  public static final String PROP_TEMPOFACTOR = "tempoFactor";
  private PlayingMode playingMode = PlayingMode.MidiOnly;
  public static final String PROP_PLAYINGMODE = "playingMode";
  private Sequence nullSequence;
  /**
   * the length in midi tick of this song*
   */
  private final long tickLength;
  private double tempoFactor = 1.0;
  private final List<SessionEventListener> sessionEventListeners = new ArrayList<SessionEventListener>();
  /**
   * The sequencerEventListener forwards the sequencer-events to the listeners
   * of this session.
   */
  private SequencerEventListener sequencerEventListener = new SequencerEventListener() {
    private volatile boolean isPlaying = false;

    @Override
    public void loopDone(int newLoopCount) {
      for (SessionEventListener listener : sessionEventListeners) {
        listener.loopDone(newLoopCount);
      }
    }

    @Override
    public void notifyPlaying(boolean newPlaying) {
      if (this.isPlaying != newPlaying) {
        boolean oldPlaying = this.isPlaying;
        this.isPlaying = newPlaying;
        propertyChangeSupport.firePropertyChange(PROP_PLAYING, oldPlaying, newPlaying);
      }
    }
  };
  private SequencerPort audioRecorderPort = null;

  /**
   * This constructor is package-private, use the SongManager to construct new
   * SongSession instances.
   */
  @SuppressWarnings("LeakingThisInConstructor")
  SongSession(Song originalSong) throws EInvalidSongFile {

    if (originalSong == null) {
      throw new IllegalArgumentException("Argument song is null.");
    }

    this.song = originalSong.clone();
    name = song.getName();
    // here we set the originalSong to immutable. This is rather coarse
    // because we do need immutability only during the time when we attach
    // sequencer. But with the current design this is OK and helps to
    // avoid problems.
    this.song.setImmutable(true);



    MasterTrack masterTrack = song.getMastertrack();
    if (masterTrack != null) {
      masterTrack.setSongSession(this);
      Track masterTrackData = masterTrack.getMidiTrack();
      if (masterTrackData != null) {
        tickLength = masterTrackData.ticks();
      } else {
        tickLength = 0L;
      }
    } else {
      tickLength = 0L;
    }
    loopStartPoint = 0L;
    leadinTime = 0L;
    leadoutTime = 0L;
    loopEndPoint = tickLength;
    try {
      nullSequence = new Sequence(Sequence.PPQ, 360);
    } catch (InvalidMidiDataException ex) {
      throw new RuntimeException(ex);
    }
    logger.log(Level.FINER, "{0} created", this);
  }

  public void addSessionEventListener(SessionEventListener listener) {
    sessionEventListeners.add(listener);
  }

  public void removeSessionEventListener(SessionEventListener listener) {
    sessionEventListeners.remove(listener);
  }

  /**
   * Get the song instance on which this session operates. In order to keep a
   * clean version of the original song, the session does not directly operate
   * on the song given in the constructor, but on a deep copy.
   *
   * @return the song instance on which this session operates.
   */
  public Song getActiveSong() {
    return song;
  }

  /**
   * Get the value of playing
   *
   * @return the value of playing
   */
  public boolean isPlaying() {
    if (sequencer == null) {
      return false;
    }
    return sequencer.isRunning();
  }

  /**
   * Set the value of playing
   *
   * @param playing new value of playing
   */
  public void setPlaying(boolean playing) {
    logger.log(Level.FINER, "setPlaying( {0} )", playing);
    if (sequencer == null) {
      if (playing) {
        throw new IllegalStateException("No sequencer attached, cannot play...");
      } else {
        return;
      }
    }

    boolean oldPlaying = sequencer.isRunning();
    if (oldPlaying == playing) {
      return;
    }
    if (playing) {
      sequencer.start(playingMode);
    } else {
      sequencer.stop();
    }

  }

  @Override
  public SongSession clone() throws CloneNotSupportedException {
    SongSession clonedSession = (SongSession) super.clone();
    /**
     * @ToDo register the songsession with the songmanager and the song object
     */
    return clonedSession;
  }

  /**
   *
   * @return the name that shall be used to display the song in the user
   * interface.
   */
  public String getName() {
    return name;
  }

  /**
   * Get the value of playingMode
   *
   * @return the value of playingMode
   */
  public PlayingMode getPlayingMode() {
    return playingMode;
  }

  /**
   * Get the name of the current playing mode.
   *
   * @return the playing mode as a string.
   */
  public String getPlayingModeName() {
    return getPlayingMode().name();
  }

  /**
   * Set the value of playingMode
   *
   * @param playingMode new value of playingMode
   */
  public void setPlayingMode(PlayingMode playingMode) {
    PlayingMode oldPlayingMode = this.playingMode;
    if (oldPlayingMode == playingMode) {
      return;
    }
    this.playingMode = playingMode;
    propertyChangeSupport.firePropertyChange(PROP_PLAYINGMODE, oldPlayingMode, playingMode);
  }

  /**
   * Set the value of playingMode
   *
   * @param playingMode the name of a playing mode
   * @throws IllegalArgumentException if the given parameter does not correspond
   * to a valid playing mode.
   */
  public void setPlayingModeStr(String playingMode) {
    setPlayingMode(PlayingMode.valueOf(playingMode));
  }

  /**
   * Set a name that shall be used to display the song in the user interface.
   *
   * @param name new value of name
   */
  public void setName(String name) {
    String oldName = this.name;
    String newName = StringUtil.cleanXmlString(name);
    if (oldName == null ? newName == null : oldName.equals(newName)) {
      return;
    }
    this.name = newName;
    propertyChangeSupport.firePropertyChange(PROP_NAME, oldName, newName);
  }

  /**
   * When the song is in looping mode, it never stops playing on its own. In
   * looping mode, when the song has reached loopEndPoint it will restart at
   * loopStartPoint.
   *
   * @return true if the song is in Looping mode.
   */
  public boolean isLooping() {
    return looping;
  }

  /**
   * Set the song in looping mode.
   *
   * @param looping if true will set the song in looping mode.
   */
  public void setLooping(boolean looping) {
    boolean oldLooping = this.looping;
    if (oldLooping == looping) {
      return;
    }
    this.looping = looping;
    if (sequencer != null) {
      if (isLooping()) {
        sequencer.setLoopCount(-1);
      } else {
        sequencer.setLoopCount(0);
      }
    }
    propertyChangeSupport.firePropertyChange(PROP_LOOPING, oldLooping, looping);
  }

  /**
   * The segment-end indicates where the normal playing should stop, it is at
   * the same time the start the lead-out.
   *
   * @return the end of the playing segment in midi ticks.
   */
  public long getLeadoutTime() {
    return leadoutTime;
  }

  /**
   * verify that a given position lays between the limits allowed for the
   * loop-end-point. If its outside, correct it.
   *
   * @param tickPos
   * @return
   */
  private long checkLoopEndLimits(long tickPos) {
    if (tickPos > tickLength) {
      return tickLength;
    }
    long loopStart = getLoopStartPoint();
    if (tickPos < loopStart) {
      return loopStart;
    }
    return tickPos;
  }

  /**
   * verify that a given position lays between the limits allowed for the
   * loop-start-point. If its outside, correct it.
   *
   * @param tickPos
   * @return
   */
  private long checkLoopStartLimits(long tickPos) {
    long loopEnd = getLoopEndPoint();
    if (tickPos > loopEnd) {
      return loopEnd;
    }
    if (tickPos < 0L) {
      return 0L;
    }
    return tickPos;
  }

  /**
   * verify that a given position lays between the start and the end of a
   * sequence. If its outside, correct it.
   *
   * @param tickPos
   * @return
   */
  private long checkLimits(long tickPos) {
    if (tickPos > tickLength) {
      return tickLength;
    }
    if (tickPos < 0L) {
      return 0L;
    }
    return tickPos;
  }

  /**
   * The leadout-time indicates how long it will take to mute the rendering at
   * the loop end point.
   *
   * @param ticks new value of the leadout-time.
   */
  public void setLeadoutTime(long ticks) {
    ticks = checkLimits(ticks);

    long oldLeadoutTime = this.leadoutTime;
    if (oldLeadoutTime == ticks) {
      return;
    }
    this.leadoutTime = ticks;
    if (sequencer != null) {
      sequencer.setLeadoutTime(ticks);
    }
    propertyChangeSupport.firePropertyChange(PROP_LEADOUTTIME, oldLeadoutTime, ticks);
  }

  /**
   * The Lead-in-Time indicates how long it will take to swell from full
   * attenuation to the target volume at the loop-start-point.
   *
   * @return the value of leadinTime in Midi ticks.
   */
  public long getLeadinTime() {
    return leadinTime;
  }

  /**
   * The Lead-in-Time indicates how long it will take to swell from full
   * attenuation to the target volume at the loop-start-point.
   *
   * @param ticks new value of the Lead-in-Time in Midi ticks.
   */
  public void setLeadinTime(long ticks) {
    ticks = checkLimits(ticks);

    long oldLeadinTime = this.leadinTime;
    if (oldLeadinTime == ticks) {
      return;
    }
    if (sequencer != null) {
      sequencer.setLeadinTime(ticks);
    }
    this.leadinTime = ticks;
    propertyChangeSupport.firePropertyChange(PROP_LEADINTIME, oldLeadinTime, ticks);
  }

  /**
   * The LoopEndPoint indicates where the all playing should end, it is at the
   * absolute end of the rendering (even when looping is off).
   *
   * @return the value of loopEndPoint in Midi ticks.
   */
  public long getLoopEndPoint() {
    return loopEndPoint;
  }

  /**
   * The loop-end-point indicates where the all playing should end, it is the
   * absolute end of the rendering.
   *
   * @param tick new value of the loop-end-point in Midi ticks. If the given
   * value is not between the loop-start and the end of the sequence the value
   * is automatically corrected.
   */
  public void setLoopEndPoint(long tick) {
    logger.log(Level.FINER, "setLoopEndPoint( {0} )", tick);
    tick = checkLoopEndLimits(tick);

    long oldLoopEndPoint = this.loopEndPoint;
    if (oldLoopEndPoint == tick) {
      return;
    }

    this.loopEndPoint = tick;
    if (sequencer != null) {
      sequencer.setLoopEndPoint(tick);
    }
    propertyChangeSupport.firePropertyChange(PROP_LOOPENDPOINT, oldLoopEndPoint, tick);
  }

  /**
   * The cursor indicates where rendering is currently happening.
   *
   * @return the position of the cursor in Midi ticks.
   * @deprecated use getTickPosition(0)
   */
  @Deprecated
  public long getCursor() {
    if (sequencer == null) {
      return startPoint;
    } else {
      return (long) sequencer.getTickPosition(0D);
    }
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
   * @return the position in midi ticks.
   */
  public double getTickPosition(double offset) {
    if (sequencer == null) {
      return startPoint;
    } else {
      return sequencer.getTickPosition(offset);
    }
  }

  public long getStartPoint() {
    return startPoint;
  }

  /**
   * Translates a position in the loaded sequence given as a musical position
   * expressed as measure and beat into midi ticks. This function returns
   * correct values only for the active session (the one that has a sequencer)
   *
   * @param beatPosition a position in the sequence. The numerator and
   * denominator values are ignored. If the value given as beats is larger than
   * the actual denominator (larger than there are beats in one measure) the
   * position is extrapolated over the given measure.
   * @return
   */
  public double beatPositionToTick(RPosition position) {
    if (sequencer == null) {
      return 0D;
    } else {
      return sequencer.beatPositionToTick(position);
    }
  }

  /**
   * Translates a position in the loaded sequence given in Midi-Ticks into a
   * musical position expressed as measure and beat. This function returns
   * correct values only for the active session (the one that has a sequencer)
   *
   * @param tickPosition a position in the sequence. If the given position lies
   * outside the sequence the returned position is truncated to the lower or the
   * upper end. If no sequence is currently loaded the returned position is
   * always zero.
   * @return
   */
  public RPositionEx tickToRPositionEx(double tickPosition) {
    if (sequencer == null) {
      return new RPositionEx();
    }
    return sequencer.tickToRPositionEx(tickPosition);
  }

  /**
   * Determines the speed at a given position of the loaded sequence. This
   * function returns correct values only for the active session (the one that
   * has a sequencer)
   *
   * @param tickPosition a position in the sequence.
   * @return
   */
  public double tickToEffectiveBPM(double tickPosition) {
    if (sequencer == null) {
      return 120;
    }
    return sequencer.tickToEffectiveBPM(tickPosition);
  }

  /**
   * @deprecated use tickToRPositionEx
   * @param tickPosition
   * @return
   */
  @Deprecated
  public BeatPosition tickToBeatPosition(double tickPosition) {
    if (sequencer == null) {
      return new BeatPosition() {
        @Override
        public int getNumerator() {
          return 4;
        }

        @Override
        public int getDenominator() {
          return 4;
        }

        @Override
        public long getMeasure() {
          return 0;
        }

        @Override
        public double getBeat() {
          return 0;
        }
      };
    } else {
      return sequencer.tickToBeatPosition(tickPosition);
    }
  }

  /**
   * The startPoint is the position where the begin playing.
   *
   * @param cursor new position of the cursor in Midi ticks.
   */
  public void setStartPoint(long tick) {
    tick = checkLimits(tick);
    long oldTick = getStartPoint();
    if (tick == oldTick) {
      return;
    }
    this.startPoint = tick;
    if (sequencer != null) {
      sequencer.setTickPosition(tick);
    }
    propertyChangeSupport.firePropertyChange(PROP_STARTPOINT, oldTick, tick);
  }

  /**
   * Add PropertyChangeListener.
   *
   * @param listener
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(listener);
  }

  /**
   * Remove PropertyChangeListener.
   *
   * @param listener
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(listener);
  }

  /**
   *
   * @return the value of loopStartPoint in Midi ticks.
   */
  public long getLoopStartPoint() {
    return loopStartPoint;
  }

  /**
   * Set the value of the loop-start-point.
   *
   * @param tick new value of tick in Midi ticks.
   */
  public void setLoopStartPoint(long tick) {
    logger.log(Level.FINER, "setLoopStartPoint( {0} )", tick);
    tick = checkLoopStartLimits(tick);

    long oldLoopStartPoint = this.loopStartPoint;
    if (oldLoopStartPoint == tick) {
      return;
    }
    if (tick < 0) {
      tick = 0;
    }
    if (tick > getTickLength()) {
      tick = getTickLength();
    }

    this.loopStartPoint = tick;
    if (sequencer != null) {
      sequencer.setLoopStartPoint(tick);
    }
    propertyChangeSupport.firePropertyChange(PROP_LOOPSTARTPOINT, oldLoopStartPoint, tick);
  }

  /**
   * @ToDo this function should not be public. Only the song-session-manager
   * should have access to this function...
   *
   * @param sequencer
   * @throws EInvalidSongFile
   * @throws MidiUnavailableException
   */
  public void attachSequencer(MicroSequencer sequencer) throws EInvalidSongFile, MidiUnavailableException {
    if (this.sequencer == sequencer) {
      return;
    }
    song.detachAudio();
    audioRecorderPort = null;



    this.sequencer = sequencer;

    if (sequencer == null) {
      return;
    }
    // clear the sequencer
    sequencer.stop();
    sequencer.removeAllPorts();
    sequencer.setSequence(nullSequence);
    sequencer.setLoopStartPoint(0);
    sequencer.setLoopEndPoint(0);
    sequencer.setLeadinTime(0);
    sequencer.setLeadoutTime(0);
    sequencer.setLoopCount(0);
    sequencer.setTickPosition(0);
    sequencer.setTempoFactor(1.0);
    sequencer.addSequencerEventListener(sequencerEventListener);


    if (song == null) {
      // no song attached, we are done.
      return;
    }
    MasterTrack masterTrack = song.getMastertrack();
    if (masterTrack == null) {
      // no masterTrack attached, that's strange but we are done.
      return;
    }

    sequencer.setSequence(masterTrack.getSequence());
    // allign();
    sequencer.setLoopStartPoint(getLoopStartPoint());
    sequencer.setLoopEndPoint(getLoopEndPoint());
    sequencer.setLeadinTime(getLeadinTime());
    sequencer.setLeadoutTime(getLeadoutTime());
    if (isLooping()) {
      sequencer.setLoopCount(-1);
    } else {
      sequencer.setLoopCount(0);
    }
    sequencer.setTickPosition(startPoint);
    sequencer.setTempoFactor(tempoFactor);

    GenericTrack[] subtracks = masterTrack.getSubtracks();
    attachTracksToSequencer(subtracks, sequencer, null, null);
    masterTrack.InitializeAudio();
    //------------------------------------------------------------------
    //this is provisional code to provide Audio recording facility!!!!!
    try {
      audioRecorderPort = sequencer.createAudioRecorderPort("Feedback");
    } catch (IOException ex) {
      Exceptions.printStackTrace(ex);
      audioRecorderPort = null;
    }
    setActive(true);

  }

  /**
   * Attach several sub-tracks to the given sequencer.
   *
   * @param subtracks an array of sub-tracks that shall be attached.
   * @param sequencer the given sequencer to which we shall attach the
   * sub-tracks.
   * @param port the port of the sequencer to which the track shall be attached
   * if the given port is {@code  null} a new port will be created.
   * @param tracks the tracks attached so far to the port given above (this
   * parameter is null if "port" is {@code  null}).
   * @throws EInvalidSongFile
   * @throws MidiUnavailableException
   */
  private void attachTracksToSequencer(GenericTrack[] subtracks, MicroSequencer sequencer, SequencerMidiPort port, ArrayList<Track> tracks) throws EInvalidSongFile, MidiUnavailableException {
    if (subtracks == null) {
      return;
    }
    for (GenericTrack track : subtracks) {
      attachOneTrackToSequencer(track, sequencer, port, tracks);
    }

  }

  /**
   * @deprecated use setPlaying...
   */
  @Deprecated
  private void start() {
    setPlaying(true);
  }

  /**
   * Obtains the length of the current sequence, expressed in MIDI ticks, or 0
   * if no sequence is set.
   *
   * @return length of the loaded sequence in ticks
   */
  public long getTickLength() {
    return tickLength;
  }

  /**
   * @deprecated
   */
  @Deprecated
  private void stop() {
    if (sequencer != null) {
      sequencer.stop();
    }
  }

  public void setTempoFactor(double factor) {
    logger.log(Level.FINER, "{0}.setTempoFactor({1})", new Object[]{this, factor});
    double oldTempoFactor = this.tempoFactor;
    if (oldTempoFactor == factor) {
      return;
    }
    tempoFactor = factor;
    if (sequencer != null) {
      sequencer.setTempoFactor(factor);
    }
    propertyChangeSupport.firePropertyChange(PROP_TEMPOFACTOR, oldTempoFactor, tempoFactor);
  }

  public double getTempoFactor() {
    if (sequencer != null) {
      return sequencer.getTempoFactor();
    } else {
      return tempoFactor;
    }
  }

  public double getDspLoadAndClear() {
    if (sequencer == null) {
      return 0D;
    }
    return sequencer.getMaxLoadAndClear();
  }

  /**
   * There can only be one session among all sessions that currently has access
   * to the sequencer. This session is said to be the active session.
   *
   * @return true if this session is the active session.
   */
  public boolean isActive() {
    return active;
  }

  private void setActive(boolean newValue) {
    boolean oldValue = active;
    if (oldValue == newValue) {
      return;
    }
    active = newValue;
    propertyChangeSupport.firePropertyChange(PROP_ACTIVE, oldValue, newValue);
  }

  /**
   * This is provisional code to provide Audio Recoding facilities.
   *
   * @return the last peak level of the synthesizers Audio port.
   */
  public float getAudioVuLevel() {
    if (audioRecorderPort != null) {
      return audioRecorderPort.getAudioPort().getPeakVuAndClear(0);
    } else {
      return 0.0F;
    }
  }

  /**
   * This is provisional code to provide Audio Recoding facilities. Sets the
   * attenuation audio playback.
   */
  public void setAudioAttenuation(int value) {
    if (audioRecorderPort != null) {
      for (int ch = 0; ch < AudioPort.MAXCHANNELS; ch++) {
        audioRecorderPort.getAudioPort().setAttenuation(ch, value);
      }
    }
  }

  /**
   * Forces the sequencer to close. 
   * 
   * Can be used to force to throw exceptions
   * that where collected during process cycles.
   *
   * @deprecated only for test.
   */
  public void forceClosing() {
    if (sequencer != null) {
      sequencer.stop();
      sequencer.close();
    }
  }

  private class MidiTrackHandler implements GenericTrack.EventHandler {

    final int trackIndex;
    final SequencerMidiPort port;

    public MidiTrackHandler(int trackIndex, SequencerMidiPort port) {
      this.trackIndex = trackIndex;
      this.port = port;
    }

    @Override
    public void onMuteChange(boolean value) {
      port.setMute(trackIndex, value);
    }

    @Override
    public void onAttenuationChange(float value) {
      port.setAttenuation(trackIndex, value);
    }
  }

  private class SynthrackHandler implements GenericTrack.EventHandler {

    final SequencerPort port;

    public SynthrackHandler(SequencerPort port) {

      this.port = port;
    }

    @Override
    public void onMuteChange(boolean value) {
      port.setMute(value);
    }

    @Override
    public void onAttenuationChange(float value) {
      int channelCount = port.getAudioPort().getAttenuations().length;
      for (int i = 0; i < channelCount; i++) {
        port.getAudioPort().setAttenuation(i, value);
      }
    }
  }

  /**
   * Attach single sub-tracks and all of its sub-tracks to the given sequencer.
   *
   * @param track the given track that we shall attach to the given sequencer.
   * @param sequencer the given sequencer to which we shall attach the
   * sub-tracks.
   * @param port the port of the sequencer to which the track shall be attached
   * if the given port is {@code  null} a new port will be created.
   * @param tracks the tracks attached so far to the port given above (this
   * parameter is null if "port" is {@code  null}).
   * @throws EInvalidSongFile
   * @throws MidiUnavailableException
   */
  private void attachOneTrackToSequencer(GenericTrack track, MicroSequencer sequencer, SequencerMidiPort port, ArrayList<Track> tracks) throws EInvalidSongFile, MidiUnavailableException {
    boolean hasHandler = false;
    if (track.getSongSession() != this) {
      throw new RuntimeException("internal Error in SongSession");
    }
    if (track instanceof MidiSynthesizerTrack) {
      MidiSynthesizerTrack synthTrack = (MidiSynthesizerTrack) track;
      SynthesizerData synth = synthTrack.getSynthesizer();
      if (synth != null) {
        if (synth instanceof BuiltinSynthesizer) {
          BuiltinSynthesizer builtinSynthData = (BuiltinSynthesizer) synth;
          Soundbank soundbank = builtinSynthData.getSoundbank();
          port = sequencer.createDefaultSynthesizerPort(synth.getName(), soundbank);
          builtinSynthData.setPort(port);
          SynthrackHandler handler = new SynthrackHandler(port);
          synthTrack.addEventHandler(handler);
          hasHandler = true;
          tracks = new ArrayList<Track>();
        }
      }
    }

    if (track instanceof MidiTrack) {
      MidiTrack midiTrack = (MidiTrack) track;
      Track midiData = midiTrack.getMidiTrack();
      if (port != null) {
        if (midiData != null) {
          tracks.add(midiData);
          port.setTracks(tracks.toArray(new Track[]{}));
          if (!hasHandler) {
            MidiTrackHandler handler = new MidiTrackHandler(tracks.size() - 1, port);
            midiTrack.addEventHandler(handler);
          }
        }

      } else {
        System.err.println("Warning: Invalid song data structure. There are Midi Tracks which are not connected to any synthesizer");
      }

    }
    GenericTrack[] subtracks = track.getSubtracks();
    attachTracksToSequencer(subtracks, sequencer, port, tracks);
  }

  public void detachSequencer() {
    if (sequencer != null) {
      sequencer.stop();
      startPoint = (long) sequencer.getTickPosition(0D);
      sequencer.removeAllPorts();
      sequencer.removeSequencerEventListener(sequencerEventListener);
      setActive(false);

    }
    try {
      attachSequencer(null);
    } catch (MidiUnavailableException ex) {
      Exceptions.printStackTrace(ex);
    } catch (EInvalidSongFile ex) {
      Exceptions.printStackTrace(ex);
    }
  }

  /**
   * Returns an array of all the listeners that were added to the Song object
   * with addPropertyChangeListener(). This function was introduced just for
   * testing purposes.
   *
   * @return all of the PropertyChangeListeners added or an empty array if no
   * listeners have been added
   */
  PropertyChangeListener[] getPropertyChangeListeners() {
    return propertyChangeSupport.getPropertyChangeListeners();
  }

  @Override
  public String toString() {
    return "SongSession{" + name + '}';
  }
}
