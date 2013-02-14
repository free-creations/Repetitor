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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;
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
public class AudioWriter {

  private static final Logger logger = Logger.getLogger(AudioWriter.class.getName());
  private static final int bytesPerFloat = Float.SIZE / Byte.SIZE;
  private static final int defaultFileBufferSizeByte = 10 * 1000 * 1000;
  /**
   * The Current-buffers shall take the data provided by "putNext()". These
   * buffers are packed into a future. The underlying Buffer-Pair can be
   * accessed when the buffer is ready to be filled with new data. The access
   * will be blocked during the time the data is being written to file.
   */
  private Future<BufferPair> currentBuffersReadyToBeFilled;
  /**
   * The Previous-buffers hold the data that is being written to file. These
   * buffers are packed into a future. The underlying Buffer-Pair can be
   * accessed when the buffer when all data have been written to file. The
   * access will be blocked during the time the data is being written to file.
   */
  private Future<BufferPair> previousBuffReadyToBeFilled;
  private final FileChannel outChannel;
  private boolean closed = false;
  private final Object closingLock = new Object();
  /**
   *
   */
  private final ExecutorService executor = Executors.newSingleThreadExecutor(
          new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
              Thread thread = new Thread(r);
              thread.setPriority(Thread.NORM_PRIORITY);
              thread.setName("MyFileWriterThread");
              return thread;
            }
          });
  private ExecutionException writingException = null;

  /**
   * A BufferPair is a set of a byte- buffer and its corresponding float buffer.
   */
  private class BufferPair {

    private final FloatBuffer floatBuffer;
    private final ByteBuffer byteBuffer;
    private final boolean last;

    public BufferPair(FloatBuffer floatBuffer, ByteBuffer byteBuffer, boolean last) {
      this.floatBuffer = floatBuffer;
      this.byteBuffer = byteBuffer;
      this.last = last;
    }

    public FloatBuffer getFloatBuffer() {
      return floatBuffer;
    }

    public ByteBuffer getByteBuffer() {
      return byteBuffer;
    }

    public boolean isLast() {
      return last;
    }
  }

  private class RealizedFuture implements Future<BufferPair> {

    private final BufferPair bufferPair;

    RealizedFuture(FloatBuffer floatBuffer, ByteBuffer byteBuffer, boolean last) {
      assert (floatBuffer != null);
      assert (byteBuffer != null);
      bufferPair = new BufferPair(floatBuffer, byteBuffer, last);
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
      throw new UnsupportedOperationException("RealizedFuture cannot be cancelled.");
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
    public BufferPair get() {
      return bufferPair;
    }

    @Override
    public BufferPair get(long timeout, TimeUnit unit) {
      return bufferPair;
    }
  }

  /**
   * The FileWriteTask takes a Buffer-pair filled with audio data and writes it
   * to the given channel. Once all data are written to file the byte buffer is
   * returned ready to be filled with new audio data. It is assumed that the the
   * position of the float- buffer points to one byte after the last audio frame
   * and that this buffer has not been flipped yet. The position and limit of
   * the byte-buffer is ignored.
   */
  private class FileWriteTask implements Callable<BufferPair> {

    private final ByteBuffer byteBuffer;
    private final FloatBuffer floatBuffer;
    private final FileChannel channel;

    public FileWriteTask(FileChannel channel, BufferPair buffers) {
      this.byteBuffer = buffers.getByteBuffer();
      this.floatBuffer = buffers.getFloatBuffer();
      this.channel = channel;
    }

    @Override
    public BufferPair call() throws IOException {
      // adjust the position and the limit of the byte-buffer and the float-buffer.
      floatBuffer.flip();
      byteBuffer.clear();
      byteBuffer.limit(floatBuffer.limit() * bytesPerFloat);
      //write to the channel
      channel.write(byteBuffer);

      // prepare for a new write
      floatBuffer.clear();
      byteBuffer.clear();

      return new BufferPair(floatBuffer, byteBuffer, false);
    }
  }

  public AudioWriter(File file) throws FileNotFoundException {
    this(file, defaultFileBufferSizeByte);
  }

  public AudioWriter(File file, int requestedFileBufferSizeByte) throws FileNotFoundException {
    
    //make the file buffer length an exact multiple of float
    int fileBufferSizeFloat = requestedFileBufferSizeByte / bytesPerFloat;
    int fileBufferSizeByte = fileBufferSizeFloat * bytesPerFloat;

    ByteBuffer byteBuffer1 = ByteBuffer.allocateDirect(fileBufferSizeByte).order(ByteOrder.LITTLE_ENDIAN);
    ByteBuffer byteBuffer2 = ByteBuffer.allocateDirect(fileBufferSizeByte).order(ByteOrder.LITTLE_ENDIAN);

    // open the output file
    FileOutputStream outFile = new FileOutputStream(file);
    outChannel = outFile.getChannel();

    // prepare the current and the previous buffers
    previousBuffReadyToBeFilled = new RealizedFuture(byteBuffer1.asFloatBuffer(), byteBuffer1, false);
    currentBuffersReadyToBeFilled = new RealizedFuture(byteBuffer2.asFloatBuffer(), byteBuffer2, false);


  }

  /**
   * Closes the file and disposes the buffers.
   *
   * @throws IOException when the file could not be closed
   * @throws ExecutionException when the file could not be written (disk full?)
   */
  public void close() throws IOException, ExecutionException {
    try {
      synchronized (closingLock) {
        if (closed) {
          return;
        }
        // setting "closed" to true makes sure that "putNext" will not access the buffers any more.
        closed = true;
        executor.shutdown();
        if (writingException != null) {
          throw writingException;
        }
      }
      //wait until the previous buffers have been completely written to file
      previousBuffReadyToBeFilled.get();

      // write the current buffer to file (method switchBuffers() does this nicely)
      switchBuffers(currentBuffersReadyToBeFilled.get());
      //wait again until the buffers have been completely written to file
      previousBuffReadyToBeFilled.get();

      currentBuffersReadyToBeFilled = null;
      previousBuffReadyToBeFilled = null;
      outChannel.close();

      closed = true;
    } catch (InterruptedException ex) {
      throw new RuntimeException(ex);
    }
  }

  public void putNext(float[] audioArray) {
    synchronized (closingLock) {
      if (closed) {
        return;
      }

      // retrieve the current Buffer immediately.
      // ..If the buffer cannot be retrieved because it is still being streamed to
      // disk we just ignore the current audio data, hoping that the buffer will 
      // be ready on the next call.
      // ..If the writing thread has abandonned on an exception,
      // we'll do a "dirty close" and store the exception for being thrown
      // when the user attemps to close the stream.
      // In no case we shall bock or interrupt the audio thread here.
      BufferPair thisBuffers;
      try {
        thisBuffers = currentBuffersReadyToBeFilled.get(0, TimeUnit.MILLISECONDS);
      } catch (InterruptedException ex) {
        // how can this happen?
        writingException = new ExecutionException(ex);
        closed = true;
        return;
      } catch (ExecutionException ex) {
        // writing did not succeed (Disk full?)
        writingException = ex;
        closed = true;
        return;
      } catch (TimeoutException ex) {
        logger.log(Level.WARNING, "Buffer underrun.");
        return;
      }

      FloatBuffer floatBuffer = thisBuffers.getFloatBuffer();

      // fill the buffer with samples from the audio array 
      floatBuffer.put(audioArray);

      // if we have reached the end of this buffer, switch buffers for the next call.
      if (floatBuffer.remaining() >= audioArray.length) {
        switchBuffers(thisBuffers);
      }
    }
  }

  /**
   * This function is only for test.
   *
   * @deprecated only for test
   */
  @Deprecated
  void waitForBufferReady() {
    try {
      currentBuffersReadyToBeFilled.get();
    } catch (InterruptedException | ExecutionException ex) {
      throw new RuntimeException(ex);
    }
  }

  private void switchBuffers(BufferPair currentBuffers) {

    // we'll reuse the previous buffers (crossing the fingers that they will be
    // written to file when "putNext" tries to use them).
    currentBuffersReadyToBeFilled = previousBuffReadyToBeFilled;

    // start the writer thread
    previousBuffReadyToBeFilled =
            executor.submit(new FileWriteTask(outChannel, currentBuffers));

  }
}
