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

import de.free_creations.midiutil.*;
import de.free_creations.midiutil.TempoTrack.TimeMap;
import java.io.IOException;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Soundbank;

/**
 * The master-sequencer steers a number of sub-sequencers. The main task of the
 * master-sequencer is to maintain the common information about timing. This
 * includes information such as: <ul> <li>at which Midi-tick does the next cycle
 * start.</li> <li>at which Midi-tick does the next cycle start.</li> <li>at
 * which speed must the next cycle be rendered.</li> </ul>
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
interface MasterSequencer {

  public void add(SequencerEventListener listener);

  public void remove(SequencerEventListener listener);

  /**
   * This interface defines the sub-sequencer as it is seen by the
   * master-sequencer. The sub-sequencer is defined as an interface to make it
   * easy to unit-test the master-sequencer independently of the sub-sequencer.
   */
  public interface SubSequencer {

    public void prepareSession(double startTick, PlayingMode mode);

    public void stopSession();
  }

  /**
   * This interface defines the sub-sequencer as it is seen by the
   * master-sequencer. The sub-sequencer is defined as an interface to make it
   * easy to unit-test the master-sequencer independently of the sub-sequencer.
   */
  public interface MidiSubSequencer extends SubSequencer {

    /**
     * The master-sequencer uses this function to indicate to its sub-sequencers
     * that they shall be prepared to process a cycle that contains a loop-end
     * point, which means the cursor must jump back to the beginning of the loop
     * within the current cycle.
     *
     * @param timeMap the mapping between synthesiser-time and Midi ticks, that
     * should be applied at the beginning of the current cycle.
     * @param thisCycleStartTick the first midi tick that shall be processed.
     * @param nextCycleStartTick one tick after the last midi tick that shall be
     * processed.
     */
    public void prepareNormalCycle(TimeMap timeMap,
            double thisCycleStartTick, double nextCycleStartTick);

    /**
     * The master-sequencer uses this function to indicate to its sub-sequencers
     * that they shall be prepared to process a cycle that contains a loop-end
     * point, which means the cursor must jump back to the beginning of the loop
     * within the current cycle.
     *
     * @param timeMap_1 the mapping between synthesiser-time and Midi ticks,
     * that should be applied at the beginning of the current cycle.
     * @param timeMap_2 the mapping between synthesiser-time and Midi ticks,
     * that should be applied after the cursor has jumped back to the beginning
     * of the loop.
     * @param thisCycleStartTick the first midi tick that shall be processed.
     * @param nextCycleStartTick one tick after the last midi tick that shall be
     * processed.
     * @param loopStartTick where to restart once loopEndTick has been reached.
     * @param loopEndTick how far we shall go until we are jumping back
     * loopStartTick.
     */
    public void prepareLoopEndCycle(TimeMap timeMap_1, TimeMap timeMap_2,
            double thisCycleStartTick, double nextCycleStartTick,
            double loopStartTick, double loopEndTick);
  }

  public interface SubSequencerFactory {

    public MidiSubSequencer make(final String name, Soundbank soundbank) throws MidiUnavailableException;

    public SubSequencer makeAudioRecorder(String name)throws IOException;
  }

  /**
   * Create a new subSequencer using the default synthesiser. The synthesiser
   * will use the given soundbank.
   *
   * @param name a name for the sub-sequencer.
   * @param soundbank the soundbank that the synthesiser shall use. If null, the
   * built-in soundbank will be used.
   * @return a newly created {@link MidiSubSequencer}.
   * @throws MidiUnavailableException if the synthesiser does not provide a
   * suitable MIDI-receiver.
   */
  public MidiSubSequencer createMidiSubSequencer(final String name, Soundbank soundbank) throws MidiUnavailableException;

  public SubSequencer createAudioRecorderSubSequencer(final String name) throws IOException;

  public double getTempoFactor();

  public void setTempoFactor(double tempoFactor);

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
  public double getCurrentTickPosition(double streamTime);

  /**
   * Determines the speed for a given position in the loaded sequence. The
   * returned result is expressed in Quarter-Beats per minute. The result takes
   * into account the tempo factor.
   *
   * @param tickPosition a position in the currently loaded sequence
   * @return the beats per minute at the given position.
   */
  public double tickToEffectiveBPM(double tickPosition);

  @Deprecated
  public BeatPosition getCurrentBeatPosition(double streamTime);

  @Deprecated
  public BeatPosition tickToBeatPosition(double tickPosition);

  public RPositionEx tickToRPositionEx(double tickPosition);

  public double beatPositionToTick(RPosition position);

  /**
   * Prepares the attached sub-sequencers to render the next cycle.
   *
   * @param streamTime the time in seconds for which this cycle should be
   * prepared.
   * @param cycleLength the length of the next cycle in seconds.
   */
  public void prepareCycle(double streamTime, double cycleLength);

  public void startMidi(PlayingMode playingMode);

  public void stopMidi();

  public void setTickPosition(double tick);

  public boolean isRunning();

  public void removeAllSubsequncers();

  public long getTickLength();

  /**
   * Sets the number of repetitions of the loop for playback. When the playback
   * position reaches the loop end point, it will loop back to the loop start
   * point count times, after which playback will continue to play to the end of
   * the sequence.
   *
   * If the current position when this method is invoked is greater than the
   * loop end point, playback continues to the end of the sequence without
   * looping, unless the loop end point is changed subsequently.
   *
   * A count value of 0 disables looping: playback will continue at the loop end
   * point, and it will not loop back to the loop start point. This is a
   * sequencer's default.
   *
   * If playback is stopped during looping, the current loop status is cleared;
   * subsequent start requests are not affected by an interrupted loop
   * operation.
   *
   * @param count the number of times playback should loop back from the loop's
   * end position to the loop's start position, or
   * {@link javax.sound.midi.Sequencer#LOOP_CONTINUOUSLY} to indicate that
   * looping should continue until interrupted
   */
  public void setLoopCount(int count);

  /**
   * Obtains the number of repetitions for playback.
   *
   * @return the number of loops after which playback plays to the end of the
   * sequence
   */
  public int getLoopCount();

  /**
   * Sets the first MIDI tick that will be played in the loop. If the loop count
   * is greater than 0, playback will jump to this point when reaching the loop
   * end point.
   *
   * A value of 0 for the starting point means the beginning of the loaded
   * sequence. The starting point must be lower than or equal to the ending
   * point, and it must fall within the size of the loaded sequence.
   *
   * A sequencer's loop start point defaults to start of the sequence.
   *
   * @param tick the loop's starting position, in MIDI ticks (zero-based)
   * @throws IllegalArgumentException if the requested loop start point cannot
   * be set, usually because it falls outside the sequence's duration or because
   * the start point is after the end point
   */
  public void setLoopStartPoint(double tick);

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
  public void setLoopEndPoint(double tick);

  /**
   * Obtains the end position of the loop, in MIDI ticks.
   *
   * @return the end position of the loop, in MIDI ticks (zero-based), or -1 to
   * indicate the end of the sequence
   */
  public double getLoopEndPoint();

  /**
   * Obtains the start position of the loop, in MIDI ticks.
   *
   * @return the start position of the loop, in MIDI ticks (zero-based)
   */
  public double getLoopStartPoint();

  public void setMasterTrack(TempoTrack tempoTrack, TimeSignatureTrack timeSignatureTrack, long tickLength);
}
