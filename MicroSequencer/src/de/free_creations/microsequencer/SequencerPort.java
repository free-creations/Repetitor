/*
 *  Copyright 2013 Harald Postner <Harald at H-Postner.de>.
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

/**
 * A sequencer port permits to define what is played by the sequencer.
 * @author Harald Postner <Harald at H-Postner.de>
 */
public interface SequencerPort {

  /**
   * 
   * @return the {@link AudioPort} on which the audio stream is output.
   */
  public AudioPort getAudioPort();


  /**
   * Mute (or un-mute) the whole port port. 
   * @param value true - all tracks should remain quiet, false - the track 
   * may produce sound (tracks that have been muted individually must stay mute).
   */
  public void setMute(boolean value);

}
