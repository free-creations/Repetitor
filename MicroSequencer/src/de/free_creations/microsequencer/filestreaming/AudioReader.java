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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Harald Postner
 */
public class AudioReader {

  private static final Logger logger = Logger.getLogger(AudioReader.class.getName());
  /**
   * The Current-FloatBuffer provides the data for the procedure "getNext()".
   * The Buffer can be accessed when it is ready to be retrieved. The access
   * will be blocked during the time the data is being read from file.
   */
  private Future<FloatBuffer> currentBuffer;
  /**
   * The Next-FloatBuffer is the FloatBuffer that is currently in preparation.
   */
  private Future<FloatBuffer> nextBuffer;
  /**
   * The processing lock protects the procedures start() stop() and getNext()
   * against parallel access.
   */
  private final Object processingLock = new Object();
  private final ByteBuffer byteBuffer1;
  private final ByteBuffer byteBuffer2;
  /**
   * The current-byte-byteBuffer points to the ByteBuffer underlying the
   * currentBuffer.
   */
  private ByteBuffer currentByteBuffer;
  /**
   * The next-byte-byteBuffer points to the ByteBuffer underlying the
   * nextBuffer.
   */
  private ByteBuffer nextByteBuffer;
  /**
   * The number of samples to read, as requested in start().
   */
  private int samplesToProcess = 0;
  /**
   * The number of samples processed so far. This is like a pointer into the
   * input file. The pointer always points one sample after the last sample
   * taken from the file. Note this is not always equal to number of samples
   * delivered, because we might deliver null samples when Buffer-underflows
   * happen.
   */
  private int samplesProcessed = 0;
  /**
   * The number of samples delivered by the getNext() methods. This pointer
   * counts also the samples that could not be processed and were replaced by
   * null samples.
   */
  private int samplesDelivered = 0;
  private boolean started = false;
  private boolean closed = false;
  private Future<FileChannel> fileInput;
  private final ExecutorService executor;
  private int overflowCount = 0;

  private class RealizedAudioBuffer implements Future<FloatBuffer> {

    private final FloatBuffer audioBuffer;

    public RealizedAudioBuffer(FloatBuffer audioBuffer) {
      this.audioBuffer = audioBuffer;
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
    public FloatBuffer get() throws InterruptedException, ExecutionException {
      return audioBuffer;
    }

    @Override
    public FloatBuffer get(long timeout, TimeUnit unit) {
      return audioBuffer;
    }
  }

  /**
   * The FileReadTask takes a Byte-Buffer and fills it with audio data from the
   * file.
   */
  private class FileReadTask implements Callable<FloatBuffer> {

    private final ByteBuffer byteBuffer;
    private final Future<FileChannel> channel;

    public FileReadTask(Future<FileChannel> channel, ByteBuffer buffer) {
      this.byteBuffer = buffer;
      this.channel = channel;
    }

    @Override
    public FloatBuffer call() throws IOException, InterruptedException, ExecutionException {
      //read from the channel
      byteBuffer.clear();
      FileChannel filechannel = channel.get();
      if (!filechannel.isOpen()) {
        throw new IOException("filechannel is not open");
      }
      filechannel.read(byteBuffer);
      byteBuffer.flip();
      FloatBuffer result = byteBuffer.asFloatBuffer();
      return result;
    }
  }

  /**
   * The FileClosingTask closes the given channel.
   */
  private class FileClosingTask implements Callable<Void> {

    private final Future<FileChannel> channel;

    public FileClosingTask(Future<FileChannel> channel) {
      this.channel = channel;
    }

    @Override
    public Void call() throws IOException, InterruptedException, ExecutionException {
      channel.get().close();
      return null;
    }
  }

  /**
   * Creates a new Audio reader.
   *
   * Note: this call is potentially blocking and should therefore not be called
   * from within the processing thread.
   *
   * @param executor the Executor which shall perform the background tasks.
   */
  public AudioReader(ExecutorService executor) {
    this(executor, Const.fileBufferSizeFloat);
  }

  /**
   * Creates a new Audio reader.
   *
   * Note: this call is potentially blocking and should therefore not be called
   * from within the processing thread.
   *
   * @param executor the Executor which shall perform the background tasks.
   * @param requestedFileBufferSizeFloat for testing purposes the byteBuffer
   * size can be set to something different than the value given in
   * {@link Const}
   */
  public AudioReader(ExecutorService executor, int requestedFileBufferSizeFloat) {
    int fileBufferSizeByte = requestedFileBufferSizeFloat * Const.bytesPerFloat;
    this.executor = executor;
    currentBuffer = null;
    nextBuffer = null;

    fileInput = null;


    byteBuffer1 = ByteBuffer.allocateDirect(fileBufferSizeByte).order(ByteOrder.LITTLE_ENDIAN);
    byteBuffer2 = ByteBuffer.allocateDirect(fileBufferSizeByte).order(ByteOrder.LITTLE_ENDIAN);

    currentByteBuffer = byteBuffer1;
    nextByteBuffer = byteBuffer2;
    currentByteBuffer.clear();
    currentByteBuffer.rewind();
    nextByteBuffer.clear();
    nextByteBuffer.rewind();

  }

  public void start(AudioWriter.WriterResult fileToRead) {
    start(fileToRead.getStartBuffer().asFloatBuffer(),
            fileToRead.getChannel(),
            fileToRead.getSamplesWritten());
  }

  /**
   * Starts reading samples.
   *
   * Note: this function is non-blocking and can be called from within the
   * process tread.
   *
   * @param firstByteBuffer the first byteBuffer to read from. Position and
   * limit are assumed to be set so that the byteBuffer is ready to read.
   * @param input a file to be read from when the first byteBuffer has been
   * exhausted. This value can be null if all samples fit into the first
   * byteBuffer.
   * @param samplesToProcess the number of samples to be read.
   */
  public void start(FloatBuffer firstFloatBuffer, Future<FileChannel> input, int samplesToRead) {
    synchronized (processingLock) {
      if (started) {
        throw new RuntimeException("Attempt to start twice.");
      }
      if (closed) {
        throw new RuntimeException("A closed reader cannot be started.");
      }
      this.samplesToProcess = samplesToRead;
      this.samplesProcessed = 0;
      this.samplesDelivered = 0;
      this.fileInput = input;
      if (firstFloatBuffer.position() != 0) {
        throw new RuntimeException("First buffer is not ready for consumption, has it been flipped?");
      }
      RealizedAudioBuffer realizedAudioBuffer = new RealizedAudioBuffer(firstFloatBuffer);
      currentBuffer = realizedAudioBuffer;
      nextBuffer = null;

      if (samplesToRead > firstFloatBuffer.limit()) {
        if (input != null) {
          nextBuffer = executor.submit(new FileReadTask(fileInput, nextByteBuffer));
        } else {
          logger.severe("\"input\" is null.");
          this.samplesToProcess = firstFloatBuffer.limit();
        }
      }
      started = true;
    }
  }

  /**
   * Returns samples from the file byteBuffer, the result is written into the
   * audio array.
   *
   * Note: this function is non-blocking and can be called from within the
   * process tread.
   *
   * @param audioArray the array to be filled.
   */
  public void getNext(float[] audioArray) {
    getNext(samplesDelivered, audioArray);
  }

  /**
   * Returns samples from the file byteBuffer, the result is written into the
   * audio array.
   *
   * Note: this function is non-blocking and can be called from within the
   * process tread.
   *
   * @param startSample the file-position of the first sample. If the position
   * cannot be reached, an empty array will be returned.
   * @param audioArray the array to be filled.
   */
  public void getNext(int startSample, float[] audioArray) {
    synchronized (processingLock) {

      if (startSample >= samplesToProcess) {
        Arrays.fill(audioArray, 0.0F);
        return;
      }
      if (startSample < samplesDelivered) {
        Arrays.fill(audioArray, 0.0F);
        logger.severe("\"startSample\" already delivered.");
        return;
      }
      samplesDelivered = startSample + audioArray.length;
      if (currentBuffer == null) {
        Arrays.fill(audioArray, 0.0F);
        logger.severe("Current buffer is null.");
        return;
      }
      if (!currentBuffer.isDone()) {
        Arrays.fill(audioArray, 0.0F);
        overflowCount++;
        logger.warning("File buffer not ready.");
        return;
      }

      FloatBuffer currentFloatBuffer;
      try {

        currentFloatBuffer = currentBuffer.get();
      } catch (InterruptedException | ExecutionException ex) {
        logger.log(Level.SEVERE, null, ex);
        Arrays.fill(audioArray, 0.0F);
        return;
      }

      int offset = startSample - samplesProcessed;
      int required = offset + audioArray.length;

      // Do we need samples from the next byteBuffer?
      if (currentFloatBuffer.remaining() < required) {
        switchBuffers(offset, audioArray, currentFloatBuffer);
        return;
      }

      // Now we have checked all special conditions... we can proceed to the normal work.
      currentFloatBuffer.position(currentFloatBuffer.position() + offset);
      currentFloatBuffer.get(audioArray);
      samplesProcessed = startSample + audioArray.length;

    }
  }

  private void switchBuffers(int offset, float[] audioArray, FloatBuffer currentFloatBuffer) {
    Arrays.fill(audioArray, 0.0F);

    // we may have reached the end of the file
    int remainingInFile = samplesToProcess - samplesProcessed;
    int remainingInCurrentBuffer = currentFloatBuffer.remaining();
    if (remainingInCurrentBuffer >= remainingInFile) {
      currentFloatBuffer.position(currentFloatBuffer.position() + offset);
      currentFloatBuffer.get(audioArray, 0, remainingInFile - offset);
      samplesProcessed = samplesToProcess;
      return;
    }

    //
    if (nextBuffer == null) {
      logger.severe("Next buffer is null.");
      return;
    }
    if (!nextBuffer.isDone()) {
      logger.warning("Next buffer not ready.");
      overflowCount++;
      return;
    }
    FloatBuffer nextFloatBuffer;
    try {
      nextFloatBuffer = nextBuffer.get();
    } catch (InterruptedException | ExecutionException ex) {
      logger.log(Level.SEVERE, null, ex);
      return;
    }

    int takenFromCurrent = 0;
    // use the tail from the current byteBuffer
    if (offset < remainingInCurrentBuffer) {
      currentFloatBuffer.position(currentFloatBuffer.position() + offset);
      takenFromCurrent = currentFloatBuffer.remaining();
      assert (takenFromCurrent < audioArray.length);
      currentFloatBuffer.get(audioArray, 0, takenFromCurrent);
      samplesProcessed = samplesProcessed + offset + takenFromCurrent;
      offset = 0;
    }
    // get the rest from the next byteBuffer
    if (offset < nextFloatBuffer.remaining()) {
      nextFloatBuffer.position(nextFloatBuffer.position() + offset);
      int takenFromNext = Math.min(audioArray.length - takenFromCurrent, nextFloatBuffer.remaining());
      nextFloatBuffer.get(audioArray, takenFromCurrent, takenFromNext);
      samplesProcessed = samplesProcessed + offset + takenFromNext;
    } else {
      logger.severe("File buffer too small for this offset.");
    }


    // switch the buffers
    ByteBuffer toBeReused = currentByteBuffer;
    currentByteBuffer = nextByteBuffer;
    nextByteBuffer = toBeReused;

    currentBuffer = nextBuffer;
    nextBuffer = null;

    if ((nextFloatBuffer.remaining() + samplesProcessed) < samplesToProcess) {
      nextBuffer = executor.submit(new FileReadTask(fileInput, nextByteBuffer));
    }
  }

  /**
   * Stop reading samples and closes the input file.
   *
   * Note: this function is non-blocking and can be called from within the
   * process tread.
   *
   */
  public void stop() {
    synchronized (processingLock) {
      if (fileInput != null) {
        executor.submit(new FileClosingTask(fileInput));
      }
      started = false;
      samplesToProcess = 0;
      samplesProcessed = 0;
      currentBuffer = null;
      nextBuffer = null;
      currentByteBuffer = byteBuffer1;
      nextByteBuffer = byteBuffer2;
      // mark the byte buffers as being empty
      currentByteBuffer.clear();
      currentByteBuffer.rewind();
      nextByteBuffer.clear();
      nextByteBuffer.rewind();
      fileInput = null;
    }
  }

  /**
   * stops the reader and disposes all resources. A closed reader can not be
   * started again.
   */
  public void close() {
    stop();
    closed = true;
  }

  /**
   * Can be used for test.
   *
   * @return the number of times an empty audioArray was written because the
   * buffer was not ready.
   */
  public int getOverflowCount() {
    return overflowCount;
  }

  public boolean isStarted() {
    synchronized (processingLock) {
      return started;
    }
  }

  /**
   * Can be used in test.
   *
   * @throws InterruptedException
   * @throws ExecutionException
   */
  public void waitForBufferReady() throws InterruptedException, ExecutionException {
    currentBuffer.get();
    if (nextBuffer != null) {
      nextBuffer.get();
    }
  }
}
