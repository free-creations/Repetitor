/*
 * Copyright 2013 Harald Postner.
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
package de.free_creations.microsequencer;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Harald Postner
 */
public class AudioRecorderSubSequencerTest {

  public AudioRecorderSubSequencerTest() {
  }

  @BeforeClass
  public static void setUpClass() {
  }

  @AfterClass
  public static void tearDownClass() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testConstructor() throws Exception {
    System.out.println("testConstructor");
    AudioRecorderSubSequencer instance = new AudioRecorderSubSequencer();
    System.out.println("...TempDir:" + instance.getTempDir());
  }

  /**
   * Test of openOut method, of class AudioRecorderSubSequencer.
   */
  @Test
  @Ignore("OpenOut is empty")
  public void testOpenOut() throws Exception {
  }

  /**
   * Test of closeOut method, of class AudioRecorderSubSequencer.
   */
  @Test
  public void testCloseOut() {
    System.out.println("closeOut");

    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of startOut method, of class AudioRecorderSubSequencer.
   */
  @Test
  @Ignore("method is empty")
  public void testStartOut() {
  }

  /**
   * Test of stopOut method, of class AudioRecorderSubSequencer.
   */
  @Test
  @Ignore("method is empty")
  public void testStopOut() {
  }

  /**
   * Test of processOut method, of class AudioRecorderSubSequencer.
   * Specification: If the current mode of operations is "Replay", read samples
   * from the temp file.
   */
  @Test
  public void testProcessOut() throws Exception {
    System.out.println("processOut");
    double streamTime = 0.0;
    AudioRecorderSubSequencer instance = new AudioRecorderSubSequencer();
    float[] expResult = null;
    float[] result = instance.processOut(streamTime);

    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of openIn method, of class AudioRecorderSubSequencer.
   */
  @Test
  @Ignore("method is empty")
  public void testOpenIn() throws Exception {
    System.out.println("openIn");
    int samplingRate = 0;
    int nFrames = 0;
    int inputChannelCount = 0;
    boolean noninterleaved = false;
    AudioRecorderSubSequencer instance = new AudioRecorderSubSequencer();
    instance.openIn(samplingRate, nFrames, inputChannelCount, noninterleaved);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of closeIn method, of class AudioRecorderSubSequencer. Specification:
   * remove the temporary the temporary output file. If during ProcessIn there
   * was an error, throw the exception here.
   */
  @Test
  public void testCloseIn() {
    System.out.println("closeIn");

    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of startIn method, of class AudioRecorderSubSequencer.
   */
  @Test
  @Ignore("method is empty")
  public void testStartIn() {
  }

  /**
   * Test of stopIn method, of class AudioRecorderSubSequencer.
   */
  @Test
  @Ignore("method is empty")
  public void testStopIn() {
  }

  /**
   * Test of processIn method, of class AudioRecorderSubSequencer.
   * Specification: if the current mode of operations is "RecordAudio", write
   * the given samples to the temp file.
   */
  @Test
  public void testProcessIn() throws Exception {
    System.out.println("processIn");
    double streamTime = 0.0;
    float[] samples = null;
    AudioRecorderSubSequencer instance = new AudioRecorderSubSequencer();
    instance.processIn(streamTime, samples);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of prepareSession method, of class AudioRecorderSubSequencer.
   * Specification: Depending on the PlayingMode start the appropriate
   * file-streamer.
   */
  @Test
  public void testPrepareSession() {
    System.out.println("prepareSession");

    fail("The test case is a prototype.");
  }

  /**
   * Test of stopSession method, of class AudioRecorderSubSequencer.
   *
   * Specification:
   */
  @Test
  public void testStopSession() {
    System.out.println("stopSession");

    fail("The test case is a prototype.");
  }

  /**
   * Test of getTempDir method, of class AudioRecorderSubSequencer.
   */
  @Test
  @Ignore("method is trivial")
  public void testGetTempDir() {
  }
}
