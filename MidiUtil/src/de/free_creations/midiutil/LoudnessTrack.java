/*
 * Copyright 2011 harald.
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
package de.free_creations.midiutil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

/**
 * The LoudnessTrack collects all events that influence the sound level of a
 * Midi track.
 *
 * @author harald
 */
public class LoudnessTrack implements List<LoudnessEvent> {

  private ArrayList<LoudnessEvent> loudnessList;
  private final long maxTicks;
  private int channel = NOT_SET_YET;
  private int trackVolume = NOT_SET_YET;
  private static final int NOT_SET_YET = -1;
  private final boolean severeChannelCheck = true;

  /**
   * Create a loudnessList of all the loudness events contained in the given
   * track. The LoudnessEvent are sorted by ascending order.
   *
   * @param sequence the given sequence
   */
  public LoudnessTrack(Track track) {
    this(track, false);

  }

  /**
   * Create a loudnessList of all the loudness events contained in the given
   * track. The switch permits to include consider NoteOn messages as being
   * loudness events. The LoudnessEvent are sorted by ascending order.
   *
   * @param sequence the given sequence
   */
  public LoudnessTrack(Track track, boolean includeNoteOn) {
    loudnessList = new ArrayList<LoudnessEvent>();
    if (track == null) {
      throw new IllegalArgumentException("Track is null.");
    }
    // go backwards through  the track and create appropriate Loudness Events.
    int soundingNoteCount = 0;
    boolean precedesNoteOn = false;

    for (int i = track.size() - 1; i >= 0; i--) {
      MidiEvent event = track.get(i);
      if (Note.isNoteOffEvent(event)) {
        setChannelFromEvent(event);
        soundingNoteCount++;
      }
      if (Note.isNoteOnEvent(event)) {
        setChannelFromEvent(event);
        soundingNoteCount--;
        if (soundingNoteCount == 0) {
          precedesNoteOn = true;
        }
      }
      if (isLoudnessEvent(event, includeNoteOn)) {
        setChannelFromEvent(event);
        LoudnessEvent newLoudnessEvent = new LoudnessEvent(event);
        newLoudnessEvent.setObsolete(true);
        if (soundingNoteCount > 0) {
          newLoudnessEvent.setObsolete(false);
        }
        if (precedesNoteOn) {
          newLoudnessEvent.setObsolete(false);
          precedesNoteOn = false;
        }
        loudnessList.add(newLoudnessEvent);
      }
    }

    maxTicks = track.ticks();
    Collections.sort(loudnessList);

    determineLoudnessLevel();
    determineRedundantEvents();

  }

  private boolean isLoudnessEvent(MidiEvent event, boolean includeNoteOn) {
    if (includeNoteOn) {
      return LoudnessEvent.isLoudnessEventEx(event);
    } else {
      return LoudnessEvent.isLoudnessEvent(event);
    }
  }

  /**
   * This is a hack that permits to use the function "createNormalizedTrack" for
   * an other channel than the original channel.
   *
   * @param value
   */
  public void setChannel(int value) {
    channel = value;
  }

  /**
   * can be used to overwrite the automatic setting of the volume in function
   * createNormalizedTrack(). If this value is set, the output track might
   * differ in loudness from the input track.
   *
   * @param value
   */
  public void setTrackVolume(int value) {
    trackVolume = value;
  }

  /**
   * For every loudness-event we determine the loudness at this point taking
   * into account the current expression and the current volume values.
   */
  private void determineLoudnessLevel() {
    int currentExpression = 127;
    int currentVolume = 127;
    int currentVelocity = 127;

    for (LoudnessEvent event : loudnessList) {
      if (event.isExpression()) {
        currentExpression = event.getValue();
      } else if (event.isVolume()) {
        currentVolume = event.getValue();
      } else if (event.isKeyOn()) {
        currentVelocity = event.getValue();
      }
      event.setLoudnessLevel(currentVolume * currentExpression * currentVelocity);
    }
  }

  /**
   * For every loudness-event we determine the loudness at this point taking
   * into account the current expression and the current volume values.
   */
  private void determineRedundantEvents() {
    int currentLevel = -1;

    for (LoudnessEvent event : loudnessList) {
      if(event.getLoudnessLevel() == currentLevel){
        event.setObsolete(true);
      }  
      currentLevel = event.getLoudnessLevel();
    }
  }

  /**
   * Create a Midi track that has the loudness curve of this object. The
   * expression and volume events are normalised as explained below. <p> The
   * normalisation strategy: <ul> <li>Set one volume-event at the beginning of
   * the track; all following events must be expression events.</li> <li>Set the
   * events in such a way that the sequence still sounds the same. That is, at
   * every note the effective loudness (the product of volume and expression)
   * must be the same. Note exception to this rule below.</li> <li>try to
   * balance volume and expression so that the volume has maximum head room for
   * later mix down. That means, try to put the loudest expression at 100% and
   * turn down the volume accordingly.</li> </ul> </p> <p> The volume of the
   * resulting tracks can be tuned by setting the track-volume prior to run this
   * function, in this case the resulting track will sound in a different
   * loudness than the input track. </p>
   *
   * @param originalTrack the given input track.
   * @param emptyTrack the output track. All events from the original track will
   * be copied over to this track. Loudness events will be normalised. The empty
   * track should not contain any Midi events and must belong to a sequence that
   * is compatible with the input sequence (same time division).
   * @return the empty track filled with normalised events.
   */
  public Track createNormalizedTrack(Track originalTrack, Track emptyTrack) {

    // add all the loudness events collected so far
    if (!isEmpty()) {
      int originalTrackVolume = calcMaxTotalLevel() / (127 * 127);
      if (originalTrackVolume < 10) {
        originalTrackVolume = 10;
      }
      if (trackVolume == NOT_SET_YET) {
        emptyTrack.add(new_VolumeEvent(0L, originalTrackVolume));
      } else {
        emptyTrack.add(new_VolumeEvent(0L, trackVolume));
      }

      for (LoudnessEvent event : loudnessList) {
        if (!event.isObsolete()) {
          int expressionValue = Math.min(event.getLoudnessLevel() / (originalTrackVolume*127), 127);
          emptyTrack.add(new_ExpressionEvent(event.getTickPos(), expressionValue));
        }
      }
    }
    // add the remaining (non loudness )events.
    for (int i = 0; i < originalTrack.size(); i++) {
      MidiEvent event = originalTrack.get(i);
      if (!LoudnessEvent.isLoudnessEvent(event)) {
        emptyTrack.add(event);
      }
    }
    return emptyTrack;
  }

  /**
   * Inspect all events and report the biggest LoudnesslLevel.
   *
   * @return the biggest LoudnesslLevel in all events.
   */
  private int calcMaxTotalLevel() {
    int maxTotalLevel = 1;
    for (LoudnessEvent event : loudnessList) {
      if (!event.isObsolete()) {
        maxTotalLevel = Math.max(maxTotalLevel, event.getLoudnessLevel());
      }

    }
    return maxTotalLevel;
  }

  /**
   * Obtains the length of the track, expressed in MIDI ticks. This is the
   * length of the longest track contained in the sequence given on
   * construction.
   *
   * @return the duration, in ticks
   */
  public long ticks() {
    return maxTicks;

  }

  /**
   * Get the number of LoudnessEvents contained in this Track.
   *
   * @return the number of LoudnessEvents contained in this Track.
   */
  public int size() {
    return loudnessList.size();
  }

  public boolean isEmpty() {
    return loudnessList.isEmpty();
  }

  public boolean contains(Object o) {
    if (o instanceof LoudnessEvent) {
      return loudnessList.contains((LoudnessEvent) o);
    }
    return false;
  }

  public Iterator<LoudnessEvent> iterator() {
    return loudnessList.iterator();
  }

  public Object[] toArray() {
    return loudnessList.toArray();
  }

  public <T> T[] toArray(T[] a) {
    return loudnessList.toArray(a);
  }

  public boolean add(LoudnessEvent e) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean remove(Object o) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean containsAll(Collection<?> c) {
    return loudnessList.containsAll(c);
  }

  public boolean addAll(Collection<? extends LoudnessEvent> c) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean addAll(int index, Collection<? extends LoudnessEvent> c) {
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

  public LoudnessEvent get(int index) {
    return loudnessList.get(index);
  }

  public LoudnessEvent set(int index, LoudnessEvent element) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void add(int index, LoudnessEvent element) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public LoudnessEvent remove(int index) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public int indexOf(Object o) {
    return loudnessList.indexOf(o);
  }

  public int lastIndexOf(Object o) {
    return loudnessList.lastIndexOf(o);
  }

  public ListIterator<LoudnessEvent> listIterator() {
    return loudnessList.listIterator();
  }

  public ListIterator<LoudnessEvent> listIterator(int index) {
    return loudnessList.listIterator(index);
  }

  public List<LoudnessEvent> subList(int fromIndex, int toIndex) {
    return loudnessList.subList(fromIndex, toIndex);
  }

  /**
   * Get the index of the loudness-event that precedes the given Midi tick.
   *
   * @param tick the search position given in Midi ticks
   * @return the index of an event that is valid for the given midi tick.
   */
  public int indexForTick(long tick) {
    if (tick < 0) {
      tick = 0;
    }
    return eventForTickBinarySearch(tick, 0, loudnessList.size() - 1);
  }

  private int eventForTickBinarySearch(long searchTick, int lowIndex, int highIndex) {
    if (isEventPrecedingTick(searchTick, lowIndex)) {
      return lowIndex;
    }
    int midIndex = (lowIndex + highIndex + 1) / 2; //plus one -> round-Ceiling
    long midTick = loudnessList.get(midIndex).getTickPos();
    if (midTick > searchTick) {
      return eventForTickBinarySearch(searchTick, lowIndex, midIndex);
    } else {
      return eventForTickBinarySearch(searchTick, midIndex, highIndex);
    }

  }

  private boolean isEventPrecedingTick(long tick, int index) {
    long eventTick = loudnessList.get(index).getTickPos();
    if (eventTick > tick) {
      return false;
    }
    int nextEvent = index + 1;
    if (nextEvent >= loudnessList.size()) {
      return true;
    }
    long nextEventTick = loudnessList.get(nextEvent).getTickPos();
    if (nextEventTick > tick) {
      return true;
    }
    return false;

  }

  private MidiEvent new_ExpressionEvent(long pos, int value) {

    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(ShortMessage.CONTROL_CHANGE,
              channel, MidiUtil.contExpression_MSB, value);


    } catch (InvalidMidiDataException ex) {
      throw new RuntimeException(ex);
    }
    return new MidiEvent(message, pos);
  }

  private MidiEvent new_VolumeEvent(long pos, int value) {

    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(ShortMessage.CONTROL_CHANGE,
              channel, MidiUtil.contMainVolume_MSB, value);


    } catch (InvalidMidiDataException ex) {
      throw new RuntimeException(ex);
    }
    return new MidiEvent(message, pos);
  }

  private void setChannelFromEvent(MidiEvent event) {
    ShortMessage message = (ShortMessage) event.getMessage();
    int messageChannel = message.getChannel();
    if (this.channel == NOT_SET_YET) {
      this.channel = messageChannel;
    } else {
      if (this.channel != messageChannel) {
        if (severeChannelCheck) {
          throw new RuntimeException("channel missmatch in input track");
        }
      }
    }
  }
}
