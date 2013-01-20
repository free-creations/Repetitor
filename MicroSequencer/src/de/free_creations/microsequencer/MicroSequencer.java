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

import de.free_creations.midiutil.BeatPosition;
import de.free_creations.midiutil.RPosition;
import de.free_creations.midiutil.RPositionEx;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Soundbank;

/**
 * The {@link MicroSequencer } replaces the Midi sequencer provided with {@link javax.sound.midi
 * } and is specially tailored to be used with the {@link  rtaudio4java RtAudio}
 * package. <p> Special features of this sequencer are: </p> <ul> <li>The
 * sequencer is synchronised through the audio interface, enabling a precise
 * timing of the Midi reproduction.</li> <li>The reproduction is not limited to
 * sixteen Midi channels. The sequencer can control an unlimited number of
 * synthesisers thus imposing no upper limit on the number of sounds that can be
 * played simultaneously. </li> </ul>
 *
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
public interface MicroSequencer extends Sequencer {

  /**
   * Registers a meta-event listener to receive notification whenever the
   * sequencer has reached the loopEnd point and is jumping backwards to the
   * loop start-point.
   *
   * @param listener listener to add
   */
  void addSequencerEventListener(SequencerEventListener listener);

  /**
   * Removes the specified loop-event listener from this sequencer's list of 
   * registered listeners, if in fact the listener is registered.
   * @param listener 
   */
  void removeSequencerEventListener(SequencerEventListener listener);

  /**
   * Set the length of the lead-in. At the loopStartPoint the volume is
   * incremented from zero to reach the target value.
   *
   * @param ticks the length in of the lead in in midi ticks.
   * @see Sequencer#getLoopStartPoint()
   */
  public void setLeadinTime(long ticks);

  /**
   * Get the length of the lead-in. At the loopStartPoint the volume is
   * incremented from zero to the target value.
   *
   * @return the length in of the lead-in in midi ticks.
   * @see Sequencer#getLoopStartPoint()
   */
  public long getLeadinTime();

  /**
   * Set the length of the lead-out. At the given number of ticks before the
   * loopEndPoint, the volume will decrement to reach zero at the loopEndPoint.
   *
   * @param ticks the length in of the lead-out in midi ticks.
   * @see Sequencer#getLoopEndPoint()
   */
  public void setLeadoutTime(long ticks);

  /**
   * Get the length of the lead-out. At the returned number of ticks before the
   * loopEndPoint the volume will decrement to reach zero at the loopEndPoint.
   *
   * @return the length in of the lead-out in midi ticks.
   * @see Sequencer#getLoopEndPoint()
   */
  public long getLeadoutTime();

  /**
   * Sets the sequence that defines the timing of the tracks. The current
   * implementation differs to the one specified in {@link Sequencer#setSequence(Sequence)}
   * by the fact that the tracks are ignored. To make tracks play they must be
   * added to a port that is obtained by {@link #createDefaultSynthesizerPort}
   *
   * @param sequence
   */
  @Override
  public void setSequence(Sequence sequence);

  /**
   * Returns the current tempo factor as a double value. The default is 1.0.
   *
   * @return tempo factor.
   */
  public double getTempoFactorEx();

  /**
   * Obtains the current position in the sequence, expressed in MIDI ticks. (The
   * duration of a tick in seconds is determined both by the tempo and by the
   * timing resolution stored in the Sequence.)
   *
   * @param offset an offset in seconds that is added to the current time
   * so the returned value is the position that will (probably) be reached
   * in the given time. ("Probably" - because if a sudden tempo-factor change occurs
   * within the given time interval, the estimation will be wrong.)
   * @return the position in midi ticks.
   */
  public double getTickPosition(double offset);

//  /**
//   * Obtains the current position in the sequence, as musical measure and beat.
//   * (The duration of a measures and beats is determined by the tempo and the
//   * time signature.
//   *
//   * @param offset
//   * @return
//   * @deprecated use tickToRPositionEx(getTickPosition(double offset));
//   */
//  @Deprecated
//  public BeatPosition getBeatPosition(double offset);

  /**
   * Translates a position in the loaded sequence given in Midi-Ticks into a
   * musical position expressed as measure and beat.
   *
   * @param tickPosition a position in the sequence. If the given position lies
   * outside the sequence the returned position is truncated to the lower or the
   * upper end. If no sequence is currently loaded the returned position is
   * always zero.
   * @return
   */
  public RPositionEx tickToRPositionEx(double tickPosition);

  /**
   * @deprecated use tickToRPositionEx
   * @param tickPosition
   * @return
   */
  @Deprecated
  public BeatPosition tickToBeatPosition(double tickPosition);

  /**
   * Translates a position in the loaded sequence given as a musical position
   * expressed as measure and beat into midi ticks.
   *
   * @param beatPosition a position in the sequence. The numerator and
   * denominator values are ignored. If the value given as beats is larger than
   * the actual denominator (larger than there are beats in one measure) the
   * position is extrapolated over the given measure.
   * @return
   */
  public double beatPositionToTick(RPosition position);

  /**
   * Sets the sequencer position in MIDI ticks. At the next invocation of
   * start(), the sequencer will start from this position.
   *
   * @param tick the tick to start from.
   */
  @Override
  public void setTickPosition(long tick);

  /**
   * Sets the sequencer position in MIDI ticks. At the next invocation of
   * start(), the sequencer will start from this position.
   *
   * @param tick the tick to start from.
   */
  public void setTickPosition(double tick);

  /**
   * Opens the synthesiser and the the required audio resources. <p> Note that
   * once closed, it cannot be reopened. Attempts to reopen this device will
   * always result in a MidiUnavailableException. </p>
   *
   * @throws MidiUnavailableException
   */
  @Override
  public void open() throws MidiUnavailableException;

  /**
   * Closes the synthesiser and and the audio system. <p> Note that once closed,
   * it cannot be reopened. </p> Attempts to reopen this device will always
   * result in a MidiUnavailableException.
   */
  @Override
  public void close();

  /**
   * Create a port that renders tracks on the default synthesiser using the
   * given sound-bank.
   *
   * @param name a name for the port.
   * @param soundbank the sound-bank that the synthesiser shall use (if null,
   * the built-in sound-bank will be used).
   * @return a sequencer port which allows to attach the tracks that shall be
   * rendered on this port.
   * @throws MidiUnavailableException if the port could not be created.
   */
  public SequencerPort createDefaultSynthesizerPort(final String name, Soundbank soundbank) throws MidiUnavailableException;

  /**
   * Closes all ports and removes them from the process loop.
   */
  public void removeAllPorts();

  public void setTempoFactor(double factor);

  /**
   * Returns the maximum DSP load that was measured since the last request.
   *
   * @return
   */
  public double getMaxLoadAndClear();

  public double tickToEffectiveBPM(double tickPosition);


}
