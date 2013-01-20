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

import java.util.logging.Logger;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * This class tests verifies some properties of the implementation
 * of the class javax.sound.midi.Track.
 * The specification says: "These operations keep the event
 * list in the correct time order". As many operations rely
 * on this fact we'll verify whether there are exceptions to this rule.
 * @author H. Postner
 */
public class JavaxTrackTest {

  Track instance;
  MidiMessage midiMessage = new ShortMessage();

  public JavaxTrackTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() throws InvalidMidiDataException {
    Sequence sequence = new Sequence(Sequence.PPQ, 240, 1);
    instance = sequence.getTracks()[0];
  }

  @After
  public void tearDown() {
  }

  /**
   * inserting in random order keeps the time sequence.
   */
  @Test
  public void testInsertInRandomOrder() {
    instance.add(new MidiEvent(midiMessage, 0));
    instance.add(new MidiEvent(midiMessage, 1000));
    instance.add(new MidiEvent(midiMessage, 500));
    instance.add(new MidiEvent(midiMessage, 500));
    assertTrue(timeOrderIsOK());
  }

  /**
   * manipulating individual events DOES NOT KEEP the time order.
   */
  @Test
  public void testInsertAndManipulate() {
    instance.add(new MidiEvent(midiMessage, 100));
    instance.add(new MidiEvent(midiMessage, 200));

    MidiEvent event_1 = instance.get(0);
    event_1.setTick(1000);

    assertEquals(1000, instance.get(0).getTick());

    // now the time order is not OK anymore!!!
    assertTrue(!timeOrderIsOK());

    //and even length of the track is wrong!!!
    assertEquals(200, instance.ticks());
  }

  private boolean timeOrderIsOK() {
    for (int i = 1; i < instance.size(); i++) {
      MidiEvent prevEvent = instance.get(i - 1);
      MidiEvent thisEvent = instance.get(i);
      if(prevEvent.getTick() > thisEvent.getTick()){
        return false;
      }
    }
    return true;
  }
}
