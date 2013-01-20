/*
 * Copyright 2012 Harald Postner.
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
package de.free_creations.midiutil;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class LyricTest {

  public LyricTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   */
  @Test
  public void testConstructor_1() {
    System.out.println("testConstructor_1");
    Lyric instanceH = new Lyric(123L, "Abc-");

    assertTrue(instanceH.isHyphenated());
    assertEquals(123L, instanceH.getTickPos());
    assertTrue("Expected \"Abc\" but got \"" + instanceH.getText() + "\"",
            "Abc".equals(instanceH.getText()));
    Lyric instance2 = new Lyric(456L, "defg");

    assertFalse(instance2.isHyphenated());
    assertEquals(456L, instance2.getTickPos());
    assertTrue("Expected \"defg\" but got \"" + instance2.getText() + "\"",
            "defg".equals(instance2.getText()));

  }

  @Test
  public void testConstructor_2() throws InvalidMidiDataException {
    System.out.println("testConstructor_2");
    Lyric instanceH = new Lyric(makeTextEvent(123L, "Abc-"));

    assertTrue(instanceH.isHyphenated());
    assertEquals(123L, instanceH.getTickPos());
    assertTrue("Expected \"Abc\" but got \"" + instanceH.getText() + "\"",
            "Abc".equals(instanceH.getText()));

    Lyric instance2 = new Lyric(makeTextEvent(456L, "defg"));
    assertFalse(instance2.isHyphenated());
    assertEquals(456L, instance2.getTickPos());
    assertTrue("Expected \"defg\" but got \"" + instance2.getText() + "\"",
            "defg".equals(instance2.getText()));

  }

  /**
   * Test of compareTo method, of class Lyric.
   */
  @Test
  public void testCompareTo() {
    System.out.println("compareTo");
    Lyric instance_1 = new Lyric(0L, "ABC");
    Lyric instance_2 = new Lyric(0L, "ABC");
    Lyric instance_3 = new Lyric(100L, "ABC");
    Lyric instance_4 = new Lyric(100L, "XYZ");
    assertEquals(0, instance_1.compareTo(instance_2));
    assertEquals(0, instance_2.compareTo(instance_1));

    assertTrue(instance_1.compareTo(instance_3) < 0);
    assertTrue(instance_3.compareTo(instance_1) > 0);

    assertTrue(instance_3.compareTo(instance_4) < 0);
    assertTrue(instance_4.compareTo(instance_3) > 0);
  }

  @Test
  public void testIsTextEvent() throws InvalidMidiDataException {
    ShortMessage arbitraryMessage = new ShortMessage();
    boolean actual = Lyric.isLyricsEvent(new MidiEvent(arbitraryMessage, 123));
    assertFalse(actual);

    actual = Lyric.isLyricsEvent(makeTextEvent(123, "bla bla"));
    assertTrue(actual);
  }

  private MidiEvent makeTextEvent(long tickPos, String text) throws InvalidMidiDataException {
    return new MidiEvent(makeTextMessage(text), tickPos);
  }

  private MidiMessage makeTextMessage(String text) throws InvalidMidiDataException {
    MetaMessage message = new MetaMessage();
    message.setMessage(5, text.getBytes(), text.length());
    return message;
  }
}
