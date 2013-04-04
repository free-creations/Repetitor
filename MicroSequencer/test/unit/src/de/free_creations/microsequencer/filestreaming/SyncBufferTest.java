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
import java.nio.FloatBuffer;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Harald Postner
 */
public class SyncBufferTest {



  /**
   * Test of asFloatBuffer method, of class SyncBuffer.
   */
  @Test
  public void testAsFloatBuffer() {
    System.out.println("asFloatBuffer");
    int cap = 123;
    SyncBuffer instance = new SyncBuffer(cap);
    FloatBuffer result = instance.asFloatBuffer();
    assertEquals(cap, result.capacity());
    assertEquals(cap, result.limit());
    assertEquals(0, result.position());

  }

  /**
   * Test of asByteBuffer method, of class SyncBuffer.
   */
  @Test
  public void testAsByteBuffer() {
    System.out.println("asByteBuffer");
    int capFloat = 123;
    int capByte = capFloat * Const.bytesPerFloat;
    SyncBuffer instance = new SyncBuffer(capFloat);
    ByteBuffer result = instance.asByteBuffer();
    assertEquals(capByte, result.capacity());
    assertEquals(capByte, result.limit());
    assertEquals(0, result.position());
  }

  /**
   * Test of syncBytesWithFloats method, of class SyncBuffer.
   */
  @Test
  public void testSyncBytesWithFloats() {
    System.out.println("syncBytesWithFloats");
    int capFloat = 123;
    SyncBuffer instance = new SyncBuffer(capFloat);
    instance.asFloatBuffer().put(1F);
    instance.syncBytesWithFloats();
    assertEquals(4, instance.asByteBuffer().position());

  }

  /**
   * Test of syncFloatsWithBytes method, of class SyncBuffer.
   */
  @Test
  public void testSyncFloatsWithBytes() {
    System.out.println("syncFloatsWithBytes");
    int capFloat = 123;
    SyncBuffer instance = new SyncBuffer(capFloat);
    byte[] bytes = new byte[4];
    instance.asByteBuffer().put(bytes);
    instance.syncFloatsWithBytes();
    assertEquals(1, instance.asFloatBuffer().position());
  }

  /**
   * Test of clear method, of class SyncBuffer.
   */
  @Test
  public void testClear() {
    System.out.println("clear");
    int capFloat = 123;
    int capByte = capFloat * Const.bytesPerFloat;
    SyncBuffer instance = new SyncBuffer(capFloat);
    instance.asByteBuffer().limit(2);
    instance.asFloatBuffer().limit(2);
    instance.asByteBuffer().position(1);
    instance.asFloatBuffer().position(1);

    instance.clear();

    assertEquals(capByte, instance.asByteBuffer().limit());
    assertEquals(capFloat, instance.asFloatBuffer().limit());
    assertEquals(0, instance.asByteBuffer().position());
    assertEquals(0, instance.asFloatBuffer().position());


  }

  /**
   * Test of flipBytes method, of class SyncBuffer.
   */
  @Test
  public void testFlipBytes() {
    System.out.println("flipBytes");
    int capFloat = 123;
    SyncBuffer instance = new SyncBuffer(capFloat);
    byte[] bytes = new byte[4];
    instance.asByteBuffer().put(bytes);

    instance.flipBytes();

    assertEquals(4, instance.asByteBuffer().limit());
    assertEquals(1, instance.asFloatBuffer().limit());
    assertEquals(0, instance.asByteBuffer().position());
    assertEquals(0, instance.asFloatBuffer().position());


  }

  /**
   * Test of flipFloats method, of class SyncBuffer.
   */
  @Test
  public void testFlipFloats() {
    System.out.println("flipFloats");
    int capFloat = 123;
    SyncBuffer instance = new SyncBuffer(capFloat);

    instance.asFloatBuffer().put(1F);

    instance.flipFloats();

    assertEquals(4, instance.asByteBuffer().limit());
    assertEquals(1, instance.asFloatBuffer().limit());
    assertEquals(0, instance.asByteBuffer().position());
    assertEquals(0, instance.asFloatBuffer().position());
  }
}
