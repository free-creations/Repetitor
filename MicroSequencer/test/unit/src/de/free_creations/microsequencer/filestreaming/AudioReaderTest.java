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

import de.free_creations.microsequencer.filestreaming.AudioWriter.WriterResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Harald Postner
 */
public class AudioReaderTest {

  private static File testDir;
  private static final ExecutorService executor = Executors.newSingleThreadExecutor(
          new ThreadFactory() {
    @Override
    public Thread newThread(Runnable r) {
      Thread thread = new Thread(r);
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.setDaemon(true);
      thread.setName("AudioReaderTestBackgroundThread");
      return thread;
    }
  });

  public AudioReaderTest() {
  }

  @BeforeClass
  public static void setUp() throws IOException {
    testDir = Files.createTempDirectory("Test").toFile();
    assertTrue("Please create a directory named " + testDir.getAbsolutePath(), testDir.exists());
    assertTrue("Uups " + testDir.getAbsolutePath() + " is not a directory.", testDir.isDirectory());
    System.out.println("Test files will be written to:" + testDir.getAbsolutePath());
  }

  @AfterClass
  public static void tearDown() {
    executor.shutdown();
  }

  /**
   * Test of start method, of class AudioReader.
   */
  @Test
  @Ignore("Tested in TestReadXXX procedures")
  public void testStart() {
  }

  /**
   * Test of getNext method, of class AudioReader.
   */
  @Test
  @Ignore("Tested in TestReadXXX procedures")
  public void testGetNext() {
  }

  /**
   * Test of stop method, of class AudioReader.
   */
  @Test
  @Ignore("Tested in TestReadXXX procedures")
  public void testStop() {
  }

  /**
   * Verify that the AudioReader correctly processes an empty input.
   *
   * Specification:
   *
   * <p>1) If the AudioReader is not started it shall return null-samples.</p>
   *
   * <p>2) If the AudioReader has processed all input samples it shall return
   * null-samples.</p>
   *
   * <p>3) If the AudioReader has been stopped it shall return null-samples.</p>
   *
   */
  @Test
  public void testEmpty() {
    System.out.println("testEmpty");
    int size = 0;
    int audioArraySize = 1024;
    float[] audioArray = new float[audioArraySize];

    FloatBuffer firstBuffer = prepareFirstBuffer(size);
    AudioReader audioReader = new AudioReader(executor);

    // 1) check the not started case
    Arrays.fill(audioArray, 123F);
    audioReader.getNext(12345, audioArray);
    for (float sample : audioArray) {
      assertEquals(0F, sample, 0F);
    }
    // 2) check the file ended case
    Arrays.fill(audioArray, 123F);
    audioReader.start(firstBuffer, null, 0);
    audioReader.getNext(0, audioArray);
    for (float sample : audioArray) {
      assertEquals(0F, sample, 0F);
    }
    // 3) check the stopped case
    Arrays.fill(audioArray, 123F);
    audioReader.stop();
    audioReader.getNext(0, audioArray);
    for (float sample : audioArray) {
      assertEquals(0F, sample, 0F);
    }

    audioReader.close();
  }

  /**
   * Verify that the AudioReader correctly processes an empty input.
   *
   * Specification:
   *
   * <p>1) If the AudioReader is not started it shall return null-samples.</p>
   *
   * <p>2) If the AudioReader has processed all input samples it shall return
   * null-samples.</p>
   *
   * <p>3) If the AudioReader has been stopped it shall return null-samples.</p>
   *
   */
  @Test
  public void testEmpty2() throws IOException {
    System.out.println("testEmpty2");

    int audioArraySize = 1024;
    float[] audioArray = new float[audioArraySize];


    AudioReader audioReader = new AudioReader(executor);

    // 1) check the not started case
    Arrays.fill(audioArray, 123F);
    audioReader.getNext(12345, audioArray);
    for (float sample : audioArray) {
      assertEquals(0F, sample, 0F);
    }
    // 2) check the file ended case
    Arrays.fill(audioArray, 123F);
    audioReader.start(null);
    audioReader.getNext(0, audioArray);
    for (float sample : audioArray) {
      assertEquals(0F, sample, 0F);
    }
    // 3) check the stopped case
    Arrays.fill(audioArray, 123F);
    audioReader.stop();
    audioReader.getNext(0, audioArray);
    for (float sample : audioArray) {
      assertEquals(0F, sample, 0F);
    }

    audioReader.close();
  }

  /**
   * Verify that the AudioReader correctly processes when audio array boundaries
   * exactly match the available input.
   *
   * Specification:
   *
   * <p>1) The AudioReader shall return the samples from the first buffer.</p>
   *
   * <p>2) If AudioReader has processed all input samples it shall return
   * null-samples.</p>
   *
   */
  @Test
  public void testExactMatch() {
    System.out.println("testExactMatch");

    int audioArraySize = 2063;
    int audioArrayCount = 31;
    int firstBufferSize = audioArraySize * audioArrayCount;
    float[] audioArray = new float[audioArraySize];

    FloatBuffer firstBuffer = prepareFirstBuffer(firstBufferSize);
    AudioReader audioReader = new AudioReader(executor);

    audioReader.start(firstBuffer, null, firstBufferSize);

    // 1 ----
    int sampleIndex = 0;
    for (int i = 0; i < audioArrayCount; i++) {
      Arrays.fill(audioArray, 123F);
      audioReader.getNext(sampleIndex, audioArray);
      for (float sample : audioArray) {
        assertEquals(sampleIndex, (int) sample);
        sampleIndex++;
      }
    }
    // 2 ---
    audioReader.getNext(sampleIndex, audioArray);
    for (float sample : audioArray) {
      assertEquals(0F, sample, 0F);
    }
    audioReader.close();
  }

  /**
   * Verify that the AudioReader correctly processes when audio array boundaries
   * do not match the available input.
   *
   * Specification:
   *
   * <p>1) The AudioReader shall return the samples from the first buffer if the
   * available samples do not fill a complete audio array, the remaining samples
   * shall be null samples.</p>
   *
   * <p>2) If AudioReader has processed all input samples it shall return
   * null-samples.</p>
   *
   */
  @Test
  public void testSurplus() {
    System.out.println("testSurplus");

    int audioArraySize = 2063;
    int regularAudioArrayCount = 33;
    int surplus = 1013;
    int firstBufferSize = audioArraySize * regularAudioArrayCount + surplus;
    float[] audioArray = new float[audioArraySize];

    FloatBuffer firstBuffer = prepareFirstBuffer(firstBufferSize);
    AudioReader audioReader = new AudioReader(executor);

    audioReader.start(firstBuffer, null, firstBufferSize);

    // 1 ----
    int sampleIndex = 0;
    for (int i = 0; i < regularAudioArrayCount; i++) {
      Arrays.fill(audioArray, 123F);
      audioReader.getNext(sampleIndex, audioArray);
      for (float sample : audioArray) {
        assertEquals(sampleIndex, (int) sample);
        sampleIndex++;
      }
    }
    // 2 ---
    Arrays.fill(audioArray, 123F);
    audioReader.getNext(sampleIndex, audioArray);
    for (int i = 0; i < surplus; i++) {
      assertEquals(sampleIndex, (int) audioArray[i]);
      sampleIndex++;
    }
    for (int i = surplus; i < audioArraySize; i++) {
      assertEquals(0, (int) audioArray[i]);
      sampleIndex++;
    }

    audioReader.getNext(sampleIndex, audioArray);
    for (float sample : audioArray) {
      assertEquals(0F, sample, 0F);
    }
    audioReader.close();
  }

  /**
   * Verify that the AudioReader correctly processes when we skip samples
   * between audio arrays.
   *
   * Specification:
   *
   * <p>1) On successive calls of getNext samples can be skipped (jumping over a
   * complete file buffer will hopefully never(?) happen).</p>
   *
   * <p>2) The AudioReader shall return the samples from the first buffer if the
   * available samples do not fill a complete audio array, the remaining samples
   * shall be null samples.</p>
   *
   */
  @Test
  public void testSkipRead() {
    System.out.println("testSkipRead");
    // set all sizes to prime numbers so that all buffers are interleaved.
    // prime numbers can be found with http://easycalculation.com/prime-number.php


    int audioArraySize = 2063;
    int skipSamples = 11;
    int periode = audioArraySize + skipSamples;
    int regularAudioArrayCount = 33;
    int surplus = 1013;
    int firstBufferSize = periode * regularAudioArrayCount + surplus + skipSamples;
    float[] audioArray = new float[audioArraySize];

    FloatBuffer firstBuffer = prepareFirstBuffer(firstBufferSize);
    AudioReader audioReader = new AudioReader(executor);

    audioReader.start(firstBuffer, null, firstBufferSize);

    // 1 ----
    int sampleIndex = 0;
    for (int i = 0; i < regularAudioArrayCount; i++) {
      sampleIndex += skipSamples;
      Arrays.fill(audioArray, 123F);
      audioReader.skip(skipSamples);
      audioReader.getNext(audioArray);
      for (float sample : audioArray) {
        assertEquals(sampleIndex, (int) sample);
        sampleIndex++;
      }
    }
    // 2 ---
    Arrays.fill(audioArray, 123F);
    sampleIndex += skipSamples;
    audioReader.getNext(sampleIndex, audioArray);
    for (int i = 0; i < surplus; i++) {
      assertEquals(sampleIndex, (int) audioArray[i]);
      sampleIndex++;
    }
    for (int i = surplus; i < audioArraySize; i++) {
      assertEquals(0, (int) audioArray[i]);
      sampleIndex++;
    }

    audioReader.getNext(sampleIndex, audioArray);
    for (float sample : audioArray) {
      assertEquals(0F, sample, 0F);
    }
    audioReader.close();
  }

  /**
   * Verify that the AudioReader correctly processes when first buffer has been
   * exhausted (simple case without skipping samples).
   *
   * Specification:
   *
   * <p>When all samples from the first buffer have been processed, the next
   * samples shall be taken from the file buffers.</p>
   *
   * @throws FileNotFoundException
   * @throws IOException
   * @throws InterruptedException
   * @throws ExecutionException
   */
  @Test
  public void testFileBufferRead() throws FileNotFoundException, IOException, InterruptedException, ExecutionException {
    System.out.println("testFileBufferRead");

    // set all sizes to prime numbers so that all buffers are interleaved.
    // prime numbers can be found with http://easycalculation.com/prime-number.php

    int audioArraySize = 2053;

    int firstBufferSizeFloat = 6007;
    int fileBufferSizeFloat = 5987;

    int fileSizeFloat = 18013;

    int samplesToProcess = fileSizeFloat + firstBufferSizeFloat;




    float[] audioArray = new float[audioArraySize];

    FloatBuffer firstBuffer = prepareFirstBuffer(firstBufferSizeFloat);
    File testFile = new File(testDir, "testFileBufferRead.tmp");
    Future<FileChannel> inputFile = makeTestFile(testFile, fileSizeFloat, firstBufferSizeFloat);
    AudioReader audioReader = new AudioReader(executor, fileBufferSizeFloat);

    audioReader.start(firstBuffer, inputFile, samplesToProcess);

    // 1 ----
    int sampleIndex = 0;
    boolean more = true;
    while (more) {
      Arrays.fill(audioArray, 123F);
      audioReader.waitForBufferReady();
      audioReader.getNext(audioArray);
      for (float sample : audioArray) {
        if (sampleIndex < samplesToProcess) {
          assertEquals(sampleIndex, (int) sample);
          more = true;
        } else {
          assertEquals(0, (int) sample);
          more = false;
        }
        sampleIndex++;
      }
    }
    // 2 ---
    Arrays.fill(audioArray, 123F);
    audioReader.waitForBufferReady();
    audioReader.getNext(sampleIndex, audioArray);
    for (float sample : audioArray) {
      assertEquals(0F, sample, 0F);
    }
    audioReader.close();
  }

  /**
   * Verify that the AudioReader can be re-started.
   *
   * Specification:
   *
   * <p>When all samples from the first buffer have been processed, the next
   * samples shall be taken from the file buffers.</p>
   *
   * @throws FileNotFoundException
   * @throws IOException
   * @throws InterruptedException
   * @throws ExecutionException
   */
  @Test
  public void testFileBufferRestartRead() throws FileNotFoundException, IOException, InterruptedException, ExecutionException {
    System.out.println("testFileBufferRestartRead");

    // set all sizes to prime numbers so that all buffers are interleaved.
    // prime numbers can be found with http://easycalculation.com/prime-number.php

    int audioArraySize = 2053;

    int firstBufferSizeFloat = 6007;
    int fileBufferSizeFloat = 5987;

    int fileSizeFloat = 11987;

    int samplesToProcess = fileSizeFloat + firstBufferSizeFloat;




    float[] audioArray = new float[audioArraySize];

    FloatBuffer firstBuffer = prepareFirstBuffer(firstBufferSizeFloat);
    File testFile = new File(testDir, "testFileBufferRead.tmp");
    Future<FileChannel> inputFile = makeTestFile(testFile, fileSizeFloat, firstBufferSizeFloat);
    AudioReader audioReader = new AudioReader(executor, fileBufferSizeFloat);

    audioReader.start(firstBuffer, inputFile, samplesToProcess);

    // 1 ----
    int sampleIndex = 0;
    boolean more = true;
    while (more) {
      Arrays.fill(audioArray, 123F);
      audioReader.waitForBufferReady();
      audioReader.getNext(sampleIndex, audioArray);
      for (float sample : audioArray) {
        if (sampleIndex < samplesToProcess) {
          assertEquals(sampleIndex, (int) sample);
          more = true;
        } else {
          assertEquals(0, (int) sample);
          more = false;
        }
        sampleIndex++;
      }
    }
    audioReader.stop();

    //  --- prepare a first buffer and a file with different values
    audioArraySize = 2039;
    firstBufferSizeFloat = 6011;
    fileSizeFloat = 12007;

    samplesToProcess = fileSizeFloat + firstBufferSizeFloat;
    firstBuffer = prepareFirstBuffer(firstBufferSizeFloat);
    testFile = new File(testDir, "testFileBufferRead2.tmp");
    inputFile = makeTestFile(testFile, fileSizeFloat, firstBufferSizeFloat);
    audioArray = new float[audioArraySize];

    //  --- restart
    audioReader.start(firstBuffer, inputFile, samplesToProcess);
    sampleIndex = 0;
    more = true;
    while (more) {
      Arrays.fill(audioArray, 123F);
      audioReader.waitForBufferReady();
      audioReader.getNext(sampleIndex, audioArray);
      for (float sample : audioArray) {
        if (sampleIndex < samplesToProcess) {
          assertEquals(sampleIndex, (int) sample);
          more = true;
        } else {
          assertEquals(0, (int) sample);
          more = false;
        }
        sampleIndex++;
      }
    }
    audioReader.stop();


    audioReader.close();
  }

  /**
   * Verify that the AudioReader correctly processes when first buffer has been
   * exhausted (case with skipping samples).
   *
   * Specification:
   *
   * <p>When all samples from the first buffer have been processed, the next
   * samples shall be taken from the file buffers.</p>
   *
   * <p> On successive calls of getNext samples can be skipped (jumping over a
   * complete file buffer will hopefully never(?) happen).</p>
   *
   * @throws FileNotFoundException
   * @throws IOException
   * @throws InterruptedException
   * @throws ExecutionException
   */
  @Test
  public void testFileBufferReadSkip() throws FileNotFoundException, IOException, InterruptedException, ExecutionException {
    System.out.println("testFileBufferReadSkip");

    // set all sizes to prime numbers so that all buffers are interleaved.
    // prime numbers can be found with http://easycalculation.com/prime-number.php

    int audioArraySize = 2053;
    int periode = 2063;
    int skipSamples = periode - audioArraySize;

    int firstBufferSizeFloat = 6007;
    int fileBufferSizeFloat = 5987;

    int fileSizeFloat = 60013;

    int samplesToProcess = fileSizeFloat + firstBufferSizeFloat;
    int regularAudioArrayCount = samplesToProcess / periode;



    float[] audioArray = new float[audioArraySize];

    FloatBuffer firstBuffer = prepareFirstBuffer(firstBufferSizeFloat);
    File testFile = new File(testDir, "testFileBufferRead.tmp");
    Future<FileChannel> inputFile = makeTestFile(testFile, fileSizeFloat, firstBufferSizeFloat);
    AudioReader audioReader = new AudioReader(executor, fileBufferSizeFloat);

    audioReader.start(firstBuffer, inputFile, samplesToProcess);

    // 1 ----
    int sampleIndex = 0;
    for (int i = 0; i < regularAudioArrayCount; i++) {
      sampleIndex += skipSamples;
      Arrays.fill(audioArray, 123F);
      audioReader.waitForBufferReady();
      audioReader.getNext(sampleIndex, audioArray);
      for (float sample : audioArray) {
        assertEquals(sampleIndex, (int) sample);
        sampleIndex++;
      }
    }
    // 2 ---
    Arrays.fill(audioArray, 123F);
    sampleIndex += skipSamples;
    int surplus = Math.max(0, samplesToProcess - sampleIndex);
    audioReader.getNext(sampleIndex, audioArray);
    for (int i = 0; i < surplus; i++) {
      assertEquals(sampleIndex, (int) audioArray[i]);
      sampleIndex++;
    }
    for (int i = surplus; i < audioArraySize; i++) {
      assertEquals(0, (int) audioArray[i]);
      sampleIndex++;
    }

    Arrays.fill(audioArray, 123F);
    audioReader.getNext(sampleIndex, audioArray);
    for (float sample : audioArray) {
      assertEquals(0F, sample, 0F);
    }
    audioReader.close();
  }

  /**
   * Test the overall performance.
   *
   * Note: the shown performance figure tells you how many processes could
   * (theoretically) run in parallel on this machine in a real world
   * application. This figure should be far lager than 100.
   *
   * @throws FileNotFoundException
   * @throws IOException
   * @throws InterruptedException
   * @throws ExecutionException
   */
  @Test
  public void testFileBufferPerformance() throws FileNotFoundException, IOException, InterruptedException, ExecutionException {
    System.out.println("testFileBufferPerformance");
    int samplingRate = 44100;
    int nFrames = 256;
    int inputChannelCount = 2;
    int audioArraySize = inputChannelCount * nFrames;

    int firstBufferSizeFloat = 256 * 1024;
    int fileBufferSizeFloat = firstBufferSizeFloat;

    int fileSizeFloat = 3 * firstBufferSizeFloat;

    int samplesToProcess = fileSizeFloat + firstBufferSizeFloat;
    int regularAudioArrayCount = samplesToProcess / audioArraySize;

    float[] audioArray = new float[audioArraySize];

    FloatBuffer firstBuffer = prepareFirstBuffer(firstBufferSizeFloat);
    File testFile = new File(testDir, "testFileBufferPerformance.tmp");
    Future<FileChannel> inputFile = makeTestFile(testFile, fileSizeFloat, firstBufferSizeFloat);
    AudioReader audioReader = new AudioReader(executor, fileBufferSizeFloat);

    audioReader.start(firstBuffer, inputFile, samplesToProcess);

    // 1 ----
    long readStartTime = System.nanoTime();
    int sampleIndex = 0;
    for (int i = 0; i < regularAudioArrayCount; i++) {
      audioReader.waitForBufferReady();
      audioReader.getNext(sampleIndex, audioArray);
      sampleIndex += audioArraySize;

    }
    long readStopTime = System.nanoTime();
    int floatsRead = sampleIndex + audioArraySize;


    // how long would the file take if it was encoded in stereo with 44100 samples per second
    double realWorldDurationNano = (floatsRead * 1E09) / (samplingRate * inputChannelCount);
    System.out.printf("...File size              : %d bytes.%n", floatsRead * 4);
    System.out.printf("...Real world duration    : %.2f seconds.%n", realWorldDurationNano * 1E-09);
    System.out.printf("...Read Performance       : %.2f units.%n", realWorldDurationNano / (readStopTime - readStartTime));

    audioReader.close();
  }

  /**
   * Test the performance of first buffer handling.
   *
   * Note: the shown performance figure tells you how many processes could
   * (theoretically) run in parallel on this machine in a real world
   * application. This figure should be far lager than 100.
   */
  @Test
  public void testFirstBufferPerformance() {
    System.out.println("testFirstBufferPerformance");

    int samplingRate = 44100;
    int nFrames = 256;
    int inputChannelCount = 2;
    int repetitions = 100; // the number of times we'll read the first buffer
    int audioArraySize = inputChannelCount * nFrames;

    int audioArrayCount = 10000;
    int firstBufferSize = audioArraySize * audioArrayCount;

    float[] audioArray = new float[audioArraySize];


    FloatBuffer firstBuffer = prepareFirstBuffer(firstBufferSize);
    AudioReader audioReader = new AudioReader(executor);

    long readStartTime = System.nanoTime();
    for (int r = 0; r < repetitions; r++) {
      audioReader.start(firstBuffer, null, firstBufferSize);
      int sampleIndex = 0;
      for (int i = 0; i < audioArrayCount; i++) {
        audioReader.getNext(sampleIndex, audioArray);
        sampleIndex += audioArraySize;
      }
      audioReader.stop();
      firstBuffer.flip();
    }
    long readStopTime = System.nanoTime();
    int floatsRead = repetitions * firstBufferSize;

    // how long would the file take if it was encoded in stereo with 44100 samples per second
    double realWorldDurationNano = (floatsRead * 1E09) / (samplingRate * inputChannelCount);
    System.out.printf("...File size              : %d bytes.%n", floatsRead * 4);
    System.out.printf("...Real world duration    : %.2f seconds.%n", realWorldDurationNano * 1E-09);
    System.out.printf("...Read Performance       : %.2f units.%n", realWorldDurationNano / (readStopTime - readStartTime));
  }

  /**
   * Allocates a float buffer and fills it samples corresponding to the index
   * position.
   *
   * @param sizeFloat the Size of the requested buffer in Floating point values.
   * @return a test buffer.
   */
  private FloatBuffer prepareFirstBuffer(int sizeFloat) {
    FloatBuffer newBuffer = FloatBuffer.allocate(sizeFloat);
    newBuffer.clear();
    for (int i = 0; i < sizeFloat; i++) {
      newBuffer.put(i);
    }
    newBuffer.flip();
    return newBuffer.asReadOnlyBuffer();
  }

  private class RealizedInputFile implements Future<FileChannel> {

    private final FileChannel inChannel;

    RealizedInputFile(File file) throws FileNotFoundException {
      // open the input file
      FileInputStream inFile = new FileInputStream(file);
      inChannel = inFile.getChannel();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      return false;
    }

    @Override
    public boolean isCancelled() {
      return false;
    }

    @Override
    public boolean isDone() {
      return true;
    }

    @Override
    public FileChannel get() throws InterruptedException, ExecutionException {
      return inChannel;
    }

    @Override
    public FileChannel get(long timeout, TimeUnit unit) {
      return inChannel;
    }
  }

  private Future<FileChannel> makeTestFile(File file, int fileSizeFloat, int startIndex) throws FileNotFoundException, IOException {

    FileOutputStream outFile = new FileOutputStream(file);
    try (FileChannel outChannel = outFile.getChannel()) {
      java.nio.ByteBuffer byteBuffer = java.nio.ByteBuffer.allocateDirect(Const.bytesPerFloat).order(ByteOrder.LITTLE_ENDIAN);

      for (int i = 0; i < fileSizeFloat; i++) {
        float nextFloat = (float) (i + startIndex);
        byteBuffer.clear();
        byteBuffer.putFloat(nextFloat);
        byteBuffer.flip();
        outChannel.write(byteBuffer);
      }
      outChannel.close();
    }
    RealizedInputFile realizedInputFile = new RealizedInputFile(file);
    return realizedInputFile;


  }

  /**
   * Test of "start(AudioWriter.WriterResult fileToRead)" method, of class
   * AudioReader.
   *
   * 1) when the "AudioReader" is started with a "WriterResult"
   * AudioReader.getNext shall return the samples contained in the WriterResult.
   */
  @Test
  public void testStart_WriterResult() throws IOException {
    System.out.println("testStart_WriterResult");
    int sampleCount = Const.fileBufferSizeFloat - 13; // make sample count just a wee bit smaller than the file buffer size.
    int audioArraySize = 2053;
    SyncBuffer syncBuffer = new SyncBuffer(Const.fileBufferSizeFloat);

    FloatBuffer floatBuffer = syncBuffer.asFloatBuffer();

    for (int i = 0; i < sampleCount; i++) {
      floatBuffer.put(i);
    }
    syncBuffer.flipFloats();
    //prepare a writer result with kown content (ascending index numbers).
    WriterResult result = new WriterResult(syncBuffer, null, sampleCount);

    float[] audioArray = new float[audioArraySize];
    AudioReader instance = new AudioReader(executor);
    instance.start(result);

    int sampleIdx = 0;
    while (sampleIdx < sampleCount) {
      instance.getNext(audioArray);
      for (float sample : audioArray) {
        if (sampleIdx < sampleCount) {
          assertEquals(sampleIdx, (int) sample);
        } else {
          assertEquals(0, (int) sample);
        }
        sampleIdx++;
      }
    }


  }

  /**
   * Test that "start(AudioWriter.WriterResult fileToRead)" method, can be be
   * used several times on the same input.
   *
   * 1) when the "AudioReader" is started with a "WriterResult"
   * AudioReader.getNext shall return the samples contained in the WriterResult.
   *
   * 2) when the "AudioReader" is stopped and than re-started with the same
   * "WriterResult" AudioReader.getNext shall again return the samples contained
   * in the WriterResult.
   */
  @Test
  public void testStart_WriterResultTwice() throws IOException, InterruptedException, ExecutionException {
    System.out.println("testStart_WriterResult");
    int approxSampleCount =  3*Const.fileBufferSizeFloat ;
    int audioArraySize = 2053;
    int audioArraysCount = approxSampleCount / audioArraySize;
    float[] audioArray = new float[audioArraySize];
    File outFile = new File(testDir, "testStart_WriterResultTwice.test");

    AudioWriter audioWriter = new AudioWriter(executor);

        //prepare a writer result with kown content (ascending index numbers).
    audioWriter.start(outFile);
    //... put asceding integers.
    int sampleCount = 0;
    for (int i = 0; i < audioArraysCount; i++) {
      for (int sample = 0; sample < audioArraySize; sample++) {
        audioArray[sample] = sampleCount;
        sampleCount++;
      }
      audioWriter.waitForBufferReady();
      //audioWriter.putNext(i * audioArraySize, audioWriterArray);
      audioWriter.putNext(audioArray);
    }
    audioWriter.waitForBufferReady();
    WriterResult result = audioWriter.stop();


    AudioReader instance = new AudioReader(executor);
    instance.start(result);

    // 1) read the samples for a first time
    int sampleIdx = 0;
    while (sampleIdx < sampleCount) {
      instance.getNext(audioArray);
      for (float sample : audioArray) {
        if (sampleIdx < sampleCount) {
          assertEquals(sampleIdx, (int) sample);
        } else {
          assertEquals(0, (int) sample);
        }
        sampleIdx++;
      }
    }
    instance.stop();

    // 2) read the samples for a second time
    instance.start(result);
    sampleIdx = 0;
    while (sampleIdx < sampleCount) {
      instance.getNext(audioArray);
      for (float sample : audioArray) {
        if (sampleIdx < sampleCount) {
          assertEquals(sampleIdx, (int) sample);
        } else {
          assertEquals(0, (int) sample);
        }
        sampleIdx++;
      }
    }
    
    assertTrue(outFile.delete()); // make sure an outfile was written

  }

  /**
   * Test of start method, of class AudioReader.
   */
  @Test
  @Ignore("Tested with other tests")
  public void testStart_3args() {
  }

  /**
   * Test of getNext method, of class AudioReader.
   */
  @Test
  @Ignore("Tested with other tests")
  public void testGetNext_floatArr() {
  }

  /**
   * Test of getNext method, of class AudioReader.
   */
  @Test
  @Ignore("Tested with other tests")
  public void testGetNext_int_floatArr() {
  }

  /**
   * Test of close method, of class AudioReader.
   */
  @Test
  @Ignore("Tested with other tests?")
  public void testClose() {
  }

  /**
   * Test of getOverflowCount method, of class AudioReader.
   */
  @Test
  @Ignore("Test is trivial")
  public void testGetOverflowCount() {
  }

  /**
   * Test of isStarted method, of class AudioReader.
   */
  @Test
  @Ignore("Test is trivial")
  public void testIsStarted() {
  }

  /**
   * Test of waitForBufferReady method, of class AudioReader.
   */
  @Test
  @Ignore("method is only for test")
  public void testWaitForBufferReady() throws Exception {
  }
}
