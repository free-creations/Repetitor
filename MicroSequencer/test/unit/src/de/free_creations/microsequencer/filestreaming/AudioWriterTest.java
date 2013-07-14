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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Harald Postner
 */
public class AudioWriterTest {
  
  private static File testDir;
  private static final ExecutorService executor = Executors.newSingleThreadExecutor(
          new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
              Thread thread = new Thread(r);
              thread.setPriority(Thread.MIN_PRIORITY);
              thread.setDaemon(true);
              thread.setName("AudioWriterTestBackgroundThread");
              return thread;
            }
          });
  
  public AudioWriterTest() {
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
   * Test of start method, of class AudioWriter.
   */
  @Test
  @Ignore("Tested in TestWriteXXX procedures")
  public void testStart() {
  }

  /**
   * Test of putNext method, of class AudioWriter.
   */
  @Test
  @Ignore("Tested in TestWriteXXX procedures")
  public void testPutNext() {
  }

  /**
   * Test of stop method, of class AudioWriter.
   */
  @Test
  @Ignore("Tested in TestWriteXXX procedures")
  public void testStop() {
  }

  /**
   * Test of close method, of class AudioWriter.
   */
  @Test
  @Ignore("Tested in TestWriteXXX procedures")
  public void testClose() {
  }

  /**
   * Verify that the AudioWriter correctly processes an empty input.
   *
   * Specification:
   *
   * <p>1) If the AudioWriter is not started it shall ignore putNext calls.</p>
   *
   *
   * <p>3) If the AudioWriter is stopped before any putNext calls have been
   * issued, it shall return an empty first buffer and the file pointer shall be
   * null.</p>
   *
   */
  @Test
  public void testWriteEmpty() {
    System.out.println("testWriteEmpty");
    File outFile = new File(testDir, "testWriteEmpty.test");
    float[] audioArray = new float[503];
    
    AudioWriter audioWriter = new AudioWriter(executor);
    
    audioWriter.putNext(123, audioArray); // this call should be ignored

    audioWriter.start(outFile);
    //... put nothing
    WriterResult result = audioWriter.stop();
    
    assertNotNull(result);
    assertNull(result.getChannel());
    assertEquals(0, result.getSamplesWritten());
    FloatBuffer firstBuffer = result.getStartBuffer().asFloatBuffer();
    assertNotNull(firstBuffer);
    assertEquals(0, firstBuffer.remaining());
    
    audioWriter.close();
    outFile.delete();
  }

  /**
   * Verify that the AudioWriter correctly writes to the first buffer (simple
   * case with adjacent audioArrays).
   *
   * Specification:
   *
   * <p>putNext() shall write the provided samples to the first buffer.</p>
   *
   *
   */
  @Test
  public void testWriteFirstBuffer() {
    System.out.println("testWriteFirstBuffer");
    File outFile = new File(testDir, "testWriteFirstBuffer.test");
    outFile.delete();
    int audioArraySize = 503;
    int audioArraysToPut = 13;
    int firstBufferSize = (audioArraysToPut * audioArraySize) + 19;
    float[] audioArray = new float[audioArraySize];
    
    AudioWriter audioWriter = new AudioWriter(executor, firstBufferSize);
    
    audioWriter.start(outFile);
    //... put asceding integers.
    int samplesWritten = 0;
    for (int i = 0; i < audioArraysToPut; i++) {
      for (int sample = 0; sample < audioArraySize; sample++) {
        audioArray[sample] = samplesWritten;
        samplesWritten++;
      }
      //audioWriter.putNext(i * audioArraySize, audioWriterArray);
      audioWriter.putNext(audioArray);
    }
    WriterResult result = audioWriter.stop();
    
    assertNotNull(result);
    assertNull(result.getChannel());
    assertEquals(samplesWritten, result.getSamplesWritten());
    FloatBuffer firstBuffer = result.getStartBuffer().asFloatBuffer();
    assertNotNull(firstBuffer);
    assertEquals(samplesWritten, firstBuffer.remaining());
    
    for (int i = 0; i < samplesWritten; i++) {
      float test = firstBuffer.get();
      assertEquals(i, (int) test);
    }
    
    audioWriter.close();
    assertFalse(outFile.delete()); // no file should have been written
  }

  /**
   * Verify that the AudioWriter correctly writes to the first buffer (case with
   * skips between audioArrays).
   *
   * Specification:
   *
   * <p>if the value of "startSample" in putNext(), skips some sample points
   * these shall be replaced by null samples.</p>
   *
   *
   */
  @Test
  public void testWriteFirstBufferSkip() {
    System.out.println("testWriteFirstBufferSkip");
    File outFile = new File(testDir, "testWriteFirstBufferSkip.test");
    outFile.delete();
    int audioArraySize = 503;
    int audioArraysToPut = 13;
    int skip = 101;
    int firstBufferSize = (audioArraysToPut * audioArraySize + skip) + 19;
    float[] audioArray = new float[audioArraySize];
    
    AudioWriter audioWriter = new AudioWriter(executor, firstBufferSize);
    
    audioWriter.start(outFile);
    //... put asceding integers.
    int samplesWritten = skip;
    for (int i = 0; i < audioArraysToPut; i++) {
      for (int sample = 0; sample < audioArraySize; sample++) {
        audioArray[sample] = samplesWritten;
        samplesWritten++;
      }
      audioWriter.putNext((i * audioArraySize) + skip, audioArray);
    }
    WriterResult result = audioWriter.stop();
    
    assertNotNull(result);
    assertNull(result.getChannel());
    assertEquals(samplesWritten, result.getSamplesWritten());
    FloatBuffer firstBuffer = result.getStartBuffer().asFloatBuffer();
    assertNotNull(firstBuffer);
    assertEquals(samplesWritten, firstBuffer.remaining());
    
    for (int i = 0; i < samplesWritten; i++) {
      float test = firstBuffer.get();
      if (i < skip) {
        assertEquals(0, (int) test);
      } else {
        assertEquals(i, (int) test);
      }
    }
    
    audioWriter.close();
    assertFalse(outFile.delete());
  }

  /**
   * Verify that the AudioWriter correctly writes to the first buffer when
   * restarted.
   *
   * Specification:
   *
   * <p>It must be possible to read the first buffer while the audioWriter is
   * writing new samples (to an other first buffer).</p>
   *
   *
   */
  @Test
  public void testWriteFirstBufferRestart() {
    System.out.println("testWriteFirstBufferRestart");
    File outFile_1 = new File(testDir, "testWriteFirstBufferRestart_1.test");
    outFile_1.delete();
    File outFile_2 = new File(testDir, "testWriteFirstBufferRestart_2.test");
    outFile_2.delete();
    int audioArraySize = 503;
    int audioArraysToPut = 13;
    int firstBufferSize = (audioArraysToPut * audioArraySize) + 19;
    float[] audioArray = new float[audioArraySize];
    
    AudioWriter audioWriter = new AudioWriter(executor, firstBufferSize);

    // put a first series of audioArrays
    audioWriter.start(outFile_1);
    //... put asceding integers.
    int samplesWritten_1 = 0;
    for (int i = 0; i < audioArraysToPut; i++) {
      for (int sample = 0; sample < audioArraySize; sample++) {
        audioArray[sample] = samplesWritten_1;
        samplesWritten_1++;
      }
      audioWriter.putNext((i * audioArraySize), audioArray);
    }
    WriterResult result_1 = audioWriter.stop();

    // put a second series of audioArrays
    audioWriter.start(outFile_2);
    int discriminator = 1234567;
    //... put asceding integers.
    int samplesWritten_2 = 0;
    for (int i = 0; i < audioArraysToPut; i++) {
      for (int sample = 0; sample < audioArraySize; sample++) {
        audioArray[sample] = samplesWritten_2 + discriminator;
        samplesWritten_2++;
      }
      //audioWriter.putNext((i * audioArraySize), audioWriterArray);
      audioWriter.putNext(audioArray);
    }
    WriterResult result_2 = audioWriter.stop();

    // check the first series
    assertNotNull(result_1);
    assertNull(result_1.getChannel());
    assertEquals(samplesWritten_1, result_1.getSamplesWritten());
    FloatBuffer firstBuffer_1 = result_1.getStartBuffer().asFloatBuffer();
    assertNotNull(firstBuffer_1);
    assertEquals(samplesWritten_1, firstBuffer_1.remaining());
    
    for (int i = 0; i < samplesWritten_1; i++) {
      float test = firstBuffer_1.get();
      assertEquals(i, (int) test);
    }

    // check the second series
    assertNotNull(result_2);
    assertNull(result_2.getChannel());
    assertEquals(samplesWritten_2, result_2.getSamplesWritten());
    FloatBuffer firstBuffer_2 = result_2.getStartBuffer().asFloatBuffer();
    assertNotNull(firstBuffer_2);
    assertEquals(samplesWritten_2, firstBuffer_2.remaining());
    
    for (int i = 0; i < samplesWritten_2; i++) {
      float test = firstBuffer_2.get();
      assertEquals(i + discriminator, (int) test);
    }
    
    audioWriter.close();
    assertFalse(outFile_1.delete());
    assertFalse(outFile_2.delete());
  }

  /**
   * Verify that the AudioWriter correctly writes to one file buffer (simple
   * case with adjacent audioArrays).
   *
   * Specification:
   *
   * <p>when the start-buffer has been filled, putNext() shall write the
   * provided samples to a first file-buffer.</p>
   *
   *
   */
  @Test
  public void testWriteOneFileBuffer() throws InterruptedException, ExecutionException, IOException {
    System.out.println("testWriteOneFileBuffer");
    File outFile = new File(testDir, "testWriteOneFileBuffer.test");
    int audioArraySize = 503;
    int bufferSize = 1499;
    int audioArraysToPut = (2 * bufferSize) / audioArraySize;
    
    float[] audioArray = new float[audioArraySize];
    
    AudioWriter audioWriter = new AudioWriter(executor, bufferSize);
    
    audioWriter.start(outFile);
    //... put asceding integers.
    int samplesWritten = 0;
    for (int i = 0; i < audioArraysToPut; i++) {
      for (int sample = 0; sample < audioArraySize; sample++) {
        audioArray[sample] = samplesWritten;
        samplesWritten++;
      }
      audioWriter.waitForBufferReady();
      //audioWriter.putNext(i * audioArraySize, audioWriterArray);
      audioWriter.putNext(audioArray);
    }
    audioWriter.waitForBufferReady();
    WriterResult result = audioWriter.stop();
    
    System.out.printf("...Samples written total          : %d.%n", samplesWritten);

    // now check the result
    assertEquals(samplesWritten, result.getSamplesWritten());
    assertNotNull(result);
    FloatBuffer firstBuffer = result.getStartBuffer().asFloatBuffer();
    assertNotNull(firstBuffer);
    assertEquals(bufferSize, firstBuffer.remaining());
    
    Future<FileChannel> futureChannel = result.getChannel();
    assertNotNull(futureChannel);
    FileChannel channel = futureChannel.get();
    assertNotNull(channel);
    assertTrue(channel.isOpen());
    audioWriter.close();


    // check the file written
    assertTrue(outFile.exists());
    assertEquals((samplesWritten - bufferSize) * Const.bytesPerFloat, outFile.length());
    
    FileInputStream inFile = new FileInputStream(outFile);
    FileChannel inchannel = inFile.getChannel();
    ByteBuffer readBuffer = ByteBuffer.allocateDirect(Const.bytesPerFloat).order(ByteOrder.LITTLE_ENDIAN);
    
    
    for (int i = bufferSize; i < samplesWritten; i++) {
      readBuffer.clear();
      inchannel.read(readBuffer);
      readBuffer.flip();
      float sample = readBuffer.getFloat();
      assertEquals(i, (int) sample);
    }
    
    
    outFile.delete();
  }

  /**
   * Verify that the AudioWriter correctly writes many file buffers (simple case
   * with adjacent audioArrays).
   *
   * Specification:
   *
   * <p>when the start-buffer has been filled, putNext() shall write the
   * provided samples to file-buffers.</p>
   *
   *
   */
  @Test
  public void testWriteManyFileBuffer() throws InterruptedException, ExecutionException, IOException {
    System.out.println("testWriteManyFileBuffer");
    File outFile = new File(testDir, "testWriteManyFileBuffer.test");
    int audioArraySize = 503;
    int bufferSize = 1499;
    
    int audioArraysToPut = (13 * bufferSize) / audioArraySize;
    
    float[] audioArray = new float[audioArraySize];
    
    AudioWriter audioWriter = new AudioWriter(executor, bufferSize);
    
    audioWriter.start(outFile);
    //... put asceding integers.
    int samplesWritten = 0;
    for (int i = 0; i < audioArraysToPut; i++) {
      for (int sample = 0; sample < audioArraySize; sample++) {
        audioArray[sample] = samplesWritten;
        samplesWritten++;
      }
      audioWriter.waitForBufferReady();
      //audioWriter.putNext(i * audioArraySize, audioWriterArray);
      audioWriter.putNext(audioArray);
    }
    audioWriter.waitForBufferReady();
    WriterResult result = audioWriter.stop();
    
    System.out.printf("...Samples written total          : %d.%n", samplesWritten);

    // now check the result
    assertEquals(samplesWritten, result.getSamplesWritten());
    assertNotNull(result);
    FloatBuffer firstBuffer = result.getStartBuffer().asFloatBuffer();
    assertNotNull(firstBuffer);
    assertEquals(bufferSize, firstBuffer.remaining());
    
    Future<FileChannel> futureChannel = result.getChannel();
    assertNotNull(futureChannel);
    FileChannel channel = futureChannel.get();
    assertNotNull(channel);
    assertTrue(channel.isOpen());
    audioWriter.close();


    // check the file written
    assertTrue(outFile.exists());
    assertEquals((samplesWritten - bufferSize), outFile.length() / Const.bytesPerFloat);
    
    FileInputStream inFile = new FileInputStream(outFile);
    FileChannel inchannel = inFile.getChannel();
    ByteBuffer readBuffer = ByteBuffer.allocateDirect(Const.bytesPerFloat).order(ByteOrder.LITTLE_ENDIAN);
    
    for (int i = bufferSize; i < samplesWritten; i++) {
      readBuffer.clear();
      inchannel.read(readBuffer);
      readBuffer.flip();
      float sample = readBuffer.getFloat();
      assertEquals(i, (int) sample);
    }
    outFile.delete();
  }

  /**
   * Verify that the AudioWriter correctly writes file buffers, skipping some
   * samples before each buffer.
   *
   * Specification:
   *
   * <p>when the start-buffer has been filled, putNext() shall write the
   * provided samples to file-buffers.</p>
   *
   *
   */
  @Test
  public void testWriteFileBufferSkip() throws InterruptedException, ExecutionException, IOException {
    System.out.println("testWriteFileBufferSkip");
    File outFile = new File(testDir, "testWriteFileBufferSkip.test");
    int audioArraySize = 491;
    int skip = 12;
    int bufferSize = 1499;
    
    
    int cycleLengt = audioArraySize + skip;
    
    
    int audioArraysToPut = (13 * bufferSize) / cycleLengt;
    
    float[] audioArray = new float[audioArraySize];
    
    AudioWriter audioWriter = new AudioWriter(executor, bufferSize);
    
    audioWriter.start(outFile);
    //... put asceding integers.
    int samplesWritten = 0;
    for (int i = 0; i < audioArraysToPut; i++) {
      samplesWritten += skip;
      for (int sample = 0; sample < audioArraySize; sample++) {
        audioArray[sample] = samplesWritten;
        samplesWritten++;
      }
      audioWriter.waitForBufferReady();
      audioWriter.putNext(skip + (i * cycleLengt), audioArray);
    }
    audioWriter.waitForBufferReady();
    WriterResult result = audioWriter.stop();
    
    System.out.printf("...Samples written total          : %d.%n", samplesWritten);

    // now check the result
    assertEquals(samplesWritten, result.getSamplesWritten());
    assertNotNull(result);
    FloatBuffer firstBuffer = result.getStartBuffer().asFloatBuffer();
    assertNotNull(firstBuffer);
    assertEquals(bufferSize, firstBuffer.remaining());
    
    Future<FileChannel> futureChannel = result.getChannel();
    assertNotNull(futureChannel);
    FileChannel channel = futureChannel.get();
    assertNotNull(channel);
    assertTrue(channel.isOpen());
    audioWriter.close();

    //check the first buffer
    int samplesRead = 0;
    while (samplesRead < bufferSize) {
      for (int s = 0; s < cycleLengt; s++) {
        if (samplesRead < bufferSize) {
          float sample = firstBuffer.get();
          if (s < skip) {
            assertEquals(0, (int) sample);
          } else {
            assertEquals(samplesRead, (int) sample);
          }
        } else {
          break;
        }
        samplesRead++;
      }
    }


    // check the file written
    assertTrue(outFile.exists());
    assertEquals((samplesWritten - bufferSize), outFile.length() / Const.bytesPerFloat);
    
    FileInputStream inFile = new FileInputStream(outFile);
    FileChannel inchannel = inFile.getChannel();
    ByteBuffer readBuffer = ByteBuffer.allocateDirect(Const.bytesPerFloat).order(ByteOrder.LITTLE_ENDIAN);
    
    int samplesIndex = 0;
    for (int i = 0; i < audioArraysToPut; i++) {
      for (int s = 0; s < cycleLengt; s++) {
        if (samplesIndex >= bufferSize) {
          readBuffer.clear();
          inchannel.read(readBuffer);
          readBuffer.flip();
          float sample = readBuffer.getFloat();
          if (s < skip) {
            assertEquals(0, (int) sample);
          } else {
            assertEquals(samplesIndex, (int) sample);
          }
        }
        samplesIndex++;
      }
    }
    outFile.delete();
  }

  /**
   * Test the performance of start buffer handling.
   *
   * Note: the shown performance figure tells you how many processes could
   * (theoretically) run in parallel on this machine in a real world
   * application. This figure should be far lager than 100.
   */
  @Test
  public void testStartBufferPerformance() throws InterruptedException, ExecutionException {
    System.out.println("testStartBufferPerformance");
    int samplingRate = 44100;
    int nFrames = 256;
    int inputChannelCount = 2;
    int repetitions = 100; // the number of times we'll write the start buffer
    int audioArraySize = inputChannelCount * nFrames;

    // for simplicty we'll make the buffer an exact mutiple of the audio array
    int audioArraysToPut = Const.fileBufferSizeFloat / audioArraySize;
    int bufferSize = audioArraySize * audioArraysToPut;
    
    float[] audioArray = new float[audioArraySize];
    
    AudioWriter audioWriter = new AudioWriter(executor, bufferSize);
    int samplesWritten = 0;
    
    long startTime = System.nanoTime();
    for (int r = 0; r < repetitions; r++) {
      audioWriter.start(null);
      for (int i = 0; i < audioArraysToPut; i++) {
        audioWriter.waitForBufferReady();
        audioWriter.putNext((i * audioArraySize), audioArray);
        samplesWritten += audioArraySize;
      }
      audioWriter.waitForBufferReady();
      audioWriter.stop();
    }
    long stopTime = System.nanoTime();
    // how long would the file take if it was encoded in stereo with 44100 samples per second
    double realWorldDurationNano = (samplesWritten * 1E09) / (samplingRate * inputChannelCount);
    System.out.printf("...File size              : %d bytes.%n", samplesWritten * 4);
    System.out.printf("...Real world duration    : %f.2 seconds.%n", realWorldDurationNano * 1E-09);
    System.out.printf("...Write Performance      : %.2f units.%n", realWorldDurationNano / (stopTime - startTime));
  }

  /**
   * Test the performance of file buffer handling.
   *
   * Note: the shown performance figure tells you how many processes could
   * (theoretically) run in parallel on this machine in a real world
   * application. This figure should be far lager than 100.
   */
  @Test
  public void testFileBufferPerformance() throws InterruptedException, ExecutionException, IOException {
    System.out.println("testFileBufferPerformance");
    int samplingRate = 44100;
    int nFrames = 256;
    int inputChannelCount = 2;
    int repetitions = 3; // the number of times we'll write the start buffer
    int audioArraySize = inputChannelCount * nFrames;

    // for simplicty we'll make the buffer an exact mutiple of the audio array
    int audioArraysPerBuffer = (Const.fileBufferSizeFloat / audioArraySize);
    int bufferSize = audioArraySize * audioArraysPerBuffer;
    int audioArraysToPut = audioArraysPerBuffer * 3;
    
    float[] audioArray = new float[audioArraySize];
    
    AudioWriter audioWriter = new AudioWriter(executor, bufferSize);
    int samplesWritten = 0;
    
    long startTime = System.nanoTime();
    for (int r = 0; r < repetitions; r++) {
      File outFile = new File(testDir, "testWrite_" + r + ".test");
      audioWriter.start(outFile);
      for (int i = 0; i < audioArraysToPut; i++) {
        audioWriter.waitForBufferReady();
        audioWriter.putNext((i * audioArraySize), audioArray);
        samplesWritten += audioArraySize;
      }
      audioWriter.waitForBufferReady();
      WriterResult result = audioWriter.stop();
      audioWriter.waitForBufferReady();
      Future<FileChannel> futureChannel = result.getChannel();
      FileChannel channel = futureChannel.get();
      channel.close();
    }
    long stopTime = System.nanoTime();
    audioWriter.close();
    
    for (int r = 0; r < repetitions; r++) {
      File outFile = new File(testDir, "testWrite_" + r + ".test");
      assertTrue(outFile.delete());
    }
    // how long would the file take if it was encoded in stereo with 44100 samples per second
    double realWorldDurationNano = (samplesWritten * 1E09) / (samplingRate * inputChannelCount);
    System.out.printf("...File size              : %d bytes.%n", samplesWritten * 4);
    System.out.printf("...Real world duration    : %.2f seconds.%n", realWorldDurationNano * 1E-09);
    System.out.printf("...Write Performance      : %.2f units.%n", realWorldDurationNano / (stopTime - startTime));
  }

  /**
   * Test the performance of file buffer handling with a realistic timing.
   *
   * Instead of inserting waitForBufferReady() we insert wait cycles
   * corresponding to the duration of one cycle.
   *
   */
  @Test
  @SuppressWarnings("SleepWhileInLoop")
  public void testRealisticTiming() throws InterruptedException, ExecutionException, IOException {
    System.out.println("testRealisticTiming");
    int samplingRate = 44100 * 20; // by setting 20 times the normal sampling rate, we show that there is enough security margin
    int cycleDurationMillis = 5;
    int nFrames = (samplingRate * cycleDurationMillis) / 1000;
    int inputChannelCount = 2;
    int audioArraySize = inputChannelCount * nFrames;

    // for simplicty we'll make the buffer an exact mutiple of the audio array
    int audioArraysPerBuffer = (Const.fileBufferSizeFloat / audioArraySize);
    int bufferSize = audioArraySize * audioArraysPerBuffer;
    int audioArraysToPut = audioArraysPerBuffer * 3; // we want three buffers to be written
    int framesToProcessPerTest = nFrames * audioArraysToPut;
    int framesTotal = framesToProcessPerTest * 3;
    
    float estimatedTestDuration = (framesTotal) / samplingRate;
    System.out.println("... This test will take about " + estimatedTestDuration + " seconds to terminate.");
    Thread.sleep(100);// time to show the previous message

    File file1 = new File(testDir, "realisticTiming_1.test");
    File file2 = new File(testDir, "realisticTiming_2.test");
    
    float[] audioWriterArray = new float[audioArraySize];
    float[] audioReaderArray = new float[audioArraySize];
    
    AudioWriter audioWriter = new AudioWriter(executor, bufferSize);
    AudioReader audioReader = new AudioReader(executor, bufferSize);

    // write file1
    audioWriter.start(file1);
    for (int i = 0; i < audioArraysToPut; i++) {
      Thread.sleep(cycleDurationMillis);
      audioWriter.putNext(audioWriterArray);
    }
    WriterResult result1 = audioWriter.stop();

    // write file2 and in parallel read file1
    audioReader.start(result1);
    audioWriter.start(file2);
    for (int i = 0; i < audioArraysToPut; i++) {
      Thread.sleep(cycleDurationMillis);
      audioWriter.putNext(audioWriterArray);
      audioReader.getNext(audioReaderArray);
    }
    WriterResult result2 = audioWriter.stop();
    audioReader.stop();

    //  read file2
    audioReader.start(result2);
    for (int i = 0; i < audioArraysToPut; i++) {
      Thread.sleep(cycleDurationMillis);
      audioReader.getNext(audioReaderArray);
    }
    audioReader.stop();
    
    assertEquals(0, audioReader.getOverflowCount());
    assertEquals(0, audioWriter.getOverflowCount());
    
    audioReader.close();
    audioWriter.close();
    
    assertTrue(file1.delete());
    assertTrue(file2.delete());
    
    
  }
}
