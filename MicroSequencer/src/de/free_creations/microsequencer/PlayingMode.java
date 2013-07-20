/*
 * Copyright 2013 Harald Postner.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.free_creations.microsequencer;

/**
 *
 * @author Harald Postner
 */
 public enum PlayingMode {
  /**
   * Play only Midi, Audio is off.
   */
  MidiOnly, // 
  /**
   * Play Midi and record Audio.
   */
  RecordAudio, // 
  /**
   * Play Midi and re-play previously recorded Audio.
   */
  PlayAudio, // play Midi and re-play prevously recorded Audio
  /**
   * Play Midi in loop mode and, in parallel,
   * re-play Audio and record Audio input. 
   * At the loop end point switch input and output.
   */
  PlayRecordAudio
  
}
