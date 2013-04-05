/*
 * Copyright 2013 Harald Postner.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless toBeWritten by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.free_creations.microsequencer.filestreaming;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
public class AudioWriter {

  private static final Logger logger = Logger.getLogger(AudioWriter.class.getName());
  /**
   * The Current-FloatBuffer takes the data from the procedure "putNext()".
   *
   * This is the buffer we are currently writing to. The Buffer can be accessed
   * when it is ready to be written. The access will be blocked during the time
   * the data is being streamed to file.
   */
  private Future<FileWriteTaskResult> currentBufferProvider;
  /**
   * The Next-FloatBuffer is the FloatBuffer that is currently being streamed to
   * file.
   */
  private Future<FileWriteTaskResult> bussyBufferProvider;
  /**
   * The startBuffer is not streamed to file.
   */
  private SyncBuffer startBuffer;
  /**
   * The processing lock protects the procedures start() stop() and getNext()
   * against parallel access.
   */
  private final Object processingLock = new Object();
  private boolean started = false;
  private boolean closed = false;
  /**
   * The Executor which shall perform the background tasks.
   */
  private final ExecutorService executor;
  /**
   * The number of samples processed so far. This is like a pointer into the
   * output file. The pointer always points one sample after the last sample
   * written to the file. Note this is not always equal to number of samples
   * delivered, because we might have ignored some write requests (putNext())
   * because the file was not ready to be written.
   */
  private int samplesProcessed = 0;
  /**
   * The number of samples delivered by the putNext() methods. This pointer
   * counts also the samples that have not be processed and were replaced by
   * null samples.
   */
  private int samplesDelivered = 0;
  private boolean startBufferDone = false;
  private boolean firstFileBufferDone = false;
  private File outputFile;
  private final int requestedFileBufferSizeFloat;
  private int overflowCount = 0;

  private class AlwaysStreamedBuffer implements Future<FileWriteTaskResult> {

    private final FileWriteTaskResult result;

    public AlwaysStreamedBuffer(SyncBuffer buffer) {
      buffer.clear();
      this.result = new FileWriteTaskResult(null, buffer);
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
    public FileWriteTaskResult get() throws InterruptedException, ExecutionException {
      return result;
    }

    @Override
    public FileWriteTaskResult get(long timeout, TimeUnit unit) {
      return result;
    }
  }

  public static class WriterResult {

    private final Future<FileChannel> channel;
    private final int samplesWritten;
    private final SyncBuffer startBuffer;

    protected WriterResult(SyncBuffer firstBuffer, Future<FileChannel> channel, int samplesWritten) {
      this.startBuffer = firstBuffer;
      this.channel = channel;
      this.samplesWritten = samplesWritten;
    }

    public SyncBuffer getStartBuffer() {
      return startBuffer;
    }

    public Future<FileChannel> getChannel() {
      return channel;
    }

    public int getSamplesWritten() {
      return samplesWritten;
    }
  }

  /**
   * The result of a FileWriteTask.
   */
  private class FileWriteTaskResult {

    private final FileChannel channel;
    private final SyncBuffer buffer;

    public FileWriteTaskResult(FileChannel channel, SyncBuffer buffer) {
      this.channel = channel;
      this.buffer = buffer;
    }

    /**
     * @return the open channel where the buffer has been written to.
     */
    public FileChannel getChannel() {
      return channel;
    }

    /**
     * @return a FloatBuffer mapped onto the byte buffer, ready to take new
     * samples.
     */
    public SyncBuffer getbuffer() {
      return buffer;
    }
  }

  /**
   * The FileWriteTask takes a Sync-Buffer and streams it to the given file.
   *
   * It returns a FloatBuffer mapped to the given byte buffer. The returned
   * float buffer is cleared so it is ready to receive new samples.
   */
  private class FileWriteTask implements Callable<FileWriteTaskResult> {

    private final SyncBuffer buffer;
    private final FileChannel channel;
    private final File file;

    /**
     * Creates a new FileWriteTask.
     *
     * @param file the filename of the file to be written.
     * @param channel the file channel to be used. This parameter might be null
     * if the file has not yet been opened.
     * @param buffer the buffer to take the samples from.
     */
    public FileWriteTask(File file, FileChannel channel, SyncBuffer buffer) {
      this.buffer = buffer;
      this.channel = channel;
      this.file = file;
    }

    @Override
    public FileWriteTaskResult call() throws FileNotFoundException, IOException {
      FileChannel openedChannel;
      if (channel == null) {
        openedChannel = openChannel(file);
      } else {
        openedChannel = channel;
      }

      buffer.flipFloats();
      openedChannel.write(buffer.asByteBuffer());
      buffer.clear();

      FileWriteTaskResult result = new FileWriteTaskResult(openedChannel, buffer);
      return result;
    }

    private FileChannel openChannel(File outfile) throws FileNotFoundException {
      // open the output file
      FileOutputStream outStream = new FileOutputStream(outfile);
      return outStream.getChannel();
    }
  }

  /**
   * The LastFileWriteTask does about the same as FileWriteTask, but instead of
   * returning a fresh buffer, it returns the file channel ready to be accessed
   * in read mode.
   */
  private class LastFileWriteTask implements Callable<FileChannel> {

    private final SyncBuffer buffer;
    private final FileChannel channel;
    private final File file;

    /**
     * Creates a new LastFileWriteTask.
     *
     * @param file the filename of the file to be written.
     * @param channel the file channel to be used. This parameter might be null
     * if the file has not yet been opened.
     * @param buffer the buffer to take the samples from.
     */
    public LastFileWriteTask(File file, FileChannel channel, SyncBuffer buffer) {
      this.buffer = buffer;
      this.channel = channel;
      this.file = file;
    }

    @Override
    public FileChannel call() throws FileNotFoundException, IOException {
      FileWriteTask fileWriteTask = new FileWriteTask(file, channel, buffer);
      FileWriteTaskResult fileWriteResult = fileWriteTask.call();
      return reopenForInput(fileWriteResult.getChannel());
    }

    private FileChannel reopenForInput(FileChannel channel) throws IOException {
      channel.close();
      // open the input file
      FileInputStream inFile = new FileInputStream(file);
      return inFile.getChannel();
    }
  }

  /**
   * Creates a new Audio writer.
   *
   * Note: this call is potentially blocking and should therefore not be called
   * from within the processing thread.
   *
   * @param executor the Executor which shall perform the background tasks.
   */
  public AudioWriter(ExecutorService executor) {
    this(executor, Const.fileBufferSizeFloat);
  }

  /**
   * Creates a new Audio writer.
   *
   * Note: this call is potentially blocking and should therefore not be called
   * from within the processing thread.
   *
   * @param executor the Executor which shall perform the background tasks.
   * @param requestedFileBufferSizeFloat for testing purposes the buffer size
   * can be set to something different than the value given in {@link Const}
   */
  public AudioWriter(ExecutorService executor, int requestedFileBufferSizeFloat) {
    this.executor = executor;
    this.requestedFileBufferSizeFloat = requestedFileBufferSizeFloat;

    currentBufferProvider = null;
    bussyBufferProvider = null;
    startBuffer = null;
  }

  /**
   * Starts writing samples.
   *
   * Note: this function is non-blocking and can be called from within the
   * process tread.
   *
   * @param file the file to write to. If the file exists it will be erased.
   */
  public void start(File file) {
    synchronized (processingLock) {
      if (started) {
        throw new RuntimeException("Attempt to start twice.");
      }
      if (closed) {
        throw new RuntimeException("A closed reader cannot be started.");
      }
      samplesProcessed = 0;
      samplesDelivered = 0;
      outputFile = file;

      startBuffer = new SyncBuffer(requestedFileBufferSizeFloat);

      currentBufferProvider = new AlwaysStreamedBuffer(startBuffer);
      bussyBufferProvider = null;

      startBufferDone = false;
      firstFileBufferDone = false;
      started = true;
    }
  }

  /**
   * Writes the given samples to the file.
   *
   * Note: this function is non-blocking and can be called from within the
   * process tread.
   *
   * @param audioArray the data to be written to file.
   */
  public void putNext(float[] audioArray) {
    putNext(samplesDelivered, audioArray);
  }

  /**
   * Writes the given samples to the file.
   *
   * Note: this function is non-blocking and can be called from within the
   * process tread.
   *
   * @param startSample the file-position of the first sample. If the position
   * larger than the number of samples written so far, the gap will be filled
   * with null samples.
   * @param audioArray the data to be written to file.
   */
  public void putNext(int startSample, float[] audioArray) {
    synchronized (processingLock) {
      if (!started) {
        return;
      }
      if (startSample < samplesDelivered) {
        Arrays.fill(audioArray, 0.0F);
        logger.severe("\"startSample\" already delivered.");
        return;
      }
      samplesDelivered = startSample + audioArray.length;

      if (currentBufferProvider == null) {
        logger.severe("Current buffer is null.");
        return;
      }
      if (!currentBufferProvider.isDone()) {
        logger.warning("File buffer not ready.");
        overflowCount++;
        return;
      }
      SyncBuffer currentBuffer;
      try {
        FileWriteTaskResult result = currentBufferProvider.get();
        currentBuffer = result.getbuffer();
      } catch (InterruptedException | ExecutionException ex) {
        logger.log(Level.SEVERE, null, ex);
        return;
      }
      FloatBuffer currentFloatBuffer = currentBuffer.asFloatBuffer();

      int offset = startSample - samplesProcessed;
      int toBeWritten = offset + audioArray.length;

      // Do we need to write samples to the next buffer?
      int remainingSpace = currentFloatBuffer.remaining();
      if (remainingSpace < toBeWritten) {
        switchBuffers(offset, audioArray, currentBuffer);
        return;
      }

      // Now we have checked all special conditions... we can proceed to the normal work.
      // 1) Pad with null samples.
      for (int i = 0; i < offset; i++) {
        currentFloatBuffer.put(0F);
      }
      // 2) append the audioArray
      currentFloatBuffer.put(audioArray);
      samplesProcessed = startSample + audioArray.length;
    }
  }

  private void switchBuffers(int offset, float[] audioArray, SyncBuffer oldBuffer) {
    int toBePadded = offset;
    FloatBuffer oldFloatBuffer = oldBuffer.asFloatBuffer();
    //1) use the remaining space in the current buffer
    // ...pad the current buffer as much as fits.
    while ((toBePadded > 0) && (oldFloatBuffer.hasRemaining())) {
      oldFloatBuffer.put(0F);
      toBePadded--;
      samplesProcessed++;
    }

    // ...put as much as fits from the audioArray
    int audioArrayWrittenToOld = 0;
    if (oldFloatBuffer.hasRemaining() && toBePadded == 0) {
      audioArrayWrittenToOld = oldFloatBuffer.remaining();
      oldFloatBuffer.put(audioArray, 0, audioArrayWrittenToOld);
      samplesProcessed += audioArrayWrittenToOld;
    }

    // 2) put the rest in a new buffer
    if (!startBufferDone) {
      // setup the first file buffer
      bussyBufferProvider = new AlwaysStreamedBuffer(new SyncBuffer(requestedFileBufferSizeFloat));
    } else {
      if (!firstFileBufferDone) {
        // setup the second file buffer
        bussyBufferProvider = new AlwaysStreamedBuffer(new SyncBuffer(requestedFileBufferSizeFloat));
      }
    }

    if (bussyBufferProvider == null) {
      logger.severe("Next buffer is null!!");
      return;
    }
    if (!bussyBufferProvider.isDone()) {
      logger.warning("Next buffer not ready.");
      overflowCount++;
      return;
    }
    SyncBuffer newBuffer;
    FileChannel channel;
    try {
      FileWriteTaskResult result = bussyBufferProvider.get();
      newBuffer = result.getbuffer();
      channel = result.getChannel();
    } catch (InterruptedException | ExecutionException ex) {
      logger.log(Level.SEVERE, null, ex);
      return;
    }
    FloatBuffer newFloatBuffer = newBuffer.asFloatBuffer();
    //

    int remainingInNewBuffer = newFloatBuffer.remaining();
    int toBeWrittenToNew = toBePadded + audioArray.length - audioArrayWrittenToOld;
    if (toBeWrittenToNew > remainingInNewBuffer) {
      logger.severe("File buffer too small for this offset.");
    } else {
      for (int i = 0; i < toBePadded; i++) {
        newFloatBuffer.put(0F);
        samplesProcessed++;
      }
      int restAudioArray = audioArray.length - audioArrayWrittenToOld;
      newFloatBuffer.put(audioArray, audioArrayWrittenToOld, restAudioArray);
      samplesProcessed += restAudioArray;
    }




    if (startBufferDone) {
      FileWriteTask fileWriteTask = new FileWriteTask(outputFile, channel, oldBuffer);
      currentBufferProvider = bussyBufferProvider;
      bussyBufferProvider = executor.submit(fileWriteTask);
      firstFileBufferDone = true;
    } else {
      // we just have processed the first buffer:
      currentBufferProvider = bussyBufferProvider;
      bussyBufferProvider = null;
      startBufferDone = true;
    }
  }

  /**
   * Stops writing and starts to close the output file.
   *
   * Note: this function is non-blocking and can be called from within the
   * process tread.
   *
   */
  public WriterResult stop() {
    synchronized (processingLock) {
      if (!started) {
        throw new RuntimeException("Attempt to stop altough not started.");
      }
      startBuffer.flipFloats();

      Future<FileChannel> readerChannel = null;
      FileChannel channel = null;

      if (startBufferDone) {
        // wait for the bussyBufferProvider to terminate streaming.
        // ToDo: this ought to be done inside LastFileWriteTask (beware of possible deadlocks)
        if (bussyBufferProvider != null) {
          if (!bussyBufferProvider.isDone()) {
            logger.warning("Waiting for a previous buffer to be streamed.");
            overflowCount++;
          }
          try {
            channel = bussyBufferProvider.get().getChannel();
          } catch (InterruptedException | ExecutionException ex) {
            logger.log(Level.SEVERE, null, ex);
          }
        }
        FileWriteTaskResult result = null;
        try {
          result = currentBufferProvider.get();
        } catch (InterruptedException | ExecutionException ex) {
          logger.log(Level.SEVERE, null, ex);
        }
        if (result != null) {
          SyncBuffer buffer = result.getbuffer();
          LastFileWriteTask lastFileWriteTask = new LastFileWriteTask(outputFile, channel, buffer);
          readerChannel = executor.submit(lastFileWriteTask);
        }
      }

      WriterResult result = new WriterResult(startBuffer, readerChannel, samplesProcessed);
      currentBufferProvider = null;
      bussyBufferProvider = null;
      startBuffer = null;
      started = false;
      return result;
    }
  }

  /**
   * Stops the Writer and disposes resources. A closed writer can not be started
   * again.
   */
  public void close() {

    if (started) {
      WriterResult stopResult = stop();
      Future<FileChannel> futurechannel = stopResult.getChannel();
      FileChannel channel;
      try {
        channel = futurechannel.get();
        if (channel != null) {
          channel.close();
        }

      } catch (IOException | InterruptedException | ExecutionException ex) {
        throw new RuntimeException(ex);
      }

    }

    closed = true;
  }

  /**
   * Can be used for test.
   *
   * @return the number of times an empty audioArray was returned because the
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
    if (currentBufferProvider != null) {
      currentBufferProvider.get();
    }
    if (bussyBufferProvider != null) {
      bussyBufferProvider.get();
    }
  }
}
