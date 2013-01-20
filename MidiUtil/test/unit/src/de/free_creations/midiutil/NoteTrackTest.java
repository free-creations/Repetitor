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
import javax.sound.midi.*;
import static org.junit.Assert.assertEquals;
import org.junit.*;

/**
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class NoteTrackTest {

  private final int channel = 3;
  private final int velocity = 32;

  public NoteTrackTest() {
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
   * Test of size method, of class NoteTrack.
   */
  @Test
  public void testCreation() throws InvalidMidiDataException {
    System.out.println("testCreation");
    Sequence sequence = new Sequence(Sequence.PPQ, 240, 1);
    Track track = sequence.getTracks()[0];

    int pitch_1 = 20;
    int pitch_2 = 30;

    long tick_1 = 10;
    long tick_2 = 20;
    long dur_1 = 1000;
    long dur_2 = 10;

    track.add(makeNoteOn(pitch_1, tick_1));
    track.add(makeNoteOff(pitch_1, tick_1 + dur_1));

    track.add(makeNoteOn(pitch_2, tick_2));
    track.add(makeNoteOff(pitch_2, tick_2 + dur_2));

    NoteTrack instance = new NoteTrack(track, true);

    assertEquals(2, instance.size());

    Note note_1 = instance.get(0);
    assertEquals(pitch_1, note_1.getPitch());
    assertEquals(tick_1, note_1.getTickPos());
    assertEquals(dur_1, note_1.getDuration());

    assertEquals(pitch_1, instance.getMinPitch());
    assertEquals(pitch_2, instance.getMaxPitch());
  }
  /**
   * Test of size method, of class NoteTrack.
   */
  @Test
  public void testNullCreation() throws InvalidMidiDataException {
    System.out.println("testNullCreation");

    NoteTrack instance = new NoteTrack(null, true);

    assertEquals(0, instance.size());

    assertEquals(0, instance.getMinPitch());
    assertEquals(127, instance.getMaxPitch());
  }

  private MidiEvent makeNoteOn(int pitch, long tick) {
    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(ShortMessage.NOTE_ON, channel, pitch, velocity);
    } catch (InvalidMidiDataException ex) {
      Logger.getLogger(NoteTest.class.getName()).log(Level.SEVERE, null, ex);
    }
    return new MidiEvent(message, tick);
  }

  private MidiEvent makeNoteOff(int pitch, long tick) {
    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(ShortMessage.NOTE_OFF, channel, pitch, velocity);
    } catch (InvalidMidiDataException ex) {
      Logger.getLogger(NoteTest.class.getName()).log(Level.SEVERE, null, ex);
    }
    return new MidiEvent(message, tick);
  }
}
