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

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author harald
 */
public class LoudnessEventTest {

  static final int testValue = 64;

  @Test
  public void testCreation1() {
    System.out.println("testCreation1");
    LoudnessEvent item = new LoudnessEvent(new_NoteOnEvent(123));
    assertEquals(testValue, item.getValue());
    assertTrue(item.isKeyOn());
    assertFalse(item.isExpression());
    assertFalse(item.isVolume());
  }

  @Test
  public void testIsExpressionEvent() {
    System.out.println("testIsExpressionEvent");
    assertFalse(
            LoudnessEvent.isExpressionEvent(null));
    assertTrue(
            LoudnessEvent.isExpressionEvent(new_ExpressionEvent(123)));

  }

  @Test
  public void testIsVolumeEvent() {
    System.out.println("testIsVolumeEvent");
    assertFalse(
            LoudnessEvent.isVolumeEvent(null));
    assertTrue(
            LoudnessEvent.isVolumeEvent(new_VolumeEvent(123)));

  }

  @Test
  public void testIsLoudnessEventEx() {
    System.out.println("testIsLoudnessEventEx");
    assertFalse(
            LoudnessEvent.isLoudnessEventEx(null));
    assertTrue(
            LoudnessEvent.isLoudnessEventEx(new_VolumeEvent(123)));
    assertTrue(
            LoudnessEvent.isLoudnessEventEx(new_ExpressionEvent(123)));
    assertTrue(
            LoudnessEvent.isLoudnessEventEx(new_NoteOnEvent(123)));
  }

  @Test
  public void testCompareTo() {
    System.out.println("compareTo");
    MidiEvent event = new_ExpressionEvent(100);
    LoudnessEvent instance = new LoudnessEvent(event);
    MidiEvent otherEvent = new_ExpressionEvent(200);
    LoudnessEvent otherInstance = new LoudnessEvent(otherEvent);
    assertEquals(0, instance.compareTo(instance));
    assertEquals(-1, instance.compareTo(otherInstance));
    assertEquals(1, otherInstance.compareTo(instance));
  }

  @Test
  public void testCompareTo2() {
    System.out.println("compareTo2");
    MidiEvent event = new_VolumeEvent(100);
    LoudnessEvent instance = new LoudnessEvent(event);
    MidiEvent otherEvent = new_ExpressionEvent(100);
    LoudnessEvent otherInstance = new LoudnessEvent(otherEvent);
    assertEquals(0, instance.compareTo(instance));
    assertEquals(-1, instance.compareTo(otherInstance));
    assertEquals(1, otherInstance.compareTo(instance));
  }

  private MidiEvent new_ExpressionEvent(long pos) {
    final int expressionMSB = 11;
    final int channel = 0;
    final int value = testValue;
    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(ShortMessage.CONTROL_CHANGE,
              channel, expressionMSB, value);


    } catch (InvalidMidiDataException ex) {
      throw new RuntimeException(ex);
    }
    return new MidiEvent(message, pos);
  }

  private MidiEvent new_VolumeEvent(long pos) {
    final int volumeMSB = 7;
    final int channel = 0;
    final int value = testValue;
    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(ShortMessage.CONTROL_CHANGE,
              channel, volumeMSB, value);


    } catch (InvalidMidiDataException ex) {
      throw new RuntimeException(ex);
    }
    return new MidiEvent(message, pos);
  }

  private MidiEvent new_NoteOnEvent(long pos) {

    final int channel = 0;
    final int pitch = 64;
    final int velocity = testValue;
    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(ShortMessage.NOTE_ON,
              channel, pitch, velocity);


    } catch (InvalidMidiDataException ex) {
      throw new RuntimeException(ex);
    }
    return new MidiEvent(message, pos);
  }
}
