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


import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.openide.filesystems.FileObject;

/**
 * This class represents one single Track in a
 * {@link Song song}.
 * @author Harald Postner <Harald at H-Postner.de>
 */
@XmlRootElement
@XmlType
@XmlAccessorType(value = XmlAccessType.NONE)
public class MidiTrack extends GenericTrack implements Cloneable {




  /**
   * The path to a MIDI file. The Path must be expressed relatively to
   * the path of the song file. For the master-track this
   * parameter is mandatory, for all other tracks this parameter
   * is optional. If this file is given, the track-data
   * referenced by {@link #midiTrackIndex } is fetched from the given file.
   * If no file is given, a midi-file is searched in the parent track.
   * The given file must be compatible in timing with the midi-file
   * in the master-track.
   */
  @XmlElement(name = "sequencefile", required = false)
  private String sequencefile = null;

  public String getSequencefile() {
    return sequencefile;
  }

  public void setSequencefile(String sequencefile) {
    if (isImmutable()) {
      throw new EIllegalUpdate("Internal Error: object is immutable.");
    }
    this.sequencefile = sequencefile;
  }
  @XmlTransient
  private Sequence sequence = null;

  /**
   * Set the sequence from which this track fetches its Midi data.
   * The concrete data is determined by {@link #midiTrackIndex}.
   * Not every track needs its own sequence, if this parameter is
   * null, the track data is fetched from a parent track.
   * @param sequence 
   */
  public void setSequence(Sequence sequence) {
    this.sequence = sequence;
  }
  /**
   * This value links the track-data to a track in the sequence-file.
   * Negative values indicate that there is no link to a track.
   */
  @XmlElement(name = "midiTrackIndex", required = true)
  private int midiTrackIndex = -1;

  /**
   * Determine the Midi sequence from which the Midi Tracks should
   * be extracted.
   * @return the Midi sequence to be used for this track.
   * @throws EInvalidSongFile 
   */
  @Override
  public Sequence getSequence() throws EInvalidSongFile {
    if (sequence != null) {
      // A midi-sequence has already been identified for this track.
      return sequence;
    }
    if (sequencefile != null) {
      // A midi-sequence is identified for this track.
      // But it has not been loaded so far. Now it is time to load it.
      loadSequenceFromFile();
      return sequence;
    }
    if (getParentTrack() != null) {
      // No midi-sequence is identified for this track, so try with the parent track.
      return getParentTrack().getSequence();
    }
    // This must be the mastertrack because parentTrack == null.
    // For the mastertrack there must be a sequence, so we have a problem.
    throw new RuntimeException("Internal Error: No sequence for master-track.");
  }
  /**
   * Defines which midi channel will be used by the events
   * on this track. Although the MIDI specification allows
   * to mix several channels on the same track, we'll use
   * only one channel per track.
   * Usable  values range from 0 to 15.
   *
   * If no channel is defined this value is -1.
   */
  @XmlElement(name = "midiChannel", required = false)
  private int midiChannel = -1;
  /**
   * This variable can be used to indicate the patches that
   * are used to render the track.
   */
  @XmlElement(name = "instrumentDescription", required = false)
  private String instrumentDescription = null;

  /**
   * This variable can be used to indicate the patches that
   * are used to render the track.
   * @param description a string indicating which patches are used
   * to render the track.
   */
  public void setInstrumentDescription(String description) {
    if (isImmutable()) {
      throw new EIllegalUpdate("Internal Error: object is immutable.");
    }
    this.instrumentDescription = StringUtil.cleanXmlString(description);
  }

  public MidiTrack() {
  }

  public int getMidiTrackIndex() {
    return midiTrackIndex;
  }

  public void setMidiTrackIndex(int midiTrackIndex) {
    if (isImmutable()) {
      throw new EIllegalUpdate("Internal Error: object is immutable.");
    }
    this.midiTrackIndex = midiTrackIndex;
  }

  public Track getMidiTrack() throws EInvalidSongFile {
    int index = getMidiTrackIndex();
    if (index < 0) {
      return null;
    }
    Sequence seq = getSequence();
    if (seq == null) {
      return null;
    }
    Track[] tracks = seq.getTracks();
    if (index >= tracks.length) {
      throw new RuntimeException("Wrong MidiTrackIndex " + index + " in \"" + getName() + "\".");
    }
    return tracks[index];
  }

  /**
   * Set the midi channel used for this track. 
   * @param channel the midi channel used in this track
   */
  public void setMidiChannel(int channel) {
    if (isImmutable()) {
      throw new EIllegalUpdate("Internal Error: object is immutable.");
    }
    this.midiChannel = channel;
  }

  /**
   * Get the midi channel used for this track. Usually the whole track
   * should use one and the same channel. Negative values indicate that
   * the track has no channel-events (i.e. -2 for a master track)
   * @return the midi channel used in this track
   */
  public int getMidiChannel() {
    return this.midiChannel;
  }

  /**
   * This routine is called by MidiSythesizerTrack-objects which have
   * an attached sequence and which are
   * attempting to access the sequence but the sequence has
   * not yet been loaded from file.
   * sets the member {@link #sequence } by creating a
   * {@link Sequence } object from the {@link #sequencefile }
   * parameter.
   */
  private void loadSequenceFromFile() throws EInvalidSongFile {
    // as precondition we assume that this track has an attached sequence-file
    if (sequencefile == null) {
      throw new RuntimeException("Internal Error: member sequencefile is null.");
    }

    FileObject baseDir = getBaseDirectory();

    //open an input stream for the midi file.
    FileObject midiFile = baseDir.getFileObject(sequencefile);
    if (midiFile == null) {
      throw new EInvalidSongFile("Could not load file " + sequencefile);
    }
    InputStream stream;
    try {
      stream = midiFile.getInputStream();
    } catch (FileNotFoundException ex) {
      throw new EInvalidSongFile(ex);
    }

    // extract the midi sequence 
    try {
      sequence = MidiSystem.getSequence(stream);
    } catch (InvalidMidiDataException ex) {
      throw new EInvalidSongFile(ex);
    } catch (IOException ex) {
      throw new EInvalidSongFile(ex);
    }

  }

  @Override
  public MidiTrack clone() {
    MidiTrack clonedTrack = null;
    try {
      clonedTrack = (MidiTrack) super.clone();
      // clonedTrack.sequence = this.sequence.???; shall we deep copy ?
    } catch (CloneNotSupportedException ex) {
      throw new RuntimeException(ex);
    }
    return clonedTrack;
  }

  @Override
  public void detachAudio() {
    super.detachAudio();

  }
  private GenericTrack.EventHandler requiemHandler=null;

  /**
   * @deprecated 
   * @param handler 
   */
  @Deprecated
  public void addRequiemEventHandler(GenericTrack.EventHandler handler) {
    requiemHandler = handler;
  }
  @Override
    public void setMute(boolean value) {
    super.setMute(value);
    if(requiemHandler != null){
      requiemHandler.onMuteChange(value);
    }
  }
}
