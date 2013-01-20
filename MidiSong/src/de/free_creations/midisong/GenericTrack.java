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

import java.util.ArrayList;
import javax.sound.midi.Sequence;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.openide.filesystems.FileObject;

/**
 * This class represents one single Track in a
 * {@link Song song}. This is the base class of {@link MasterTrack}, {@link MidiTrack
 * } and
 * {@link MidiSynthesizerTrack } and defines the features witch are common to
 * all kinds of tracks, namely the possibility to have sub-track, to hold a
 * reference to disk files.
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
@XmlRootElement
@XmlType
@XmlAccessorType(value = XmlAccessType.NONE)
public class GenericTrack {

  private ArrayList<EventHandler> eventHandlers = new ArrayList<EventHandler>();
  private ArrayList<EventHandler> nonAudioEventHandlers = new ArrayList<EventHandler>();

  /**
   * A sound producer can register an event handler in order to get informed
   * when an attribute has changed.
   */
  public interface EventHandler {

    public void onMuteChange(boolean value);

    public void onAttenuationChange(float value);
  }

  /**
   * Register an event handler. These event handlers will automatically be
   * removed when the function detachAudio is executed.
   *
   * @param eventHandlers
   */
  public void addEventHandler(EventHandler eventHandler) {
    eventHandlers.add(eventHandler);
  }

  /**
   * Register an event handler. These event handlers will NOT be removed when
   * the function detachAudio is executed.
   *
   * @param eventHandlers
   */
  public void addNonAudioEventHandler(EventHandler eventHandler) {
    nonAudioEventHandlers.add(eventHandler);
  }

  private void fireMuteChange() {
    for (EventHandler eventHandler : eventHandlers) {
      eventHandler.onMuteChange(mute);
    }
    for (EventHandler eventHandler : nonAudioEventHandlers) {
      eventHandler.onMuteChange(mute);
    }

  }

  private void fireAttenuationChange() {
    for (EventHandler eventHandler : eventHandlers) {
      eventHandler.onAttenuationChange(attenuation);
    }
    for (EventHandler eventHandler : nonAudioEventHandlers) {
      eventHandler.onAttenuationChange(attenuation);
    }

  }
  @XmlTransient
  private GenericTrack parentTrack = null;
  /**
   * The name of this track.
   */
  @XmlAttribute(name = "name")
  private String name;
  /**
   * A description of this track.
   */
  @XmlElement(name = "description", required = false)
  private String description;
  /**
   * A track can be set mute, than this track and all its sub-tracks are not
   * heard anymore.
   */
  @XmlElement(name = "mute", required = false)
  protected boolean mute = false;
  /**
   * A track may contain sub-tracks. A sub-track inherits properties from its
   * parent-track.
   */
  @XmlElementWrapper(name = "subtracks")
  @XmlElementRefs({
    @XmlElementRef(type = GenericTrack.class),
    @XmlElementRef(type = MidiSynthesizerTrack.class),
    @XmlElementRef(type = MidiTrack.class)})
  private ArrayList<GenericTrack> subtracks = new ArrayList<GenericTrack>();
  /**
   * The volume in decibel. The volume is expressed as an attenuation. Zero
   * means the original loudness, positive values are quieter negative values
   * are louder. Usable values range from -6 (doubling of volume) to 100 dB
   * (full damping, no sound).
   */
  @XmlElement(name = "attenuation", required = false)
  private float attenuation = 0F;

  public GenericTrack() {
  }

  public boolean isMute() {
    return mute;
  }

  public void setMute(boolean value) {
    mute = value;
    fireMuteChange();
  }

  public GenericTrack getParentTrack() {
    return parentTrack;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    if (isImmutable()) {
      throw new EIllegalUpdate("Internal Error: object is immutable.");
    }
    this.description = StringUtil.cleanXmlString(description);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    if (isImmutable()) {
      throw new EIllegalUpdate("Internal Error: object is immutable.");
    }
    this.name = StringUtil.cleanXmlString(name);
  }

  /**
   * Get all sub-tracks of this track.
   *
   * @return the sub-tracks of this track. If no sub-tracks are attached, an
   * empty array (of length zero) will be returned.
   */
  public GenericTrack[] getSubtracks() {
    return subtracks.toArray(new GenericTrack[]{});
  }

  /**
   * Get the volume in decibel. The volume is expressed as an attenuation. Zero
   * means the original loudness, positive values are quieter negative values
   * are louder. Usable values range from -6 (doubling of volume) to 100 dB
   * (full damping, no sound).
   *
   * @return the attenuation in decibel.
   */
  public float getAttenuation() {

    return attenuation;
  }

  /**
   * Set the volume in decibel. The volume is expressed as an value. Zero means
   * the original loudness, positive values are quieter negative values are
   * louder. Usable values range from -6 (doubling of volume) to 100 dB (full
   * damping, no sound).
   *
   * @param value the value in decibel.
   */
  public void setAttenuation(float value) {
    this.attenuation = value;
    fireAttenuationChange();
  }

  public void addSubtrack(GenericTrack newTrack) {
    newTrack.parentTrack = this;
    subtracks.add(newTrack);

  }

  /**
   * finalise the un-marshaling process by doing all the stuff that the {@link javax.xml.bind.Unmarshaller
   * } does not do (i.e. link the sub-tracks to their parents).
   */
  void finalizeUnmarshalling() {
    GenericTrack[] songTracks = this.getSubtracks();
    for (GenericTrack songTrack : songTracks) {
      songTrack.parentTrack = this;
      songTrack.finalizeUnmarshalling();
    }
  }

  public Sequence getSequence() throws EInvalidSongFile {

    if (getParentTrack() != null) {
      // No midi-sequence is identified for this track, so try with the parent track.
      return getParentTrack().getSequence();
    }
    // This must be the mastertrack because parentTrack == null.
    // For the mastertrack there must be a sequence, so we have a problem.
    throw new RuntimeException("Internal Error: No sequence for master-track.");
  }

  protected FileObject getBaseDirectory() {

    if (parentTrack != null) {
      return parentTrack.getBaseDirectory();
    }
    // This must be the mastertrack because parentTrack == null.
    // For the mastertrack there must be a base-directory, so we have a problem.
    throw new RuntimeException("Internal Error: No base-directory for master-track.");
  }

  public SongSession getSongSession() {

    if (parentTrack != null) {
      return parentTrack.getSongSession();
    }
    // This must be the mastertrack because parentTrack == null.
    // But for mastertrack this procedure is over-written, 
    // and cannot be called in this way, so we have a problem.
    throw new RuntimeException("Internal Error: wrong Track linking (root is not a Mastertrack).");
  }

  /**
   * A Track is said to be immutable if it cannot change structurally, this is
   * particularly the case if the song (from which this track is part-of) is
   * involved in one or more active songSessions.
   *
   * @return true if the track cannot be changed.
   */
  public synchronized boolean isImmutable() {

    if (parentTrack != null) {
      return parentTrack.isImmutable();
    }
    // No parent track, we are still under construction. So we allow changes
    return false;
  }

  @Override
  protected GenericTrack clone() throws CloneNotSupportedException {
    GenericTrack clonedTrack = (GenericTrack) super.clone();
    clonedTrack.parentTrack = null;
    clonedTrack.subtracks = new ArrayList<GenericTrack>();
    eventHandlers = new ArrayList<EventHandler>(eventHandlers);
    nonAudioEventHandlers = new ArrayList<EventHandler>(eventHandlers);
    for (GenericTrack subtrack : subtracks) {
      GenericTrack clonedSubtrack = subtrack.clone();
      clonedSubtrack.parentTrack = clonedTrack;
      clonedTrack.subtracks.add(clonedSubtrack);
    }


    return clonedTrack;
  }

  /**
   * Detach this track and all its sub-tracks from the audio hardware. The
   * event-handler is also removed.
   */
  public void detachAudio() {
    eventHandlers.clear();
    for (GenericTrack subtrack : subtracks) {
      subtrack.detachAudio();
    }
  }

  /**
   * Initialise the audio hardware attached to this track and all its
   * sub-tracks.
   */
  public void InitializeAudio() {
    for (GenericTrack subtrack : subtracks) {
      subtrack.InitializeAudio();
    }
    fireAttenuationChange();
    fireMuteChange();

  }
}
