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

import de.free_creations.microsequencer.SequencerPort;
import javax.sound.midi.Soundbank;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
public class BuiltinSynthesizer extends SynthesizerData implements Cloneable {

  private SequencerPort port;

  public String getSoundbankfile() {
    return soundbankfile;
  }

  /**
   * Returns the port that this Synthesiser uses to communicate with
   * the sequencer. This function is not for public use its sole use is
   * unit testing.
   * @return 
   */
  SequencerPort getPort() {
    return port;
  }

  void setPort(SequencerPort port) {
    this.port = port;
  }

  public void setSoundbankfile(String soundbankfile) {
    if (isImmutable()) {
      throw new EIllegalUpdate("Internal Error: object is immutable.");
    }
    this.soundbankfile = soundbankfile;
  }
  /**
   * A name for this instance of synthesiser.
   */
  @XmlAttribute
  private String name;
  /**
   * A description of this track.
   */
  @XmlElement
  private String description;
  /**
   * Path to the soundfont file. 
   * The Path must be expressed relatively to
   * the BaseDirectory of the song file. If no sound-bank-file
   * is defined, the default sound-bank will be used.
   */
  @XmlElement
  private String soundbankfile;
  /**
   * The content of the soundfont file. This value is <code>null</code>
   * until the sound-bank is accessed for the first time (lazy loading).
   */
  @XmlTransient
  private Soundbank soundbank = null;

  public void setSoundbank(Soundbank soundbank) {
    this.soundbank = soundbank;
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

  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = StringUtil.cleanXmlString(name);
  }

  /**
   * Determine the sound-bank that shall be used by the synthesiser.
   * @return the sound-bank for the synthesiser (or null if none defined)
   * @throws EInvalidSongFile if the sound-bank cannot be accessed.
   */
  public Soundbank getSoundbank() throws EInvalidSongFile {

    if (soundbank != null) {
      // A soundbank has already been loaded.
      return soundbank;
    }
    if (soundbankfile != null) {
      // A soundbank is identified for this track.
      // But it has not been loaded so far. Now it is time to load it.
      loadSoundbankFromFile();
      return soundbank;
    }

    // No soundbank has been defined.
    return null;
  }

  public BuiltinSynthesizer() {
  }

  private void loadSoundbankFromFile() throws EInvalidSongFile {
    // as precondition we assume that this track has an attached soundbank-file
    if (soundbankfile == null) {
      throw new EIllegalUpdate("Internal Error: member soundbankfile is null.");
    }

    FileObject baseDir = getTrack().getBaseDirectory();
    
    soundbank = SoundbankCache.getSoundbank(baseDir, soundbankfile);
  }

  @Override
  public BuiltinSynthesizer clone() throws CloneNotSupportedException {
    if (port != null) {
      throw new CloneNotSupportedException("Cannot clone when connected to a port.");
    }
    return (BuiltinSynthesizer) super.clone();
  }

  @Override
  public void detach() {
    port = null;
  }

  @Override
  public float getVuLevel(int audioChannel) {
    if (port == null) {
      return 0F;
    } else {
      float result = port.getAudioPort().getPeakVuAndClear(audioChannel);
      return result;
    }
  }
}
