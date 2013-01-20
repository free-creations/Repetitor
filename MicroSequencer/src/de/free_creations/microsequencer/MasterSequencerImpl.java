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

import de.free_creations.midiutil.TempoTrack.TimeMap;
import de.free_creations.midiutil.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Soundbank;


/**
 * The master-sequencer steers a number of sub-sequencers. The main task of the
 * master-sequencer is to maintain the common information about timing. This
 * includes information such as: <ul> <li>at which Midi-tick does the next cycle
 * start.</li> <li>at which speed must the next cycle be rendered.</li> </ul>
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
class MasterSequencerImpl implements MasterSequencer {

  static final private Logger logger = Logger.getLogger(MasterSequencerImpl.class.getName());
  private final SubSequencerFactory subSequencerFactory;
  private TempoTrack tempoTrack = null;
  private TimeSignatureTrack timeSignatureTrack = null;
  private final ArrayList<MasterSequencer.SubSequencer> subSequencers =
          new ArrayList<MasterSequencer.SubSequencer>();
  private final Object updateLock = new Object();
  private volatile double tempoFactor = 1.0D;
  private double startPosition = 0D;
  private long tickLength;
  private double loopEndPoint;
  private double loopStartPoint;
  private int loopCount;
  private volatile ActiveMasterSequencer activeMasterSequencer = null;
  private volatile boolean stopping = false;
  private final List<SequencerEventListener> sequencerEventListeners = new ArrayList<SequencerEventListener>();

  public MasterSequencerImpl(SubSequencerFactory subSequencerFactory) {
    this.subSequencerFactory = subSequencerFactory;
  }

  @Override
  public void setMasterTrack(TempoTrack tempoTrack, TimeSignatureTrack timeSignatureTrack, long tickLength) {
    synchronized (updateLock) {
      this.tempoTrack = tempoTrack;
      this.timeSignatureTrack = timeSignatureTrack;
      this.tickLength = tickLength;
    }
  }

  /**
   * Create a new subSequencer.
   *
   * @param name
   * @param soundbank
   * @return
   * @throws MidiUnavailableException if the synthesiser does not provide a
   * suitable MIDI-receiver.
   *
   */
  @Override
  public SubSequencer createSubSequencer(String name, Soundbank soundbank) throws MidiUnavailableException {
    synchronized (updateLock) {
      //SubSequencer newSubSequencer = new SubSequencer(name, soundbank);
      SubSequencer newSubSequencer = subSequencerFactory.make(name, soundbank);
      synchronized (subSequencers) {
        subSequencers.add(newSubSequencer);
      }
      return newSubSequencer;
    }
  }

  @Override
  public double getTempoFactor() {
    return tempoFactor;
  }

  @Override
  public void setTempoFactor(double tempoFactor) {
    this.tempoFactor = tempoFactor;
  }

  @Override
  public double getCurrentTickPosition(double streamTime) {
    synchronized (updateLock) {
      if (activeMasterSequencer != null) {
        return activeMasterSequencer.getTickPosition(streamTime);
      } else {
        return startPosition;
      }
    }
  }

  @Override
  public double beatPositionToTick(RPosition position) {
    if (timeSignatureTrack == null) {
      return 0D;
    }
    return timeSignatureTrack.getTickPosition(position);
  }

  @Override
  public RPositionEx tickToRPositionEx(double tickPosition) {
    if (timeSignatureTrack == null) {
      return new RPositionEx();
    }
    return timeSignatureTrack.getRPositionEx(tickPosition);
  }

  @Override
  public BeatPosition tickToBeatPosition(double tickPosition) {
    if (timeSignatureTrack == null) {
      // if there is no time signature track, construct on the fly a default return value 
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
          return 0D;
        }
      };

    }
    return timeSignatureTrack.getBeatPosition(tickPosition);
  }

  @Override
  public BeatPosition getCurrentBeatPosition(double streamTime) {
    synchronized (updateLock) {
      TimeSignatureTrack usedTimesig = null;


      if (activeMasterSequencer != null) {
        usedTimesig = activeMasterSequencer.getTimeSignatureTrack();
      } else {
        usedTimesig = this.timeSignatureTrack;
      }

      if (usedTimesig == null) {
        // if there is no time signature track, construct on the fly a default return value 
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
            return 0D;
          }
        };
      }

      double tickPos = getCurrentTickPosition(streamTime);
      return usedTimesig.getBeatPosition(tickPos);
    }
  }

  @Override
  public void prepareCycle(double streamTime,
          double cycleLength) {
    synchronized (updateLock) {
      double thisCycleTempoFactor = tempoFactor;
      if (activeMasterSequencer != null) {
        activeMasterSequencer.prepareCycle(streamTime, cycleLength, thisCycleTempoFactor, this);
      }
      if (stopping) {
        activeMasterSequencer = null;
        stopping = false;
      }
    }
  }

  @Override
  public void startMidi() {
    logger.log(Level.FINER, "startMidi()");
    synchronized (updateLock) {
      if (activeMasterSequencer != null) {
        return; //already started
      }

      if (tempoTrack == null) {
        stopMidi();
        return;
      }
      stopping = false;
      ArrayList<MasterSequencer.SubSequencer> subSequencersSnapShot;
      synchronized (subSequencers) {
        subSequencersSnapShot =
                new ArrayList<MasterSequencer.SubSequencer>(subSequencers);
      }
      activeMasterSequencer =
              new ActiveMasterSequencer(
              startPosition,
              subSequencersSnapShot,
              tempoTrack,
              timeSignatureTrack,
              tickLength,
              loopEndPoint,
              loopStartPoint,
              loopCount,
              sequencerEventListeners);
      activeMasterSequencer.startMidi();
    }

  }

  @Override
  public void stopMidi() {
    logger.log(Level.FINER, "stopMidi()");
    synchronized (updateLock) {
      if (activeMasterSequencer == null) {
        return; //already stopped
      }
      activeMasterSequencer.stopMidi();
      stopping = true;
      activeMasterSequencer = null; //<<<<<<<<<<<<<<<<????????
    }
  }

  @Override
  public void setTickPosition(double tick) {
    synchronized (updateLock) {
      startPosition = tick;
    }
  }

  @Override
  public boolean isRunning() {
    return (activeMasterSequencer != null);
  }

  @Override
  public void removeAllSubsequncers() {
    synchronized (updateLock) {
      stopMidi();
      subSequencers.clear();
    }
  }

  @Override
  public long getTickLength() {
    return tickLength;
  }

  /**
   * Sets the number of repetitions of the loop for playback. When the playback
   * position reaches the loop end point, it will loop back to the loop start
   * point count times, after which playback will continue to play to the end of
   * the sequence. @note if loop-start and loop end are too close (less than one
   * cycle apart), loops will not be repeated.
   *
   * @param count the number of times playback should loop back from the loop's
   * end position to the loop's start position, or {@link javax.sound.midi.Sequencer#LOOP_CONTINUOUSLY}
   * to indicate that looping should continue until interrupted
   */
  @Override
  public void setLoopCount(int count) {
    synchronized (updateLock) {
      this.loopCount = count;
    }
  }

  /**
   * Gets the number of repetitions of the loop for playback.
   */
  @Override
  public int getLoopCount() {
    return loopCount;
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
   * @note if loop-start and loop end are too close (less than one cycle apart),
   * loops will not be repeated. A sequencer's loop start point defaults to
   * start of the sequence.
   *
   * @param tick the loop's starting position, in MIDI ticks (zero-based)
   * @throws IllegalArgumentException if the requested loop start point cannot
   * be set, usually because it falls outside the sequence's duration or because
   * the start point is after the end point
   */
  @Override
  public void setLoopStartPoint(double tick) {
    synchronized (updateLock) {
      this.loopStartPoint = tick;
    }
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
   * sequence. @note if loop-start and loop end are too close (less than one
   * cycle apart), loops will not be repeated.
   *
   * @param tick the loop's ending position, in MIDI ticks (zero-based), or -1
   * to indicate the final tick
   * @throws IllegalArgumentException if the requested loop point cannot be set,
   * usually because it falls outside the sequence's duration or because the
   * ending point is before the starting point
   */
  @Override
  public void setLoopEndPoint(double tick) {
    synchronized (updateLock) {
      this.loopEndPoint = tick;
    }
  }

  /**
   * Obtains the end position of the loop, in MIDI ticks.
   *
   * @return the end position of the loop, in MIDI ticks (zero-based), or -1 to
   * indicate the end of the sequence
   */
  @Override
  public double getLoopEndPoint() {
    return loopEndPoint;
  }

  /**
   * Obtains the start position of the loop, in MIDI ticks.
   *
   * @return the start position of the loop, in MIDI ticks (zero-based)
   */
  @Override
  public double getLoopStartPoint() {
    return loopStartPoint;
  }

  @Override
  public void add(SequencerEventListener listener) {
    sequencerEventListeners.add(listener);
  }

  @Override
  public void remove(SequencerEventListener listener) {
    sequencerEventListeners.remove(listener);
  }

  @Override
  public double tickToEffectiveBPM(double tickPosition) {
    if (tempoTrack == null) {
      return 120 * getTempoFactor();
    }
     int index = tempoTrack.indexForTick((long)tickPosition);
     TempoEvent tempoEvent = tempoTrack.get(index);
     double tempoPerQarter =  tempoEvent.getTempoPerQuarter(); //   The tempo in microseconds per quarter note.
     
     return (60000000D * getTempoFactor())/tempoPerQarter;
  
  }
}

/**
 * The master-sequencer delegates the "prepare-cycle" function to the
 * active-master-sequencer. The active-master-sequencer is immutable in most of
 * its variables thus avoiding problems with concurrent accesses.
 *
 * @author harald
 */
class ActiveMasterSequencer {

  private static final Logger logger = Logger.getLogger(ActiveMasterSequencer.class.getName());
  // immutable objects
  private final TempoTrack tempoTrack;
  private final TimeSignatureTrack timeSignatureTrack;
  private final ArrayList<MasterSequencer.SubSequencer> subSequencers;
  private final long sequenceLenght;
  private final double loopEndPoint;
  private final double loopStartPoint;
  //
  private int loopCount;
  private final Object cycleLock = new Object();
  private double thisCycleStartTick = 0D;
  private double nextCycleStartTick = 0D;
  private double thisStreamTime = 0D;
  private double previousStreamTime = 0D;
  private TimeMap thisTimeMap = null;
  private TimeMap previousTimeMap = null;
  private final List<SequencerEventListener> sequencerEventListeners =
          new ArrayList<SequencerEventListener>();

  public ActiveMasterSequencer(
          double startPosition,
          final ArrayList<MasterSequencer.SubSequencer> subSequencers,
          TempoTrack tempoTrack,
          TimeSignatureTrack timeSignatureTrack,
          long tickLength,
          double loopEndPoint,
          double loopStartPoint,
          int loopCount,
          List<SequencerEventListener> sequencerEventListeners) {

    this.tempoTrack = tempoTrack;
    this.timeSignatureTrack = timeSignatureTrack;
    this.subSequencers = subSequencers;
    thisCycleStartTick = startPosition;
    nextCycleStartTick = startPosition;
    sequenceLenght = tickLength;
    this.loopEndPoint = loopEndPoint;
    this.loopStartPoint = loopStartPoint;
    this.loopCount = loopCount;
    this.sequencerEventListeners.addAll(sequencerEventListeners);
  }

  /**
   * Obtains the current position in the sequence, expressed in MIDI ticks. (The
   * duration of a tick in seconds is determined both by the tempo and by the
   * timing resolution stored in the Sequence.)
   *
   * @param streamTime the exact stream- time for which the tick position should
   * be calculated the stream time should be within the current or the previous
   * cycle.
   * @return current tick
   */
  public double getTickPosition(double streamTime) {
    if (streamTime > thisStreamTime) {
      TimeMap thisTimeMapLocal = thisTimeMap; // not really thread save, but better...
      if (thisTimeMapLocal == null) {
        return thisCycleStartTick;
      } else {
        return thisTimeMapLocal.getTickForOffset(streamTime - thisStreamTime);
      }
    } else {
      TimeMap previousTimeMapLocal = previousTimeMap;
      if (previousTimeMapLocal == null) {
        return thisCycleStartTick;
      } else {
        return previousTimeMapLocal.getTickForOffset(streamTime - previousStreamTime);
      }
    }
  }

  /**
   * Prepares the attached sub-sequencers to render the next cycle.
   *
   * @param streamTime the time in seconds for which this cycle should be
   * prepared.
   * @param cycleLength the length of the next cycle in seconds.
   */
  public void prepareCycle(double streamTime, double cycleLength, double tempoFactor, MasterSequencerImpl master) {

    synchronized (cycleLock) {
      // first calculate all the values we will need for a normal cycle
      boolean isLoopingCycle = false;
      TimeMap timeMap_2 = null;
      previousStreamTime = thisStreamTime;
      thisStreamTime = streamTime;
      thisCycleStartTick = nextCycleStartTick;
      thisTimeMap = tempoTrack.CreateTimeMap(thisCycleStartTick, 1.1 * cycleLength, tempoFactor);
      nextCycleStartTick = thisTimeMap.getTickForOffset(cycleLength);

      // let's see if we are doing a loop; if yes correct above values
      if (loopCount != 0) {
        if ((thisCycleStartTick <= loopEndPoint)
                && (loopEndPoint < nextCycleStartTick)) {
          isLoopingCycle = true;
          if (loopCount > 0) {
            loopCount--;
          }
          fireLoopCountChanged();
          double endpointOffset = thisTimeMap.getTimeOffset(loopEndPoint);
          timeMap_2 = tempoTrack.CreateTimeMap(loopStartPoint, 1.1 * cycleLength, tempoFactor);
          nextCycleStartTick = timeMap_2.getTickForOffset(cycleLength - endpointOffset);
        }
      }

      // now inform all the subSequencers
      for (MasterSequencer.SubSequencer s : subSequencers) {
        if (isLoopingCycle) {
          s.prepareLoopEndCycle(thisTimeMap, timeMap_2, thisCycleStartTick,
                  nextCycleStartTick, loopStartPoint, loopEndPoint);

        } else {
          s.prepareNormalCycle(thisTimeMap, thisCycleStartTick, nextCycleStartTick);
        }
      }
    }

    if (nextCycleStartTick > sequenceLenght) {
      master.stopMidi();
    }
  }

  public void startMidi() {
    synchronized (cycleLock) {
      logger.log(Level.FINER, "startMidi()");
      for (MasterSequencer.SubSequencer s : subSequencers) {
        s.preparePlaying(thisCycleStartTick);
      }
    }
    firePlayingChanged(true);
  }

  public void stopMidi() {
    synchronized (cycleLock) {
      for (MasterSequencer.SubSequencer s : subSequencers) {
        s.stopPlaying();
      }
    }
    firePlayingChanged(false);
  }

  public TimeSignatureTrack getTimeSignatureTrack() {
    return timeSignatureTrack;
  }

  private void firePlayingChanged(boolean value) {
    for (SequencerEventListener listener : sequencerEventListeners) {
      listener.notifyPlaying(value);
    }
  }

  private void fireLoopCountChanged() {
    for (SequencerEventListener listener : sequencerEventListeners) {
      listener.loopDone(loopCount);
    }
  }
}
