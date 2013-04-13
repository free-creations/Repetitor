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

import de.free_creations.microsequencer.AudioPort;
import de.free_creations.microsequencer.SequencerMidiPort;
import de.free_creations.microsequencer.SequencerPort;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
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
public class MidiSynthesizerTrackTest {

  private static final String SONGNAME = "Test Song";
  private static final String MASTERTRACKNAME = "Test Mastertrack";
  private static final String SYNTHESIZERTRACKNAME = "Test Synthesizertrack";
  private static final String DESC = "This is a test Song-instance";
  private static final int channel = 15;
  private static final int trackindex = 55;
  private static final String SYNTHNAME = "Test Synthesizer";

  public MidiSynthesizerTrackTest() {
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

  @After
  public void tearDown() {
  }

  /**
   * Verify that a Track object can be marshaled into an XML stream and
   * unmarshaled back to the original structure.
   *
   * @throws MalformedURLException
   * @throws IOException
   */
  @Test
  public void testReadWrite() throws MalformedURLException, IOException, EInvalidSongFile, JAXBException {
    System.out.println("testReadWrite");




    //-----------------------------------------------------------------------
    // Create s test candidate (a song with a mastertrack and a midi synthesizer track).
    Song testCandidate = new Song();
    testCandidate.setName(SONGNAME);
    testCandidate.setDescription(DESC);

    MasterTrack masterTrack = testCandidate.createMastertrack();
    masterTrack.setName(MASTERTRACKNAME);

    MidiSynthesizerTrack synthesizerTrackCandidate = new MidiSynthesizerTrack();
    masterTrack.addSubtrack(synthesizerTrackCandidate);
    synthesizerTrackCandidate.setName(SYNTHESIZERTRACKNAME);
    synthesizerTrackCandidate.setMidiChannel(channel);
    synthesizerTrackCandidate.setMidiTrackIndex(trackindex);

    BuiltinSynthesizer synth = new BuiltinSynthesizer();
    synth.setName(SYNTHNAME);
    synthesizerTrackCandidate.setSynthesizer(synth);

    // -------------------------------------------------------------------------
    // marshal the test candidate to a file
    File file = new File(tempDir, "song3.xml");
    FileOutputStream outstream = new FileOutputStream(file);
    testCandidate.marshal(outstream);
    System.out.println("... test candidate written to :" + file.getAbsolutePath());

    // -------------------------------------------------------------------------
    // unmarshal the test candidate from file and verify that it is
    // equal to the original
    Song songback = Song.createFromFile(file.toURI().toURL());
    MasterTrack masterTrackBack = songback.getMastertrack();
    MidiSynthesizerTrack synthesizerTrackBack = (MidiSynthesizerTrack) masterTrackBack.getSubtracks()[0];

    assertEquals(synthesizerTrackBack.getName(), SYNTHESIZERTRACKNAME);
    assertEquals(synthesizerTrackBack.getMidiChannel(), channel);
    assertEquals(synthesizerTrackBack.getMidiTrackIndex(), trackindex);

    //assertEquals(synthesizerTrackBack.getBaseDirectory().getURL(), file.getParentFile().toURI().toURL());
    assertEquals(synthesizerTrackBack.getBaseDirectory(),
            FileUtil.toFileObject(FileUtil.normalizeFile(file.getParentFile())));

    BuiltinSynthesizer synthBack = (BuiltinSynthesizer) synthesizerTrackBack.getSynthesizer();
    assertEquals(synthBack.getName(), SYNTHNAME);


  }

  /**
   * Test whether a soundbank referenced by the song's data is correctly loaded.
   * The point to be tested is that the {@link  Song} object knows about the base
   * directory whereas the {@link BuiltinSynthesizer} object knows about a
   * relative path to the soundbank file. When accessing the soundbank these two
   * informations must be correctly linked. To make things a little bit more
   * complicated, we use a midi file that is hidden in a NetBeans file system
   * (thus the URL will look like "nbfs://nbhost//testmidi.mid", which is only
   * understood by the NetBeans utilities).
   *
   * @throws IOException
   * @throws EInvalidSongFile
   */
  @Test
  public void testLoadSf2File() throws IOException, EInvalidSongFile, InvalidMidiDataException {
    System.out.println("testLoadFiles");

    // access the resource file
    URL sf2TestUrl = this.getClass().getResource("resources/Chorium.SF2");
    assertNotNull(sf2TestUrl);
    FileObject sf2TestFile = URLMapper.findFileObject(sf2TestUrl);
    assertNotNull(sf2TestFile);
    //verifiy that we have the expected testfile
    Soundbank soundBank = MidiSystem.getSoundbank(sf2TestUrl);
    String SOUNDBANKNAME = soundBank.getName();
    System.out.println("...Testing with soundbank: " + SOUNDBANKNAME);


    // copy the resource file to the NetBeans filesystem
    FileObject configRoot = FileUtil.getConfigRoot();
    assertNotNull(configRoot);
    FileObject sf2TestFile_2 = FileUtil.copyFile(sf2TestFile, configRoot, "testsf2");


    String sf2Path = sf2TestFile_2.getPath();
    System.out.println("... path to Midi file: \"" + sf2Path + "\"");

    // create Song object with a synthesizer who's Sf2file points to this NetBeans file
    Song testSong = new Song();
    testSong.setName("TestLoadFiles");
    testSong.setBaseDirectory(configRoot);

    MasterTrack masterTrack = testSong.createMastertrack();

    // add a number of song tracks and link them to midi tracks
    // (these midi tracks must exist in the midi file).
    MidiSynthesizerTrack synthesizerTrackCandidate = new MidiSynthesizerTrack();
    masterTrack.addSubtrack(synthesizerTrackCandidate);

    BuiltinSynthesizer synth = new BuiltinSynthesizer();
    synth.setSoundbankfile(sf2Path);
    synthesizerTrackCandidate.setSynthesizer(synth);



    // now when accessing the soundbank, the data should be
    // fetched from the given sf2 file
    Soundbank accessedBank = synth.getSoundbank();
    assertEquals(accessedBank.getName(), SOUNDBANKNAME);
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
    // Create s test candidate (a song with a mastertrack and a midi synthesizer track).
    Song originalSong = new Song();
    originalSong.setName(SONGNAME);
    originalSong.setDescription(DESC);

    MasterTrack masterTrack = originalSong.createMastertrack();
    masterTrack.setName(MASTERTRACKNAME);

    MidiSynthesizerTrack originalSynthTrack = new MidiSynthesizerTrack();
    masterTrack.addSubtrack(originalSynthTrack);
    originalSynthTrack.setName(SYNTHESIZERTRACKNAME);
    originalSynthTrack.setMidiChannel(channel);
    originalSynthTrack.setMidiTrackIndex(trackindex);

    BuiltinSynthesizer synth = new BuiltinSynthesizer();
    synth.setName(SYNTHNAME);
    originalSynthTrack.setSynthesizer(synth);
    // -------------------------------------------------------------------------
    // clone the song (which will also clone the synthesizer track).
    Song clonedSong = originalSong.clone();
    MidiSynthesizerTrack clonedSynthTrack = (MidiSynthesizerTrack) clonedSong.getMastertrack().getSubtracks()[0];
    // -------------------------------------------------------------------------
    // verify that the cloned Midi track has the same values than the original
    // but is an other object

    assertEquals(clonedSynthTrack.getName(), SYNTHESIZERTRACKNAME);
    assertEquals(clonedSynthTrack.getMidiChannel(), channel);
    assertEquals(clonedSynthTrack.getMidiTrackIndex(), trackindex);

    assertTrue(clonedSynthTrack != originalSynthTrack);

    // also the sythesizer should not point to the same object
    assertTrue(clonedSynthTrack.getSynthesizer() != originalSynthTrack.getSynthesizer());
    assertTrue(originalSynthTrack.getSynthesizer().getTrack() == originalSynthTrack);
    assertTrue(clonedSynthTrack.getSynthesizer().getTrack() == clonedSynthTrack);


  }

  /**
   * Test the method detachAudio of MidiTrack
   *
   * @throws MalformedURLException
   * @throws IOException
   * @throws EInvalidSongFile
   */
  @Test
  public void testDetach() throws MalformedURLException, IOException, EInvalidSongFile {
    System.out.println("testClone");
    //-----------------------------------------------------------------------
    // Create s test candidate (a song with a mastertrack and a midi synthesizer track).
    Song song = new Song();
    song.setName(SONGNAME);
    song.setDescription(DESC);

    MasterTrack masterTrack = song.createMastertrack();
    masterTrack.setName(MASTERTRACKNAME);

    MidiSynthesizerTrack synthTrack = new MidiSynthesizerTrack();
    masterTrack.addSubtrack(synthTrack);
    synthTrack.setName(SYNTHESIZERTRACKNAME);
    synthTrack.setMidiChannel(channel);
    synthTrack.setMidiTrackIndex(trackindex);

    BuiltinSynthesizer synth = new BuiltinSynthesizer();
    synth.setName(SYNTHNAME);
    synthTrack.setSynthesizer(synth);
    synth.setPort(new SequencerMidiPort() {
      @Override
      public AudioPort getAudioPort() {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public void setTracks(Track[] tracks) {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public Track[] getTracks() {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public void send(MidiMessage message, double streamTime) {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public void setMute(int trackIndex, boolean value) {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public void setMute(boolean value) {
        throw new UnsupportedOperationException("Not supported yet.");
      }

      @Override
      public void setAttenuation(int trackIndex, float value) {
        throw new UnsupportedOperationException("Not supported yet.");
      }
    });
    // -------------------------------------------------------------------------
    // now call detachAudio on the song => we expect the port on the sysnth to have disapeard
    song.detachAudio();

    assertNull(synth.getPort());



  }
}
