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

import de.free_creations.microsequencer.filestreaming.AudioReader;
import de.free_creations.microsequencer.filestreaming.AudioWriter;
import de.free_creations.microsequencer.filestreaming.Const;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
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
    AudioRecorderSubSequencer instance = new AudioRecorderSubSequencer("Test");
    System.out.println("...TempDir:" + instance.getTempDir());
  }

  /**
   * Test of open method, of class AudioRecorderSubSequencer.
   */
  @Test
  @Ignore("OpenOut is empty")
  public void testOpenOut() throws Exception {
  }

  /**
   * Test of close method, of class AudioRecorderSubSequencer.
   */
  @Test
  @Ignore("method is trivial")
  public void testCloseOut() {
  }

  /**
   * Test of start method, of class AudioRecorderSubSequencer.
   */
  @Test
  @Ignore("method is empty")
  public void testStartOut() {
  }

  /**
   * Test of stop method, of class AudioRecorderSubSequencer.
   */
  @Test
  @Ignore("method is empty")
  public void testStopOut() {
  }

  /**
   * Test of process method, of class AudioRecorderSubSequencer.
   *
   * Specification: If the current mode of operations is "PlayAudio", write the
   * samples to the temp file.
   */
  @Test
  public void testProcessOut() throws Exception {
    System.out.println("processOut");
    // limit the file to one buffer. So to aviod buffer underruns when no audio real processing is done.
    int fileSizeFloat = Const.fileBufferSizeFloat;
    int samplingRate = 44100;
    int nFrames = 256;
    int inputChannelCount = 2;
    int outputChannelCount = 2;
    boolean noninterleaved = false;
    int audioArraySize = inputChannelCount * nFrames;
    float[] audioArray = new float[audioArraySize];
    int bufferCount = fileSizeFloat / audioArraySize;

    AudioRecorderSubSequencer instance = new AudioRecorderSubSequencer("Test");
    instance.open(samplingRate, nFrames, inputChannelCount, outputChannelCount, noninterleaved);


    //Here we procees to the test: we write the index position of each sample to the file
    instance.prepareSession(0, PlayingMode.RecordAudio);
    Thread.sleep(100); //<<< that's very sad
    long writeStartTime = System.nanoTime();
    int floatsWritten = 0;
    for (int i = 0; i < bufferCount; i++) {
      for (int sample = 0; sample < audioArraySize; sample++) {
        audioArray[sample] = floatsWritten;
        floatsWritten++;
      }
      instance.processIn(-1, audioArray);
    }
    long writeStopTime = System.nanoTime();
    instance.stopSession();
    instance.checkAndClearExecutionException();
    Thread.sleep(100); //<<< that's very sad

    // To check the file we use the function "PlayAudio" that has proven to be correct in testProcessIn()
    instance.prepareSession(0, PlayingMode.PlayAudio);
    Thread.sleep(200); //<<< that's very sad
    boolean more = true;
    int floatsRead = 0;

    long readStartTime = System.nanoTime();
    while (more) {
      float[] producedSamples = instance.process(-1, null);
      for (float sample : producedSamples) {
        if (floatsRead < floatsWritten) {
          assertEquals(floatsRead, (long) sample);
          floatsRead++;
        } else {
          assertEquals(sample, 0F, 0F);
          more = false;
        }
      }
    }
    long readStopTime = System.nanoTime();

    instance.stopSession();
    instance.close();

    // how long would the file take if it was encoded in stereo with 44100 samples per second
    double fileDurationNano = (floatsWritten * 1E09) / (samplingRate * inputChannelCount);
    System.out.printf("...File size        : %d bytes.%n", floatsWritten * 4);
    System.out.printf("...File duration    : %f seconds.%n", fileDurationNano * 1E-09);
    System.out.printf("...Write Performance: %f units.%n", fileDurationNano / (writeStopTime - writeStartTime));
    System.out.printf("...Read Performance : %f units.%n", fileDurationNano / (readStopTime - readStartTime));


  }

  /**
   * Test that always a number corresponding to the output channels is recorded.
   *
   * Specification: If there are n input channels and m output channels and if n
   * is larger than m, only the m first input channels are recorded.
   */
  @Test
  public void testUnbalancedChannels1() throws Exception {
    System.out.println("testUnbalancedChannels");
    int samplingRate = 44100;
    int nFrames = 256;
    int inputChannelCount = 7;
    int outputChannelCount = 2;
    boolean noninterleaved = false;
    int inAudioArraySize = inputChannelCount * nFrames;
    float[] inAudioArray = new float[inAudioArraySize];
    int outAudioArraySize = outputChannelCount * nFrames;
    int bufferCount = 10;//fileSizeFloat / outAudioArraySize;

    AudioRecorderSubSequencer instance = new AudioRecorderSubSequencer("Test");
    instance.open(samplingRate, nFrames, inputChannelCount, outputChannelCount, noninterleaved);


    //Here we proceed to the test: in each sample we'll write its channel
    instance.prepareSession(0, PlayingMode.RecordAudio);
    Thread.sleep(100);
    int sample;
    for (int i = 0; i < bufferCount; i++) {
      sample = 0;
      for (int frame = 0; frame < nFrames; frame++) {
        for (int channel = 0; channel < inputChannelCount; channel++) {
          inAudioArray[sample] = channel;
          sample++;
        }
      }
      instance.processIn(-1, inAudioArray);
    }
    instance.stopSession();
    instance.checkAndClearExecutionException();


    // Here we verify only the first two channels have been recorded
    instance.prepareSession(0, PlayingMode.PlayAudio);
    Thread.sleep(100);

    for (int i = 0; i < bufferCount; i++) {
      float[] producedSamples = instance.process(-1, null);
      assertEquals(outAudioArraySize, producedSamples.length);
      sample = 0;
      for (int frame = 0; frame < nFrames; frame++) {
        for (int channel = 0; channel < outputChannelCount; channel++) {
          assertEquals("Error buffer(" + i + "), "
                  + "frame(" + frame
                  + "), channel(" + channel + ")",
                  channel, (int) producedSamples[sample]);
          sample++;
        }
      }
    }

    instance.stopSession();
    instance.close();
  }

  /**
   * Test that always a number corresponding to the output channels is recorded.
   *
   * Specification: If there are n input channels and m output channels and if m
   * is larger than n, only the highest input channel is repeated so to record
   * m channels.
   */
  @Test
  public void testUnbalancedChannels2() throws Exception {
    System.out.println("testUnbalancedChannels2");
    int samplingRate = 44100;
    int nFrames = 256;
    int inputChannelCount = 2;
    int outputChannelCount = 7;
    boolean noninterleaved = false;
    int inAudioArraySize = inputChannelCount * nFrames;
    float[] inAudioArray = new float[inAudioArraySize];
    int outAudioArraySize = outputChannelCount * nFrames;
    int bufferCount = 10;//fileSizeFloat / outAudioArraySize;

    AudioRecorderSubSequencer instance = new AudioRecorderSubSequencer("Test");
    instance.open(samplingRate, nFrames, inputChannelCount, outputChannelCount, noninterleaved);


    //Here we proceed to the test: in each sample we'll write its channel
    instance.prepareSession(0, PlayingMode.RecordAudio);
    Thread.sleep(100);
    int sample;
    for (int i = 0; i < bufferCount; i++) {
      sample = 0;
      for (int frame = 0; frame < nFrames; frame++) {
        for (int channel = 0; channel < inputChannelCount; channel++) {
          inAudioArray[sample] = channel;
          sample++;
        }
      }
      instance.processIn(-1, inAudioArray);
    }
    instance.stopSession();
    instance.checkAndClearExecutionException();


    // Here we verify the second channel has been repeated
    instance.prepareSession(0, PlayingMode.PlayAudio);
    Thread.sleep(100);

    for (int i = 0; i < bufferCount; i++) {
      float[] producedSamples = instance.process(-1, null);
      assertEquals(outAudioArraySize, producedSamples.length);
      sample = 0;
      for (int frame = 0; frame < nFrames; frame++) {
        for (int channel = 0; channel < outputChannelCount; channel++) {
          int expected = channel;
          if(channel >= inputChannelCount ){
            expected = inputChannelCount-1;
          }
          assertEquals("Error buffer(" + i + "), "
                  + "frame(" + frame
                  + "), channel(" + channel + ")",
                  expected, (int) producedSamples[sample]);
          sample++;
        }
      }
    }

    instance.stopSession();
    instance.close();


  }

  /**
   * Test of openIn method, of class AudioRecorderSubSequencer.
   */
  @Test
  @Ignore("method is empty")
  public void testOpenIn() throws Exception {
  }

  /**
   * Test of closeIn method, of class AudioRecorderSubSequencer. Specification:
   */
  @Test
  @Ignore("method is trivial")
  public void testCloseIn() {
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
   *
   * Specification: if the current mode of operations is "PlayAudio", read the
   * samples to the temp file.
   */
  @Test
  public void testProcessIn() throws Exception {
    System.out.println("testProcessIn");
    int samplingRate = 44100;
    int nFrames = 256;
    int outputChannelCount = 2;
    int inputChannelCount = 2;
    boolean noninterleaved = false;
    // prepare an input file.
    // The input file will be in such that the value of each sample will be it's index.
    AudioRecorderSubSequencer instance = new AudioRecorderSubSequencer("Test");
    File inputFile = new File(instance.getTempFile());
    long mtfStart = System.nanoTime();
    long fileSizeFloat = makeTestFile(inputFile);
    long mtfEnd = System.nanoTime();
    assertTrue(inputFile.exists());
    // how long would the file take if it was encoded in stereo with 44100 samples per second
    double fileDurationNano = (fileSizeFloat * 1E09) / (samplingRate * outputChannelCount);

    // read the file and check the audio buffer
    instance.open(samplingRate, nFrames, inputChannelCount, outputChannelCount, noninterleaved);
    instance.prepareSession(0, PlayingMode.PlayAudio);

    Thread.sleep(100);


    // here we proceed to the test.
    boolean more = true;
    float floatCout = 0;
    long startTime = System.nanoTime();
    while (more) {
      float[] audioArray = instance.process(-1, null);
      for (float sample : audioArray) {
        if (floatCout < fileSizeFloat) {
          // verify that the value of each sample is its index (as prepared above).
          if (floatCout != sample) {
            fail("Sample(" + floatCout + ") was " + sample);
          }
          floatCout++;
        } else {
          assertEquals(sample, 0F, 0F);
          more = false;
        }
      }
    }
    long endTime = System.nanoTime();
    instance.stopSession();
    instance.close();
    System.out.printf("...Creating the test file: %f seconds.%n", 1E-9 * (mtfEnd - mtfStart));
    System.out.printf("...File size             : %d bytes.%n", fileSizeFloat * 4);
    System.out.printf("...File duration         : %f seconds.%n", fileDurationNano * 1E-09);
    System.out.printf("...Read Performance      : %f units.%n", fileDurationNano / (endTime - startTime));
  }

  /**
   * Verify that the AudioRecorderSubSequencer handles a non- existent input
   * file.
   *
   * Specification: if no temporary data has been written, "PlayAudio" must
   * nevertheless be able to run, the returned samples must be all be 0.0F
   */
  @Test
  public void testProcessIn_NoInputFile() throws Exception {
    System.out.println("testProcessInEmpty");
    int samplingRate = 44100;
    int nFrames = 256;
    int outputChannelCount = 2;
    int inputChannelCount = 2;
    boolean noninterleaved = false;
    File tempDir = Files.createTempDirectory("EmptyTest").toFile();
    assertTrue(tempDir.exists());
    assertTrue(tempDir.isDirectory());
    assertTrue(tempDir.list().length == 0);

    AudioRecorderSubSequencer instance = new AudioRecorderSubSequencer("Test", tempDir, true);


    // read the file and check the audio buffer
    instance.open(samplingRate, nFrames, inputChannelCount, outputChannelCount, noninterleaved);
    instance.start();
    instance.prepareSession(0, PlayingMode.PlayAudio);

    Thread.sleep(100);
    // here we proceed to the test.
    float floatCout = 0;
    for (int i = 0; i < 1000; i++) {
      float[] audioArray = instance.process(-1, null);
      for (float sample : audioArray) {

        if (0.0F != sample) {
          fail("Sample(" + floatCout + ") was " + sample);
        }
        floatCout++;
      }
    }

    instance.stopSession();
    instance.close();
  }

  /**
   * Verify that the AudioRecorderSubSequencer handles an empty input file.
   *
   * Specification: if the temporary file is empty, "PlayAudio" must
   * nevertheless be able to run, the returned samples must be all be 0.0F
   */
  @Test
  public void testProcessInEmpty() throws Exception {
    System.out.println("testProcessInEmpty");
    int samplingRate = 44100;
    int nFrames = 256;
    int outputChannelCount = 2;
    int inputChannelCount = 2;
    boolean noninterleaved = false;

    AudioRecorderSubSequencer instance = new AudioRecorderSubSequencer("Test");
    File inputFile = new File(instance.getTempFile());
    FileOutputStream tempFile = new FileOutputStream(inputFile);
    tempFile.close();
    assertTrue(inputFile.exists());

    // read the file and check the audio buffer
    instance.open(samplingRate, nFrames, inputChannelCount, outputChannelCount, noninterleaved);
    instance.start();
    instance.prepareSession(0, PlayingMode.PlayAudio);

    Thread.sleep(100);
    // here we proceed to the test.
    float floatCout = 0;
    for (int i = 0; i < 1000; i++) {
      float[] audioArray = instance.process(-1, null);
      for (float sample : audioArray) {

        if (0.0F != sample) {
          fail("Sample(" + floatCout + ") was " + sample);
        }
        floatCout++;
      }
    }

    instance.stopSession();
    instance.close();
  }

  /**
   * Test of prepareSession method, of class AudioRecorderSubSequencer.
   * Specification: Depending on the PlayingMode start the appropriate
   * file-streamer.
   */
  @Test
  @Ignore("tested in testProcessIn and testProcessOut")
  public void testPrepareSession() {
  }

  /**
   * Test of stopSession method, of class AudioRecorderSubSequencer.
   *
   * Specification:
   */
  @Test
  @Ignore("tested in testProcessIn and testProcessOut")
  public void testStopSession() {
  }

  /**
   * Test of getTempDir method, of class AudioRecorderSubSequencer.
   */
  @Test
  @Ignore("method is trivial")
  public void testGetTempDir() {
  }

  private long makeTestFile(File file) throws FileNotFoundException, IOException {

    // limit the file to one buffer. So to aviod buffer underruns when no audio real processing is done.
    long fileSizeFloat = Const.fileBufferSizeFloat/10;

    FileOutputStream outFile = new FileOutputStream(file);
    try (FileChannel outChannel = outFile.getChannel()) {
      java.nio.ByteBuffer byteBuffer = java.nio.ByteBuffer.allocateDirect(Const.bytesPerFloat).order(ByteOrder.LITTLE_ENDIAN);

      for (long i = 0; i < fileSizeFloat; i++) {
        float nextFloat = (float) i;
        byteBuffer.clear();
        byteBuffer.putFloat(nextFloat);
        byteBuffer.flip();
        outChannel.write(byteBuffer);
      }

      outChannel.close();
    }
    return fileSizeFloat;
  }

  /**
   * Test of getTempFile method, of class AudioRecorderSubSequencer.
   */
  @Test
  @Ignore("method is trivial")
  public void testGetTempFile() {
  }
}
