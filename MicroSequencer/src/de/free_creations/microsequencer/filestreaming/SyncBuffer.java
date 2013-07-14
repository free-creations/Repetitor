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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 *
 * @author Harald Postner
 */
public class SyncBuffer {

  private final ByteBuffer byteBuffer;
  private final FloatBuffer floatBuffer;

  /**
   * Allocates a new direct byte-buffer and maps a float buffer onto it.
   *
   * @param floatCapacity the capacity of the float buffer.
   */
  public SyncBuffer(int floatCapacity) {
    byteBuffer = ByteBuffer.allocateDirect(floatCapacity * Const.bytesPerFloat).order(ByteOrder.LITTLE_ENDIAN);
    floatBuffer = byteBuffer.asFloatBuffer();
    byteBuffer.clear();
    floatBuffer.clear();
  }

  /**
   * Gives access on the float-buffer mapped to the byte buffer.
   *
   * @return The mapped float-buffer.
   */
  public FloatBuffer asFloatBuffer() {
    return floatBuffer;
  }

  /**
   * Gives access on the underlying byte-buffer.
   *
   * @return The underlying byte-buffer.
   */
  public ByteBuffer asByteBuffer() {
    return byteBuffer;
  }

  /**
   * Puts the byte-buffer in synchronization with the float-buffer.
   *
   * @return This buffer.
   */
  public SyncBuffer syncBytesWithFloats() {
    byteBuffer.limit(floatBuffer.limit() * Const.bytesPerFloat);
    byteBuffer.position(floatBuffer.position() * Const.bytesPerFloat);
    return this;
  }

  /**
   * Puts the float-buffer in synchronization with the byte-buffer.
   */
  public SyncBuffer syncFloatsWithBytes() {
    floatBuffer.limit(byteBuffer.limit() / Const.bytesPerFloat);
    floatBuffer.position(byteBuffer.position() / Const.bytesPerFloat);
    return this;
  }

  /**
   * Clears both buffers.
   *
   * @return This buffer.
   */
  public SyncBuffer clear() {
    floatBuffer.clear();
    byteBuffer.clear();
    return this;
  }

  /**
   * Flips the byte-buffer and puts the float-buffer in synchronization with the
   * byte-buffer.
   *
   * <p>After a sequence of channel-read or put operations on the byte-buffer,
   * invoke this method to prepare for a sequence of channel-write or relative
   * get operations on either of the buffers.</p>
   *
   * @return This buffer.
   */
  public SyncBuffer flipBytes() {
    byteBuffer.flip();
    return syncFloatsWithBytes();
  }

  /**
   * Flips the float-buffer and puts the byte-buffer in synchronization with the
   * float-buffer. The limit is set to the current position (of the
   * float-buffer) and then the position is set to zero. If the mark is defined
   * then it is discarded
   *
   * <p>After a sequence of channel-read or put operations on the float-buffer,
   * invoke this method to prepare for a sequence of channel-write or relative
   * get operations on either of the buffers.</p>
   *
   * @return This buffer.
   */
  public SyncBuffer flipFloats() {
    floatBuffer.flip();
    return syncBytesWithFloats();
  }

  /**
   * Rewinds the float-buffer and puts the byte-buffer in synchronization with
   * the float-buffer. The position of both buffers is set to zero and the mark
   * is discarded.
   *
   * <p>Invoke this method before a sequence of channel-write or get operations,
   * assuming that the limit has already been set appropriately</p>
   *
   * @return This buffer.
   */
  public SyncBuffer rewindFloats() {
    floatBuffer.rewind();
    return syncBytesWithFloats();
  }
}
