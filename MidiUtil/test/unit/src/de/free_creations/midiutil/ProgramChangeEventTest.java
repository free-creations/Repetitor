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

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import static org.junit.Assert.*;
import org.junit.*;

/**
 *
 * @author Harald Postner
 */
public class ProgramChangeEventTest {
  
  public ProgramChangeEventTest() {
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
   * Test of getTickPos method, of class ProgramChangeEvent.
   */
  @Test
  public void testGetTickPos() {
  }

  /**
   * Test of isProgramChangeEvent method, of class ProgramChangeEvent.
   */
  @Test
  public void testIsProgramChangeEvent() throws InvalidMidiDataException {
    System.out.println("testIsProgramChangeEvent");
    MidiEvent pcEvent = ProgramChangeEvent.newMidiEvent(0, 0, 0);
    MidiEvent otherEvent = new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, 0, 64, 64), 0);
    assertTrue(ProgramChangeEvent.isProgramChangeEvent(pcEvent));
    assertFalse(ProgramChangeEvent.isProgramChangeEvent(otherEvent));
  }

  /**
   * Test of isProgramChangeMessage method, of class ProgramChangeEvent.
   */
  @Test
  public void testIsProgramChangeMessage() {
    // implictly tested with "testIsProgramChangeEvent"
  }

  /**
   * Test of compareTo method, of class ProgramChangeEvent.
   */
  @Test
  public void testCompareTo() throws InvalidMidiDataException {
    System.out.println("testCompareTo");
    ProgramChangeEvent earlyEvent = new ProgramChangeEvent(0, 0, 0);
    ProgramChangeEvent equalEvent = new ProgramChangeEvent(0, 0, 0);
    ProgramChangeEvent laterEvent = new ProgramChangeEvent(0, 0, 1000);
    
    assertEquals(0, earlyEvent.compareTo(equalEvent));
    assertEquals(-1, earlyEvent.compareTo(laterEvent));
    
  }

  /**
   * Test of newMidiMessage method, of class ProgramChangeEvent.
   */
  @Test
  public void testNewMidiMessage() throws Exception {
  }

  /**
   * Test of newMidiEvent method, of class ProgramChangeEvent.
   */
  @Test
  public void testNewMidiEvent() throws Exception {
    System.out.println("testNewMidiEvent");
    MidiEvent pcEvent = ProgramChangeEvent.newMidiEvent(63, 5, 123);
    assertEquals(123, pcEvent.getTick());
    ShortMessage message = (ShortMessage)pcEvent.getMessage();
    assertEquals(5, message.getChannel());
    assertEquals(0xC0, message.getCommand());
    assertEquals(63, message.getData1());
    
  }
}
