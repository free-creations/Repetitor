/*
 * Copyright 2011 Harald Postner .
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
package de.free_creations.midisong;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * This class represents the synthesiser settings in a song file.
 * A synthesiser object is always attached to a 
 * {@link MidiSynthesizerTrack } object. This class should not 
 * be initialised as it is, but rather one of its descendant classes
 * {@link BuiltinSynthesizer} or {@link AsioSynthesizer}.
 * @author Harald Postner 
 */
@XmlRootElement
@XmlType
@XmlAccessorType(value = XmlAccessType.NONE)
public abstract class SynthesizerData {

  @XmlTransient
  private MidiSynthesizerTrack track = null;

  public String getName() {
    /** @TODO implement something more meaningful.**/
    return "" + this;
  }

  /**
   * Set the track to which this synthesiser is attached to.
   * When attaching a synthesiser to a {@link MidiSynthesizerTrack}
   * by means of the {@link MidiSynthesizerTrack#setSynthesizer}
   * function this procedure is automatically called.
   * @param track the track to which this synthesiser is attached to.
   */
  public void setTrack(MidiSynthesizerTrack track) {
    //no "setter-protection" because we would never be able to create a new song hierarchy.
    this.track = track;
  }

  /**
   * Get the track to which this synthesiser is attached to.
   * @return the track to which this synthesiser is attached to.
   */
  public MidiSynthesizerTrack getTrack() {
    return track;
  }

  /**
   * This function can be overwritten to finalise the un-marshaling 
   * process by doing all the stuff that
   * the {@link javax.xml.bind.Unmarshaller } does not do.
   * The default implementation does nothing.
   */
  void finalizeUnmarshalling() {
  }

  /**
   * A Synthesiser-data-object is said to be immutable if it must not change structurally.
   * This is particularly the case if the song (from which this synthesiser-object is part-of)
   * is involved in one or more active songSessions.
   *
   * @return true if the Synthesiser data cannot be changed.
   */
  public synchronized boolean isImmutable() {

    if (track != null) {
      return track.isImmutable();
    } else {
      //we assume that the track is null because this synthesizer data object
      //is still under construction. So we allow changes.
      return false;
    }

  }

  @Override
  public SynthesizerData clone() throws CloneNotSupportedException {
    return (SynthesizerData) super.clone();
  }

  abstract public void detach() ;
  
  abstract public float getVuLevel(int audioChannel);
}
