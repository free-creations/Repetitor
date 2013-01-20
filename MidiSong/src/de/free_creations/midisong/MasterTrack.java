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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.openide.filesystems.FileObject;

/**
 * This class represents the master Track in a {@link Song song}.
 * The master track is the track at the top of track-hierarchy.
 * A master-track has an obligatory midi-file attached (see
 * {@link MidiTrack#getSequencefile()}). The track-data
 * referenced by {@link MidiTrack#midiTrackIndex } gives the timing for the whole
 * song.
 * @author Harald Postner <Harald at H-Postner.de>
 */
@XmlRootElement
@XmlType
@XmlAccessorType(value = XmlAccessType.NONE)
public class MasterTrack extends MidiTrack implements Cloneable {

  @XmlTransient
  private SongSession songSession = null;
  @XmlTransient
  private FileObject baseDirectory = null;
  @XmlTransient
  private boolean immutable = false;

  public void setBaseDirectory(FileObject baseDirectory) {
    if (isImmutable()) {
      throw new EIllegalUpdate("Internal Error: object is immutable.");
    }
    this.baseDirectory = baseDirectory;
  }

  @Override
  protected FileObject getBaseDirectory() {
    if (baseDirectory != null) {
      return baseDirectory;
    }
    if (getParentTrack() != null) {
      return getParentTrack().getBaseDirectory();
    }
    // This must be the mastertrack because parentTrack == null.
    // For the mastertrack there must be a base-directory, so we have a problem.
    throw new RuntimeException("Internal Error: No base-directory for master-track.");
  }

  /**
   * {@inheritDoc }
   */
  @Override
  public synchronized boolean isImmutable() {
    return immutable;
  }

  synchronized void setImmutable(boolean immutable) {
    this.immutable = immutable;
  }

  @Override
  public MasterTrack clone() {
    MasterTrack clonedTrack = (MasterTrack) super.clone();
    return clonedTrack;
  }

  @Override
  public SongSession getSongSession() {

    return songSession;
  }

  /**
   * The Song session shall register here.
   * @param songSession 
   */
  void setSongSession(SongSession songSession) {

    this.songSession = songSession;
  }
}
