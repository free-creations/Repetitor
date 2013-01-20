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
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 * The Song class maintains the data specific
 * to one song and permits to write and read these data from
 * an XML-file.
 * <br>
 * The Song is a piece of music that can be played-back
 * in a well defined manner. To specify how the song should sound,
 * we need a number of pieces of information:
 * <ol>
 * <li> A number of {@link GenericTrack tracks}
 * that defines the notes to be played.</li>
 * <li> synthesiser settings that defines the how each instrument should sound.</li>
 * <li> Initial values for the main volume of each track.</li>
 * </ol>
 * <p>
 * The tracks are organised in a hierarchy. Subordinate tracks 
 * inherit properties from superior tracks. The root of the hierarchy is always 
 * a {@link MasterTrack }.
 * </p>
 * The song class maintains this information and can store itself
 * on disk (into an XML stream).
 * @author Harald Postner <Harald at H-Postner.de>
 */
@XmlRootElement(namespace = "repetitormedia")
@XmlAccessorType(XmlAccessType.NONE)
@XmlType
public class Song implements Cloneable {

  @XmlAttribute(name = "schemaVersion", required = true)
  public String schemaVersion = "1.0";
  @XmlTransient
  private FileObject baseDirectory;
  /**
   * The name of this song.
   */
  @XmlAttribute(name = "name")
  private String name;
  /**
   * A short descriptive of this song.
   */
  @XmlElement(name = "description", required = false)
  private String description;
  /**
   * A song must have at least one master-track that defines the timing and
   * contains further sub-tracks for the music.
   */
  @XmlElement(name = "mastertrack", required = true)
  private MasterTrack mastertrack = null;
  /** 
   * The {@link SongSession  SongSession objects} currently attached to this song. 
   */
  private ArrayList<SongSession> sessions = new ArrayList<SongSession>();

  public MasterTrack getMastertrack() {
    return mastertrack;
  }

  public FileObject getBaseDirectory() {
    return baseDirectory;
  }

  public void setBaseDirectory(FileObject directory) {
    if (isImmutable()) {
      throw new EIllegalUpdate("Internal Error: object is immutable.");
    }
    baseDirectory = directory;
    if (mastertrack != null) {
      mastertrack.setBaseDirectory(baseDirectory);
    }
  }

  public MasterTrack createMastertrack() {
    mastertrack = new MasterTrack();
    mastertrack.setName("Mastertrack");
    if (baseDirectory != null) {
      mastertrack.setBaseDirectory(baseDirectory);
    }
    return mastertrack;
  }

  public static Song createFromFile(URL xmlFileUrl) throws EInvalidSongFile {
    FileObject xmlFile = URLMapper.findFileObject(xmlFileUrl);
    if (xmlFile == null) {
      throw new EInvalidSongFile("File not found " + xmlFileUrl);
    }
    InputStream stream;
    try {
      stream = xmlFile.getInputStream();
    } catch (FileNotFoundException ex) {
      throw new EInvalidSongFile(ex);
    }
    Song newSong = unmarshal(stream);
    try {
      stream.close();
    } catch (IOException ex) {
      Exceptions.printStackTrace(ex);
    }
    newSong.setBaseDirectory(xmlFile.getParent());
    return newSong;
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
   * Write the properties of this object to an output stream.
   * @param stream The XML will be sent to the given OutputStream.
   * Upon a successful completion, the stream will be closed by this method.
   */
  public void marshal(OutputStream stream) throws JAXBException {
    JAXBContext jAXBContext = JAXBContext.newInstance(BuiltinSynthesizer.class,
            SynthesizerData.class,
            MidiSynthesizerTrack.class,
            MasterTrack.class,
            MidiTrack.class,
            GenericTrack.class,
            Song.class);
    Marshaller marshaller = jAXBContext.createMarshaller();
    marshaller.marshal(this, stream);

  }

  /**
   * Reads in a Song object tree from the given XML input.
   * @param stream The entire stream is read as an XML info-set.
   * Upon a successful completion, the stream will be closed by this method.
   * @return a song object 
   */
  static Song unmarshal(InputStream stream) throws EInvalidSongFile {
    try {
      JAXBContext jAXBContext = JAXBContext.newInstance(BuiltinSynthesizer.class,
              SynthesizerData.class,
              MidiSynthesizerTrack.class,
              MasterTrack.class,
              MidiTrack.class,
              GenericTrack.class,
              Song.class);
      Unmarshaller unmarshaller = jAXBContext.createUnmarshaller();
      Song newSong = (Song) unmarshaller.unmarshal(stream);
      newSong.finalizeUnmarshalling();
      return newSong;
    } catch (Throwable ex) {
      throw new EInvalidSongFile(ex);
    }
  }

  /**
   * finalise the un-marshaling process by doing all the stuff that
   * the {@link javax.xml.bind.Unmarshaller } does not do (i.e.
   * link the sub-tracks to their parents).
   */
  private void finalizeUnmarshalling() {
    if (mastertrack == null) {
      throw new RuntimeException("Internal error: mastertrack is null");
    }
    mastertrack.setBaseDirectory(getBaseDirectory());
    mastertrack.finalizeUnmarshalling();
  }
  private boolean immutable = false;

  /**
   * A song is said to be immutable if cannot change structurally,
   * this is particularly the case if a song is involved in one or more active songSessions.
   *
   * @return true if the song cannot be changed.
   */
  public synchronized boolean isImmutable() {
    return immutable;
  }

  /**
   * Set the value of immutable
   *
   * @param immutable new value of immutable
   */
  synchronized void setImmutable(boolean immutable) {
    this.immutable = immutable;
    if (getMastertrack() != null) {
      getMastertrack().setImmutable(immutable);
    }
  }

  @Override
  public Song clone() {
    Song clonedSong = null;
    try {
      clonedSong = (Song) super.clone();
      if (this.mastertrack != null) {
        clonedSong.mastertrack = this.mastertrack.clone();
      }
    } catch (CloneNotSupportedException ex) {
      Exceptions.printStackTrace(ex);
    }
    return clonedSong;
  }

  /**
   * Detach this song from the synthesiser.
   * This function is only used by the SongSession.
   */
  void detachAudio() {
    if (mastertrack != null) {
      mastertrack.detachAudio();
    }
  }

  @Override
  public String toString() {
    return "Song{" + "name=" + name + '}';
  }

  public SongSession createSession() throws EInvalidSongFile {
    SongSession songSession = new SongSession(this);
    sessions.add(songSession);
    String sessionName = this.getName();
    int sessionsCount = sessions.size();
    if (sessionsCount > 1) {
      sessionName = sessionName + " (" + sessionsCount + ")";
    }
    songSession.setName(sessionName);
    return songSession;
  }
}
