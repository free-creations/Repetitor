/*
 * Copyright 2011 admin.
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
 * Implementation of the {@link SequencerMidiPort} interface.
 * @author Harald Postner <Harald at H-Postner.de>
 */
class RecorderPortImpl implements SequencerPort {

  public final AudioPort audioPort;
  public final AudioRecorderSubSequencer subsequencer;

  /**
   * Creates an immutable combination of a given {@link AudioPort}
   * and a given {@link AudioRecorderSubSequencer}.
   * @param audioPort the audio-port on which the sub-sequencer operates.
   * @param subsequencer the sub-sequencer operates that uses the given audio-port.
   */
  public RecorderPortImpl(AudioPort audioPort, AudioRecorderSubSequencer subsequencer) {
    this.audioPort = audioPort;
    this.subsequencer = subsequencer;
  }

  /**
   * {@inheritDoc }
   */
  @Override
  public AudioPort getAudioPort() {
    return audioPort;
  }



  @Override
  public void setMute(boolean value) {
    subsequencer.setMute(value);
  }


}
