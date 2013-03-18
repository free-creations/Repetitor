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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.channels.FileChannel;
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
public class AudioReader {

  private static final Logger logger = Logger.getLogger(AudioReader.class.getName());
  public static final int bytesPerFloat = Float.SIZE / Byte.SIZE;
  public static final int defaultFileBufferSizeByte = 8 * 1024 * 1024;
  public static final int transitionBufferSizeByte = 32 * 2048;
  /**
   * The Current-buffers provides the data for the procedure "getNext()". The
   * Buffer can be accessed when it is ready to be retrieved. The access will be
   * blocked during the time the data is being read from file.
   */
  private Future<ByteBuffer> currentBuffReadyToBeConsumed;
  /**
   * The Next-buffer is being prepared while the above current-buffer is being
   * used.
   */
  private Future<ByteBuffer> nextBuffReadyToBeConsumed;
  /**
   * The transitionBuffer buffer will take those bytes from the tail of file
   * buffer that did not fit into the audioArray.
   */
  private final ByteBuffer transitionBuffer;
  private final FileChannel inChannel;
  private boolean closed = false;
  private final Object processingLock = new Object();
  /**
   *
   */
  private final ExecutorService executor = Executors.newSingleThreadExecutor(
          new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
              Thread thread = new Thread(r);
              thread.setPriority(Thread.NORM_PRIORITY);
              thread.setName("FreeCreationsAudioReader");
              return thread;
            }
          });
  private ExecutionException readingException = null;
  private final int fileBufferSizeByte;
  private final long cycleTimeoutNano;

  /**
   * The FileReadTask takes a Byte-Buffer and fills it with audio data from the
   * file. Once the buffer has been filled it is returned ready to be consumed.
   */
  private class FileReadTask implements Callable<ByteBuffer> {

    private final ByteBuffer byteBuffer;
    private final FileChannel channel;

    public FileReadTask(FileChannel channel, ByteBuffer buffer) {
      this.byteBuffer = buffer;
      this.channel = channel;
    }

    @Override
    public ByteBuffer call() throws IOException {
      //read from the channel
      byteBuffer.clear();
      channel.read(byteBuffer);
      // prepare the byte buffer for consuption
      byteBuffer.flip();
      return byteBuffer;
    }
  }

  /**
   * Opens the given file for reading and prepares the file buffers.
   *
   * @param file the file to be opened for reading.
   * @throws if the file does not exist, is a directory rather than a regular
   * file, or for some other reason cannot be opened for reading.
   */
  public AudioReader(File file, long cycleTimeoutNano) throws FileNotFoundException, IOException {
    this(file, cycleTimeoutNano, defaultFileBufferSizeByte);
  }

  public AudioReader(File file, long cycleTimeoutNano, int requestedFileBufferSizeByte) throws FileNotFoundException, IOException {
    this.cycleTimeoutNano = cycleTimeoutNano;
    fileBufferSizeByte = requestedFileBufferSizeByte;

    ByteBuffer byteBuffer1 = ByteBuffer.allocateDirect(fileBufferSizeByte).order(ByteOrder.LITTLE_ENDIAN);
    ByteBuffer byteBuffer2 = ByteBuffer.allocateDirect(fileBufferSizeByte).order(ByteOrder.LITTLE_ENDIAN);
    transitionBuffer = ByteBuffer.allocateDirect(transitionBufferSizeByte).order(ByteOrder.LITTLE_ENDIAN);
    //mark the buffers as being exhausted
    byteBuffer1.limit(0);
    byteBuffer2.limit(0);
    transitionBuffer.limit(0);

    // open the input file
    FileInputStream inFile = new FileInputStream(file);
    inChannel = inFile.getChannel();

    // start to fill the first buffer
    switchBuffers(byteBuffer1);
    // wait until it has completely been read.
    waitForNextBufferReady();

    // start to fill the second buffer
    switchBuffers(byteBuffer2);
  }

  /**
   * Closes the file and disposes the buffers.
   *
   * @throws IOException when the file could not be correctly written.
   */
  public synchronized void close() throws IOException {

    synchronized (processingLock) {
      if (readingException != null) {
        closed = true;
        executor.shutdown();
        inChannel.close();
        ExecutionException ex = readingException;
        readingException = null;
        if (ex.getCause() instanceof IOException) {
          throw new IOException(ex.getMessage());
        }
        throw new RuntimeException(ex);
      }
      if (closed) {
        return;
      }
      // setting "closed" to true makes sure that "getNext" will not access the buffers any more.
      closed = true;
    }

    currentBuffReadyToBeConsumed = null;
    nextBuffReadyToBeConsumed = null;
    inChannel.close();
    executor.shutdown();
  }

  public boolean getNext(float[] audioArray) {
    assert (transitionBufferSizeByte >= audioArray.length * bytesPerFloat);
    assert (fileBufferSizeByte >= audioArray.length * bytesPerFloat);
    synchronized (processingLock) {
      if (closed) {
        Arrays.fill(audioArray, 0F);
        return false;
      }

      // consume the current Buffer immediately.
      // ..If the buffer cannot be retrieved because it is still being streamed from
      // disk, we return an empty array.
      // ..If the file reading thread has abandonned on an exception,
      // we'll do a "dirty close" and store the exception for being thrown
      // when the user attemps to close the stream.
      // In no case we shall block or interrupt the audio thread here.
      ByteBuffer currentByteBuff;
      try {
        currentByteBuff = currentBuffReadyToBeConsumed.get(cycleTimeoutNano, TimeUnit.NANOSECONDS);
      } catch (InterruptedException ex) {
        // how can this happen?
        readingException = new ExecutionException(ex);
        closed = true;
        Arrays.fill(audioArray, 0F);
        return false;
      } catch (ExecutionException ex) {
        // error in file reading??
        readingException = ex;
        closed = true;
        Arrays.fill(audioArray, 0F);
        return false;
      } catch (TimeoutException ex) {
        logger.log(Level.WARNING, "Read-Buffer underrun.");
        Arrays.fill(audioArray, 0F);
        return true;
      }

      FloatBuffer thisFloatBuffer;
      int remainingBytes = currentByteBuff.remaining();

      // are there left-overs form the previous cycle?
      if (transitionBuffer.position() > 0) {
        // there are left-overs
        // so we will copy some bytes from the current byte buffer
        // into the transition buffer so that we can fill an entire audioArray.
        // thus we'll have more bytes remaining:
        remainingBytes += transitionBuffer.position();
        assert (currentByteBuff.position() == 0);
        int byteBuffLimit = currentByteBuff.limit();
        int bytesToCopy = (audioArray.length * bytesPerFloat) - transitionBuffer.position();
        assert (bytesToCopy > 0);
        // temporarly shorten the current byte-buffer so as to copy only the bytes we need
        if (byteBuffLimit > bytesToCopy) {
          currentByteBuff.limit(bytesToCopy);
          assert (currentByteBuff.remaining() == bytesToCopy);
        }
        transitionBuffer.put(currentByteBuff);
        transitionBuffer.flip();
        //reset the current byte-buffer to its original length
        currentByteBuff.limit(byteBuffLimit);
        //the float buffer will be mapped on the transition buffer
        thisFloatBuffer = transitionBuffer.asFloatBuffer();
        assert (thisFloatBuffer.remaining() <= audioArray.length);
        //mark the transition-buffer as empty (the float buffers limit will not be affected)
        transitionBuffer.limit(0);
      } else {
        // no left-overs, so the float buffer will be mapped on the current byte buffer
        /**
         * @ToDo re-mapping the byte buffer to a float buffer on each cycle
         * reduces the performance by a factor of 8!!!
         **
         */
        thisFloatBuffer = currentByteBuff.asFloatBuffer();
      }

      // fill the audio array 
      int remainingFloats = remainingBytes / bytesPerFloat;
      if (audioArray.length > remainingFloats) {
        // threre are not enough bytes remaining to fill an entire audio array
        // this indicates that we have reached the end of the file
        Arrays.fill(audioArray, 0F);
        thisFloatBuffer.get(audioArray, 0, remainingFloats);
        // mark the byte buffer as being exhausted
        currentByteBuff.limit(0);
        // we can stop processing here. (Having reached the end of the file, we do not want to flip buffers any more)
        return false;
      } else {
        thisFloatBuffer.get(audioArray);
        remainingBytes = remainingBytes - (audioArray.length * bytesPerFloat);
        remainingFloats = remainingBytes / bytesPerFloat;
      }
      //adjust the position-pointer of the byte-buffer
      int newPosition = currentByteBuff.limit() - remainingBytes;
      currentByteBuff.position(newPosition);

      // are there enough floats for the next cycle? 
      // if not -> switch buffers.
      if (audioArray.length > remainingFloats) {

        // save the tail of the current byte-buffer into the transition-buffer
        transitionBuffer.clear();
        transitionBuffer.put(currentByteBuff);
        assert ((audioArray.length * bytesPerFloat) > transitionBuffer.position());
        switchBuffers(currentByteBuff);
      }
      return true;
    }
  }

  /**
   * Block the calling thread until the next buffer is ready.
   *
   * @throws IOException if there was a problem when reading the file.
   * @deprecated Use this function only for test
   */
  @Deprecated
  final void waitForNextBufferReady() throws IOException {
    if (closed) {
      return;
    }
    try {
      nextBuffReadyToBeConsumed.get();
    } catch (InterruptedException ex) {
      // how can this happen?
      throw new RuntimeException(ex);
    } catch (ExecutionException ex) {
      // IOException in FileReadTask?
      if (ex.getCause() instanceof IOException) {
        throw new IOException(ex.getCause());
      } else {
        throw new RuntimeException(ex);
      }
    }
  }

  private void switchBuffers(ByteBuffer reusuableBuff) {

    // From now on "getNext" should consume the bytes that were read into nextBuff
    currentBuffReadyToBeConsumed = nextBuffReadyToBeConsumed;

    // start the reader thread
    nextBuffReadyToBeConsumed =
            executor.submit(new FileReadTask(inChannel, reusuableBuff));

  }
}
