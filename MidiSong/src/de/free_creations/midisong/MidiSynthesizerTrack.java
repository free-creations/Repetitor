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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * This class represents one single Track in a
 * {@link Song song}.
 * Like any {@link MidiTrack} this track may hold any kind
 * of Midi data, additionally this track has a {@link SynthesizerData}
 * object attached. All midi data of this track and of all subordinate
 * tracks will be send to the attached synthesiser.
 * @author Harald Postner <Harald at H-Postner.de>
 */
@XmlRootElement
@XmlType
@XmlAccessorType(value = XmlAccessType.NONE)
public class MidiSynthesizerTrack extends MidiTrack implements Cloneable {

  @XmlElementRefs({
    @XmlElementRef(type = BuiltinSynthesizer.class),
    @XmlElementRef(type = AsioSynthesizer.class)})
  private SynthesizerData synthesizer;

  public SynthesizerData getSynthesizer() {
    return synthesizer;
  }

  public void setSynthesizer(SynthesizerData synthesizer) {
    if (isImmutable()) {
      throw new EIllegalUpdate("Internal Error: object is immutable.");
    }
    this.synthesizer = synthesizer;
    synthesizer.setTrack(this);
  }

  public MidiSynthesizerTrack() {
  }

  /**
   * finalise the un-marshaling process by doing all the stuff that
   * the {@link javax.xml.bind.Unmarshaller } does not do (i.e.
   * link the sub-tracks to their parents).
   */
  @Override
  void finalizeUnmarshalling() {
    super.finalizeUnmarshalling();
    if (synthesizer != null) {
      synthesizer.setTrack(this);
      synthesizer.finalizeUnmarshalling();
    }
  }

  @Override
  public MidiSynthesizerTrack clone() {
    MidiSynthesizerTrack clonedSynthTrack = (MidiSynthesizerTrack) super.clone();
    try {
      if (this.synthesizer != null) {
        clonedSynthTrack.synthesizer = this.synthesizer.clone();
        clonedSynthTrack.synthesizer.setTrack(clonedSynthTrack);
      }
    } catch (CloneNotSupportedException ex) {
      throw new RuntimeException(ex);
    }
    return clonedSynthTrack;
  }

  @Override
  public void detachAudio() {
    super.detachAudio();
    synthesizer.detach();
  }
}
