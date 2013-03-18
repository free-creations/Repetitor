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

  //File testDir = new File("tmp");
  File testDir = new File("/home/harald/rubbish");

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
    int fileSizeFloat = 0;
    int extraBytes = 0;
    int audioArraySizeFloat = 512;
    int fileBufferSizeByte = 4 * audioArraySizeFloat * AudioReader.bytesPerFloat;

    ReaderTest(testFile, fileSizeFloat, extraBytes, audioArraySizeFloat, fileBufferSizeByte);
    testFile.delete();

  }

  @Test
  public void TestEmptyExtraBytes() throws FileNotFoundException, IOException {
    File testFile = new File(testDir, "TestEmptyExtraBytes.tmp");
    int fileSizeFloat = 0;
    int extraBytes = 3;
    int audioArraySizeFloat = 512;
    int fileBufferSizeByte = 4 * audioArraySizeFloat * AudioReader.bytesPerFloat;

    ReaderTest(testFile, fileSizeFloat, extraBytes, audioArraySizeFloat, fileBufferSizeByte);
    testFile.delete();

  }

  @Test
  public void TestOneFloat() throws FileNotFoundException, IOException {
    File testFile = new File(testDir, "TestEmptyExtraBytes.tmp");
    int fileSizeFloat = 1;
    int extraBytes = 0;
    int audioArraySizeFloat = 512;
    int fileBufferSizeByte = 4 * audioArraySizeFloat * AudioReader.bytesPerFloat;

    ReaderTest(testFile, fileSizeFloat, extraBytes, audioArraySizeFloat, fileBufferSizeByte);
    testFile.delete();

  }

  @Test
  public void TestOneFloatExtraBytes() throws FileNotFoundException, IOException {
    File testFile = new File(testDir, "TestOneFloatExtraBytes.tmp");
    int fileSizeFloat = 1;
    int extraBytes = 3;
    int audioArraySizeFloat = 512;
    int fileBufferSizeByte = 4 * audioArraySizeFloat * AudioReader.bytesPerFloat;

    ReaderTest(testFile, fileSizeFloat, extraBytes, audioArraySizeFloat, fileBufferSizeByte);
    testFile.delete();

  }

  @Test
  public void TestOneBlock() throws FileNotFoundException, IOException {
    File testFile = new File(testDir, "TestOneBlock.tmp");
    int audioArraySizeFloat = 512;
    int blockSizeFloat = 4 * audioArraySizeFloat;
    int fileSizeFloat = 1 * blockSizeFloat;
    int extraBytes = 0;

    int fileBufferSizeByte = blockSizeFloat * AudioReader.bytesPerFloat;

    ReaderTest(testFile, fileSizeFloat, extraBytes, audioArraySizeFloat, fileBufferSizeByte);
    testFile.delete();

  }

  @Test
  public void TestTwoBlocks() throws FileNotFoundException, IOException {
    File testFile = new File(testDir, "TestTwoBlocks.tmp");
    int audioArraySizeFloat = 512;
    int blockSizeFloat = 4 * audioArraySizeFloat;
    int fileSizeFloat = 2 * blockSizeFloat;
    int extraBytes = 0;

    int fileBufferSizeByte = blockSizeFloat * AudioReader.bytesPerFloat;

    ReaderTest(testFile, fileSizeFloat, extraBytes, audioArraySizeFloat, fileBufferSizeByte);
    testFile.delete();

  }

  @Test
  public void TestThreeBlocks() throws FileNotFoundException, IOException {
    File testFile = new File(testDir, "TestThreeBlocks.tmp");
    int audioArraySizeFloat = 512;
    int blockSizeFloat = 4 * audioArraySizeFloat;
    int fileSizeFloat = 3 * blockSizeFloat;
    int extraBytes = 0;

    int fileBufferSizeByte = blockSizeFloat * AudioReader.bytesPerFloat;

    ReaderTest(testFile, fileSizeFloat, extraBytes, audioArraySizeFloat, fileBufferSizeByte);
    testFile.delete();

  }

  @Test
  public void TestThreeBlocksExtraBytes() throws FileNotFoundException, IOException {
    File testFile = new File(testDir, "TestThreeBlocksExtra.tmp");
    int audioArraySizeFloat = 512;
    int blockSizeFloat = 4 * audioArraySizeFloat;
    int fileSizeFloat = 3 * blockSizeFloat;
    int extraBytes = 1;

    int fileBufferSizeByte = blockSizeFloat * AudioReader.bytesPerFloat;

    ReaderTest(testFile, fileSizeFloat, extraBytes, audioArraySizeFloat, fileBufferSizeByte);
    testFile.delete();

  }

  @Test
  public void TestTransitionBufferLast() throws FileNotFoundException, IOException {

    // set the sizes in such a way as to use the TransitionBuffer for the last read
    File testFile = new File(testDir, "TestTransitionBufferLast.tmp");
    int blockSizeFloat = 250;
    int blockSizeByte = blockSizeFloat * AudioReader.bytesPerFloat;

    int audioArraySizeFloat = blockSizeFloat;
    int fileBufferSizeByte = (blockSizeByte * 2) + 1;
    int fileSizeFloat = blockSizeFloat * 3;
    int extraBytes = 0;

    ReaderTest(testFile, fileSizeFloat, extraBytes, audioArraySizeFloat, fileBufferSizeByte);
    testFile.delete();

  }

  @Test
  public void TestTransitionBufferButNotLast() throws FileNotFoundException, IOException {

    // set the sizes in such a way as to use the TransitionBuffer, but not for the last read.
    File testFile = new File(testDir, "TestTransitionBufferNotLast.tmp");
    int blockSizeFloat = 250;
    int blockSizeByte = blockSizeFloat * AudioReader.bytesPerFloat;

    int audioArraySizeFloat = blockSizeFloat;
    int fileBufferSizeByte = (blockSizeByte * 2) + 1;
    int fileSizeFloat = blockSizeFloat * 4;
    int extraBytes = 0;

    ReaderTest(testFile, fileSizeFloat, extraBytes, audioArraySizeFloat, fileBufferSizeByte);
    testFile.delete();

  }

  @Test
  @Ignore
  public void TestPrimeNumbers() throws FileNotFoundException, IOException {

    // set all sizes to prime numbers so that all buffers are interleaved.
    // prime numbers can be found with http://easycalculation.com/prime-number.php
    File testFile = new File(testDir, "TestPrimeNumbers.tmp");
    int audioArraySizeFloat = 257;
    int fileBufferSizeByte = 2063;
    int fileSizeFloat = 20639;
    int extraBytes = 1;

    ReaderTest(testFile, fileSizeFloat, extraBytes, audioArraySizeFloat, fileBufferSizeByte);
    testFile.delete();

  }

  private void ReaderTest(
          File file,
          int fileSizeFloat,
          int extraBytes,
          int audioArraySize,
          int fileBufferSizeByte) throws FileNotFoundException, IOException {

    makeTestFile(file, fileSizeFloat, extraBytes);

    AudioReader audioReader = new AudioReader(file, 0, fileBufferSizeByte);

    long expectedAudioArrays = (fileSizeFloat / audioArraySize) + 1;
    long audioArraysCount = 0;
    long floatCount = 0;

    float[] audioArray = new float[audioArraySize];
    boolean more = true;
    while (more) {
      Arrays.fill(audioArray, 123F);
      audioReader.waitForNextBufferReady(); // do this only when testing!
      more = audioReader.getNext(audioArray);
      audioArraysCount++;
      assertTrue(audioArraysCount <= expectedAudioArrays);
      for (float sample : audioArray) {
        if (floatCount < fileSizeFloat) {
          assertEquals(floatCount, (long) sample);
          floatCount++;
        } else {
          assertEquals(0F, sample, 0F);
        }
      }
    }
    //
    Arrays.fill(audioArray, 123F);
    more = audioReader.getNext(audioArray);
    assertFalse(more);
    for (float sample : audioArray) {
      assertEquals(0F, sample, 0F);
    }

    audioReader.close();
    //
    Arrays.fill(audioArray, 123F);
    more = audioReader.getNext(audioArray);
    assertFalse(more);
    for (float sample : audioArray) {
      assertEquals(0F, sample, 0F);
    }
  }

  private void makeTestFile(File file, int fileSizeFloat, int extraBytes) throws FileNotFoundException, IOException {
    assertTrue(extraBytes <= 4);
    FileOutputStream outFile = new FileOutputStream(file);
    try (FileChannel outChannel = outFile.getChannel()) {
      java.nio.ByteBuffer byteBuffer = java.nio.ByteBuffer.allocateDirect(AudioWriter.bytesPerFloat).order(ByteOrder.LITTLE_ENDIAN);

      for (int i = 0; i < fileSizeFloat; i++) {
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
