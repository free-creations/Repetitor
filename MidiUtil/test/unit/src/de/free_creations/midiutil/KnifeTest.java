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

import javax.sound.midi.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertEquals;
import org.junit.*;

/**
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class KnifeTest {

  Sequence sequence = null;

  public KnifeTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() throws InvalidMidiDataException {
    sequence = new Sequence(Sequence.PPQ, 240, 2);
  }

  @After
  public void tearDown() {
  }

  /**
   * Test the handling of meta events
   */
  @Test
  public void testMetaEvent() throws InvalidMidiDataException {
    System.out.println("testMetaEvent");
    Track track = sequence.getTracks()[1];



    final int cutPos = 480;

    // an event to be ignored
    final int LYRICS = 0x05;
    insertMeta(track, 0, LYRICS, (byte) 0);

    // an event to be shifted to the beginning of the new sequence
    final int TRACKNAME = 0x03;
    insertMeta(track, 0, TRACKNAME, (byte) 0);

    // an event to be taken over
    insertMeta(track, cutPos + 100, LYRICS, (byte) 02);




    Knife knife = new Knife(sequence, cutPos);
    Sequence resultSeq = knife.getResult();
    Track resultTrack = resultSeq.getTracks()[1];

    // verify that the resulting initialTrack has an equivalent events in the correct pos
    assertEquals(3, resultTrack.size());
    compareEvents(track.get(1), 0, resultTrack.get(0), 0);
    compareEvents(track.get(2), cutPos + 100, resultTrack.get(1), 100);

  }

   /**
   * Test the handling of meta events
   */
  @Test
  public void testNegativeCut() throws InvalidMidiDataException {
    System.out.println("testNegativeCut");
    Track initialTrack = sequence.getTracks()[1];



    final int cutPos = -480;

    // an event to be ignored
    final int LYRICS = 0x05;
    insertMeta(initialTrack, 0, LYRICS, (byte) 0);

    // an event to be shifted (?? only if we shit full measures???) to the beginning of the new sequence
    final int TIMESIGNATURE = 0x54;
    insertMeta(initialTrack, 0, TIMESIGNATURE, (byte) 0);

    // an event to be taken over
    insertMeta(initialTrack, 100, LYRICS, (byte) 02);




    Knife knife = new Knife(sequence, cutPos);
    Sequence resultSeq = knife.getResult();
    Track resultTrack = resultSeq.getTracks()[1];

    // verify that the resulting initialTrack has an equivalent events in the correct pos
    assertEquals(4, resultTrack.size());
    compareEvents(initialTrack.get(0), 0, resultTrack.get(1), -cutPos);
    compareEvents(initialTrack.get(1), 0, resultTrack.get(0), 0);
    compareEvents(initialTrack.get(2), 100, resultTrack.get(2), 100-cutPos);

  }
  /**
   * Test the handling of "main volume" messages
   */
  @Test
  public void testMainVolume() throws InvalidMidiDataException {
    System.out.println("testMainVolume");
    Track track = sequence.getTracks()[1];


    final int shiftEventPos = 240; // the position of an event that must be shifted
    final int cutPos = shiftEventPos + 480; //here we'll cut the sequence

    // an event to be ignored (before the "shift event pos")
    insertVolume(track, shiftEventPos - 10, 01, 00);

    // an event to be shifted to the beginning of the new sequence
    insertVolume(track, shiftEventPos, 01, 01);

    // an event to be taken over (after the cut)
    insertVolume(track, cutPos + 100, 01, 02);




    Knife knife = new Knife(sequence, cutPos);
    Sequence resultSeq = knife.getResult();
    Track resultTrack = resultSeq.getTracks()[1];

    // verify that the resulting initialTrack has an equivalent events in the correct pos
    assertEquals(3, resultTrack.size());
    compareEvents(track.get(1), shiftEventPos, resultTrack.get(0), 0);
    compareEvents(track.get(2), cutPos + 100, resultTrack.get(1), 100);

  }

  /**
   * Test the handling of pitch bend messages
   */
  @Test
  public void testPitchBend() throws InvalidMidiDataException {
    System.out.println("testPitchBend");
    Track track = sequence.getTracks()[1];


    final int shiftEventPos = 240;
    final int cutPos = shiftEventPos + 480;

    // an event to be ignored
    insertPitchBend(track, shiftEventPos - 10, 01, 00);

    // an event to be shifted to the beginning of the new sequence
    insertPitchBend(track, shiftEventPos, 01, 01);

    // an event to be taken over
    insertPitchBend(track, cutPos + 100, 01, 02);




    Knife knife = new Knife(sequence, cutPos);
    Sequence resultSeq = knife.getResult();
    Track resultTrack = resultSeq.getTracks()[1];

    // verify that the resulting initialTrack has an equivalent events in the correct pos
    assertEquals(3, resultTrack.size());
    compareEvents(track.get(1), shiftEventPos, resultTrack.get(0), 0);
    compareEvents(track.get(2), cutPos + 100, resultTrack.get(1), 100);
  }

  /**
   * Test the correct handling of a negative cutpos
   */
  @Test
  public void testInsertion() throws InvalidMidiDataException {
    System.out.println("testInsertion");
    Track masterTrack = sequence.getTracks()[0];


    // an event to be shifted to the beginning of the new sequence
    final int TRACKNAME = 0x03;
    insertMeta(masterTrack, 0, TRACKNAME, (byte) 0);


    // an event to be shifted away from the beginning
    final int LYRICS = 0x05;
    insertMeta(masterTrack, 100, LYRICS, (byte) 0);
    assertEquals(3, masterTrack.size());


    Knife knife = new Knife(sequence, -300);
    Sequence resultSeq = knife.getResult();
    Track resultTrack = resultSeq.getTracks()[0];

    // verify that the resulting initialTrack has an equivalent events in the correct pos

    assertEquals(3, resultTrack.size());
    compareEvents(masterTrack.get(0), 0, resultTrack.get(0), 0);
    compareEvents(masterTrack.get(1), 100, resultTrack.get(1), 400);
  }

  private void compareEvents(MidiEvent event1, long pos1, MidiEvent event2, long pos2) {
    assertEquals("Midi event 1 not at expected position.", pos1, event1.getTick());
    assertEquals("Midi event 2 not at expected position.", pos2, event2.getTick());
    MidiMessage message1 = event1.getMessage();
    MidiMessage message2 = event2.getMessage();
    assertEquals(message1.getLength(), message2.getLength());
    for (int i = 0; i < message1.getLength(); i++) {
      assertEquals("Midi events not equal.", message1.getMessage()[i], message2.getMessage()[i]);
    }
  }

  private void insertPitchBend(Track track, long pos, int channel, int data) throws InvalidMidiDataException {
    ShortMessage message = new ShortMessage();
    message.setMessage(
            ShortMessage.PITCH_BEND, //command,
            channel, //channel,
            data, //data1,
            0);//data2)

    MidiEvent event = new MidiEvent(message, pos);
    track.add(event);
  }

  private void insertVolume(Track track, long pos, int channel, int data) throws InvalidMidiDataException {
    ShortMessage message = new ShortMessage();
    message.setMessage(
            ShortMessage.CONTROL_CHANGE, //command,
            channel, //channel,
            07, //data1,
            data);//data2)

    MidiEvent event = new MidiEvent(message, pos);
    track.add(event);
  }

  private void insertMeta(Track track, long pos, int type, byte data) throws InvalidMidiDataException {
    MetaMessage message = new MetaMessage();
    message.setMessage(
            type,
            new byte[]{data},
            1);//data2)

    MidiEvent event = new MidiEvent(message, pos);
    track.add(event);
  }
}
