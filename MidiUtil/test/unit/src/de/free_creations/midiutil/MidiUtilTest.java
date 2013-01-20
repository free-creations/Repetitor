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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.sound.midi.*;
import static org.junit.Assert.*;
import org.junit.*;

/**
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class MidiUtilTest {

  public MidiUtilTest() {
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

  @Test
  public void testReadTrackname() throws InvalidMidiDataException, IOException {
    //it is allowed to pass a null pointer
    assertEquals("", MidiUtil.readTrackname(null));

    // now let's verify on a real case
    System.out.println("TestReadTrackname(). ");
    URL midiUrl = this.getClass().getResource("resources/GoodNightLadies.midi");
    Sequence sequence = MidiSystem.getSequence(midiUrl);

    Track[] tracks = sequence.getTracks();
    assertEquals("control track", MidiUtil.readTrackname(tracks[0]));
    assertEquals("", MidiUtil.readTrackname(tracks[1]));
    assertEquals("\\new", MidiUtil.readTrackname(tracks[2]));
  }

  /**
   * This is not really a unit test. Here we we cut the last measures from a
   * midi file and write them to a new file. This file can be manually inspected
   * with an external midi file editor.
   */
  //--@Test
  public void testLeftCut() throws InvalidMidiDataException, IOException {
    System.out.println("Writing to an external midi file. ");
    URL midiUrl = this.getClass().getResource("resources/4_WieLieblich.mid");
    Sequence sequence = MidiSystem.getSequence(midiUrl);

    long startPos = 100000L; //max 130320

    sequence = MidiUtil.leftCut(sequence, startPos);

    File testOutDir = new File("test/temp/");
    testOutDir.mkdirs();

    File outFile = new File(testOutDir, "MidiUtilTest.mid");
    MidiSystem.write(sequence, 1, outFile);
    System.out.println("File " + outFile.getCanonicalPath() + " has been written. Please verify...");

  }

  @Test
  public void testUsedChannels() throws InvalidMidiDataException {
    System.out.println("testUsedChannels ");
    Sequence seq = new Sequence(Sequence.PPQ, 240, 1);
    Track track = seq.getTracks()[0];
    int[] usedChannels = MidiUtil.usedChannels(track);
    assertEquals(0, usedChannels.length);

    // lets add events for eight different channels
    for (int channel = 0; channel < 8; channel++) {
      ShortMessage shortMessage = new ShortMessage();
      shortMessage.setMessage(ShortMessage.NOTE_ON, channel, 64, 64);
      MidiEvent event = new MidiEvent(shortMessage, 0);
      track.add(event);
    }

    // lets add again events for these eight channels
    for (int channel = 0; channel < 8; channel++) {
      ShortMessage shortMessage = new ShortMessage();
      shortMessage.setMessage(ShortMessage.NOTE_ON, channel, 64, 64);
      MidiEvent event = new MidiEvent(shortMessage, 200);
      track.add(event);
    }

    // now the function "usedChannels(track)" shall return eight channel numbers.
    usedChannels = MidiUtil.usedChannels(track);
    assertEquals(8, usedChannels.length);


    for (int channel = 0; channel < 8; channel++) {
      boolean found = false;
      for (int uch : usedChannels) {
        if (uch == channel) {
          found = true;
        }
      }
      assertTrue("channel " + channel + " missing.", found);
    }

  }

  @Test
  public void testPrependSequence() throws InvalidMidiDataException {
    System.out.println("testPrependSequence ");
    Sequence seq = new Sequence(Sequence.PPQ, 240, 1);
    Track track = seq.getTracks()[0];

    // add an event at position zero
    ShortMessage shortMessage = new ShortMessage();
    shortMessage.setMessage(ShortMessage.NOTE_ON, 0, 64, 64);
    long position = 0;
    MidiEvent event = new MidiEvent(shortMessage, position);
    track.add(event);

    Sequence longerSeq = MidiUtil.leftCut(seq, -1000L);
    Track newTrack = longerSeq.getTracks()[0];
    assertEquals(2, newTrack.size());

    event = newTrack.get(0);
    assertEquals(1000L, event.getTick());

  }

  @Test
  public void testInsertSilence() throws InvalidMidiDataException {
    System.out.println("testInsertSilence ");
    Sequence seq = new Sequence(Sequence.PPQ, 240, 1);
    Track track = seq.getTracks()[0];

    // add an event at position zero
    ShortMessage shortMessage = new ShortMessage();
    shortMessage.setMessage(ShortMessage.NOTE_ON, 0, 64, 64);
    long position = 0;
    MidiEvent event = new MidiEvent(shortMessage, position);
    track.add(event);

    Sequence seqWithSilence = MidiUtil.insertSilence(seq, 123L);
    Track newTrack = seqWithSilence.getTracks()[0];
    assertEquals(2, newTrack.size());

    event = newTrack.get(0);
    assertEquals(123L, event.getTick());

  }

  @Test
  public void testStretch() throws InvalidMidiDataException {
    System.out.println("testStretch ");
    Sequence seq = new Sequence(Sequence.PPQ, 240, 1);
    Track track = seq.getTracks()[0];

    // add an event that should not be shifted
    ShortMessage shortMessage = new ShortMessage();
    shortMessage.setMessage(ShortMessage.NOTE_ON, 0, 64, 64);
    long position_noshiftEvent = 240;
    MidiEvent event_0 = new MidiEvent(shortMessage, position_noshiftEvent);
    track.add(event_0);

    // add the marker event (that should be shifted)
    long rightBorder = 4 * 240;
    MidiEvent event_1 = new MidiEvent(shortMessage, rightBorder);
    track.add(event_1);

    // add an additional event that should be shifted)
    long position_shift = 8 * 240;
    MidiEvent event_2 = new MidiEvent(shortMessage, position_shift);
    track.add(event_2);

    // now do the test
    long leftBorder = rightBorder - 240;
    long newRightBorder = leftBorder;
    Sequence shiftedSeq = MidiUtil.stretch(seq, leftBorder, rightBorder, newRightBorder);
    Track shiftedTrack = shiftedSeq.getTracks()[0];
    assertEquals(4, shiftedTrack.size());

    MidiEvent event_0s = shiftedTrack.get(0);
    assertEquals(position_noshiftEvent, event_0s.getTick());
    MidiEvent event_1s = shiftedTrack.get(1);
    assertEquals(leftBorder, event_1s.getTick());
    MidiEvent event_2s = shiftedTrack.get(2);
    assertEquals(position_shift - 240, event_2s.getTick());
  }

  @Test
  public void testCut() throws InvalidMidiDataException {
    System.out.println("testCut ");
    Sequence seq = new Sequence(Sequence.PPQ, 240, 1);
    Track track = seq.getTracks()[0];

    // add an event that should not be shifted
    ShortMessage shortMessage = new ShortMessage();
    shortMessage.setMessage(ShortMessage.NOTE_ON, 0, 64, 64);
    long position_noshiftEvent = 240;
    MidiEvent event_0 = new MidiEvent(shortMessage, position_noshiftEvent);
    track.add(event_0);

    // add an event that should be deleted
    long positionDeleteEvent = 3 * 240;
    MidiEvent event_1 = new MidiEvent(shortMessage, positionDeleteEvent);
    track.add(event_1);

    // add an additional event that should be shifted)
    long position_shiftEvent = 8 * 240;
    MidiEvent event_2 = new MidiEvent(shortMessage, position_shiftEvent);
    track.add(event_2);

    // now do the test
    long fromCut = 2 * 240;
    long toCut = 4 * 240;
    long shift = toCut - fromCut;
    Sequence cutSeq = MidiUtil.cut(seq, fromCut, toCut);

    // verify
    assertNotNull(cutSeq);
    assertNotSame(seq, cutSeq);

    Track cutTrack = cutSeq.getTracks()[0];
    assertEquals(3, cutTrack.size());

    MidiEvent event_0c = cutTrack.get(0);
    assertEquals(position_noshiftEvent, event_0c.getTick());

    MidiEvent event_1c = cutTrack.get(1);
    assertEquals(position_shiftEvent - shift, event_1c.getTick());
  }

  @Test
  public void testTranspose() throws InvalidMidiDataException {
    System.out.println("testTranspose ");
    Sequence seq = new Sequence(Sequence.PPQ, 240, 1);
    Track track = seq.getTracks()[0];

    // add a note on
    ShortMessage shortMessage = new ShortMessage();
    shortMessage.setMessage(ShortMessage.NOTE_ON, 0, 64, 64);
    long position = 0;
    MidiEvent event_on = new MidiEvent(shortMessage, position);
    track.add(event_on);

    // add a note off
    shortMessage = new ShortMessage();
    shortMessage.setMessage(ShortMessage.NOTE_OFF, 0, 64, 64);
    position = 240;
    MidiEvent event_off = new MidiEvent(shortMessage, position);
    track.add(event_off);


    // now do the test
    int semiTones = 3;
    Sequence transposedSeq = MidiUtil.transpose(seq, semiTones);
    Track transposedTrack = transposedSeq.getTracks()[0];

    //verify 
    assertEquals(2 + 1, transposedTrack.size());
    MidiEvent event_onT = transposedTrack.get(0);
    int pitchonT = ((ShortMessage) event_onT.getMessage()).getData1();
    assertEquals(64 + 3, pitchonT);
    MidiEvent event_offT = transposedTrack.get(1);
    int pitchoffT = ((ShortMessage) event_offT.getMessage()).getData1();
    assertEquals(64 + 3, pitchoffT);

  }

  @Test
  public void testAppendSequence() throws InvalidMidiDataException {
    System.out.println("testApendSequence ");
    Sequence seq = new Sequence(Sequence.PPQ, 240, 1);
    Track track = seq.getTracks()[0];

    // add an event at position 100
    ShortMessage shortMessage = new ShortMessage();
    shortMessage.setMessage(ShortMessage.NOTE_ON, 0, 64, 64);
    long position = 100;
    MidiEvent event = new MidiEvent(shortMessage, position);
    track.add(event);
    assertEquals(2, track.size());
    assertEquals(100L, seq.getTickLength());

    Sequence longerSeq = MidiUtil.rightCut(seq, 1000);
    Track newTrack = longerSeq.getTracks()[0];

    assertEquals(1000L, longerSeq.getTickLength());

  }
}
