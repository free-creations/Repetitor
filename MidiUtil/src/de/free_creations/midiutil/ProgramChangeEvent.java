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
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

/**
 * A ProgramChangeEvent causes the MIDI device to change to a particular Program
 * (which some devices refer to as Patch, or Instrument, or Preset, or
 * whatever). <br/> This class also provides some utility functions to discover
 * ProgramChangeEvent in a sequence of MIDI events, to c new midi events and
 * more . <br/>
 *
 * @author Harald Postner
 */
public class ProgramChangeEvent implements Comparable<ProgramChangeEvent> {

  private final long tickPos;
  private final int channel;
  /**
   * See General MIDI Standard.
   */
  private final int programNumber;

  /**
   * Construct a new ProgramChange event object for a given position a with the
   * speed given by the ProgramChangePerQuarter argument. The existence of the
   * given MIDI-sequence is implied but not represented by this class.
   *
   * @param programNumber the time in microseconds per quarter note.
   * @param tickPos the position in the implied sequence.
   */
  public ProgramChangeEvent(int programNumber, int channel, long tickPos) {
    this.programNumber = programNumber;
    this.channel = channel;
    this.tickPos = tickPos;
  }

  /**
   * Construct a new ProgramChange Event object by extracting the programNumber
   *
   * @param ProgramChangeEvent a MIDI meta message that must be of type
   * "ProgramChange"
   * @param tickPQN the number of ticks per quarter note in the implied
   * sequence.
   */
  public ProgramChangeEvent(MidiEvent pcEvent) {
    if (!isProgramChangeEvent(pcEvent)) {
      throw new IllegalArgumentException(pcEvent + " is not  a \"Program Change\" event.");
    }
    ShortMessage pcMessage = (ShortMessage) pcEvent.getMessage();
    this.tickPos = pcEvent.getTick();

    this.programNumber = pcMessage.getData1();
    this.channel = pcMessage.getChannel();
  }

  /**
   * @return the position in MIDI ticks.
   */
  public long getTickPos() {
    return tickPos;
  }

  /**
   * @return the channel.
   */
  public long getChannel() {
    return channel;
  }

  /**
   * A function useful to discover ProgramChange events in a sequence of MIDI
   * events.
   *
   * @param event any kind of MIDI event
   * @return true if the event is a ProgramChange event
   */
  public static boolean isProgramChangeEvent(MidiEvent event) {
    if (null == event) {
      return false;
    }
    return isProgramChangeMessage(event.getMessage());
  }

  /**
   * A function useful to discover ProgramChange events in a sequence of MIDI
   * messages.
   *
   * @param message any kind of MIDI message (or null)
   * @return true if the event is a ProgramChange event
   */
  public static boolean isProgramChangeMessage(MidiMessage message) {
    if (null == message) {
      return false;
    }
    if (!(message instanceof ShortMessage)) {
      return false;
    }
    ShortMessage shortMessage = (ShortMessage) message;
    int command = shortMessage.getCommand();
    return (command == ShortMessage.PROGRAM_CHANGE);
  }

  /**
   * Compare two ProgramChange Objects on their position. (Note, we assume that
   * both belong to the same sequence or at least to sequences with the same 
   * resolution).
   *
   * @param o an other ProgramChange event.
   * @return 1 if the other ProgramChange is earlier than this one.
   */
  public int compareTo(ProgramChangeEvent o) {
    if (this.tickPos < o.getTickPos()) {
      return -1;
    }
    if (this.tickPos > o.getTickPos()) {
      return 1;
    }
    if (this.channel < o.channel) {
      return -1;
    }
    if (this.channel > o.channel) {
      return 1;
    }

    //--- both are at the same time, compare on programNumber
    if (this.programNumber < o.programNumber) {
      return -1;
    }
    if (this.programNumber > o.programNumber) {
      return 1;
    }
    return 0;

  }

  /**
   * Constructs a new ShortMessage which represents a change of the sounding
   * program.
   *
   * @param programNumber the ProgramNumber as defined by the General Midi
   * specification.
   * @param channel the channel associated with the message
   * @return a midi message
   * @throws InvalidMidiDataException if the command value, channel value or all
   * data bytes belonging to the message do not specify a valid MIDI message
   */
  public static ShortMessage newMidiMessage(int programNumber, int channel) throws InvalidMidiDataException {
    if (programNumber < 0) {
      throw new InvalidMidiDataException("Invalid program number " + programNumber + ".");
    }
    if (programNumber > 127) {
      throw new InvalidMidiDataException("Invalid program number " + programNumber + ".");
    }
    if (channel < 0) {
      throw new InvalidMidiDataException("Invalid channel " + channel + ".");
    }
    if (channel > 15) {
      throw new InvalidMidiDataException("Invalid channel " + channel + ".");
    }
    ShortMessage message = new ShortMessage(
            ShortMessage.PROGRAM_CHANGE,
            channel,
            programNumber,
            0);
    return message;

  }

  /**
   * Constructs a new midi event which represents a change of the sounding
   * program.
   *
   * @param programNumber the ProgramNumber as defined by the General Midi
   * specification.
   * @param channel the channel associated with the message
   * @param tick the time-stamp for the event, in MIDI ticks
   * @return a midi message
   * @throws InvalidMidiDataException if the command value, channel value or all
   * data bytes belonging to the message do not specify a valid MIDI message
   */
  public static MidiEvent newMidiEvent(int programNumber, int channel, long tick) throws InvalidMidiDataException {

    return new MidiEvent(newMidiMessage(programNumber, channel), tick);

  }
}
