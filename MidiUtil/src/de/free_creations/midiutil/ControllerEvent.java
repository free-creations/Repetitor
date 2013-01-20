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
 * A MIDI controller is an abstraction of the hardware used to control a
 * performance, but which is not directly related to note-on/note-off events.
 * <br/> This class also provides some utility functions to discover
 * ControllerEvent in a sequence of MIDI events, to c new midi events and more .
 * <br/>
 *
 * @author Harald Postner
 */
public class ControllerEvent implements Comparable<ControllerEvent> {

  private final long tickPos;
  private final int channel;
  /**
   * See General MIDI Standard.
   */
  private final int controllerNumber;
  private final int controllerValue;

  /**
   * Construct a new Controller event object for a given position a with the
   * speed given by the ControllerPerQuarter argument. The existence of the
   * given MIDI-sequence is implied but not represented by this class.
   *
   * @param controllerNumber the time in microseconds per quarter note.
   * @param tickPos the position in the implied sequence.
   */
  public ControllerEvent(int programNumber, int programValue, int channel, long tickPos) {
    this.controllerNumber = programNumber;
    this.controllerValue = programValue;
    this.channel = channel;
    this.tickPos = tickPos;
  }

  /**
   * Construct a new Controller Event object by extracting the controllerNumber
   *
   * @param ControllerEvent a MIDI meta message that must be of type
   * "Controller"
   */
  public ControllerEvent(MidiEvent controllerEvent) {
    if (!isControllerEvent(controllerEvent)) {
      throw new IllegalArgumentException(controllerEvent + " is not  a \"Controller\" event.");
    }
    ShortMessage pcMessage = (ShortMessage) controllerEvent.getMessage();
    this.tickPos = controllerEvent.getTick();

    this.controllerNumber = pcMessage.getData1();
    this.controllerValue = pcMessage.getData2();
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
   * @return the controller number.
   */
  public long getControllerNumber() {
    return controllerNumber;
  }

  /**
   * @return the controller number.
   */
  public long getControllerValue() {
    return controllerValue;
  }

  /**
   * A function useful to discover Controller events in a sequence of MIDI
   * events.
   *
   * @param event any kind of MIDI event
   * @return true if the event is a Controller event
   */
  public static boolean isControllerEvent(MidiEvent event) {
    if (null == event) {
      return false;
    }
    return isControllerMessage(event.getMessage());
  }

  /**
   * A function useful to discover Controller events in a sequence of MIDI
   * messages.
   *
   * @param message any kind of MIDI message (or null)
   * @return true if the event is a Controller event
   */
  public static boolean isControllerMessage(MidiMessage message) {
    if (null == message) {
      return false;
    }
    if (!(message instanceof ShortMessage)) {
      return false;
    }
    ShortMessage shortMessage = (ShortMessage) message;
    int command = shortMessage.getCommand();
    return (command == ShortMessage.CONTROL_CHANGE);
  }

  /**
   * Compare two Controller Objects on their position. (Note, we assume that
   * both belong to the same sequence or at least to sequences with the same
   * resolution).
   *
   * @param o an other Controller event.
   * @return 1 if the other Controller is earlier than this one.
   */
  public int compareTo(ControllerEvent o) {
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

    //--- both are at the same time, compare on controllerNumber
    if (this.controllerNumber < o.controllerNumber) {
      return -1;
    }
    if (this.controllerNumber > o.controllerNumber) {
      return 1;
    }
    //--- both are at the same type, compare on controllerValue
    if (this.controllerValue < o.controllerValue) {
      return -1;
    }
    if (this.controllerValue > o.controllerValue) {
      return 1;
    }
    return 0;

  }

  /**
   * Constructs a new ShortMessage which represents a change of the sounding
   * program.
   *
   * @param controllerNumber the ProgramNumber as defined by the General Midi
   * specification.
   * @param controllerValue a number from 0 to 127 representing the value of the
   * controller
   * @param channel the channel associated with the message
   * @return a midi message
   * @throws InvalidMidiDataException if the command value, channel value or all
   * data bytes belonging to the message do not specify a valid MIDI message
   */
  public static ShortMessage newMidiMessage(int controllerNumber, int controllerValue, int channel) throws InvalidMidiDataException {
    if (controllerNumber < 0) {
      throw new InvalidMidiDataException("Invalid controller number " + controllerNumber + ".");
    }
    if (controllerNumber > 127) {
      throw new InvalidMidiDataException("Invalid controller number " + controllerNumber + ".");
    }
    if (controllerValue < 0) {
      throw new InvalidMidiDataException("Invalid controller value " + controllerNumber + ".");
    }
    if (controllerValue > 127) {
      throw new InvalidMidiDataException("Invalid controller value " + controllerNumber + ".");
    }
    if (channel < 0) {
      throw new InvalidMidiDataException("Invalid channel " + channel + ".");
    }
    if (channel > 15) {
      throw new InvalidMidiDataException("Invalid channel " + channel + ".");
    }
    ShortMessage message = new ShortMessage(
            ShortMessage.CONTROL_CHANGE,
            channel,
            controllerNumber,
            controllerValue);
    return message;

  }

  /**
   * Constructs a new midi event which represents a change of the sounding
   * program.
   *
   * @param controllerNumber the ProgramNumber as defined by the General Midi
   * specification.
   * @param controllerValue a number from 0 to 127 representing the value of the
   * controller.
   * @param channel the channel associated with the message
   * @param tick the time-stamp for the event, in MIDI ticks
   * @return a midi message
   * @throws InvalidMidiDataException if the command value, channel value or all
   * data bytes belonging to the message do not specify a valid MIDI message
   */
  public static MidiEvent newMidiEvent(int programNumber,  int controllerValue, int channel, long tick) throws InvalidMidiDataException {

    return new MidiEvent(newMidiMessage(programNumber, controllerValue, channel), tick);

  }
}
