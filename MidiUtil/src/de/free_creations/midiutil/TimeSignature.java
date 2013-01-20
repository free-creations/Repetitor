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

import javax.sound.midi.*;

/**
 * A TimeSignature-object indicates how many beats are in each measure.
 * Instances of this class represent specific MIDI events in a given
 * MIDI-sequence. These events are the <em>time-signature meta-events</em>. The
 * existence of the given MIDI-sequence is implied but not represented by this
 * class.
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class TimeSignature implements Comparable<TimeSignature> {

  private int numerator;
  private int denominator;
  private int ticksPerQuarterNote;
  private long tickPos;

  /**
   * Construct a new TimeSignature object.
   *
   * @param num the numerator.
   * @param denom the denominator.
   * @param tickPQN the number of ticks per quarter note.
   * @param tickPos the position in the implied sequence.
   */
  public TimeSignature(int num, int denom, int tickPQN, long tickPos) {
    this.numerator = num;
    this.denominator = denom;
    this.ticksPerQuarterNote = tickPQN;
    this.tickPos = tickPos;

  }

  /**
   * Construct a new TimeSignature object representing a given midi event.
   *
   * @param signatureChangeEvent the given midi event.
   * @param tickPQN the number of ticks per quarter note in the implied
   * sequence.
   */
  public TimeSignature(MidiEvent signatureChangeEvent, int tickPQN) {
    // get the message from the event
    MetaMessage signatureChangeMessage = (MetaMessage) signatureChangeEvent.getMessage();
    // check if it is really the right kind of message
    if (signatureChangeMessage.getType() != 88) {
      throw new RuntimeException(signatureChangeMessage + "is not a time-signature change.");
    }

    // extract the data bytes from the message
    byte[] data = signatureChangeMessage.getData();

    // initialise a new TimeSignature object
    numerator = data[0];
    denominator = powerOfTwo(data[1]);
    ticksPerQuarterNote = tickPQN;
    tickPos = signatureChangeEvent.getTick();
  }

  /**
   * Utility function helping to discover time-signature events in a sequence.
   *
   * @param event any kind of MIDI event or null.
   * @return true, if the given event is a time-signature event.
   */
  public static boolean isTimeSignatureEvent(MidiEvent event) {
    if (event == null) {
      return false;
    }
    return isTimeSignatureMessage(event.getMessage());
  }

  /**
   * Utility function helping to discover time-signature messages in a sequence.
   *
   * @param message any kind of MIDI message or null.
   * @return true, if the given event is a time-signature event.
   */
  public static boolean isTimeSignatureMessage(MidiMessage message) {
    if (message == null) {
      return false;
    }
    // if not a meta message then return
    if (message.getStatus() != ShortMessage.SYSTEM_RESET) {
      return false;
    }

    // cast to meta-message
    MetaMessage metaMessage = (MetaMessage) message;

    // "change Time Signature" Message have the type 88

    if (metaMessage.getType() == 88) {
      return true;
    } else {
      return false;
    }
  }

  private static int powerOfTwo(int i) {
    if (i < 0) {
      throw new ArithmeticException("Cannot raise 2 into the power of " + i + ".");
    }
    if (i > 16) {
      throw new ArithmeticException("Cannot raise 2 into the power of " + i + ".");
    }

    if (i == 0) {
      return 1;
    }

    return 2 << (i - 1);
  }

  /**
   * The numerator tells how many beats are in a measure.
   *
   * @return the number beats in a measure(a number between 1 and usually less
   * than 12).
   */
  public int getNumerator() {
    return numerator;
  }

  /**
   * The denominator tells which note value constitutes one beat.
   *
   * @return the note value that constitutes one beat.
   */
  public int getDenominator() {
    return denominator;
  }

  /**
   * The length of one measure in midi ticks.
   *
   * @return The length of one measure in midi ticks.
   */
  public long getBarLength() {
    return numerator * getBeatLength();
  }

  /**
   * The position of this time-signature in the implied sequence.
   *
   * @return time position in midi ticks.
   */
  public long getTickPos() {
    return tickPos;
  }

  /**
   * The length of one beat in midi ticks.
   *
   * @return The length of one beat in midi ticks.
   */
  public long getBeatLength() {
    return (ticksPerQuarterNote * 4) / denominator;
  }

  /**
   * Compare two timeSignature Objects on their position. (Note, we assume that
   * both belong to the same sequence and have the same tickPQN);
   *
   * @param o an other timeSignature event.
   * @return 1 if the other timeSignature is earlier than this one.
   */
  public int compareTo(TimeSignature o) {
    if (this.tickPos < o.getTickPos()) {
      return -1;
    }
    if (this.tickPos > o.getTickPos()) {
      return 1;
    }
    //--- both are at the same time, compare on speed

    return 0;

  }

  /**
   *
   * @param numerator the numerator gives the number of beats in one measure
   * @param denominator the denominator indicates the length of one beat
   * @param cc expresses the number of MIDI clocks in a metronome click.
   * @param bb expresses the number of notated 32nd notes in a MIDI quarter note
   * @return a MetaMessage with the given parameters.
   * @throws InvalidMidiDataException
   */
  public static MetaMessage newMidiMessage(int numerator, int denominator, int cc, int bb) throws InvalidMidiDataException {

    // x is the integer binary -logarithm of the denominator
    byte x = (byte) ((Math.log(denominator) / Math.log(2)));
    if (powerOfTwo(x) != denominator) {
      throw new InvalidMidiDataException("Invalid  denominator for time signature " + denominator);
    }

    MetaMessage message = new MetaMessage();
    byte[] timeSignatureData = new byte[4];
    timeSignatureData[0] = (byte) numerator;
    timeSignatureData[1] = x;
    timeSignatureData[2] = (byte)cc;
    timeSignatureData[3] = (byte)bb;


    message.setMessage(MidiUtil.timeSignatureMeta, //
            timeSignatureData, //
            timeSignatureData.length); //data2)
    return message;
  }

  public static MetaMessage newMidiMessage(int numerator, int denominator, int cc) throws InvalidMidiDataException {
    return newMidiMessage(numerator, denominator, cc, 8);
  }

  public static MetaMessage newMidiMessage(int numerator, int denominator) throws InvalidMidiDataException {
    int cc = (int) Math.round((4D * 24D) / (double) denominator);
    return newMidiMessage(numerator, denominator, cc, 8);
  }

  /**
   * Utility function that permits to create a midi event for a given tempo.
   *
   * @param bpm the tempo expressed in quarter-beats per minute.
   * @param tick the time-stamp for the event, in MIDI ticks
   * @return a midi message
   */
  public static MidiEvent newMidiEvent(int numerator, int denominator, int cc, int bb, long tick) throws InvalidMidiDataException {
    return new MidiEvent(newMidiMessage(numerator, denominator, cc, bb), tick);
  }

  public static MidiEvent newMidiEvent(int numerator, int denominator, int cc, long tick) throws InvalidMidiDataException {
    return new MidiEvent(newMidiMessage(numerator, denominator, cc), tick);
  }

  public static MidiEvent newMidiEvent(int numerator, int denominator, long tick) throws InvalidMidiDataException {
    return new MidiEvent(newMidiMessage(numerator, denominator), tick);
  }
}