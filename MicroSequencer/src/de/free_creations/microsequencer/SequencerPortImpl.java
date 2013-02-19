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

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Track;

/**
 * Implementation of the {@link SequencerPort} interface.
 * @author Harald Postner <Harald at H-Postner.de>
 */
class SequencerPortImpl implements SequencerPort {

  public final AudioPort audioPort;
  public final MidiSubSequencer subsequncer;

  /**
   * Creates an immutable combination of a given {@link AudioPort}
   * and a given {@link MidiSubSequencer}.
   * @param audioPort the audio-port on which the sub-sequencer operates.
   * @param subsequncer the sub-sequencer operates that uses the given audio-port.
   */
  public SequencerPortImpl(AudioPort audioPort, MidiSubSequencer subsequncer) {
    this.audioPort = audioPort;
    this.subsequncer = subsequncer;
  }

  /**
   * {@inheritDoc }
   */
  @Override
  public AudioPort getAudioPort() {
    return audioPort;
  }

  /**
   * {@inheritDoc }
   */
  @Override
  public void setTracks(Track[] tracks) {
    subsequncer.setTracks(tracks);
  }

  /**
   * {@inheritDoc }
   */
  @Override
  public void send(MidiMessage message, double streamTime) {
    subsequncer.send(message, streamTime);
  }

  @Override
  public Track[] getTracks() {
    return subsequncer.getTracks();
  }

  @Override
  public void setMute(int trackIndex, boolean value) {
    subsequncer.setMute(trackIndex,  value);
  }

  @Override
  public void setMute(boolean value) {
    subsequncer.setMute(value);
  }

  @Override
  public void setAttenuation(int trackIndex, float value) {
    subsequncer.setAttenuation(trackIndex,  value);
  }
}
