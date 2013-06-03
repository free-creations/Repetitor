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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

/**
 * A Note-object represents a sound in a song. Instances of this class represent
 * specific MIDI events in a given MIDI sequence. These events are a note-on
 * event together with its corresponding note-off event. The existence of a MIDI
 * sequence is implied but not represented by this class.
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class Note implements Comparable<Note> {

  int pitch;
  int channel;
  int velocity;
  private long duration;
  private long tickPos;

  /**
   * Constructs a new Note object for a given channel, pitch, velocity, starting
   * position and duration.
   *
   * @param channel an integer in the range from zero to fifteen.
   * @param pitch an integer in the range from 0 to 127
   * @param velocity an integer in the range from 0 to 127
   * @param tickPos the position in the sequence (expressed in midi ticks)
   * @param duration the time between note-on and note off (expressed in midi
   * ticks)
   */
  public Note(int channel, int pitch, int velocity, long tickPos, long duration) {
    this.channel = channel;
    this.velocity = velocity;
    this.pitch = pitch;
    this.tickPos = tickPos;
    this.duration = duration;
  }

  /**
   * Construct a new Note object from a pair of note-on and note-off MIDI
   * events.
   *
   * @param noteOn the start of the sound
   * @param noteOff the end of the sound
   */
  public Note(MidiEvent noteOn, MidiEvent noteOff) {
    ShortMessage noteOnMessage = (ShortMessage) noteOn.getMessage();
    ShortMessage noteOffMessage = (ShortMessage) noteOff.getMessage();
    pitch = noteOnMessage.getData1();
    if (noteOffMessage.getData1() != pitch) {
      throw new IllegalArgumentException("Note On and Note Off must have same pitch.");
    }
    if (noteOnMessage.getChannel() != noteOffMessage.getChannel()) {
      throw new IllegalArgumentException("Note On and Note Off must be on the same channel.");
    }
    tickPos = noteOn.getTick();
    duration = noteOff.getTick() - tickPos;
    channel = noteOnMessage.getChannel();
    velocity = noteOnMessage.getData2();
    if (duration < 0) {
      throw new IllegalArgumentException("Note-Off must come after note-On");
    }

  }

  public MidiEvent getNoteOnEvent() {
    try {
      ShortMessage shortMessage = new ShortMessage(
              ShortMessage.NOTE_ON, channel, pitch, velocity);
      return new MidiEvent(shortMessage, tickPos);
    } catch (InvalidMidiDataException ex) {
      throw new RuntimeException(ex);
    }
  }

  public MidiEvent getNoteOffEvent() {
    try {
      ShortMessage shortMessage = new ShortMessage(
              ShortMessage.NOTE_OFF, channel, pitch, 0);
      return new MidiEvent(shortMessage, tickPos + duration);
    } catch (InvalidMidiDataException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Time that the note is sounding.
   *
   * @return the duration in midi ticks.
   */
  public long getDuration() {
    return duration;
  }

  public void setDuration(long value) {
    duration = value;
  }

  public void setVelocity(int value) {
    velocity = value;
  }

  /**
   * The channel in the track in the sequence.
   *
   * @return a number between 0 and 15.
   */
  public int getChannel() {
    return channel;
  }

  /**
   * The velocity of the note.
   *
   * @return a number between 0 and 127.
   */
  public int getVelocity() {
    return velocity;
  }

  /**
   * The position in the implied sequence in MIDI ticks.
   *
   * @return The position in the implied sequence in MIDI ticks.
   */
  public long getTickPos() {
    return tickPos;
  }

  /**
   * The pitch in midi-note numbers.
   *
   * @return a number between 0 and 127.
   */
  public int getPitch() {
    return pitch;
  }

  /**
   * Utility function helping to discover Note-on events in a sequence.
   *
   * @param event any kind of MIDI event or null.
   * @return true, if the given event is a <em>real note-on</em> event (note-on
   * events with velocity=0 are not considered to be note-off events).
   */
  public static boolean isNoteOnEvent(MidiEvent event) {
    if (null == event) {
      return false;
    }
    return isNoteOnMessage(event.getMessage());
  }

  /**
   * Utility function helping to discover Note-on messages in a MIDI-sequence.
   *
   * @param message any kind of MIDI message or null.
   * @return true, if the given event if a <em>real note-on event</em>
   * (midi-note-on events with velocity=0 are considered to be note-off events).
   */
  public static boolean isNoteOnMessage(MidiMessage message) {
    if (null == message) {
      return false;
    }
    if (!(message instanceof ShortMessage)) {
      return false;
    }
    ShortMessage shortMessage = (ShortMessage) message;
    if (shortMessage.getCommand() == ShortMessage.NOTE_ON) {
      return (shortMessage.getData2() != 0);
    }
    return false;
  }

  /**
   * Utility function helping to discover Note-off events in a MIDI-sequence.
   *
   * @param event any kind of MIDI event or null.
   * @return true, if the given event if a <em>real note-off event</em>
   * (midi-note-on events with velocity=0 are also considered to be real
   * note-off events).
   */
  public static boolean isNoteOffEvent(MidiEvent event) {
    if (null == event) {
      return false;
    }
    return isNoteOffMessage(event.getMessage());
  }

  /**
   * Utility function helping to discover Note-off messages in a MIDI-sequence.
   *
   * @param message any kind of MIDI message or null.
   * @return true, if the given event if a <em>real note-off event</em>
   * (midi-note-on events with velocity=0 are also considered to be real
   * note-off events).
   */
  public static boolean isNoteOffMessage(MidiMessage message) {
    if (null == message) {
      return false;
    }
    if (!(message instanceof ShortMessage)) {
      return false;
    }
    ShortMessage shortMessage = (ShortMessage) message;
    if (shortMessage.getCommand() == ShortMessage.NOTE_OFF) {
      return true;
    }
    if (shortMessage.getCommand() == ShortMessage.NOTE_ON) {
      return (shortMessage.getData2() == 0);
    }
    return false;

  }

  /**
   * Utility function helping to discover the note-off event for a given note-on
   * event.
   *
   * @param noteOnEvent the given note-on event.
   * @param otherEvent any other midi event
   * @return true if the given events form a pair of matching note-on and
   * note-off events.
   */
  public static boolean isNoteOffEventFor(MidiEvent noteOnEvent, MidiEvent otherEvent) {
    // the first Event must be a "note-on".
    if (!isNoteOnEvent(noteOnEvent)) {
      return false;
    }
    // the second Event must be a "note-off".
    if (!isNoteOffEvent(otherEvent)) {
      return false;
    }
    // the second Event must come after the first.
    if (noteOnEvent.getTick() > otherEvent.getTick()) {
      return false;
    }

    ShortMessage noteOnMessage = (ShortMessage) noteOnEvent.getMessage();
    ShortMessage otherMessage = (ShortMessage) otherEvent.getMessage();
    // both must be on the same channel.
    if (noteOnMessage.getChannel() != otherMessage.getChannel()) {
      return false;
    }
    // both must be for the same pitch.
    if (noteOnMessage.getData1() != otherMessage.getData1()) {
      return false;
    }
    // OK, all conditions are met, we can return true.
    return true;
  }

  /**
   * Compare this note to an other note regarding its position in an implied
   * MIDI sequence. If both notes are simultaneous the comparison is expanded to
   * the channel and the pitch.
   *
   * @param otherNote the note to be inspected
   * @return 1 if the other note is earlier than this note.
   */
  public int compareTo(Note otherNote) {
    if (this.tickPos < otherNote.getTickPos()) {
      return -1;
    }
    if (this.tickPos > otherNote.getTickPos()) {
      return 1;
    }

    if (this.channel < otherNote.getChannel()) {
      return -1;
    }
    if (this.channel > otherNote.getChannel()) {
      return 1;
    }

    if (this.pitch < otherNote.getPitch()) {
      return -1;
    }
    if (this.pitch > otherNote.getPitch()) {
      return 1;
    }

    return 0;
  }
}
