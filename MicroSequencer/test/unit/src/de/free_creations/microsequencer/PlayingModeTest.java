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

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * This (trivial) test just makes sure I have understood how Java Enums work.
 *
 * @author Harald Postner
 */
public class PlayingModeTest {

  @Test
  public void testNames() {
    System.out.println("testNames");
    assertEquals("MidiOnly", PlayingMode.MidiOnly.name());
    assertEquals("RecordAudio", PlayingMode.RecordAudio.name());
    assertEquals("PlayAudio", PlayingMode.PlayAudio.name());
    assertEquals("PlayRecordAudio", PlayingMode.PlayRecordAudio.name());
  }

  /**
   * Test of values method, of class PlayingMode.
   */
  @Test
  public void testValues() {
    System.out.println("values");
    PlayingMode[] expResult = new PlayingMode[]{
      PlayingMode.MidiOnly,
      PlayingMode.RecordAudio,
      PlayingMode.PlayAudio,
      PlayingMode.PlayRecordAudio,};
    PlayingMode[] result = PlayingMode.values();
    assertArrayEquals(expResult, result);
  }

  /**
   * Test of valueOf method, of class PlayingMode.
   */
  @Test
  public void testValueOf() {
    System.out.println("testValueOf");
    PlayingMode result;
    result = PlayingMode.valueOf("MidiOnly");
    assertEquals(PlayingMode.MidiOnly, result);

    result = PlayingMode.valueOf("RecordAudio");
    assertEquals(PlayingMode.RecordAudio, result);

    result = PlayingMode.valueOf("PlayAudio");
    assertEquals(PlayingMode.PlayAudio, result);

    result = PlayingMode.valueOf("PlayRecordAudio");
    assertEquals(PlayingMode.PlayRecordAudio, result);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testErroneousValueOf() {
    System.out.println("testErroneousValueOf");
    PlayingMode.valueOf("WrongString");
  }
}
