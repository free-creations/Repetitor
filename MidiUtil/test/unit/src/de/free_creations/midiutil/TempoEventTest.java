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
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class TempoEventTest {

  /**
   * Test of isTempoEvent method, of class TempoEvent.
   */
  @Test
  public void testIsTempoEvent() {
    System.out.println("isTempoEvent");
    assertFalse(
            TempoEvent.isTempoEvent(null));
    assertTrue(
            TempoEvent.isTempoEvent(new_30BPM_TempoEvent(123)));

  }

  /**
   * Test of getTempoPerQuarter method, of class TempoEvent.
   */
  @Test
  public void testGetTempoPerQuarter() {
    System.out.println("getTempoPerQuarter");
    int tickPQN = 360;
    MidiEvent event = new_30BPM_TempoEvent(123);
    TempoEvent instance = new TempoEvent(event, tickPQN);
    int expResult = 2000000;
    int result = instance.getTempoPerQuarter();
    assertEquals(expResult, result);

  }

  /**
   * Test of compareTo method, of class TempoEvent.
   */
  @Test
  public void testCompareTo() {
    System.out.println("compareTo");
    int tickPQN = 360;
    MidiEvent event = new_30BPM_TempoEvent(100);
    TempoEvent instance = new TempoEvent(event, tickPQN);
    MidiEvent otherEvent = new_30BPM_TempoEvent(200);
    TempoEvent otherInstance = new TempoEvent(otherEvent, tickPQN);
    assertEquals(0, instance.compareTo(instance));
    assertEquals(-1, instance.compareTo(otherInstance));
    assertEquals(1, otherInstance.compareTo(instance));
  }

  private MidiEvent new_30BPM_TempoEvent(long pos) {
    // 30 (quarter)-Beats per Minute
    // -> two seconds per beat = 2,000,000 microseconds
    // -> in Hex 0x1E8480 or as three bytes 0x1E  0x84  0x80
    int tempoMeta = 0x51;
    MetaMessage message = new MetaMessage();
    try {
      message.setMessage(tempoMeta, //
              new byte[]{(byte) 0x1E, (byte) 0x84, (byte) 0x80}, //
              3); //data2)
    } catch (InvalidMidiDataException ex) {
      throw new RuntimeException(ex);
    }

    return new MidiEvent(message, pos);

  }

  /**
   * Test of getTickPos method, of class TempoEvent.
   */
  @Test
  public void testGetTickPos() {
    System.out.println("getTickPos");
    int tickPQN = 360;
    long expResult = 100L;
    MidiEvent event = new_30BPM_TempoEvent(expResult);
    TempoEvent instance = new TempoEvent(event, tickPQN);
    long result = instance.getTickPos();
    assertEquals(expResult, result);
  }

  /**
   * Test of toSeconds method, of class TempoEvent.
   */
  @Test
  public void testToSeconds() {
    System.out.println("toSeconds");
    int tickPQN = 360;
    MidiEvent event = new_30BPM_TempoEvent(0);
    TempoEvent instance = new TempoEvent(event, tickPQN);
    double expResult = 2.0;
    double result = instance.toSeconds(360);
    assertEquals(expResult, result, 0.0);
  }

  /**
   * Test of isTempoMessage method, of class TempoEvent.
   */
  @Test
  public void testIsTempoMessage() {
    System.out.println("isTempoMessage");

    boolean expResult = false;
    assertFalse(TempoEvent.isTempoMessage(null));
  }

  /**
   * Test of toMidiTicks method, of class TempoEvent.
   */
  @Test
  public void testToMidiTicks() {
    System.out.println("toMidiTicks");
    int tickPQN = 360;
    MidiEvent event = new_30BPM_TempoEvent(0);
    TempoEvent instance = new TempoEvent(event, tickPQN);
    double offset = 2.0;
    double expResult = 360.0;
    double result = instance.toMidiTicks(offset);
    assertEquals(expResult, result, 0.0);
  }

  /**
   * Test of newMidiMessage method, of class TempoEvent.
   */
  @Test
  public void testNewTempoMidiMessage() throws InvalidMidiDataException {
    System.out.println("newTempoMidiMessage");

    MetaMessage instance = TempoEvent.newMidiMessage(30.0D);
    assertEquals(0x51, instance.getType());

    byte[] data = instance.getData();
    assertEquals(3, data.length);
    assertEquals((byte)0x1E, data[0]);
    assertEquals((byte)0x84, data[1]);
    assertEquals((byte)0x80, data[2]);

  }
}
