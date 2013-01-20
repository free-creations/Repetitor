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

import java.net.URL;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.*;


/**
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class LyricTrackTest {

    public LyricTrackTest() {
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
   * Test of size method, of class LyricTrack.
   */
  @Test
  public void testCreation() {
    try {
      URL midiUrl = this.getClass().getResource("resources/GoodNightLadies.midi");
      Sequence sequence = MidiSystem.getSequence(midiUrl);
      Track track = sequence.getTracks()[3];

      LyricTrack instance = new LyricTrack(track);
      assertFalse(instance.isEmpty());
      assertTrue(instance.size()>0);

    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

}