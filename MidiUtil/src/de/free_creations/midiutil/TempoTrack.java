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
 * A TempoTrack is a list of {@link TempoEvent}  objects.
 * This class provides methods to discover all tempo-messages in a given Midi sequence
 * and methods to construct a corresponding TermpoTrack-object.
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class TempoTrack implements List<TempoEvent> {

  private ArrayList<TempoEvent> tempoList;
  private long maxTicks = 0;

  
  /**
   * The time-map associates the midi-ticks of a sequence with clock-time.
   * During rendering the tempo-factor might change, and in consequence 
   * the mapping of midi-ticks will also change. We go around this problem and
   * calculate the mapping for a short period of time. Any tempo-factor changes
   * that happen within this period will be ignored and will be taken into account
   * in the next period.
   */
  public class TimeMap {

    private final double timeStretch;
    private final double startTick;
    /** a local set of tempo events the have effect on this time map */
    private ArrayList<TempoEvent> mappedTempoEvents = new ArrayList<TempoEvent>();
    /** the positions in time of above tempo events (in seconds)*/
    private ArrayList<Double> timePositions = new ArrayList<Double>();

    /**
     * Create a time-map that starts at the given startTick and covers 
     * the period given by "timeSize".
     * @param startTick the MIDI tick at which this time-mat shall start
     * @param timeSize the size of the time-map given in seconds.
     * @param tempoFactor the factor by which the by which the sequence shall
     * be accelerated.
     */
    private TimeMap(double startTick, double timeSize, double tempoFactor) {
      if (tempoFactor == 0.0D) {
        throw new IllegalArgumentException("Zero tempoFactor.");
      }
      if (startTick < 0.0F) {
        throw new IllegalArgumentException("startTick is less that zero.");
      }
      this.startTick = startTick;
      this.timeStretch = 1 / tempoFactor;
      //we start with the tempo event that just precedes the starting tick.
      int startIdx = indexForTick((long) startTick);
      TempoEvent start = tempoList.get(startIdx);
      int localIdx = 0;
      //we  calculate the position expressed in microseconds of the starting
      //tempo event. This offset is expressed relativ to the startTick, because
      // the starting tempo-event precedes the startTick this will give a negativ number.
      Double timeOffset = new Double((timeStretch * start.toSeconds(start.getTickPos() - startTick)));
      timePositions.add(timeOffset);
      mappedTempoEvents.add(start);

      //we continue to calculate micro-seconds offset for a number of
      //tempo-events until we have at least covered the given timeSize.
      for (int i = startIdx + 1; i < tempoList.size(); i++) {
        // note: "i" points into the tempo track's tempoList.
        // "localIdx" points into the mappedTempoEvents.
        TempoEvent end = tempoList.get(i);
        double tickDistance = end.getTickPos() - start.getTickPos();
        double timeDistance = (timeStretch * start.toSeconds(tickDistance));
        timeOffset = new Double(timePositions.get(localIdx) + timeDistance);
        timePositions.add(timeOffset);
        mappedTempoEvents.add(end);

        if (timeOffset > timeSize) {
          break;
        }
        //--- prepare next round
        localIdx++;
        start = end;
      }
    }

    /**
     * Calculate the time in seconds for a given midi tick
     * (laying within the region defined by the time-map).
     * @param tick a midi tick in the time map.
     * @return the time in seconds from startTick to the given tick
     */
    public double getTimeOffset(double tick) {
      // find the index of the tempo event preceding the tick
      int idx = -1;
      for (int i = mappedTempoEvents.size() - 1; i >= 0; i--) {
        if (mappedTempoEvents.get(i).getTickPos() <= tick) {
          idx = i;
          break;
        }
      }
      if (idx == -1) {
        //oops, should never happen
        throw new IllegalArgumentException("internal error: tick is not within this time-map");
      }
      TempoEvent tempoEvent = mappedTempoEvents.get(idx);
      if (tempoEvent.getTickPos() > startTick) {
        double tickDistance = tick - tempoEvent.getTickPos();
        double timeDistance = timeStretch * tempoEvent.toSeconds(tickDistance);
        double timeOffset = timePositions.get(idx) + timeDistance;
        return timeOffset;
      } else {
        //in this case above calculation should yield a correct result but we can do simpler:
        double tickDistance = tick - startTick;
        double timeOffset = timeStretch * tempoEvent.toSeconds(tickDistance);
        return timeOffset;
      }

    }

    /**
     * Calculate the tick position for a given time timeOffset.
     * @param timeOffset the offset in seconds
     * @return the midi tick
     */
    public double getTickForOffset(double timeOffset) {
      // find the index of the tempo event preceding the timeOffset time
      int idx = -1;
      for (int i = timePositions.size() - 1; i >= 0; i--) {
        if (timePositions.get(i) <= timeOffset) {
          idx = i;
          break;
        }
      }
      if (idx == -1) {
        //oops, the offset is before the first tempo event. We'll use
        //the the first tempo event, but the result will not be reliable..
        idx = 0;
      }

      TempoEvent tempoEvent = mappedTempoEvents.get(idx);
      if (tempoEvent.getTickPos() > startTick) {
        double timeDistance = timeOffset - timePositions.get(idx);
        double tickDist = tempoEvent.toMidiTicks(timeDistance / timeStretch);
        return tempoEvent.getTickPos() + tickDist;
      } else {
        double tickDist = tempoEvent.toMidiTicks(timeOffset / timeStretch);
        return startTick + tickDist;
      }
    }
  }

  /**
   * Create a tempoList of all the tempo events contained in the given sequence.
   * The TempoEvent are sorted by ascending order.
   * @param sequence the given sequence
   */
  public TempoTrack(Sequence sequence) {
    tempoList = new ArrayList<TempoEvent>();
    if (sequence == null) {
      throw new IllegalArgumentException("Sequence is null.");
    }
    if (sequence.getDivisionType() != Sequence.PPQ) {
      throw new IllegalArgumentException("Sequence timing must be in PPQ (pulses per quarter note).");
    }
    int tickPQN = sequence.getResolution();
    Track[] tracks = sequence.getTracks();

    // go through  all tracks and create appropriate Tempo Events.
    for (Track track : tracks) {
      for (int i = 0; i < track.size(); i++) {
        MidiEvent event = track.get(i);
        if (TempoEvent.isTempoEvent(event)) {
          tempoList.add(new TempoEvent(event, tickPQN));
        }
        maxTicks = Math.max(maxTicks, track.ticks());
      }
    }
    Collections.sort(tempoList);
    // if the sequence did not have a tempo event at tick 0, add the default event.
    if ((tempoList.isEmpty()) || (tempoList.get(0).getTickPos() != 0)) {
      long tickPos = 0;
      int defaultTempoPerQuarter = 500000;
      tempoList.add(0, new TempoEvent(defaultTempoPerQuarter, tickPQN, tickPos));
    }
  }

  public TimeMap CreateTimeMap(double startTick, double duration, double tempoFactor) {
    return new TimeMap(startTick, duration, tempoFactor);
  }
  /**
   * Obtains the length of the track, expressed in MIDI ticks.
   * This is the length of the longest track contained in the
   * sequence given on construction.
   * @return the duration, in ticks
   */
  public long ticks(){
    return maxTicks;

  }

  /**
   * Get the number of TempoEvents contained in this Track.
   * @return the number of TempoEvents contained in this Track.
   */
  public int size() {
    return tempoList.size();
  }

  public boolean isEmpty() {
    return tempoList.isEmpty();
  }

  public boolean contains(Object o) {
    if (o instanceof TempoEvent) {
      return tempoList.contains((TempoEvent) o);
    }
    return false;
  }

  public Iterator<TempoEvent> iterator() {
    return tempoList.iterator();
  }

  public Object[] toArray() {
    return tempoList.toArray();
  }

  public <T> T[] toArray(T[] a) {
    return tempoList.toArray(a);
  }

  public boolean add(TempoEvent e) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean remove(Object o) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean containsAll(Collection<?> c) {
    return tempoList.containsAll(c);
  }

  public boolean addAll(Collection<? extends TempoEvent> c) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public boolean addAll(int index, Collection<? extends TempoEvent> c) {
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

  public TempoEvent get(int index) {
    return tempoList.get(index);
  }

  public TempoEvent set(int index, TempoEvent element) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public void add(int index, TempoEvent element) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public TempoEvent remove(int index) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  public int indexOf(Object o) {
    return tempoList.indexOf(o);
  }

  public int lastIndexOf(Object o) {
    return tempoList.lastIndexOf(o);
  }

  public ListIterator<TempoEvent> listIterator() {
    return tempoList.listIterator();
  }

  public ListIterator<TempoEvent> listIterator(int index) {
    return tempoList.listIterator(index);
  }

  public List<TempoEvent> subList(int fromIndex, int toIndex) {
    return tempoList.subList(fromIndex, toIndex);
  }

  
  /**
   * Get the index of the tempo-event that precedes the given
   * Midi tick.
   * @param tick the search position given in Midi ticks
   * @return the index of an event that is valid for the given midi tick.
   */
  public int indexForTick(long tick){
    if(tick<0){
      tick=0;
    }
    return eventForTickBinarySearch(tick, 0 , tempoList.size()-1);
  }

  
  private int eventForTickBinarySearch(long searchTick, int lowIndex, int highIndex){
    if(isEventPrecedingTick(searchTick, lowIndex)){
      return lowIndex;
    }
    int midIndex = (lowIndex + highIndex+1)/2; //plus one -> round-Ceiling
    long midTick = tempoList.get(midIndex).getTickPos();
    if(midTick > searchTick){
      return eventForTickBinarySearch(searchTick, lowIndex, midIndex);
    }else{
      return eventForTickBinarySearch(searchTick, midIndex, highIndex);
    }
    
  }

  private boolean isEventPrecedingTick(long tick, int index){
    long eventTick = tempoList.get(index).getTickPos();
    if(eventTick>tick){
      return false;
    }
    int nextEvent = index+1;
    if(nextEvent >= tempoList.size()){
      return true;
    }
    long nextEventTick = tempoList.get(nextEvent).getTickPos();
    if (nextEventTick>tick){
      return true;
    }
    return false;
    
  }
}
