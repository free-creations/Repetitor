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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.*;

/**
 *
 * @author admin
 */
public class NoteTest {

  public NoteTest() {
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
   * Test of isNoteOnMessage method, of class Note.
   */
  @Test
  public void testIsNoteOnMessage() {
    assertTrue(Note.isNoteOnMessage(makeNoteOn(64, 5)));
    assertTrue(!Note.isNoteOnMessage(makeNoteOff_1(64, 5)));
    assertTrue(!Note.isNoteOnMessage(makeNoteOff_2(64, 5)));
  }

  /**
   * Test of isNoteOffMessage method, of class Note.
   */
  @Test
  public void testIsNoteOffMessage() {
    assertTrue(!Note.isNoteOffMessage(makeNoteOn(64, 5)));
    assertTrue(Note.isNoteOffMessage(makeNoteOff_1(64, 5)));
    assertTrue(Note.isNoteOffMessage(makeNoteOff_2(64, 5)));
  }

  /**
   * Test of getDuration method, of class Note.
   */
  @Test
  public void testGetter() {
    System.out.println("testGetter");
    int channel = 3;
    int pitch = 24;
    int velocity = 64;
    long tickPos = 100;
    long duration = 300;

    MidiEvent noteOn = makeNoteOnEvent(pitch, channel, tickPos);
    MidiEvent noteOff = makeNoteOffEvent(pitch, channel, tickPos + duration);
    Note instance = new Note(noteOn, noteOff);

    assertEquals(channel, instance.getChannel());
    assertEquals(pitch, instance.getPitch());
    assertEquals(velocity, instance.getVelocity());
    assertEquals(tickPos, instance.getTickPos());
    assertEquals(duration, instance.getDuration());


  }



  /**
   * Test of isNoteOffEventFor method, of class Note.
   */
  @Test
  public void testIsNoteOffEventFor() {
    System.out.println("isNoteOffEventFor");
    int channel = 15;
    int pitch = 24;
    long tickPos = 100;
    long duration = 300;

    MidiEvent noteOn = makeNoteOnEvent(pitch, channel, tickPos);
    MidiEvent noteOff = makeNoteOffEvent(pitch, channel, tickPos + duration);
    assertTrue(Note.isNoteOffEventFor(noteOn, noteOff));
  }

  private MidiEvent makeNoteOnEvent(int pitch, int channel, long tick) {
    return new MidiEvent(makeNoteOn(pitch, channel), tick);
  }

  private MidiEvent makeNoteOffEvent(int pitch, int channel, long tick) {
    return new MidiEvent(makeNoteOff_1(pitch, channel), tick);
  }

  private MidiMessage makeNoteOn(int pitch, int channel) {
    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(ShortMessage.NOTE_ON, channel, pitch, 64);
    } catch (InvalidMidiDataException ex) {
      Logger.getLogger(NoteTest.class.getName()).log(Level.SEVERE, null, ex);
    }
    return message;
  }

  private MidiMessage makeNoteOff_1(int pitch, int channel) {
    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(ShortMessage.NOTE_OFF, channel, pitch, 64);
    } catch (InvalidMidiDataException ex) {
      Logger.getLogger(NoteTest.class.getName()).log(Level.SEVERE, null, ex);
    }
    return message;
  }

  private MidiMessage makeNoteOff_2(int pitch, int channel) {
    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(ShortMessage.NOTE_ON, channel, pitch, 0);
    } catch (InvalidMidiDataException ex) {
      Logger.getLogger(NoteTest.class.getName()).log(Level.SEVERE, null, ex);
    }
    return message;
  }
}
