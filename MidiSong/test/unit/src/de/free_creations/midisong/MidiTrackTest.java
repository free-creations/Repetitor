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
package de.free_creations.midisong;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import javax.xml.bind.JAXBException;
import org.openide.filesystems.URLMapper;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import java.io.FileNotFoundException;
import java.beans.PropertyVetoException;
import java.net.URISyntaxException;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * This class tests the {@link MidiSythesizerTrack } class. The tests are merely
 * the same as in {@link SongTest } because {@link Song } and {@link MidiSythesizerTrack
 * } are quite dependant on each other.
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class MidiTrackTest {

  private static final String SONGNAME = "Test Song";
  private static final String MASTERTRACKNAME = "Test Mastertrack";
  private static final String MIDITRACKNAME = "Test Midi track";
  private static final String DESC = "This is a test Song-instance";
  private static final int CHANNEL = 15;
  private static final int TRACKINDEX = 55;

  public MidiTrackTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }
  private File resourceDir;
  private File tempDir;

  /**
   *
   * @throws URISyntaxException
   * @throws PropertyVetoException
   * @throws IOException
   */
  @Before
  public void setUp() throws URISyntaxException, FileNotFoundException, IOException {

    URL resourceDirURL = this.getClass().getResource("resources");
    resourceDir = new File(resourceDirURL.toURI());
    assertTrue(resourceDir.exists());

    tempDir = new File(System.getProperty("java.io.tmpdir"));
    assertTrue(tempDir.exists());
    assertTrue(tempDir.canWrite());



  }

  /**
   * Test the method clone of MidiTrack
   *
   * @throws MalformedURLException
   * @throws IOException
   * @throws EInvalidSongFile
   */
  @Test
  public void testClone() throws MalformedURLException, IOException, EInvalidSongFile {
    System.out.println("testClone");
    //-----------------------------------------------------------------------
    // Create s test candidate (a song with a mastertrack and a midi track).
    Song originalSong = new Song();
    originalSong.setName(SONGNAME);
    originalSong.setDescription(DESC);

    MasterTrack masterTrack = originalSong.createMastertrack();
    assertEquals(masterTrack, originalSong.getMastertrack());
    masterTrack.setName(MASTERTRACKNAME);

    MidiTrack originalMidiTrack = new MidiTrack();
    masterTrack.addSubtrack(originalMidiTrack);

    originalMidiTrack.setName(MIDITRACKNAME);
    originalMidiTrack.setMidiChannel(CHANNEL);
    originalMidiTrack.setMidiTrackIndex(TRACKINDEX);
    // -------------------------------------------------------------------------
    // clone the song (which will also clone the midi track).
    Song clonedSong = originalSong.clone();
    MidiTrack clonedMidiTrack = (MidiTrack) clonedSong.getMastertrack().getSubtracks()[0];
    // -------------------------------------------------------------------------
    // verify that the cloned Midi track has the same values than the original
    // but is an other object
    assertEquals(clonedMidiTrack.getName(), MIDITRACKNAME);
    assertEquals(clonedMidiTrack.getMidiChannel(), CHANNEL);
    assertEquals(clonedMidiTrack.getMidiTrackIndex(), TRACKINDEX);
    assertTrue(clonedMidiTrack != originalMidiTrack);

  }

  @After
  public void tearDown() {
  }

  /**
   * Verify that a Track object can be marshaled into an XML stream and
   * un-marshaled back to the original structure.
   *
   * @throws MalformedURLException
   * @throws IOException
   */
  @Test
  public void testReadWrite() throws MalformedURLException, IOException, EInvalidSongFile, JAXBException {
    System.out.println("testReadWrite");




    //-----------------------------------------------------------------------
    // Create s test candidate (a song with a mastertrack and a midi track).
    Song testCandidate = new Song();
    testCandidate.setName(SONGNAME);
    testCandidate.setDescription(DESC);

    MasterTrack masterTrack = testCandidate.createMastertrack();
    masterTrack.setName(MASTERTRACKNAME);

    MidiTrack midiTrackCandidate = new MidiTrack();
    masterTrack.addSubtrack(midiTrackCandidate);
    midiTrackCandidate.setName(MIDITRACKNAME);
    midiTrackCandidate.setMidiChannel(CHANNEL);
    midiTrackCandidate.setMidiTrackIndex(TRACKINDEX);
    // -------------------------------------------------------------------------
    // marshal the test candidate to a file
    File file = new File(tempDir, "song2.xml");
    FileOutputStream outstream = new FileOutputStream(file);
    testCandidate.marshal(outstream);
    System.out.println("... test candidate written to :" + file.getAbsolutePath());

    // -------------------------------------------------------------------------
    // unmarshal the test candidate from file and verify that it is
    // equal to the original
    Song songback = Song.createFromFile(file.toURI().toURL());
    MasterTrack masterTrackBack = songback.getMastertrack();
    MidiTrack midiTrackBack = (MidiTrack) masterTrackBack.getSubtracks()[0];

    assertEquals(midiTrackBack.getName(), MIDITRACKNAME);
    assertEquals(midiTrackBack.getMidiChannel(), CHANNEL);
    assertEquals(midiTrackBack.getMidiTrackIndex(), TRACKINDEX);

    assertEquals(midiTrackBack.getBaseDirectory(),
            FileUtil.toFileObject(FileUtil.normalizeFile(file.getParentFile())));

  }

  /**
   * Test the load Midi files function. To make things a little bit more
   * complicated, we use a midi file that is hidden in NetBeans file system
   * (thus the URL will look like "nbfs://nbhost//testmidi.mid", which is only
   * understood by the NetBeans utilities).
   *
   * @throws IOException
   * @throws EInvalidSongFile
   */
  @Test
  public void testLoadMidiFile() throws IOException, EInvalidSongFile, InvalidMidiDataException {
    System.out.println("testLoadFiles");

    // access the resource file
    URL midiTestUrl_1 = this.getClass().getResource("resources/sanctusVoices.mid");
    assertNotNull(midiTestUrl_1);
    FileObject midiTestFile_1 = URLMapper.findFileObject(midiTestUrl_1);
    assertNotNull(midiTestFile_1);
    //verifiy that we have the expected testfile
    Sequence sequence = MidiSystem.getSequence(midiTestUrl_1);
    Track[] tracks = sequence.getTracks();
    assertTrue("Test config error, wrong Midi file.", tracks.length > 4);
    assertTrue("Test config error, wrong Midi file.", tracks[4].ticks() > 90000);

    // copy the resource file to the NetBeans filesystem
    FileObject configRoot = FileUtil.getConfigRoot();
    assertNotNull(configRoot);
    FileObject midiTestFile_2 = FileUtil.copyFile(midiTestFile_1, configRoot, "testmidi");


    String midiPath = midiTestFile_2.getPath();
    System.out.println("... path to Midi file: \"" + midiPath + "\"");

    // create Song object whos Midifile points to this NetBeans file
    Song testSong = new Song();
    testSong.setName("TestLoadFiles");
    testSong.setBaseDirectory(configRoot);

    MasterTrack masterTrack = testSong.createMastertrack();
    masterTrack.setName(MASTERTRACKNAME);
    masterTrack.setSequencefile(midiPath);
    masterTrack.setMidiTrackIndex(0);




    // add a number of song tracks and link them to midi tracks
    // (these midi tracks must exist in the midi file).
    for (int i = 1; i < 5; i++) {
      MidiTrack subtrack = new MidiTrack();
      subtrack.setMidiTrackIndex(i);
      masterTrack.addSubtrack(subtrack);
    }

    // now when accessing the tracks, the Midi data should be
    // fetched from the given midifile
    MidiTrack songTrack = (MidiTrack) masterTrack.getSubtracks()[3];

    //... therefore check that track[4] has got the expected size.
    Track midiTrack = songTrack.getMidiTrack();
    assertNotNull(midiTrack);

    assertTrue(midiTrack.ticks() > 90000);
  }
}
