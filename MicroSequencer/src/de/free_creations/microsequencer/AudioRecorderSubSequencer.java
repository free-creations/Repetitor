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
import de.free_creations.microsequencer.filestreaming.AudioReader;
import de.free_creations.microsequencer.filestreaming.AudioWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Harald Postner
 */
class AudioRecorderSubSequencer implements
        MasterSequencer.SubSequencer,
        AudioProducer, AudioConsumer {

  private static final Logger logger = Logger.getLogger(AudioRecorderSubSequencer.class.getName());
  private final File tempDir;
  private final File tempFile;
  private final long minimumFreeSpace = 44100 * 2 * 4 * 60 * 4;
  private final ExecutorService executor = Executors.newSingleThreadExecutor(
          new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
              Thread thread = new Thread(r);
              thread.setPriority(Thread.NORM_PRIORITY);
              thread.setName("FreeCreationsAudioRecorder");
              return thread;
            }
          });
  private Future<AudioWriter> writer = null;
  private Future<AudioReader> reader = null;
  private PlayingMode playingMode = PlayingMode.MidiOnly;
  private ExecutionException executionException;
  private float[] outputSamples;

  private class WriterCreationTask implements Callable<AudioWriter> {

    private final File outputFile;
    private final Future<AudioWriter> previousWriter;
    private final Future<AudioReader> previousReader;

    public WriterCreationTask(File outputFile,
            Future<AudioWriter> previousWriter,
            Future<AudioReader> previousReader) {
      this.outputFile = outputFile;
      this.previousReader = previousReader;
      this.previousWriter = previousWriter;
    }

    @Override
    public AudioWriter call() throws FileNotFoundException,
            IOException, ExecutionException, InterruptedException {
      // make sure that previously used readers and writers are correctly closed
      if (previousReader != null) {
        previousReader.get().close();
      }
      if (previousWriter != null) {
        previousWriter.get().close();
      }
      return new AudioWriter(outputFile);
    }
  }

  private class ReaderCreationTask implements Callable<AudioReader> {

    private final File outputFile;
    private final Future<AudioWriter> previousWriter;
    private final Future<AudioReader> previousReader;

    public ReaderCreationTask(File outputFile,
            Future<AudioWriter> previousWriter,
            Future<AudioReader> previousReader) {
      this.outputFile = outputFile;
      this.previousReader = previousReader;
      this.previousWriter = previousWriter;
    }

    @Override
    public AudioReader call() throws FileNotFoundException,
            IOException, ExecutionException, InterruptedException {
      // make sure that previously used readers and writers are correctly closed
      if (previousReader != null) {
        previousReader.get().close();
      }
      if (previousWriter != null) {
        previousWriter.get().close();
      }
      return new AudioReader(outputFile);
    }
  }

  private class ClosingTask implements Callable<Void> {

    private final Future<AudioWriter> closingWriter;
    private final Future<AudioReader> closingReader;

    public ClosingTask(
            Future<AudioWriter> closingWriter,
            Future<AudioReader> closingReader) {

      this.closingWriter = closingWriter;
      this.closingReader = closingReader;
    }

    @Override
    public Void call() throws InterruptedException, InterruptedException, ExecutionException, IOException {
      // close reader and writer
      if (closingReader != null) {
        closingReader.get().close();
      }
      if (closingWriter != null) {
        closingWriter.get().close();
      }
      return null;
    }
  }

  /**
   * Creates a new audio recorder.
   *
   * @throws IOException if a suitable temporary file could not be allocated.
   */
  public AudioRecorderSubSequencer() throws IOException {
    this(Files.createTempDirectory("Repetitor").toFile(), true);
  }

  /**
   * constructor for test.
   *
   * @param tempDir
   * @param deleteTempFilesOnExit if false, temporary file are not deleted.
   * @throws IOException
   */
  AudioRecorderSubSequencer(File tempDir, boolean deleteTempFilesOnExit) throws IOException {
    if (!tempDir.exists()) {
      throw new IOException(tempDir.getAbsolutePath() + " does not exist.");
    }
    if (!tempDir.isDirectory()) {
      throw new IOException(tempDir.getAbsolutePath() + " is not a directory.");
    }
    if (!tempDir.canWrite()) {
      throw new IOException("Cannot write on " + tempDir.getAbsolutePath());
    }
    if (tempDir.getFreeSpace() < minimumFreeSpace) {
      throw new IOException("There is not enough free space on "
              + tempDir.getAbsolutePath() + ". Requested:" + minimumFreeSpace / 1000
              + "kB. Available: "
              + tempDir.getFreeSpace() + "kB.");
    }

    this.tempDir = tempDir;

    this.tempFile = new File(tempDir, "RepetitorTmp.raw");
    if (deleteTempFilesOnExit) {
      tempDir.deleteOnExit();
      tempFile.deleteOnExit();
    }
  }

  String getTempDir() {
    return tempDir.getAbsolutePath();
  }

  String getTempFile() {
    return tempFile.getAbsolutePath();
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
  public void openOut(int samplingRate, int nFrames, int outputChannelCount, boolean noninterleaved) {
    outputSamples = new float[nFrames * outputChannelCount];
    Arrays.fill(outputSamples, 0F);
  }

  /**
   * Gets called once when the Audio System is about to open. The calling thread
   * is not time-critical, we can do blocking operations.
   *
   *
   * @ToDo there is redundancy with openOut
   * @param samplingRate
   * @param nFrames
   * @param inputChannelCount
   * @param noninterleaved
   * @throws MidiUnavailableException
   */
  @Override
  public void openIn(int samplingRate, int nFrames, int inputChannelCount, boolean noninterleaved) {
  }

  /**
   * Gets called once when the Audio System has been closed. The calling thread
   * is not time-critical, we can do blocking operations.
   *
   * If during ProcessOut there was an error we'll throw the exception here.
   */
  @Override
  public void closeOut() {
    if (executionException != null) {
      ExecutionException ex = executionException;
      executionException = null;
      throw new RuntimeException(ex);
    }
  }

  /**
   * Gets called once when the Audio System has been closed. The calling thread
   * is not time-critical, we can do blocking operations.
   *
   * @ToDo there is redundancy with closeOut
   */
  @Override
  public void closeIn() {
    if (executionException != null) {
      ExecutionException ex = executionException;
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
  public void startOut() {
  }

  /**
   * Gets called once when the Audio System is about to start. After returning
   * from this call, the object must be able to handle the processXX calls.
   *
   * There is nothing to do here.
   *
   * @ToDo there is redundancy with startOut
   */
  @Override
  public void startIn() {
  }

  /**
   * Gets called once when the Audio System has stopped.
   *
   * There is nothing to do here.
   */
  @Override
  public void stopOut() {
  }

  /**
   * Gets called once when the Audio System has stopped.
   *
   * There is nothing to do here.
   *
   * @ToDo there is redundancy with stopOut
   */
  @Override
  public void stopIn() {
  }

  /**
   * Gets called on each cycle. The calling thread is time-critical, no blocking
   * operations allowed here.
   *
   * If the current mode of operations is "Replay", read samples from the temp
   * file.
   *
   * @param streamTime
   * @return
   * @throws Exception
   */
  @Override
  public float[] processOut(double streamTime) {
    if (playingMode != PlayingMode.Replay) {

      return outputSamples;
    }
    if (reader == null) {
      executionException = new ExecutionException(new NullPointerException("reader is null"));
      Arrays.fill(outputSamples, 0F);
      return outputSamples;
    }
    try {
      reader.get(0, TimeUnit.MILLISECONDS).getNext(outputSamples);
    } catch (InterruptedException | ExecutionException ex) {
      Arrays.fill(outputSamples, 0F);
      executionException = new ExecutionException(ex);
    } catch (TimeoutException ex) {
      Arrays.fill(outputSamples, 0F);
      logger.log(Level.WARNING, "Read-Buffer underrun.");
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
  @Override
  public void processIn(double streamTime, float[] samples) {
    if (playingMode != PlayingMode.RecordAudio) {
      return;
    }
    if (writer == null) {
      executionException = new ExecutionException(new NullPointerException("writer is null"));
      return;
    }
    try {
      writer.get(0, TimeUnit.MILLISECONDS).putNext(samples);
    } catch (InterruptedException | ExecutionException | TimeoutException ex) {
      executionException = new ExecutionException(ex);
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
    playingMode = mode;
    if (outputSamples != null) {
      Arrays.fill(outputSamples, 0F);
    }
    switch (playingMode) {
      case MidiOnly:
        return;
      case RecordAudio:
        writer = executor.submit(new WriterCreationTask(tempFile, writer, reader));
        return;
      case Replay:
        if (tempFile.exists()) {
          reader = executor.submit(new ReaderCreationTask(tempFile, writer, reader));
        }
    }
  }

  /**
   * Gets called when the session has stopped. The calling thread is
   * time-critical, no blocking operations allowed here.
   *
   */
  @Override
  public void stopSession() {
    if (outputSamples != null) {
      Arrays.fill(outputSamples, 0F);
    }
    executor.submit(new ClosingTask(writer, reader));
  }
}
