/*
 * Copyright 2012 Harald Postner.
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

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Harald Postner
 */
public class TimeSignatureTest {

  public TimeSignatureTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of isTimeSignatureEvent method, of class TimeSignature.
   */
  @Test
  public void testIsTimeSignatureEvent() {
  }

  /**
   * Test of isTimeSignatureMessage method, of class TimeSignature.
   */
  @Test
  public void testIsTimeSignatureMessage() {
  }

  /**
   * Test of getNumerator method, of class TimeSignature.
   */
  @Test
  public void testGetNumerator() {
  }

  /**
   * Test of getDenominator method, of class TimeSignature.
   */
  @Test
  public void testGetDenominator() {
  }

  /**
   * Test of getBarLength method, of class TimeSignature.
   */
  @Test
  public void testGetBarLength() {
  }

  /**
   * Test of getTickPos method, of class TimeSignature.
   */
  @Test
  public void testGetTickPos() {
  }

  /**
   * Test of getBeatLength method, of class TimeSignature.
   */
  @Test
  public void testGetBeatLength() {
  }

  /**
   * Test of compareTo method, of class TimeSignature.
   */
  @Test
  public void testCompareTo() {
  }

  /**
   * Test of newMidiMessage method, of class TimeSignature.
   */
  @Test
  public void testNewMidiMessage() throws Exception {
    System.out.println("newMidiMessage");
    MetaMessage instance = TimeSignature.newMidiMessage(4, 4);

    assertEquals(0x58, instance.getType());

    byte[] data = instance.getData();
    assertEquals(4, data.length);
    assertEquals((byte) 0x04, data[0]); //numerator
    assertEquals((byte) 0x02, data[1]); //denominator 2^2=4
    assertEquals((byte) 24, data[2]); //MIDI clocks in a metronome click.
    assertEquals((byte) 8, data[3]); //notated 32nd notes in a MIDI quarter note

    //make sure that the binary log at least works for all common cases
    TimeSignature.newMidiMessage(4, 1);
    TimeSignature.newMidiMessage(4, 2);
    TimeSignature.newMidiMessage(4, 4);
    TimeSignature.newMidiMessage(4, 8);
    TimeSignature.newMidiMessage(12, 16);

  }
}
