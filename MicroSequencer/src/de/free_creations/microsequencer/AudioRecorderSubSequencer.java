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
import de.free_creations.microsequencer.filestreaming.AudioWriter.WriterResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Soundbank;

/**
 *
 * @author Harald Postner
 */
class AudioRecorderSubSequencer implements
        MasterSequencer.AudioRecorderSubSequencerInt,
        AudioProcessor {

  private static final Logger logger = Logger.getLogger(AudioRecorderSubSequencer.class.getName());
  private final File tempDir;
  private File currentTempFile;
  private File previousTempFile;
  private final long minimumFreeFileSpace = 44100 * 2 * 4 * 60 * 4;//four minutes
  private final ExecutorService executor = Executors.newSingleThreadExecutor(
          new ThreadFactory() {
    @Override
    public Thread newThread(Runnable r) {
      Thread thread = new Thread(r);
      thread.setPriority(Thread.MIN_PRIORITY);
      thread.setName("FreeCreationsAudioRecorder");
      return thread;
    }
  });
  private PlayingMode playingMode = PlayingMode.MidiOnly;
  private Exception executionException;
  private float[] outputSamples;
  private float[] nullSamples;
  private float[] balancedInputSamples;
  private boolean mute = false;
  private final AudioReader audioReader;
  private final AudioWriter audioWriter;
  private int inputChannelCount;
  private final String name;
  private int outputChannelCount;
  private int nFrames;
  private final Object processingLock = new Object();

  /**
   * Builds a factory object that provides this implementation as sub-sequencer.
   *
   * @return a factory that can make sub-sequencer objects.
   */
  public static MasterSequencer.SubSequencerFactory getFactory() {
    MasterSequencer.SubSequencerFactory newFactory =
            new MasterSequencer.SubSequencerFactory() {
      @Override
      public MasterSequencer.MidiSubSequencer make(String name, Soundbank soundbank) throws MidiUnavailableException {
        throw new UnsupportedOperationException("Cannot make a MidiSubSequencer.");
      }

      @Override
      public MasterSequencer.AudioRecorderSubSequencerInt makeAudioRecorder(String name) throws IOException {
        return new AudioRecorderSubSequencer(name);
      }
    };
    return newFactory;
  }
  private int processInCount = 0; // (debugging variable) the number of times processIn was called within one session
  private int processOutCount = 0; // (debugging variable) the number of times processOut was called within one session
  private AudioWriter.WriterResult writerResult = null;
  private int latency;
  private int samplingRate;
  private int previousSwitchSample;

  void setMute(boolean value) {
    mute = value;
  }

  /**
   * Creates a new audio recorder.
   *
   * @throws IOException if a suitable temporary file could not be allocated.
   */
  public AudioRecorderSubSequencer(String name) throws IOException {
    this(name, Files.createTempDirectory("Repetitor").toFile(), true);
  }

  /**
   * constructor for test.
   *
   * @param tempDir
   * @param deleteTempFilesOnExit if false, temporary file are not deleted.
   * @throws IOException
   */
  AudioRecorderSubSequencer(String name, File tempDir, boolean deleteTempFilesOnExit) throws IOException {
    if (!tempDir.exists()) {
      throw new IOException(tempDir.getAbsolutePath() + " does not exist.");
    }
    if (!tempDir.isDirectory()) {
      throw new IOException(tempDir.getAbsolutePath() + " is not a directory.");
    }
    if (!tempDir.canWrite()) {
      throw new IOException("Cannot write on " + tempDir.getAbsolutePath());
    }
    if (tempDir.getFreeSpace() < minimumFreeFileSpace) {
      throw new IOException("There is not enough free space on "
              + tempDir.getAbsolutePath() + ". Requested:" + minimumFreeFileSpace / 1000
              + "kB. Available: "
              + tempDir.getFreeSpace() + "kB.");
    }
    this.audioReader = new AudioReader(executor);
    this.audioWriter = new AudioWriter(executor);

    this.tempDir = tempDir;
    this.name = name;

    this.currentTempFile = new File(tempDir, "RepetitorTmp1.raw");
    this.previousTempFile = new File(tempDir, "RepetitorTmp2.raw");
    if (deleteTempFilesOnExit) {
      currentTempFile.deleteOnExit();
      previousTempFile.deleteOnExit();
      tempDir.deleteOnExit();

    }
  }

  String getTempDir() {
    return tempDir.getAbsolutePath();
  }

  String getTempFile() {
    return currentTempFile.getAbsolutePath();
  }

  /**
   * Gets called once when the Audio System is about to open. The calling thread
   * is not time-critical, we can do blocking operations.
   *
   * There is nothing to do here.
   *
   * @param samplingRate
   * @param nFrames
   * @param outputChannelCount
   * @param noninterleaved
   * @throws MidiUnavailableException
   */
  @Override
  public void open(int samplingRate, int nFrames, int inputChannelCount, int outputChannelCount, boolean noninterleaved, long latency) {
    synchronized (processingLock) {
      this.samplingRate = samplingRate;
      this.inputChannelCount = inputChannelCount;
      this.outputChannelCount = outputChannelCount;
      this.nFrames = nFrames;
      outputSamples = new float[nFrames * outputChannelCount];
      balancedInputSamples = new float[nFrames * outputChannelCount];
      nullSamples = new float[nFrames * outputChannelCount];
      Arrays.fill(outputSamples, 0F);
      Arrays.fill(nullSamples, 0F);
      Arrays.fill(balancedInputSamples, 0F);
      processInCount = 0;
      processOutCount = 0;
      this.latency = (int) latency;


      logger.log(Level.FINER, "## AudioRecorderSubSequencer opened");
      logger.log(Level.FINER, "... inputChannelCount : {0}", inputChannelCount);
      logger.log(Level.FINER, "... outputChannelCount: {0}", outputChannelCount);
      logger.log(Level.FINER, "... latency           : {0}", latency);
    }
  }

  /**
   * Gets called once when the Audio System has been closed. The calling thread
   * is not time-critical, we can do blocking operations.
   *
   * If during ProcessOut there was an error we'll throw the exception here.
   */
  @Override
  public void close() {
    synchronized (processingLock) {
      throwAndClearExecutionException();
    }
  }

  /**
   * Can be used to test if a buffer overflow occurred during processing.
   *
   * @throws RuntimeException if there was a problem in processing.
   */
  public void throwAndClearExecutionException() {
    if (executionException != null) {
      Exception ex = executionException;
      executionException = null;
      throw new RuntimeException(ex);
    }
  }

  /**
   * Gets called once when the Audio System is about to start. After returning
   * from this call, the object must be able to handle the processXX calls.
   *
   * There is nothing to do here.
   *
   */
  @Override
  public void start() {
  }

  /**
   * Gets called once when the Audio System has stopped.
   *
   * There is nothing to do here.
   */
  @Override
  public void stop() {
  }

  @Override
  public float[] process(double streamTime, float[] input) {
    synchronized (processingLock) {
      processIn(streamTime, input);
      return processOut(streamTime);
    }
  }

  /**
   * Gets called on each cycle. The calling thread is time-critical, no blocking
   * operations allowed here.
   *
   * If the current mode of operations is "PlayAudio", read samples from the
   * temp file.
   *
   * @param streamTime
   * @return
   * @throws Exception
   */
  private float[] processOut(double streamTime) {
    processOutCount++;
    if (mute) {
      return nullSamples;
    }
    switch (playingMode) {
      case MidiOnly:
        return nullSamples;
      case RecordAudio:
        return nullSamples;
      case PlayAudio:
        audioReader.getNext(outputSamples);
        break;
      case PlayRecordAudio:
        audioReader.getNext(outputSamples);
        break;
    }

    return outputSamples;
  }

  /**
   * Gets called on each cycle. The calling thread is time-critical, no blocking
   * operations allowed here.
   *
   * If the current mode of operations is "RecordAudio", write the given samples
   * to the temp file.
   *
   * @param streamTime
   * @param samples
   * @throws Exception
   */
  private void processIn(double streamTime, float[] samples) {
    processInCount++;
    if (samples == null) {
      return;
    }
    if (inputChannelCount <= 0) {
      return;
    }
    switch (playingMode) {
      case MidiOnly:
        return;
      case RecordAudio:
        audioWriter.putNext(balanceChannels(samples));
        break;
      case PlayAudio:
        return;
      case PlayRecordAudio:
        audioWriter.putNext(balanceChannels(samples));
        break;
    }

  }

  private float[] balanceChannels(float[] samples) {
    assert (samples != null);
    assert (samples.length == inputChannelCount * nFrames);

    if (outputChannelCount == inputChannelCount) {
      return samples;
    }

    if (inputChannelCount > outputChannelCount) {
      for (int frame = 0; frame < nFrames; frame++) {
        for (int channel = 0; channel < outputChannelCount; channel++) {
          balancedInputSamples[(frame * outputChannelCount) + channel] =
                  samples[(frame * inputChannelCount) + channel];
        }
      }
      return balancedInputSamples;
    } else {
      //(outputChannelCount >inputChannelCount)
      for (int frame = 0; frame < nFrames; frame++) {
        for (int channel = 0; channel < outputChannelCount; channel++) {
          int inChannel = channel;
          if (channel >= inputChannelCount) {
            inChannel = inputChannelCount - 1;
          }
          balancedInputSamples[(frame * outputChannelCount) + channel] =
                  samples[(frame * inputChannelCount) + inChannel];
        }
      }
      return balancedInputSamples;
    }
  }

  /**
   * Gets called from the sequencer when a new session is started. The calling
   * thread is time-critical, no blocking operations allowed here.
   *
   * Depending on the PlayingMode start the appropriate file-streamer.
   *
   * @param startTick
   * @param mode
   */
  @Override
  public void prepareSession(double startTick, PlayingMode mode) {
    synchronized (processingLock) {
      playingMode = mode;
      if (outputSamples != null) {
        Arrays.fill(outputSamples, 0F);
        Arrays.fill(nullSamples, 0F);
      }
      switch (playingMode) {
        case MidiOnly:
          logger.log(Level.FINER, "### prepareSession: MidiOnly");
          return;
        case RecordAudio:
          logger.log(Level.FINER, "### prepareSession: RecordAudio");
          audioWriter.start(currentTempFile);
          File usedFile = currentTempFile;
          currentTempFile = previousTempFile;
          previousTempFile = usedFile;
          return;
        case PlayAudio:
          if (writerResult != null) {
            logger.log(Level.FINER, "### prepareSession: PlayAudio");
            try {
              audioReader.start(writerResult);
            } catch (IOException ex) {
              logger.log(Level.SEVERE, null, ex);
            }
            int delayLatency = latency * outputChannelCount;
            audioReader.skip(delayLatency);
          }
          return;
        case PlayRecordAudio:
          writerResult = null;
          previousSwitchSample = 0;
          logger.log(Level.FINER, "### prepareSession: PlayRecordAudio");
      }

    }
  }

  /**
   * Gets called when the session has stopped.
   *
   * The calling thread is time-critical, no blocking operations allowed here.
   *
   */
  @Override
  public void stopSession() {
    synchronized (processingLock) {
      if (outputSamples != null) {
        Arrays.fill(outputSamples, 0F);
        Arrays.fill(nullSamples, 0F);
      }
      switch (playingMode) {
        case MidiOnly:
          logger.log(Level.FINER, "### stopSession: MidiOnly");
          return;
        case RecordAudio:
          logger.log(Level.FINER, "### stopSession: RecordAudio");
          writerResult = audioWriter.stop();
          return;
        case PlayAudio:
          logger.log(Level.FINER, "### stopSession: PlayAudio");
          if (audioReader.isStarted()) {
            audioReader.stop();
          }
          return;
        case PlayRecordAudio:
          logger.log(Level.FINER, "### stopSession: PlayRecordAudio");
          if (audioReader.isStarted()) {
            audioReader.stop();
          }
          if (audioWriter.isStarted()) {
            audioWriter.stop();
          }
          writerResult = null;
      }
    }
  }

  public WriterResult getWriterResult() {
    synchronized (processingLock) {
      return writerResult;
    }
  }

  public void waitForWriterReady() throws InterruptedException, ExecutionException {
    audioWriter.waitForBufferReady();
  }

  public void waitForReaderReady() throws InterruptedException, ExecutionException {
    audioReader.waitForBufferReady();
  }

  @Override
  public String toString() {
    return "AudioRecorderSubSequencer{" + "name=" + name + '}';
  }

  @Override
  public void prepareSwitch(double switchPoint) {
    synchronized (processingLock) {
      if (playingMode != PlayingMode.PlayRecordAudio) {
        return;
      }
      int switchSample = ((int) (samplingRate * switchPoint) * outputChannelCount);

      logger.log(Level.FINER, ">>>>### prepareSwitch: {0}", switchSample);

      if (audioReader.isStarted()) {
        audioReader.stop();
      }
      if (audioWriter.isStarted()) {
        writerResult = audioWriter.stop();
      }

      audioWriter.start(currentTempFile);
      try {
        audioReader.start(writerResult);

        int delayLatency = latency * outputChannelCount;
        int exactDelay = delayLatency + previousSwitchSample - switchSample;
        if (exactDelay > 0) {
          audioReader.skip(exactDelay);
        } else {
          audioReader.skip(delayLatency);
        }
      } catch (IOException ex) {
        logger.log(Level.SEVERE, null, ex);
      }
      File usedFile = currentTempFile;
      currentTempFile = previousTempFile;
      previousTempFile = usedFile;
      previousSwitchSample = switchSample;

    }
  }
}
