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

import de.free_creations.microsequencer.MasterSequencer.PlayingMode;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

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

  /**
   * Test of preparePlaying method, of class AudioRecorderSubSequencer.
   */
  @Test
  public void testPreparePlaying() {
    System.out.println("preparePlaying");
    double startTick = 0.0;
    PlayingMode mode = null;
    AudioRecorderSubSequencer instance = new AudioRecorderSubSequencer();
    instance.preparePlaying(startTick, mode);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of stopPlaying method, of class AudioRecorderSubSequencer.
   */
  @Test
  public void testStopPlaying() {
    System.out.println("stopPlaying");
    AudioRecorderSubSequencer instance = new AudioRecorderSubSequencer();
    instance.stopPlaying();
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of openOut method, of class AudioRecorderSubSequencer.
   */
  @Test
  public void testOpenOut() throws Exception {
    System.out.println("openOut");
    int samplingRate = 0;
    int nFrames = 0;
    int outputChannelCount = 0;
    boolean noninterleaved = false;
    AudioRecorderSubSequencer instance = new AudioRecorderSubSequencer();
    instance.openOut(samplingRate, nFrames, outputChannelCount, noninterleaved);
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of closeOut method, of class AudioRecorderSubSequencer.
   */
  @Test
  public void testCloseOut() {
    System.out.println("closeOut");
    AudioRecorderSubSequencer instance = new AudioRecorderSubSequencer();
    instance.closeOut();
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of startOut method, of class AudioRecorderSubSequencer.
   */
  @Test
  public void testStartOut() {
    System.out.println("startOut");
    AudioRecorderSubSequencer instance = new AudioRecorderSubSequencer();
    instance.startOut();
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of stopOut method, of class AudioRecorderSubSequencer.
   */
  @Test
  public void testStopOut() {
    System.out.println("stopOut");
    AudioRecorderSubSequencer instance = new AudioRecorderSubSequencer();
    instance.stopOut();
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of processOut method, of class AudioRecorderSubSequencer.
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
   * Test of closeIn method, of class AudioRecorderSubSequencer.
   */
  @Test
  public void testCloseIn() {
    System.out.println("closeIn");
    AudioRecorderSubSequencer instance = new AudioRecorderSubSequencer();
    instance.closeIn();
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of startIn method, of class AudioRecorderSubSequencer.
   */
  @Test
  public void testStartIn() {
    System.out.println("startIn");
    AudioRecorderSubSequencer instance = new AudioRecorderSubSequencer();
    instance.startIn();
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of stopIn method, of class AudioRecorderSubSequencer.
   */
  @Test
  public void testStopIn() {
    System.out.println("stopIn");
    AudioRecorderSubSequencer instance = new AudioRecorderSubSequencer();
    instance.stopIn();
    // TODO review the generated test code and remove the default call to fail.
    fail("The test case is a prototype.");
  }

  /**
   * Test of processIn method, of class AudioRecorderSubSequencer.
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
}
