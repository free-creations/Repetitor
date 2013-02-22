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

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Track;

/**
 * A sequencer port permits to define which tracks should be played on which
 * synthesiser.
 * @author Harald Postner <Harald at H-Postner.de>
 */
public interface SequencerMidiPort extends SequencerPort {

  /**
   * 
   * @return the {@link AudioPort} on which the audio stream is output.
   */
  @Override
  public AudioPort getAudioPort();

  /**
   * Set the tracks that shall be rendered on this port.
   * @param tracks the tracks that shall be rendered on this port.
   */
  public void setTracks(Track[] tracks);

  /**
   * Get the tracks that shall be rendered on this port.
   * @return the tracks that shall be rendered on this port.
   */
  public Track[] getTracks();

  /**
   * Sends a MIDI message to the attached synthesiser. 
   * This procedure allows to render midi events that are not contained
   * in the tracks. The timestamp
   * is given relative to the current time of the Audio-System.
   * If the given streamTime is negative, the sequencer will do 
   * its best effort to send the message as soon as possible.
   * @param message the MIDI message to send.
   * @param streamTime the point in time in seconds when the message should 
   * be processed.
   */
  public void send(MidiMessage message, double streamTime);

  /**
   * Mute (or un-mute) an individual track. 
   * @param trackIndex an index into the array given in {@link #setTracks(javax.sound.midi.Track[]) }
   * @param value true - the track should remain quiet, false the track produces sound.
   */
  public void setMute(int trackIndex, boolean value);

  /**
   * Mute (or un-mute) the whole port track. 
   * @param value true - all tracks should remain quiet, false - the track 
   * may produce sound (tracks that have been muted individually must stay mute).
   */
  @Override
  public void setMute(boolean value);

  /**
   * Set the attenuation of an individual track. 
   * @param trackIndex an index into the array given in {@link #setTracks(javax.sound.midi.Track[]) }
   * @param value the value in decibels
   */
  public void setAttenuation(int trackIndex, float value);
}
