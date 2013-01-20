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

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;

/**
 * A LoudnessEvent indicates a change of the sound volume in a channel. The Midi
 * specification defines two kinds of Midi events for this purpose. <ol> <li>
 * the <em>Volume Event</em> which should be used to set the overall volume of
 * the Channel prior to music data playback as well as for mix-down fader-style
 * movements.</li> <li> the <em>Expression Event</em> should be used during
 * music data playback to attenuate the programmed MIDI volume, thus creating
 * diminuendos and crescendo.</li> </ol> This enables a listener, after the
 * fact, to adjust the relative mix of instruments (using MIDI volume) without
 * destroying the dynamic expression of that instrument. <br/> Many composers
 * seems not to be aware of these recommendations, so most files found in the
 * Internet only use Volume Event. <br/> This class provides utility functions
 * to discover LoudnessEvents in a sequence of MIDI events. And to rearrange the
 * events so that they obey to the recommendations of the Midi Specification.
 * The rearranging is done in a way so that the sound balance of the whole track
 * does not change. <br/>
 *
 * @author Harald Postner
 */
public class LoudnessEvent implements Comparable<LoudnessEvent> {

  private boolean obsolete;

  private enum LoudnessType {

    VOLUME, EXPRESSION, KEYON
  };
  private final LoudnessType type;
  private final int value;
  private final long tickPos;
  private int loudnessLevel = 0;

  /**
   * The loudness level is defined as "currentExpressionValue *
   * currentVolumeValue"
   *
   * @param level
   */
  void setLoudnessLevel(int level) {
    this.loudnessLevel = level;
  }

  public int getLoudnessLevel() {
    return loudnessLevel;
  }

  public boolean isExpression() {
    return type == LoudnessType.EXPRESSION;
  }

  public boolean isVolume() {
    return type == LoudnessType.VOLUME;
  }

  public boolean isKeyOn() {
    return type == LoudnessType.KEYON;
  }

  public long getTickPos() {
    return tickPos;
  }

  public int getValue() {
    return value;
  }

  public LoudnessEvent(long tickPos, int value, boolean expression) {
    this.tickPos = tickPos;
    this.value = value;

    if (expression) {
      type = LoudnessType.EXPRESSION;
    } else {
      type = LoudnessType.VOLUME;
    }
  }

  /**
   * Construct a new Loudness Event object by extracting the data and the
   * position from a given Midi message.
   *
   * @param loudnessEvent either a Note-on message or a MIDI controller message
   * be of type "expression MSB" or "volume MSB"
   */
  public LoudnessEvent(MidiEvent loudnessEvent) {
    if (!isLoudnessEventEx(loudnessEvent)) {
      throw new IllegalArgumentException(loudnessEvent + " is not  a \"loudness\" event.");
    }
    ShortMessage shortMessage = (ShortMessage) loudnessEvent.getMessage();
    this.tickPos = loudnessEvent.getTick();
    int locvalue = shortMessage.getData2();
    if (shortMessage.getCommand() == ShortMessage.NOTE_ON) {
      type = LoudnessType.KEYON;
     // locvalue = (int)(((float)locvalue-30F)*127F/(127F-30F)); // hack to get more dynamic
    } else {
      if (shortMessage.getData1() == MidiUtil.contExpression_MSB) {
        type = LoudnessType.EXPRESSION;
      } else {
        type = LoudnessType.VOLUME;
      }
    }
    locvalue = Math.max(1, locvalue); //at least one
    this.value = locvalue;
  }

  /**
   * A loudness event is obsolete if it does not act on any note.
   *
   * @return true if the event is obsolete.
   */
  public boolean isObsolete() {
    return obsolete;
  }

  /**
   * Compare two Loudness Objects on their position. (Note, we assume that both
   * belong to the same sequence and have the same tickPQN);
   *
   * @param o an other Loudness event.
   * @return 1 if the other Loudness-Event is earlier than this one.
   */
  public int compareTo(LoudnessEvent o) {
    if (this.tickPos < o.getTickPos()) {
      return -1;
    }
    if (this.tickPos > o.getTickPos()) {
      return 1;
    }
    //--- both are at the same time, volume events first
    if (this.type != o.type) {
      return this.type.compareTo(o.type);
    }
    // both are of the same kind, softer first 
    if (this.value < o.value) {
      return -1;
    }
    if (this.value > o.value) {
      return 1;
    }
    return 0;
  }

  /**
   * A function useful for discovering <em>Expression events</em> in a sequence
   * of MIDI messages.
   *
   * @param message any kind of MIDI message (or null)
   * @return true if the event is an expression event
   */
  public static boolean isExpressionMessage(MidiMessage message) {
    if (null == message) {
      return false;
    }
    if (!(message instanceof ShortMessage)) {
      return false;
    }
    ShortMessage shortMessage = (ShortMessage) message;
    if (shortMessage.getCommand() != ShortMessage.CONTROL_CHANGE) {
      return false;
    }
    return (shortMessage.getData1() == MidiUtil.contExpression_MSB);
  }

  /**
   * A function useful for discovering <em>Volume events</em> in a sequence of
   * MIDI messages.
   *
   * @param message any kind of MIDI message (or null)
   * @return true if the event is a volume event
   */
  public static boolean isVolumeMessage(MidiMessage message) {
    if (null == message) {
      return false;
    }
    if (!(message instanceof ShortMessage)) {
      return false;
    }
    ShortMessage shortMessage = (ShortMessage) message;
    if (shortMessage.getCommand() != ShortMessage.CONTROL_CHANGE) {
      return false;
    }
    return (shortMessage.getData1() == MidiUtil.contMainVolume_MSB);
  }

  /**
   * A function useful for discovering <em>Expression events</em> in a sequence
   * of MIDI messages.
   *
   * @param event any kind of MIDI event
   * @return true if the event is an Expression event
   */
  public static boolean isExpressionEvent(MidiEvent event) {
    if (null == event) {
      return false;
    }
    return isExpressionMessage(event.getMessage());
  }

  /**
   * A function useful for discovering <em>Volume events</em> in a sequence of
   * MIDI messages.
   *
   * @param event any kind of MIDI event
   * @return true if the event is a Volume event
   */
  public static boolean isVolumeEvent(MidiEvent event) {
    if (null == event) {
      return false;
    }
    return isVolumeMessage(event.getMessage());
  }

  /**
   * A function useful for discovering <em>Loudness events</em> in a sequence of
   * MIDI messages.
   *
   * @param message any kind of MIDI message (or null)
   * @return true if the event is a Loudness event
   */
  public static boolean isLoudnessMessage(MidiMessage message) {
    return isVolumeMessage(message) || isExpressionMessage(message);
  }

  /**
   * A function useful for discovering <em>extended Loudness events</em> in a
   * sequence of MIDI messages.
   *
   * @param message any kind of MIDI message (or null)
   * @return true if the event is a Loudness event
   */
  public static boolean isLoudnessMessageEx(MidiMessage message) {
    return Note.isNoteOnMessage(message)
            || isVolumeMessage(message) || isExpressionMessage(message);
  }

  /**
   * A function useful for discovering <em>Loudness events</em> in a sequence of
   * MIDI messages.
   *
   * @param event any kind of MIDI event
   * @return true if the event is a Loudness event
   */
  public static boolean isLoudnessEvent(MidiEvent event) {
    if (null == event) {
      return false;
    }
    return isLoudnessMessage(event.getMessage());
  }

  /**
   * A function useful for discovering <em>extended Loudness events</em> (either
   * a key on message or an expression or a voume message) in a sequence of MIDI
   * messages.
   *
   * @param event any kind of MIDI event
   * @return true if the event is a Loudness event
   */
  public static boolean isLoudnessEventEx(MidiEvent event) {
    if (null == event) {
      return false;
    }
    return isLoudnessMessageEx(event.getMessage());
  }

  void setObsolete(boolean b) {
    obsolete = b;
  }
}
