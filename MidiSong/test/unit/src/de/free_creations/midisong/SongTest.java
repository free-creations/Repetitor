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

import org.openide.filesystems.FileUtil;
import java.io.FileNotFoundException;
import java.beans.PropertyVetoException;
import java.net.URISyntaxException;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.MalformedURLException;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import javax.xml.transform.Result;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.JAXBContext;
import java.io.IOException;
import javax.sound.midi.Soundbank;
import javax.xml.bind.JAXBException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class SongTest {

  public SongTest() {
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
   * Verify that a song object can be marshaled into an XML stream and
   * un-marshaled back to the original object.
   *
   * @throws MalformedURLException
   * @throws IOException
   */
  @Test
  public void testReadWrite() throws MalformedURLException, IOException, EInvalidSongFile, JAXBException {
    System.out.println("testReadWrite");

    final String SONGNAME = "Test Song";
    final String MASTERTRACKNAME = "Test Mastertrack";
    final String DESC = "This is a test Song-instance";


    //-----------------------------------------------------------------------
    // Create s test candidate (a song with only a mastertrack).
    Song testCandidate = new Song();
    testCandidate.setName(SONGNAME);
    testCandidate.setDescription(DESC);

    MasterTrack masterTrack = testCandidate.createMastertrack();
    masterTrack.setName(MASTERTRACKNAME);
    // -------------------------------------------------------------------------
    // marshal the test candidate to a file
    File file = new File(tempDir, "song.xml");
    FileOutputStream outstream = new FileOutputStream(file);
    testCandidate.marshal(outstream);
    System.out.println("... test candidate written to :" + file.getAbsolutePath());

    // -------------------------------------------------------------------------
    // unmarshal the test candidate from file and verify that it is
    // equal to the original
    Song songback = Song.createFromFile(file.toURI().toURL());

    assertEquals(songback.getName(), SONGNAME);
    assertEquals(songback.getDescription(), DESC);
    MasterTrack masterTrackBack = songback.getMastertrack();
    assertEquals(masterTrackBack.getName(), MASTERTRACKNAME);

    assertEquals(masterTrackBack.getBaseDirectory(),
            FileUtil.toFileObject(FileUtil.normalizeFile(file.getParentFile())));

  }

  /**
   * Test the clone method.
   *
   * @throws MalformedURLException
   * @throws IOException
   * @throws EInvalidSongFile
   */
  @Test
  public void testClone() throws MalformedURLException, IOException, EInvalidSongFile {
    System.out.println("testClone");

    final String SONGNAME = "Test Song";
    final String MASTERTRACKNAME = "Test Mastertrack";
    final String DESC = "This is a test Song-instance";


    //-----------------------------------------------------------------------
    // Create s test candidate 
    Song original = new Song();
    original.setName(SONGNAME);
    original.setDescription(DESC);

    MasterTrack masterTrack = original.createMastertrack();
    masterTrack.setName(MASTERTRACKNAME);

    // -------------------------------------------------------------------------
    // clone the test candidate
    Song clonedCandidate = original.clone();

    //--------------------------------------------------------------------------
    // verify that the clone has the same values than the original
    assertEquals(SONGNAME, clonedCandidate.getName());
    assertEquals(DESC, clonedCandidate.getDescription());

    //but referenced objects should themselves be clones and not just refereces to the original object
    assertTrue(clonedCandidate.getMastertrack() != original.getMastertrack());
    assertEquals(MASTERTRACKNAME, clonedCandidate.getMastertrack().getName());

    //--------------------------------------------------------------------------
    // verify that we can change the clone without changing the original.
    clonedCandidate.setName("BlaBla");
    assertEquals("BlaBla", clonedCandidate.getName());
    assertEquals(SONGNAME, original.getName());
  }

  /**
   * What happens when the song file cannot be read? We expect an exception to
   * be thrown.
   */
  @Test(expected = EInvalidSongFile.class)
  public void testBadSongFile() throws EInvalidSongFile {
    System.out.println("testBadSongFile");
    InputStream stream = this.getClass().getResourceAsStream("resources/badSong.xml");
    Song.unmarshal(stream);
  }

  /**
   * In the resources we have a hand-carved song file called "sanctusAll.xml" .
   * We are going to read this file and we will check whether the resultant song
   * object is as expected.
   */
  @Test
  public void testCreateFromFile() throws EInvalidSongFile {
    System.out.println("testReadSongFile");
    //read the song from file
    URL xmlFileUrl = this.getClass().getResource("resources/sanctusAll.xml");
    Song song = Song.createFromFile(xmlFileUrl);

    //make sure we have got the expected song
    assertEquals(song.getName(), "Sanctus");

    //climb down the hierarchy and verify that all elements are at their expected position
    MasterTrack mastertrack = song.getMastertrack();
    assertEquals(mastertrack.getName(), "Mastertrack");
    MidiSynthesizerTrack synthTrack0 = (MidiSynthesizerTrack) mastertrack.getSubtracks()[0];
    MidiSynthesizerTrack synthTrack1 = (MidiSynthesizerTrack) mastertrack.getSubtracks()[1];

    assertEquals(synthTrack0.getName(), "Orchestra");
    assertEquals(synthTrack1.getName(), "Voices");

    BuiltinSynthesizer synth0 = (BuiltinSynthesizer) synthTrack0.getSynthesizer();
    BuiltinSynthesizer synth1 = (BuiltinSynthesizer) synthTrack1.getSynthesizer();
    Soundbank sb0 = synth0.getSoundbank();
    Soundbank sb1 = synth1.getSoundbank();
    assertEquals(sb0.getName(), "Chorium by openwrld");
    assertEquals(sb1.getName(), "Piano");
  }

  /**
   * Check if a song that is set immutable, cannot be changed by accident. We
   * are again using the hand-carved song file called "sanctusAll.xml" .
   */
  @Test(expected = EIllegalUpdate.class)
  public void testImmutableSong() throws EInvalidSongFile {
    System.out.println("testImmutableSong");
    //read the song from file
    URL xmlFileUrl = this.getClass().getResource("resources/sanctusAll.xml");
    Song song = Song.createFromFile(xmlFileUrl);

    //make sure we have got the expected song
    assertEquals(song.getName(), "Sanctus");

    //climb down the hierarchy and verify that all elements are mutable.
    MasterTrack mastertrack = song.getMastertrack();
    assertFalse(mastertrack.isImmutable());
    MidiSynthesizerTrack synthTrack0 = (MidiSynthesizerTrack) mastertrack.getSubtracks()[0];
    assertFalse(synthTrack0.isImmutable());
    MidiSynthesizerTrack synthTrack1 = (MidiSynthesizerTrack) mastertrack.getSubtracks()[1];
    assertFalse(synthTrack1.isImmutable());
    BuiltinSynthesizer synth0 = (BuiltinSynthesizer) synthTrack0.getSynthesizer();
    assertFalse(synth0.isImmutable());
    BuiltinSynthesizer synth1 = (BuiltinSynthesizer) synthTrack1.getSynthesizer();
    assertFalse(synth1.isImmutable());

    //set the song to immutable and verify this info is transmitted down the hierarchy
    song.setImmutable(true);
    assertTrue(synth1.isImmutable());

    //try to change somthing on an immutable song and expect an exception
    synth1.setSoundbankfile("XXX");

  }

  /**
   * This is not really a test. Here we let the JAXB system produce a schema. If
   * you need a schema of the Song file you may paste and copy from the file
   * that's announced in the Output.
   *
   * @throws JAXBException
   * @throws IOException
   */
  @Test
  public void generateSchema() throws JAXBException, IOException {
    System.out.println("generateSchema");

    JAXBContext jaxbContext = JAXBContext.newInstance(BuiltinSynthesizer.class,
            SynthesizerData.class,
            MidiSynthesizerTrack.class,
            MasterTrack.class,
            MidiTrack.class,
            GenericTrack.class,
            Song.class);

    SchemaOutputResolver sor = new SchemaOutputResolver() {
      @Override
      public Result createOutput(String namespaceUri, String suggestedFileName) throws MalformedURLException {
        File file = new File(tempDir, suggestedFileName);
        System.out.println("... Schema-file written to :" + file.getAbsolutePath());
        StreamResult result = new StreamResult(file);
        result.setSystemId(file.toURI().toURL().toString());
        return result;

      }
    };

    jaxbContext.generateSchema(sor);
  }
}
