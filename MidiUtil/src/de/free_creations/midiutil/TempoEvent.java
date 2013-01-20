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

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;

/**
 * A TempoEvent indicates a change of speed in a song. Instances of this class
 * represent specific MIDI meta-events in a MIDI track. <br/> This class also
 * provides some utility functions to discover tempo-events in a sequence of
 * MIDI events. <br/>
 *
 * @author Harald Postner
 */
public class TempoEvent implements Comparable<TempoEvent> {

  private final long tickPos;
  private final int tempoPerQuarter;
  private final int tickPQN;

  /**
   * Construct a new Tempo event object for a given position a with the speed
   * given by the tempoPerQuarter argument. The existence of the given
   * MIDI-sequence is implied but not represented by this class.
   *
   * @param tempoPerQuarter the time in microseconds per quarter note.
   * @param tickPQN the number of ticks per quarter note in the implied
   * sequence.
   * @param tickPos the position in the implied sequence.
   */
  public TempoEvent(int tempoPerQuarter, int tickPQN, long tickPos) {
    this.tempoPerQuarter = tempoPerQuarter;
    this.tickPQN = tickPQN;
    this.tickPos = tickPos;
  }

  /**
   * Construct a new Tempo Event object by extracting the tempoPerQuarter and
   * the position from a given Midi message.
   *
   * @param TempoEvent a MIDI meta message that must be of type "tempo"
   * @param tickPQN the number of ticks per quarter note in the implied
   * sequence.
   */
  public TempoEvent(MidiEvent TempoEvent, int tickPQN) {
    if (!isTempoEvent(TempoEvent)) {
      throw new IllegalArgumentException(TempoEvent + " is not  a \"tempo\" event.");
    }
    MetaMessage tempoMessage = (MetaMessage) TempoEvent.getMessage();
    this.tickPos = TempoEvent.getTick();
    this.tickPQN = tickPQN;
    byte[] tempoData = tempoMessage.getData();
    tempoPerQuarter = ((tempoData[0] & 0xff) << 16)
            | ((tempoData[1] & 0xff) << 8) | (tempoData[2] & 0xff);
  }

  /**
   * @return the position in MIDI ticks.
   */
  public long getTickPos() {
    return tickPos;
  }

  /**
   * A function useful to discover tempo events in a sequence of MIDI events.
   *
   * @param event any kind of MIDI event
   * @return true if the event is a tempo event
   */
  public static boolean isTempoEvent(MidiEvent event) {
    if (null == event) {
      return false;
    }
    return isTempoMessage(event.getMessage());
  }

  /**
   * Calculates the time in seconds for a given distance in midi ticks assuming
   * that this tempo event is applied.
   *
   * @param midiTicks the distance in midi ticks (need not to be an integer)
   * @return the time in seconds.
   */
  public double toSeconds(double midiTicks) {
    return 1E-6D * (tempoPerQuarter * midiTicks) / (double) tickPQN;
  }

  /**
   * The tempo in microseconds per quarter note. In other words, the microsecond
   * tempo value tells you how long each one of the "quarter notes" should be.
   *
   * @return The tempo in microseconds per quarter note.
   */
  public int getTempoPerQuarter() {
    return tempoPerQuarter;
  }

  /**
   * A function useful to discover tempo events in a sequence of MIDI messages.
   *
   * @param message any kind of MIDI message (or null)
   * @return true if the event is a tempo event
   */
  public static boolean isTempoMessage(MidiMessage message) {
    if (null == message) {
      return false;
    }
    if (!(message instanceof MetaMessage)) {
      return false;
    }
    MetaMessage metaMessage = (MetaMessage) message;
    int messageType = metaMessage.getType();
    return (messageType == MidiUtil.tempoMeta);
  }

  /**
   * Compare two Tempo Objects on their position. (Note, we assume that both
   * belong to the same sequence and have the same tickPQN);
   *
   * @param o an other Tempo event.
   * @return 1 if the other tempo is earlier than this one.
   */
  public int compareTo(TempoEvent o) {
    if (this.tickPos < o.getTickPos()) {
      return -1;
    }
    if (this.tickPos > o.getTickPos()) {
      return 1;
    }
    //--- both are at the same time, compare on speed
    if (this.tempoPerQuarter < o.tempoPerQuarter) {
      return -1;
    }
    if (this.tempoPerQuarter > o.tempoPerQuarter) {
      return 1;
    }
    return 0;

  }

  /**
   * Calculates the distance in midi ticks for a given time in seconds assuming
   * that this tempo event is applied.
   *
   * @param offset an elapse in seconds.
   * @return the distance in Midi ticks
   */
  public double toMidiTicks(double offset) {
    return 1E6 * ((double) tickPQN * offset) / (double) tempoPerQuarter;
  }

  /**
   * Utility function that permits to create a midi message for a given tempo.
   *
   * @param bpm the tempo expressed in quarter-beats per minute.
   * @return a midi message
   */
  public static MetaMessage newMidiMessage(double bpm) throws InvalidMidiDataException {
    if (bpm < 0) {
      throw new InvalidMidiDataException("Invalid beats per minute value " + bpm + ".");
    }
    MetaMessage message = new MetaMessage();

    int period = (int) Math.floor(60000000D / bpm); // the duration of one beat expressed in micro-seconds
    //Note: rounding would be more appropriate, but we do "floor" in 
    //order to be compatible with other Midi tools which are usually doing interger arithmetic.
    byte[] tempoData = new byte[3];
    tempoData[0] = (byte) ((period >> 16) & 0xff);
    tempoData[1] = (byte) ((period >> 8) & 0xff);
    tempoData[2] = (byte) (period & 0xff);


    message.setMessage(MidiUtil.tempoMeta, //
            tempoData, //
            3); //data2)
    return message;

  }

  /**
   * Utility function that permits to create a midi event for a given tempo.
   *
   * @param bpm the tempo expressed in quarter-beats per minute.
   * @param tick the time-stamp for the event, in MIDI ticks
   * @return a midi message
   */
  public static MidiEvent newMidiEvent(double bpm, long tick) throws InvalidMidiDataException {

    return new MidiEvent(newMidiMessage(bpm), tick);

  }
}
