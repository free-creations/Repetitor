/*
 * Copyright 2011 admin.
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
package de.free_creations.midisong;

import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import de.free_creations.microsequencer.AudioPort;
import de.free_creations.microsequencer.SequencerEventListener;
import de.free_creations.microsequencer.MicroSequencer;
import de.free_creations.microsequencer.PlayingMode;
import de.free_creations.microsequencer.SequencerMidiPort;
import de.free_creations.microsequencer.SequencerPort;
import de.free_creations.midiutil.BeatPosition;
import de.free_creations.midiutil.RPosition;
import de.free_creations.midiutil.RPositionEx;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Patch;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Soundbank;
import javax.sound.midi.SoundbankResource;
import javax.sound.midi.Track;
import javax.sound.midi.Transmitter;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author admin
 */
public class SongSessionTest {

  Song testSong = null;
  Track masterEvents = null;
  Track synthesizerEvents = null;
  Soundbank soundbank = null;
  Sequence sequence = null;
  final long sequenceLength = 1000L;

  public SongSessionTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() throws InvalidMidiDataException {
    final String SONGNAME = "Test Song";
    final String MASTERTRACKNAME = "Test Mastertrack";
    final String DESC = "This is a test Song-instance";
    final String MidiSynthesizerTrackName = "Midi Synthesizer Track";
    final String SoundbankName = "Soundbank name";

    //-----------------------------------------------------------------------
    // Create a simpletest candidate (a song with only a mastertrack and a synthesizer track).
    testSong = new Song();
    testSong.setName(SONGNAME);
    testSong.setDescription(DESC);

    sequence = new Sequence(Sequence.PPQ, 360, 16);
    sequence.getTracks()[0].add(new_30BPM_TempoEvent(sequenceLength));

    MasterTrack masterTrack = testSong.createMastertrack();
    masterTrack.setSequence(sequence);
    masterTrack.setMidiTrackIndex(0);
    masterEvents = sequence.getTracks()[0];
    masterTrack.setName(MASTERTRACKNAME);
    MidiSynthesizerTrack midiSynthesizerTrack = new MidiSynthesizerTrack();
    masterTrack.addSubtrack(midiSynthesizerTrack);
    midiSynthesizerTrack.setName(MidiSynthesizerTrackName);
    midiSynthesizerTrack.setMidiTrackIndex(1);
    synthesizerEvents = sequence.getTracks()[1];
    BuiltinSynthesizer builtinSynthesizer = new BuiltinSynthesizer();
    midiSynthesizerTrack.setSynthesizer(builtinSynthesizer);
    soundbank = new SoundbankMockup(SoundbankName);
    builtinSynthesizer.setSoundbank(soundbank);
    assertFalse(testSong.isImmutable());

  }

  @After
  public void tearDown() {
  }

  /**
   * Test of attachSequencer method. The attached song is empty. We verify that
   * although the song is empty, the system reacts reasonably.
   */
  @Test
  public void testAttachSequencer_0() throws EInvalidSongFile, MidiUnavailableException {
    System.out.println("attachSequencer_0");
    SequencerMockup sequencerMock = new SequencerMockup();

    Song emptySong = new Song();
    SongSession instance = new SongSession(emptySong);
    //verify that song session has been correctly initialised
    assertEquals(0, instance.getLoopStartPoint());
    assertEquals(0, instance.getLeadinTime());
    assertEquals(0, instance.getLeadoutTime());
    assertEquals(0, instance.getLoopEndPoint());

    // now attach the sequencer
    instance.attachSequencer(sequencerMock);

    //verify that the sequencer (mockup) has been correctly initialised.
    assertEquals(0, sequencerMock.sequence.getTickLength());
    assertEquals(0, sequencerMock.ports.size());
    assertEquals(0, sequencerMock.loopStartPoint);
    assertEquals(0, sequencerMock.loopEndPoint);
    assertEquals(0, sequencerMock.leadinTime);
    assertEquals(0, sequencerMock.leadoutTime);


  }

  /**
   * Test of attachSequencer method, for a very simple case. The simplest case
   * for a playable song is a song with a master-track and and a synthesiser
   * track attached to it. More elaborate cases with looping lead-in and so on,
   * are tested in the separate procedures.
   */
  @Test
  public void testAttachSequencer_1() throws EInvalidSongFile, MidiUnavailableException {
    System.out.println("attachSequencer_1");
    SequencerMockup sequencerMock = new SequencerMockup();

    SongSession instance = new SongSession(testSong);
    //verify that song session has been correctly initialised
    assertEquals(0, instance.getLoopStartPoint());
    assertEquals(0, instance.getLeadinTime());
    assertEquals(0, instance.getLeadoutTime());
    assertEquals(sequenceLength, instance.getLoopEndPoint());

    // now attach the sequencer
    instance.attachSequencer(sequencerMock);


    //verify that the sequencer (mockup) has been correctly initialised.
    assertEquals(sequence, sequencerMock.sequence);
    assertEquals(0, sequencerMock.loopStartPoint);
    assertEquals(sequenceLength, sequencerMock.loopEndPoint);
    assertEquals(0, sequencerMock.leadinTime);
    assertEquals(0, sequencerMock.leadoutTime);
    assertEquals(sequenceLength, sequencerMock.sequence.getTickLength());
    assertEquals(1, sequencerMock.ports.size());
    assertEquals(1, sequencerMock.ports.get(0).tracks.length);
    assertEquals(synthesizerEvents, sequencerMock.ports.get(0).tracks[0]);
    assertEquals(soundbank, sequencerMock.ports.get(0).soundbank);

  }

  /**
   * Test of attachSequencer method, for the case of many tracks attached to one
   * synthesiser track (flat hierarchy).
   *
   * synthesiser | +--------+-------+ | | | track_1 track_2 ....
   */
  @Test
  public void testAttachSequencer_3() throws EInvalidSongFile, MidiUnavailableException {
    System.out.println("attachSequencer_3");
    SequencerMockup sequencerMock = new SequencerMockup();

    MasterTrack mastrTrack = testSong.getMastertrack();
    GenericTrack synthTrack = mastrTrack.getSubtracks()[0];

    MidiTrack midiTrack_1 = new MidiTrack();
    midiTrack_1.setMidiTrackIndex(2);
    synthTrack.addSubtrack(midiTrack_1);

    MidiTrack midiTrack_2 = new MidiTrack();
    midiTrack_2.setMidiTrackIndex(3);
    synthTrack.addSubtrack(midiTrack_2);

    // now attach the sequencer
    SongSession instance = new SongSession(testSong);
    instance.attachSequencer(sequencerMock);


    //verify that the sequencer (mockup) has been correctly initialised.
    assertEquals(1, sequencerMock.ports.size());
    SequencerPortMock thePort = sequencerMock.ports.get(0);
    assertEquals(3, thePort.tracks.length);
    assertEquals(sequence.getTracks()[1], thePort.tracks[0]);
    assertEquals(sequence.getTracks()[2], thePort.tracks[1]);
    assertEquals(sequence.getTracks()[3], thePort.tracks[2]);


  }

  /**
   * Test of attachSequencer method, for the case of many tracks attached to one
   * another (deep hierarchy).
   *
   * synthesiser | track_1 | track_2 | .....
   */
  @Test
  public void testAttachSequencer_4() throws EInvalidSongFile, MidiUnavailableException {
    System.out.println("attachSequencer_4");
    SequencerMockup sequencerMock = new SequencerMockup();

    MasterTrack mastrTrack = testSong.getMastertrack();
    GenericTrack synthTrack = mastrTrack.getSubtracks()[0];

    MidiTrack midiTrack_1 = new MidiTrack();
    midiTrack_1.setMidiTrackIndex(2);
    synthTrack.addSubtrack(midiTrack_1);

    MidiTrack midiTrack_2 = new MidiTrack();
    midiTrack_2.setMidiTrackIndex(3);
    midiTrack_1.addSubtrack(midiTrack_2);

    // now attach the sequencer
    SongSession instance = new SongSession(testSong);
    instance.attachSequencer(sequencerMock);


    //verify that the sequencer (mockup) has been correctly initialised.
    assertEquals(1, sequencerMock.ports.size());
    SequencerPortMock thePort = sequencerMock.ports.get(0);
    assertEquals(3, thePort.tracks.length);
    assertEquals(sequence.getTracks()[1], thePort.tracks[0]);
    assertEquals(sequence.getTracks()[2], thePort.tracks[1]);
    assertEquals(sequence.getTracks()[3], thePort.tracks[2]);


  }

  /**
   * Test of attachSequencer method, for the case of many tracks attached to one
   * another the last one being a synthesiser.
   *
   * synthesiser_1 | track_1 | track_2 | synthesiser_2
   */
  @Test
  public void testAttachSequencer_5() throws EInvalidSongFile, MidiUnavailableException {
    System.out.println("attachSequencer_5");
    SequencerMockup sequencerMock = new SequencerMockup();

    MasterTrack mastrTrack = testSong.getMastertrack();
    GenericTrack synthTrack_1 = mastrTrack.getSubtracks()[0];

    MidiTrack midiTrack_1 = new MidiTrack();
    midiTrack_1.setMidiTrackIndex(2);
    synthTrack_1.addSubtrack(midiTrack_1);

    MidiTrack midiTrack_2 = new MidiTrack();
    midiTrack_2.setMidiTrackIndex(3);
    midiTrack_1.addSubtrack(midiTrack_2);

    MidiSynthesizerTrack synthTrack_2 = new MidiSynthesizerTrack();
    BuiltinSynthesizer builtinSynthesizer_2 = new BuiltinSynthesizer();
    synthTrack_2.setSynthesizer(builtinSynthesizer_2);
    synthTrack_2.setMidiTrackIndex(4);
    midiTrack_2.addSubtrack(synthTrack_2);

    // now attach the sequencer
    SongSession instance = new SongSession(testSong);
    instance.attachSequencer(sequencerMock);


    //verify that the sequencer (mockup) has been correctly initialised.
    assertEquals(2, sequencerMock.ports.size());
    SequencerPortMock port_1 = sequencerMock.ports.get(0);
    SequencerPortMock port_2 = sequencerMock.ports.get(1);
    assertEquals(3, port_1.tracks.length);
    assertEquals(sequence.getTracks()[1], port_1.tracks[0]);
    assertEquals(sequence.getTracks()[2], port_1.tracks[1]);
    assertEquals(sequence.getTracks()[3], port_1.tracks[2]);

    assertEquals(1, port_2.tracks.length);
    assertEquals(sequence.getTracks()[4], port_2.tracks[0]);



  }

  /**
   * Test of setLoopEndPoint method, of class SongSession. <ol> <li>As default
   * the loop-end-point is the end of the sequence.</li> <li>The loop-end-point
   * can be set to any value between the loop-start-point and the end of the
   * sequence. An attempt to set the loop-end-point before the loop-start-point
   * does not provoke an exception, but loop-end-point is set to be the same as
   * the loop-start-point. An attempt to set the loop-end-point after the end of
   * the sequence does not provoke an exception, but loop-end-point is set to be
   * the same as the end of the sequence.</li> <li>When a sequencer is attached
   * to a SongSession its loop-end-point is set to be the same as the
   * SongSession's.</li> <li>When the SongSession loop-end-point changes, the
   * sequencers loop-end-point is set accordingly.</li> </ol>
   *
   */
  @Test
  public void testSetLoopEndPoint() throws EInvalidSongFile, MidiUnavailableException {

    System.out.println("testSetLoopEndPoint");
    SequencerMockup sequencerMock = new SequencerMockup();

    SongSession instance = new SongSession(testSong);
    //verify that per default the LoopEndPoint points to the end of the sequence
    assertEquals(sequenceLength, instance.getLoopEndPoint());

    //verify what happens when trying to set the loop-end-point before the loop-start-point
    //or after the sequence.
    long loopStartPoint = sequenceLength / 4;
    instance.setLoopStartPoint(loopStartPoint);
    instance.setLoopEndPoint(-1L);
    assertEquals(loopStartPoint, instance.getLoopEndPoint());

    long pointAfterEnd = sequenceLength + 1000L;
    instance.setLoopEndPoint(pointAfterEnd);
    assertEquals(sequenceLength, instance.getLoopEndPoint());


    // now attach the sequencer
    instance.attachSequencer(sequencerMock);
    //verify that the sequencer (mockup) has the same value as the song-session
    assertEquals(instance.getLoopEndPoint(), sequencerMock.loopEndPoint);

    //verify that changes to the song-session are handed over to the sequencer
    long newLoopEndPoint = sequenceLength / 2;
    instance.setLoopEndPoint(newLoopEndPoint);
    assertEquals(newLoopEndPoint, instance.getLoopEndPoint());
    assertEquals(newLoopEndPoint, sequencerMock.loopEndPoint);


    // detachAudio and attach again...
    instance.detachSequencer();
    // make sure instance.getLoopEndPoint did ont change when detaching the sequncer
    assertEquals(newLoopEndPoint, instance.getLoopEndPoint());

    sequencerMock.loopEndPoint = 0L;
    instance.attachSequencer(sequencerMock);
    // make sure that when re-attaching the sequencer the sequnecer-value is re-adjusted.
    assertEquals(newLoopEndPoint, instance.getLoopEndPoint());
    assertEquals(newLoopEndPoint, sequencerMock.loopEndPoint);

  }

  /**
   * Test of setLoopStartPoint method, of class SongSession. <ol> <li>As
   * default, the loop-start-point is the start of the sequence.</li> <li>The
   * loop-start-point can be set to any value between the start of the sequence
   * and the loop-end-point. An attempt to set the loop-start-point before the
   * start of the sequence does not provoke an exception, but loop-start-point
   * is set to be the start of the sequence. An attempt to set the
   * loop-start-point after the loop-end-point does not provoke an exception,
   * but loop-start-point is set to be the same as the loop-end-point.</li>
   * <li>When a sequencer is attached to a SongSession, the sequencers
   * loop-end-point is set to be the same as the SongSession's.</li> <li>When
   * the SongSession loop-start-point changes, the sequencers loop-start-point
   * is set accordingly.</li> </ol>
   *
   */
  @Test
  public void testSetLoopStartPoint() throws EInvalidSongFile, MidiUnavailableException {

    System.out.println("testSetLoopStartPoint");
    SequencerMockup sequencerMock = new SequencerMockup();

    SongSession instance = new SongSession(testSong);
    //verify that per default the LoopStartPoint points to the start of the sequence
    assertEquals(0L, instance.getLoopStartPoint());

    //verify what happens when trying to set the loop-start-point after the loop-end-point
    //or before the start of the sequence.
    long loopEndPoint = (3 * sequenceLength) / 4;
    instance.setLoopEndPoint(loopEndPoint);
    instance.setLoopStartPoint(sequenceLength); //<<here we try 
    assertEquals(loopEndPoint, instance.getLoopStartPoint());

    long pointBeforeStart = -1000L;
    instance.setLoopStartPoint(pointBeforeStart);
    assertEquals(0L, instance.getLoopStartPoint());


    // now attach the sequencer
    instance.attachSequencer(sequencerMock);
    //verify that the sequencer (mockup) has the same value as the song-session
    assertEquals(instance.getLoopStartPoint(), sequencerMock.loopStartPoint);

    //verify that changes to the song-session are handed over to the sequencer
    long newLoopStartPoint = sequenceLength / 2;
    instance.setLoopStartPoint(newLoopStartPoint);
    assertEquals(newLoopStartPoint, instance.getLoopStartPoint());
    assertEquals(newLoopStartPoint, sequencerMock.loopStartPoint);


    // detachAudio and attach again...
    instance.detachSequencer();
    // make sure instance.getLoopEndPoint did ont change when detaching the sequncer
    assertEquals(newLoopStartPoint, instance.getLoopStartPoint());

    sequencerMock.loopStartPoint = 0L;
    instance.attachSequencer(sequencerMock);
    // make sure that when re-attaching the sequencer-value is re-adjusted.
    assertEquals(newLoopStartPoint, instance.getLoopStartPoint());
    assertEquals(newLoopStartPoint, sequencerMock.loopStartPoint);

  }

  /**
   * Test of setLeadinTime method, of class SongSession.
   */
  @Test
  public void testSetLeadinTime() throws EInvalidSongFile, MidiUnavailableException {
    System.out.println("testSetLeadinTime");

    SequencerMockup sequencerMock = new SequencerMockup();

    SongSession instance = new SongSession(testSong);
    //verify that per default the leadintime is zero
    assertEquals(0L, instance.getLeadinTime());


    // now attach the sequencer
    instance.attachSequencer(sequencerMock);
    //verify that the sequencer (mockup) has the same value as the song-session
    assertEquals(instance.getLeadinTime(), sequencerMock.leadinTime);

    //verify that changes to the song-session are handed over to the sequencer
    long newLeadinTime = sequenceLength / 2;
    instance.setLeadinTime(newLeadinTime);
    assertEquals(newLeadinTime, instance.getLeadinTime());
    assertEquals(newLeadinTime, sequencerMock.leadinTime);


    // detachAudio and attach again...
    instance.detachSequencer();
    // make sure instance.getLeadintime did ont change when detaching the sequncer
    assertEquals(newLeadinTime, instance.getLeadinTime());

    sequencerMock.leadinTime = 0L;
    instance.attachSequencer(sequencerMock);
    // make sure that when re-attaching the sequencer-value is re-adjusted.
    assertEquals(newLeadinTime, instance.getLeadinTime());
    assertEquals(newLeadinTime, sequencerMock.leadinTime);

  }

  /**
   * Test of setLeadoutTime method, of class SongSession.
   */
  @Test
  public void testSetLeadoutTime() throws EInvalidSongFile, MidiUnavailableException {
    System.out.println("testSetLeadoutTime");

    SequencerMockup sequencerMock = new SequencerMockup();

    SongSession instance = new SongSession(testSong);
    //verify that per default the leadouttime is zero
    assertEquals(0L, instance.getLeadoutTime());


    // now attach the sequencer
    instance.attachSequencer(sequencerMock);
    //verify that the sequencer (mockup) has the same value as the song-session
    assertEquals(instance.getLeadoutTime(), sequencerMock.leadoutTime);

    //verify that changes to the song-session are handed over to the sequencer
    long newLeadoutTime = sequenceLength / 2;
    instance.setLeadoutTime(newLeadoutTime);
    assertEquals(newLeadoutTime, instance.getLeadoutTime());
    assertEquals(newLeadoutTime, sequencerMock.leadoutTime);


    // detachAudio and attach again...
    instance.detachSequencer();
    // make sure instance.getLeadouttime did ont change when detaching the sequncer
    assertEquals(newLeadoutTime, instance.getLeadoutTime());

    sequencerMock.leadoutTime = 0L;
    instance.attachSequencer(sequencerMock);
    // make sure that when re-attaching the sequencer-value is re-adjusted.
    assertEquals(newLeadoutTime, instance.getLeadoutTime());
    assertEquals(newLeadoutTime, sequencerMock.leadoutTime);
  }

  /**
   * Test of setLooping method, of class SongSession.
   */
  @Test
  public void testSetLooping() throws EInvalidSongFile, MidiUnavailableException {
    System.out.println("testSetLooping");

    SequencerMockup sequencerMock = new SequencerMockup();

    SongSession instance = new SongSession(testSong);
    //verify that per default the looping is false
    assertFalse(instance.isLooping());


    // now attach the sequencer
    instance.attachSequencer(sequencerMock);
    //verify that the sequencer (mockup) has the same value as the song-session
    assertEquals(0, sequencerMock.loopCount);

    //verify that changes to the song-session are handed over to the sequencer
    instance.setLooping(true);
    assertTrue(instance.isLooping());
    assertEquals(-1, sequencerMock.loopCount);


    // detachAudio and attach again...
    instance.detachSequencer();
    // make sure instance.isLooping did ont change when detaching the sequencer
    assertTrue(instance.isLooping());

    sequencerMock.loopCount = 123;
    instance.attachSequencer(sequencerMock);
    // make sure that when re-attaching the sequencer-value is re-adjusted.
    assertTrue(instance.isLooping());
    assertEquals(-1, sequencerMock.loopCount);
  }

  /**
   * Test of setPlaying method, of class SongSession.
   */
  @Test
  public void testSetPlaying() throws EInvalidSongFile, MidiUnavailableException {
    System.out.println("testSetPlaying");

    SequencerMockup sequencerMock = new SequencerMockup();

    SongSession instance = new SongSession(testSong);
    //verify that per default, playing is false.
    assertFalse(instance.isPlaying());


    // now attach the sequencer
    instance.attachSequencer(sequencerMock);
    //verify that the sequencer (mockup) has the same value as the song-session
    assertFalse(sequencerMock.running);

    //verify that changes to the song-session are handed over to the sequencer
    instance.setPlayingMode("PlayRecordAudio");
    instance.setPlaying(true);
    assertTrue(instance.isPlaying());
    assertTrue(sequencerMock.running);
    assertEquals(PlayingMode.PlayRecordAudio, sequencerMock.playingMode);


    // detachAudio and attach again...
    instance.detachSequencer();
    // make sure the sequencer is not playing anymore
    assertFalse(instance.isPlaying());
    assertFalse(sequencerMock.running);

    //re-attach
    sequencerMock.running = true;
    instance.attachSequencer(sequencerMock);
    // make sure that when re-attaching the sequencer is not playing
    assertFalse(instance.isPlaying());
    assertFalse(sequencerMock.running);
  }

  /**
   * Test of setCursor method, of class SongSession.
   */
  @Test
  public void testSetCursor() throws EInvalidSongFile, MidiUnavailableException {
    System.out.println("testSetCursor");

    SequencerMockup sequencerMock = new SequencerMockup();

    SongSession instance = new SongSession(testSong);
    //verify that per default, cursor is at the start of the song.
    assertEquals(0L, instance.getCursor());


    // now attach the sequencer
    instance.attachSequencer(sequencerMock);
    //verify that the sequencer (mockup) has the same value as the song-session
    assertEquals(0L, sequencerMock.getTickPosition());


    //verify that changes to the song-session are handed over to the sequencer
    long newCursorPos = sequenceLength / 4;
    instance.setStartPoint(newCursorPos);
    assertEquals(newCursorPos, instance.getCursor());
    assertEquals(newCursorPos, sequencerMock.getTickPosition());


    // detachAudio and attach again...
    long cursorBeforeDetaching = sequenceLength / 2;
    sequencerMock.tickPosition = cursorBeforeDetaching;
    assertEquals(cursorBeforeDetaching, instance.getCursor());
    instance.detachSequencer();
    // make sure instance.getCursor did ont change when detaching the sequncer
    assertEquals(cursorBeforeDetaching, instance.getCursor());

    sequencerMock.tickPosition = 123L; //<<just random value
    instance.attachSequencer(sequencerMock);
    // make sure that when re-attaching the sequencer-value is re-adjusted.
    assertEquals(cursorBeforeDetaching, instance.getCursor());
    assertEquals(cursorBeforeDetaching, sequencerMock.getTickPosition());

  }

  /**
   * Test the setMute function for a playable song
   */
  @Test
  public void testSetMute() throws EInvalidSongFile, MidiUnavailableException {
    System.out.println("testSetMute");



    // prepare a structure similar to "testAttachSequencer_5" and set attenuations to arbirary values
    MasterTrack masterTrack = testSong.getMastertrack();
    MidiSynthesizerTrack synthTrack_1 = (MidiSynthesizerTrack) masterTrack.getSubtracks()[0];
    synthTrack_1.setMute(true); // <= 1
    synthTrack_1.setAttenuation(-1.0F); // <= 2

    MidiTrack midiTrack_1 = new MidiTrack();
    midiTrack_1.setMidiTrackIndex(2);
    midiTrack_1.setMute(true); //<=3
    midiTrack_1.setAttenuation(2.0F); //<=4
    synthTrack_1.addSubtrack(midiTrack_1);


    MidiTrack midiTrack_2 = new MidiTrack();
    midiTrack_2.setMidiTrackIndex(3);
    midiTrack_2.setMute(false); //<=5
    midiTrack_2.setAttenuation(3.0F); //<=6
    midiTrack_1.addSubtrack(midiTrack_2);

    SequencerMockup sequencerMock = new SequencerMockup();
    SongSession instance = new SongSession(testSong);
    instance.attachSequencer(sequencerMock);

    //verify that the sequencer (mockup) has been correctly initialised.
    assertEquals(1, sequencerMock.ports.size());
    SequencerPortMock port = sequencerMock.ports.get(0);
    assertTrue(port.portMute); // because of 1
    assertEquals(-1.0F, port.getAudioPort().getAttenuation(0), 1E-9); // because of 2

    assertTrue(port.tracksMute[1]);// because of 3
    assertEquals(2.0F, port.tracksAttenuation[1], 1E-9); // because of 4
    assertFalse(port.tracksMute[2]);// because of 5
    assertEquals(3.0F, port.tracksAttenuation[2], 1E-9); // because of 6

    //------------------------------------------------------------------------------------
    // --- next change some values and than verify that the change is correctly transmited
    // ......but first we have to fetch the active versions of the tracks...
    MidiTrack activeMasterTrack = instance.getActiveSong().getMastertrack();
    MidiSynthesizerTrack activeSynthTrack_1 = (MidiSynthesizerTrack) activeMasterTrack.getSubtracks()[0];
    MidiTrack activeMidiTrack_1 = (MidiTrack) activeSynthTrack_1.getSubtracks()[0];
    MidiTrack activeMidiTrack_2 = (MidiTrack) activeMidiTrack_1.getSubtracks()[0];
    //
    activeMidiTrack_2.setMute(true); //<=7
    activeMidiTrack_2.setAttenuation(5.0F); //<=8

    assertTrue(port.tracksMute[2]);// because of 7
    assertEquals(5.0F, port.tracksAttenuation[2], 1E-9); // because of 8

    activeSynthTrack_1.setMute(false); // <= 9
    activeSynthTrack_1.setAttenuation(9.0F); // <= 10
    assertFalse(port.portMute); // because of 9
    assertEquals(9.0F, port.getAudioPort().getAttenuation(0), 1E-9); // because of 10


  }

  private class SoundbankMockup implements Soundbank {

    public final String name;

    public SoundbankMockup(String name) {
      this.name = name;
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public String getVersion() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getVendor() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getDescription() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SoundbankResource[] getResources() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Instrument[] getInstruments() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Instrument getInstrument(Patch patch) {
      throw new UnsupportedOperationException("Not supported yet.");
    }
  }

  private class SequencerPortMock implements SequencerMidiPort {

    public final Soundbank soundbank;
    private Track[] tracks = null;
    public boolean[] tracksMute = null;
    public float[] tracksAttenuation = null;
    public boolean portMute = false;
    public AudioPort audioPort = new AudioPort() {
      public float[] attenuations = new float[16];

      @Override
      public float getPeakVuAndClear(int channel) {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public float getAttenuation(int channel) {
        return attenuations[channel];
      }

      @Override
      public float[] getAttenuations() {
        return attenuations;
      }

      @Override
      public void setAttenuation(int channel, float attenuation) {
        attenuations[channel] = attenuation;

      }
    };

    public SequencerPortMock(Soundbank soundbank) {
      this.soundbank = soundbank;
    }

    @Override
    public AudioPort getAudioPort() {
      return audioPort;
    }

    @Override
    public void setTracks(Track[] tracks) {
      this.tracks = tracks;
      tracksMute = new boolean[tracks.length];
      tracksAttenuation = new float[tracks.length];
    }

    @Override
    public void send(MidiMessage message, double streamTime) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Track[] getTracks() {
      return tracks;
    }

    @Override
    public void setMute(int trackIndex, boolean value) {
      tracksMute[trackIndex] = value;
    }

    @Override
    public void setMute(boolean value) {
      portMute = value;
    }

    @Override
    public void setAttenuation(int trackIndex, float value) {
      tracksAttenuation[trackIndex] = value;
    }
  }

  private class SequencerMockup implements MicroSequencer {

    public Sequence sequence = null;
    public ArrayList<SequencerPortMock> ports = new ArrayList<SequencerPortMock>();
    public long loopStartPoint = 1234;
    public long loopEndPoint = 4321;
    public long leadinTime = 123;
    public long leadoutTime = 321;
    public boolean running = false;
    private int loopCount = 123;
    private long tickPosition = 123L;
    private SequencerEventListener sequencerEventListener = null;
    private double tempoFactor;
    public PlayingMode playingMode = PlayingMode.MidiOnly;

    @Override
    public void setSequence(Sequence sequence) {
      this.sequence = sequence;
    }

    @Override
    public double getTempoFactorEx() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getTickPosition(double offset) {
      return tickPosition;
    }

    @Override
    public void setTickPosition(long tick) {
      tickPosition = tick;
    }

    @Override
    public void setTickPosition(double tick) {
      tickPosition = (long) tick;
    }

    @Override
    public void open() throws MidiUnavailableException {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeAllPorts() {
      ports.clear();
    }

    @Override
    public void setSequence(InputStream stream) throws IOException, InvalidMidiDataException {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Sequence getSequence() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void start() {
      running = true;
      playingMode = PlayingMode.MidiOnly;
    }

    @Override
    public void stop() {
      running = false;
    }

    @Override
    public boolean isRunning() {
      return running;
    }

    @Override
    public void startRecording() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void stopRecording() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isRecording() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void recordEnable(Track track, int channel) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void recordDisable(Track track) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float getTempoInBPM() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setTempoInBPM(float bpm) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float getTempoInMPQ() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setTempoInMPQ(float mpq) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setTempoFactor(float factor) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float getTempoFactor() {
      return (float) tempoFactor;
    }

    @Override
    public long getTickLength() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getTickPosition() {
      return tickPosition;
    }

    @Override
    public long getMicrosecondLength() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getMicrosecondPosition() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setMicrosecondPosition(long microseconds) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setMasterSyncMode(SyncMode sync) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SyncMode getMasterSyncMode() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SyncMode[] getMasterSyncModes() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setSlaveSyncMode(SyncMode sync) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SyncMode getSlaveSyncMode() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SyncMode[] getSlaveSyncModes() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setTrackMute(int track, boolean mute) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getTrackMute(int track) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setTrackSolo(int track, boolean solo) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean getTrackSolo(int track) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean addMetaEventListener(MetaEventListener listener) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeMetaEventListener(MetaEventListener listener) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int[] addControllerEventListener(ControllerEventListener listener, int[] controllers) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int[] removeControllerEventListener(ControllerEventListener listener, int[] controllers) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setLoopStartPoint(long tick) {
      loopStartPoint = tick;
    }

    @Override
    public long getLoopStartPoint() {
      return loopStartPoint;
    }

    @Override
    public void setLoopEndPoint(long tick) {
      loopEndPoint = tick;
    }

    @Override
    public long getLoopEndPoint() {
      return loopEndPoint;
    }

    @Override
    public void setLoopCount(int count) {
      loopCount = count;
    }

    @Override
    public int getLoopCount() {
      return loopCount;
    }

    @Override
    public Info getDeviceInfo() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isOpen() {
      return true;
    }

    @Override
    public int getMaxReceivers() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getMaxTransmitters() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Receiver getReceiver() throws MidiUnavailableException {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Receiver> getReceivers() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Transmitter getTransmitter() throws MidiUnavailableException {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Transmitter> getTransmitters() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SequencerMidiPort createDefaultSynthesizerPort(String name, final Soundbank sb) {
      SequencerPortMock newport = new SequencerPortMock(sb);
      ports.add(newport);
      return newport;
    }

    @Override
    public void setTempoFactor(double factor) {
      this.tempoFactor = factor;
    }

    @Override
    public void setLeadinTime(long ticks) {
      leadinTime = ticks;
    }

    @Override
    public long getLeadinTime() {
      return leadinTime;
    }

    @Override
    public void setLeadoutTime(long ticks) {
      leadoutTime = ticks;
    }

    @Override
    public long getLeadoutTime() {
      return leadoutTime;
    }

    @Override
    public BeatPosition tickToBeatPosition(double tickPosition) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getMaxLoadAndClear() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double beatPositionToTick(RPosition position) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public RPositionEx tickToRPositionEx(double tickPosition) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addSequencerEventListener(SequencerEventListener listener) {
      sequencerEventListener = listener;
    }

    @Override
    public void removeSequencerEventListener(SequencerEventListener listener) {
      sequencerEventListener = null;
    }

    @Override
    public double tickToEffectiveBPM(double tickPosition) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public SequencerPort createAudioRecorderPort(String name) throws IOException, MidiUnavailableException {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void start(PlayingMode playingMode) {
      running = true;
      this.playingMode = playingMode;
    }
  }

  /**
   * utility function to create a "tempo" event.
   *
   * @param pos midi tick where the event should be placed
   * @return a new midi event
   */
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
}
