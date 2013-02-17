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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Harald Postner
 */
public class AudioReaderTest {

  File testDir = new File("tmp");

  public AudioReaderTest() {
  }

  @Before
  public void setUp() {
    assertTrue("Please create a directory named " + testDir.getAbsolutePath(), testDir.exists());
    assertTrue("Uups " + testDir.getAbsolutePath() + " is not a directory.", testDir.isDirectory());
  }

  /**
   * Test of close method, of class AudioReader.
   */
  @Test
  @Ignore("Tested in TestReadXXX procedures")
  public void testClose() throws Exception {
    //Default call inserted by NetBeans-wizzard (do not delete to avoid regeneration)
  }

  /**
   * Test of getNext method, of class AudioReader.
   */
  @Test
  @Ignore("Tested in TestReadXXX procedures")
  public void testGetNext() throws FileNotFoundException, IOException {
    //Default call inserted by NetBeans-wizzard (do not delete to avoid regeneration)
  }

  @Test
  public void TestEmpty() throws FileNotFoundException, IOException {
    File testFile = new File(testDir, "TestEmpty.tmp");

    ReaderTest(testFile, 0, 0, 512, 4 * 512);
    testFile.delete();

  }

  private void ReaderTest(
          File file,
          int floats,
          int extraBytes,
          int audioArraySize,
          int fileBufferSize) throws FileNotFoundException, IOException {

    makeTestFile(file, floats, extraBytes);

    AudioReader audioReader = new AudioReader(file, fileBufferSize);

    long expectedAudioArrays = (floats / audioArraySize) + 1;
    long audioArraysCount = 0;
    long floatCount = 0;

    float[] audioArray = new float[audioArraySize];
    boolean more = true;
    while (more) {
      Arrays.fill(audioArray, 123F);
      audioReader.waitForBufferReady(); // do this only when testing!
      more = audioReader.getNext(audioArray);
      audioArraysCount++;
      assertTrue(audioArraysCount <= expectedAudioArrays);
      for (float sample : audioArray) {
        if (floatCount < floats) {
          assertEquals(floatCount, (long) sample);
          floatCount++;
        } else {
          assertEquals(0F, sample, 0F);
        }
      }
    }


  }

  private void makeTestFile(File file, int floats, int extraBytes) throws FileNotFoundException, IOException {
    assertTrue(extraBytes <= 4);
    FileOutputStream outFile = new FileOutputStream(file);
    try (FileChannel outChannel = outFile.getChannel()) {
      java.nio.ByteBuffer byteBuffer = java.nio.ByteBuffer.allocateDirect(AudioWriter.bytesPerFloat).order(ByteOrder.LITTLE_ENDIAN);

      for (int i = 0; i < floats; i++) {
        float nextFloat = (float) i;
        byteBuffer.clear();
        byteBuffer.putFloat(nextFloat);
        byteBuffer.flip();
        outChannel.write(byteBuffer);
      }
      byteBuffer.clear();
      byteBuffer.limit(extraBytes);
      outChannel.write(byteBuffer);
      outChannel.close();
    }
  }
}
