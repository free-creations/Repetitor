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
package de.free_creations.midiutil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Track;

/**
 * A NoteTrack is a list of {@link Note Note} objects.
 * This class provides methods to discover all note-messages in a given Midi track
 * and methods to construct a corresponding NoteTrack-object.
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class NoteTrack implements List<Note> {

  private ArrayList<Note> list;
  private ArrayList<MidiEvent> unmatchedNoteOns;
  boolean severecheck = false;
  private int minPitch;
  private int maxPitch;

  /**
   * Create a list of all the notes contained in the given track.
   * The notes are sorted by ascending order. Problems with hanging notes
   * are silently ignored.
   * @param track the given track
   */
  public NoteTrack(Track track) {
    this(track, false);

  }

  /**
   * Create a list of all the notes contained in the given track.
   * The notes are sorted by ascending order.
   * @param track the given track
   * @param severecheck when true the track will be checked on errors
   * like hanging notes (notes that are never closed).
   * @throws RuntimeException if in mode "severecheck" errors are encountered.
   */
  public NoteTrack(Track track, boolean severecheck) {
    this.severecheck = severecheck;
    list = new ArrayList<Note>();
    if (track == null) {
      maxPitch = 127;
      minPitch = 0;
      return;
    }
    maxPitch = -1;
    minPitch = 0xFFFF;
    unmatchedNoteOns = new ArrayList<MidiEvent>();

    for (int i = 0; i < track.size(); i++) {
      MidiEvent event = track.get(i);
      if (Note.isNoteOnEvent(event)) {
        unmatchedNoteOns.add(event);
      } else {
        if (Note.isNoteOffEvent(event)) {
          matchNoteOff(event);
        }
      }
    }
    Collections.sort(list);
    if (maxPitch < 0) {
      maxPitch = 127;
    }
    if (minPitch > 127) {
      minPitch = 0;
    }
    if (severecheck) {
      if (unmatchedNoteOns.size() > 0) {
        throw new RuntimeException("Track has unmatched Note-ons.");
      }
    }
  }

  /**
   * Get lowest Midi key number of all notes found in the track.
   * If the track is empty a value of 0 will be returned.
   * @return the minimum pitch in the track.
   */
  public int getMinPitch() {
    return minPitch;
  }

  /**
   * Get highest Midi key number of all notes found in the track.
   * If the track is empty a value of 127 will be returned.
   * @return the maximum pitch in the track.
   */
  public int getMaxPitch() {
    return maxPitch;
  }

  public int size() {
    return list.size();
  }

  public boolean isEmpty() {
    return list.isEmpty();
  }

  public boolean contains(Object o) {
    if (o instanceof Note) {
      return list.contains((Note) o);
    }
    return false;
  }

  public Iterator<Note> iterator() {
    return list.iterator();
  }

  public Object[] toArray() {
    return list.toArray();
  }

  public <T> T[] toArray(T[] a) {
    return list.toArray(a);
  }

  public boolean add(Note e) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean remove(Object o) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean containsAll(Collection<?> c) {
    return list.containsAll(c);
  }

  public boolean addAll(Collection<? extends Note> c) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean addAll(int index, Collection<? extends Note> c) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void clear() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Note get(int index) {
    return list.get(index);
  }

  public Note set(int index, Note element) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void add(int index, Note element) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Note remove(int index) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public int indexOf(Object o) {
    return list.indexOf(o);
  }

  public int lastIndexOf(Object o) {
    return list.lastIndexOf(o);
  }

  public ListIterator<Note> listIterator() {
    return list.listIterator();
  }

  public ListIterator<Note> listIterator(int index) {
    return list.listIterator(index);
  }

  public List<Note> subList(int fromIndex, int toIndex) {
    return list.subList(fromIndex, toIndex);
  }

  private void addNote(Note newNote) {
    list.add(newNote);
    if (newNote.getPitch() > maxPitch) {
      maxPitch = newNote.getPitch();
    }
    if (newNote.getPitch() < minPitch) {
      minPitch = newNote.getPitch();
    }
  }

  private void matchNoteOff(MidiEvent noteOff) {
    for (MidiEvent noteOn : unmatchedNoteOns) {
      if (Note.isNoteOffEventFor(noteOn, noteOff)) {
        Note newNote = new Note(noteOn, noteOff);
        addNote(newNote);
        unmatchedNoteOns.remove(noteOn);
        return;
      }
    }
    if (severecheck) {
      throw new RuntimeException("Track has useless Note-offs.");
    }
  }
}
