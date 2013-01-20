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
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

/**
 * A timeSignatureTrack is a list of {@link TimeSignature} objects. This class
 * provides methods to discover all timeSignature-messages in a given Midi
 * sequence and methods to construct a corresponding TimeSignatureTrack-object.
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class TimeSignatureTrack implements List<TimeSignature> {

  private class TimeSignatureEx extends TimeSignature {

    private TimeSignatureEx(int num, int denom, int tickPQN, long tickPos) {
      super(num, denom, tickPQN, tickPos);
    }
    private long measure;
    private double beat;

    private TimeSignatureEx(MidiEvent event, int tickPQN) {
      super(event, tickPQN);
    }

    /**
     * Get the position relative to the previous Time signature.
     *
     * @return the value of beatPosition
     */
    public BeatPosition getBeatPosition() {
      return new RPositionEx(getNumerator(), getDenominator(), measure, beat);
    }

    /**
     * Set the position relative to the previous Time signature.
     *
     * @param measure in which measure does this time signature appear (the
     * measure is calculated relative to the previous time signature).
     * @param beat in which beat does this time signature appear (the beat is
     * calculated relative to the previous time signature).
     */
    private void setBeatPosition(long measure, double beat) {
      this.measure = measure;
      this.beat = beat;
    }

    private BeatPosition getBeatPosition(double tickPos) {
      return getRPositionEx(tickPos);
    }

    private RPositionEx getRPositionEx(double tickPos) {
      double tickDist = tickPos - getTickPos();
      double measures = Math.floor(tickDist / getBarLength());
      double rest = tickDist - measures * getBarLength();
      double beats = rest / getBeatLength();
      return new RPositionEx(getNumerator(),
              getDenominator(),
              (long) (this.measure + measures),
              beats);
    }
  }
  private ArrayList<TimeSignature> timeSignatureList;
  private long maxTicks = 0;

  /**
   * Create a timeSignatureList of all the timeSignature events contained in the
   * given sequence. The timeSignatureEvent are sorted by ascending order.
   *
   * @param sequence the given sequence
   */
  public TimeSignatureTrack(Sequence sequence) {
    timeSignatureList = new ArrayList<TimeSignature>();
    if (sequence == null) {
      throw new IllegalArgumentException("Sequence is null.");
    }
    if (sequence.getDivisionType() != Sequence.PPQ) {
      throw new IllegalArgumentException("Sequence timing must be in PPQ (pulses per quarter note).");
    }
    int tickPQN = sequence.getResolution();
    Track[] tracks = sequence.getTracks();

    // go through  all tracks and create appropriate Time signature Events.
    for (Track track : tracks) {
      for (int i = 0; i < track.size(); i++) {
        MidiEvent event = track.get(i);
        if (TimeSignature.isTimeSignatureEvent(event)) {
          timeSignatureList.add(new TimeSignatureEx(event, tickPQN));
        }
        maxTicks = Math.max(maxTicks, track.ticks());
      }
    }
    Collections.sort(timeSignatureList);
    // if the sequence did not have a time signature at tick 0, add the default.
    if ((timeSignatureList.isEmpty()) || (timeSignatureList.get(0).getTickPos() != 0)) {
      long tickPos = 0;
      int num = 4;
      int denom = 4;
      timeSignatureList.add(0, new TimeSignatureEx(num, denom, tickPQN, tickPos));
    }
    // calculate the value of "beatPosition" for all time signatures
    ListIterator<TimeSignature> iter = timeSignatureList.listIterator();
    TimeSignatureEx previous = (TimeSignatureEx) iter.next();
    previous.setBeatPosition(0, 0);
    while (iter.hasNext()) {
      TimeSignatureEx next = (TimeSignatureEx) iter.next();
      BeatPosition nextPos = previous.getBeatPosition(next.getTickPos());
      next.setBeatPosition(nextPos.getMeasure(), nextPos.getBeat());
      previous = next;
    }

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
   * Get the number of timeSignatureEvents contained in this Track.
   *
   * @return the number of timeSignatureEvents contained in this Track.
   */
  public int size() {
    return timeSignatureList.size();
  }

  public boolean isEmpty() {
    return timeSignatureList.isEmpty();
  }

  public boolean contains(Object o) {
    if (o instanceof TimeSignature) {
      return timeSignatureList.contains((TimeSignature) o);
    }
    return false;
  }

  public Iterator<TimeSignature> iterator() {
    return timeSignatureList.iterator();
  }

  public Object[] toArray() {
    return timeSignatureList.toArray();
  }

  public <T> T[] toArray(T[] a) {
    return timeSignatureList.toArray(a);
  }

  public boolean add(TimeSignature e) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean remove(Object o) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean containsAll(Collection<?> c) {
    return timeSignatureList.containsAll(c);
  }

  public boolean addAll(Collection<? extends TimeSignature> c) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean addAll(int index, Collection<? extends TimeSignature> c) {
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

  public TimeSignature get(int index) {
    return timeSignatureList.get(index);
  }

  public TimeSignature set(int index, TimeSignature element) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void add(int index, TimeSignature element) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public TimeSignature remove(int index) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public int indexOf(Object o) {
    return timeSignatureList.indexOf(o);
  }

  public int lastIndexOf(Object o) {
    return timeSignatureList.lastIndexOf(o);
  }

  public ListIterator<TimeSignature> listIterator() {
    return timeSignatureList.listIterator();
  }

  public ListIterator<TimeSignature> listIterator(int index) {
    return timeSignatureList.listIterator(index);
  }

  public List<TimeSignature> subList(int fromIndex, int toIndex) {
    return timeSignatureList.subList(fromIndex, toIndex);
  }

  /**
   * Find the timeSignature event that is valid for the given midiTick.
   *
   * @param tick
   * @return the index (in the timeSignatureList) for the searched event.
   */
  private int findPreviousTimeSignatureEvent(long tick) {
    for (int i = timeSignatureList.size() - 1; i >= 0; i--) {
      if (timeSignatureList.get(i).getTickPos() <= tick) {
        return i;
      }
    }
    //oops, this is weird, because in the constructor we made sure, there is
    // at least the default event at the start.
    throw new RuntimeException("internal error");
  }

  /**
   * Find the timeSignature event that precedes a given measure
   *
   * @param measure
   * @return the index (in the timeSignatureList) for the searched event.
   */
  private int findPreviousTimeSignatureEventMeasure(long measure) {
    if (measure < 0) {
      //OK, we accept negative values (they are considered to be before the beginning of the song
      return 0;
    }
    for (int i = timeSignatureList.size() - 1; i >= 0; i--) {
      TimeSignatureEx timesig = (TimeSignatureEx) timeSignatureList.get(i);
      long thisMeasure = timesig.getBeatPosition().getMeasure();
      if (thisMeasure <= measure) {
        return i;
      }
    }
    //oops, this is weird, because in the constructor we made sure, there is
    // at least the default event at the start.
    throw new RuntimeException("internal error");
  }

  public TimeSignature timesignatureForTick(long tick) {
    return get(findPreviousTimeSignatureEvent(tick));
  }

  /**
   * Calculate the position in measures and ticks for a given midi tick.
   *
   * @param tick the midi tick for which we want to know measure and beat.
   * @return measure and beat for the given tick.
   */
  public BeatPosition getBeatPosition(double tick) {
    TimeSignatureEx ts = (TimeSignatureEx) timesignatureForTick((long) tick);
    return ts.getBeatPosition(tick);
  }

  public RPositionEx getRPositionEx(double tick) {
    TimeSignatureEx ts = (TimeSignatureEx) timesignatureForTick((long) tick);
    return ts.getRPositionEx(tick);
  }

  /**
   *
   * @param beatPosition
   * @return
   * @deprecated use getTickPosition(RPosition position) instead.
   */
  @Deprecated
  public double getTickPosition(BeatPosition beatPosition) {
    int i = findPreviousTimeSignatureEventMeasure(beatPosition.getMeasure());
    TimeSignatureEx timesig = (TimeSignatureEx) timeSignatureList.get(i);
    long deltaMeasures = beatPosition.getMeasure() - timesig.getBeatPosition().getMeasure();
    double deltaBeats = beatPosition.getBeat() - timesig.getBeatPosition().getBeat();

    return timesig.getTickPos() + deltaMeasures * timesig.getBarLength() + deltaBeats * timesig.getBeatLength();
  }

  /**
   * Calculates a position expressed as Midi ticks for the same position
   * expressed as a rhythmic position.
   *
   * @param position a position given as a rhythmic position.
   * @return the position expressed in Midi ticks
   */
  public double getTickPosition(RPosition position) {
    int i = findPreviousTimeSignatureEventMeasure(position.getMeasure());
    TimeSignatureEx timesig = (TimeSignatureEx) timeSignatureList.get(i);
    long deltaMeasures = position.getMeasure() - timesig.getBeatPosition().getMeasure();
    double deltaBeats = position.getBeat() - timesig.getBeatPosition().getBeat();
    return timesig.getTickPos() + deltaMeasures * timesig.getBarLength() + deltaBeats * timesig.getBeatLength();

  }
}
