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
 * A LyricTrack is a list of {@link Lyric Lyric} objects.
 * This class provides methods to discover all lyrics-messages in a given Midi track
 * and methods to construct a corresponding LyricTrack-object.
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class LyricTrack implements List<Lyric> {

  private ArrayList<Lyric> list;

  /**
   * Create a list of all the notes contained in the given track.
   * The notes are sorted by ascending order.
   * @param track the given track
   */
  public LyricTrack(Track track) {
    list = new ArrayList<Lyric>();
    if (track == null) {
      return;
    }

    for (int i = 0; i < track.size(); i++) {
      MidiEvent event = track.get(i);
      if (Lyric.isLyricsEvent(event)) {
        list.add(new Lyric(event));
      }
      Collections.sort(list);

    }
  }

  public int size() {
    return list.size();
  }

  public boolean isEmpty() {
    return list.isEmpty();
  }

  public boolean contains(Object o) {
    if (o instanceof Lyric) {
      return list.contains((Lyric) o);
    }
    return false;
  }

  public Iterator<Lyric> iterator() {
    return list.iterator();
  }

  public Object[] toArray() {
    return list.toArray();
  }

  public <T> T[] toArray(T[] a) {
    return list.toArray(a);
  }

  public boolean add(Lyric e) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean remove(Object o) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean containsAll(Collection<?> c) {
    return list.containsAll(c);
  }

  public boolean addAll(Collection<? extends Lyric> c) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean addAll(int index, Collection<? extends Lyric> c) {
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

  public Lyric get(int index) {
    return list.get(index);
  }

  public Lyric set(int index, Lyric element) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void add(int index, Lyric element) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public Lyric remove(int index) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public int indexOf(Object o) {
    return list.indexOf(o);
  }

  public int lastIndexOf(Object o) {
    return list.lastIndexOf(o);
  }

  public ListIterator<Lyric> listIterator() {
    return list.listIterator();
  }

  public ListIterator<Lyric> listIterator(int index) {
    return list.listIterator(index);
  }

  public List<Lyric> subList(int fromIndex, int toIndex) {
    return list.subList(fromIndex, toIndex);
  }
}
