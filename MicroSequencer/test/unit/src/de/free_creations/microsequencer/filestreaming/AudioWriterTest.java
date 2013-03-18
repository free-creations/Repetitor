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
package de.free_creations.microsequencer.filestreaming;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Harald Postner
 */
public class AudioWriterTest {

  public AudioWriterTest() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of close method, of class AudioWriter.
   */
  @Test
  @Ignore
  public void testClose() throws Exception {
  }

  /**
   * Test of putNext method, of class AudioWriter. In this version, the file
   * buffer size is not a multiple of the of the audio array, thus the file
   * buffers can not be used entirely up to the end.
   */
  @Test
  public void testPutNext() throws FileNotFoundException, IOException, ExecutionException {
    System.out.printf("testPutNext() %n");
    File testDir = new File("tmp");
    assertTrue("Please create a directory named " + testDir.getAbsolutePath(), testDir.exists());
    assertTrue("Uups " + testDir.getAbsolutePath() + " is not a directory.", testDir.isDirectory());

    File outFile = new File(testDir, "testPutNext.test");
    if (outFile.exists()) {
      outFile.delete();
    }

    int floatCount = 0;
    float[] audioArray = new float[503];

    //un matched cache
    AudioWriter audioWriter = new AudioWriter(outFile, 0, 5003);
    for (int i = 0; i < 31; i++) {
      for (int j = 0; j < audioArray.length; j++) {
        audioArray[j] = floatCount;
        floatCount++;
      }
      audioWriter.waitForBufferReady();
      audioWriter.putNext(audioArray);
    }
    audioWriter.close();
    long bytesWritten = floatCount * AudioWriter.bytesPerFloat;
    System.out.printf("...Bytes written: %d %n", floatCount * AudioWriter.bytesPerFloat);

    assertTrue(outFile.exists());
    assertEquals(bytesWritten, outFile.length());
    outFile.delete();
  }

  /**
   * Test of putNext method, of class AudioWriter. In this version, the file
   * buffer is a multiple of the of the audio array, thus the file buffers are
   * used entirely up to the end.
   */
  @Test
  public void testPutNext_1() throws FileNotFoundException, IOException, ExecutionException {
    System.out.printf("testPutNext_1() %n");
    File testDir = new File("tmp");
    assertTrue("Uups " + testDir.getAbsolutePath() + " is not a directory.", testDir.isDirectory());

    assertTrue("Please create a directory named " + testDir.getAbsolutePath(), testDir.exists());

    File outFile = new File(testDir, "testPutNext_1.test");
    if (outFile.exists()) {
      outFile.delete();
    }

    int floatCount = 0;
    float[] audioArray = new float[512];

    //matched cache
    int fileBufferSize = 2 * AudioWriter.bytesPerFloat * audioArray.length;
    AudioWriter audioWriter = new AudioWriter(outFile, 0, fileBufferSize);
    for (int i = 0; i < 32; i++) {
      for (int j = 0; j < audioArray.length; j++) {
        audioArray[j] = floatCount;
        floatCount++;
      }
      audioWriter.waitForBufferReady();
      audioWriter.putNext(audioArray);
    }
    audioWriter.close();
    long bytesWritten = floatCount * AudioWriter.bytesPerFloat;
    System.out.printf("...Bytes written: %d %n", floatCount * AudioWriter.bytesPerFloat);

    assertTrue(outFile.exists());
    assertEquals(bytesWritten, outFile.length());
    outFile.delete();
  }

  /**
   */
  @Test
  @Ignore("Switch this on if you have can provide a very small partion to perform this test")
  public void testDiskFull() throws FileNotFoundException, ExecutionException {
    System.out.printf("testDiskFull %n");
    File testDir = new File("/media/MEDIONSTICK");
    assertTrue("Please provide some media with restriced space", testDir.exists());
    assertTrue("Uups " + testDir.getAbsolutePath() + " is not a directory.", testDir.isDirectory());
    long usableSpace = testDir.getUsableSpace();
    //make sure we are not going to write more than half a gigabyte
    assertTrue("There is " + usableSpace + " free space, this test will take too long.",
            usableSpace < 512 * 1024 * 1024);


    File outFile = new File(testDir, "testDiskFull.test");
    if (outFile.exists()) {
      outFile.delete();
    }

    int floatCount = 0;
    float[] audioArray = new float[2048];

    long buffersToWrite = (2 * usableSpace) / audioArray.length;



    AudioWriter audioWriter = new AudioWriter(outFile, 0);
    for (long i = 0; i < buffersToWrite; i++) {
      for (int j = 0; j < audioArray.length; j++) {
        audioArray[j] = floatCount;
        floatCount++;
      }
      audioWriter.waitForBufferReady();
      audioWriter.putNext(audioArray);
    }
    long bytesWritten = floatCount * AudioWriter.bytesPerFloat;

    System.out.printf("...Attempted to write %d Bytes %n", bytesWritten);
    boolean thrown = false;
    try {
      audioWriter.close();
    } catch (IOException ex) {
      thrown = true;
      System.out.printf("...Exception message: %s%n", ex.getMessage());

    }
    outFile.delete();
    assertTrue(thrown);
  }

  /**
   */
  @Test
  @Ignore("Switch this on if you have can provide a read only partion to perform this test")
  public void testDiskReadOnly() {
    System.out.printf("testDiskReadOnly %n");
    File testDir = new File("/media/MEDIONSTICK");
    assertTrue("Please provide a read only media", testDir.exists());
    assertTrue("Uups " + testDir.getAbsolutePath() + " is not a directory.", testDir.isDirectory());
    assertFalse("Uups " + testDir.getAbsolutePath() + " is not read-only.", testDir.canWrite());


    File outFile = new File(testDir, "testDiskReadOnly.test");
    boolean thrown = false;
    try {
      AudioWriter audioWriter = new AudioWriter(outFile, 0);
    } catch (FileNotFoundException ex) {
      thrown = true;
    }
    assertTrue(thrown);

  }
}
