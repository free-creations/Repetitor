/*
 *  Copyright 2011 Harald Postner <Harald at H-Postner.de>.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package de.free_creations.midiutil;

import java.util.logging.Logger;
import java.util.logging.Level;
import javax.sound.midi.ShortMessage;
import de.free_creations.midiutil.TempoTrack.TimeMap;
import javax.sound.midi.Track;
import javax.sound.midi.Sequence;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class TempoTrackTest {

  /**
   * Test of subList method, of class TempoTrack.
   */
  @Test
  public void testCreation() throws InvalidMidiDataException {
    System.out.println("testCreation");
    Sequence sequence = new Sequence(Sequence.PPQ, 240, 3);
    //try with the empty sequnce.
    TempoTrack instance = new TempoTrack(sequence);
    //and verify for the default event
    assertEquals(1, instance.size());
    assertEquals(500000, instance.get(0).getTempoPerQuarter());

    //try again with a sequence with tempo event at 0
    Track track = sequence.getTracks()[0];
    track.add(new_30BPM_TempoEvent(0));
    instance = new TempoTrack(sequence);
    //and verify that this time there is the given event at pos 0
    assertEquals(1, instance.size());
    assertEquals(2000000, instance.get(0).getTempoPerQuarter());

    //and finally let's try with a displaced tempo event
    sequence = new Sequence(Sequence.PPQ, 240, 3);
    track = sequence.getTracks()[2];
    track.add(new_30BPM_TempoEvent(1000));
    instance = new TempoTrack(sequence);
    //and verify that an additional default appears on pos 0
    assertEquals(2, instance.size());
    assertEquals(500000, instance.get(0).getTempoPerQuarter());
    assertEquals(2000000, instance.get(1).getTempoPerQuarter());

  }

  /**
   * Test of the method {@link TempoTrack#ticks() }
   * @throws InvalidMidiDataException
   */
  @Test
  public void testTicks() throws InvalidMidiDataException {
    System.out.println("testTicks");
    Sequence sequence = new Sequence(Sequence.PPQ, 240, 3);
    //try with the empty sequnce.
    TempoTrack instance = new TempoTrack(sequence);
    //and verify
    assertEquals(0, instance.ticks());


    //try again with a sequence and some note-on events
    sequence.getTracks()[0].add(makeNoteOn(100));
    sequence.getTracks()[1].add(makeNoteOn(200));
    sequence.getTracks()[2].add(makeNoteOn(300));
    instance = new TempoTrack(sequence);

    assertEquals(300, instance.ticks());



  }

  /**
   * Test of CreateTimeMap method, of class TempoTrack.
   */
  @Test
  public void testCreateTimeMap_1() throws InvalidMidiDataException {
    System.out.println("CreateTimeMap_1");
    // 100 Pulses per beat
    Sequence sequence = new Sequence(Sequence.PPQ, 100, 3);
    //try with the empty sequence.
    // 120 beats per minute (default) -> 2 beats per second
    // -> 200 pulses (midi Ticks) per second
    TempoTrack instance = new TempoTrack(sequence);
    double startTick = 0;
    double elapse = 1.2344560D;
    double tempoFactor = 1.0D;
    TimeMap timeMap = instance.CreateTimeMap(startTick, elapse, tempoFactor);
    assertEquals(1.0000000D, timeMap.getTimeOffset(200D), 1E-9);
    assertEquals(1.0010000D, timeMap.getTimeOffset(200.2D), 1E-9);

    double offset = 1.0000000D;
    double tickPos = timeMap.getTickForOffset(offset);
    assertEquals(200D, tickPos, tickPos * 1E-6);
    assertEquals(200.2D, timeMap.getTickForOffset(1.0010000D), tickPos * 1E-6);



    startTick = 6794791.2345D;
    timeMap = instance.CreateTimeMap(startTick, elapse, tempoFactor);
    assertEquals(1.0000000D, timeMap.getTimeOffset(startTick + 200D), 1E-6);

    tempoFactor = 0.5D;
    startTick = 1235.12345;
    timeMap = instance.CreateTimeMap(startTick, elapse, tempoFactor);
    assertEquals(2.0000000D, timeMap.getTimeOffset(startTick + 200), 1E-9);

  }

  /**
   * Test of testIndexForTick method, of class TempoTrack.
   * Case of empty sequence.
   */
  @Test
  public void testIndexForTick_1() throws InvalidMidiDataException {
    System.out.println("testIndexForTick_1");
    Sequence sequence = new Sequence(Sequence.PPQ, 100, 3);
    //try with the empty sequence.
    TempoTrack instance = new TempoTrack(sequence);
    assertEquals(0, instance.indexForTick(0));
    assertEquals(0, instance.indexForTick(123456L));
    assertEquals(0, instance.indexForTick(-123456L));
  }

  /**
   * Test of testIndexForTick method, of class TempoTrack.
   * Case of sequence with one given tempo event at tick 0.
   */
  @Test
  public void testIndexForTick_2() throws InvalidMidiDataException {
    System.out.println("testIndexForTick_2");
    Sequence sequence = new Sequence(Sequence.PPQ, 100, 3);
    Track track = sequence.getTracks()[0];
    track.add(new_30BPM_TempoEvent(0));

    TempoTrack instance = new TempoTrack(sequence);
    assertEquals(0, instance.indexForTick(0));
    assertEquals(0, instance.indexForTick(123456L));
    assertEquals(0, instance.indexForTick(-123456L));
  }

  /**
   * Test of testIndexForTick method, of class TempoTrack.
   * Case of sequence with two given tempo events at tick- position 0 and 100.
   */
  @Test
  public void testIndexForTick_3() throws InvalidMidiDataException {
    System.out.println("testIndexForTick_3");
    Sequence sequence = new Sequence(Sequence.PPQ, 100, 3);
    Track track = sequence.getTracks()[0];
    track.add(new_30BPM_TempoEvent(0)); // i = 0
    track.add(new_30BPM_TempoEvent(100)); // i = 1

    TempoTrack instance = new TempoTrack(sequence);
    assertEquals(0, instance.indexForTick(0));
    assertEquals(0, instance.indexForTick(50L));
    assertEquals(1, instance.indexForTick(100L));
    assertEquals(1, instance.indexForTick(123456L));
    assertEquals(0, instance.indexForTick(-123456L));
  }

  /**
   * Test of testIndexForTick method, of class TempoTrack.
   * Case of sequence with three given tempo events at tick- position 0 100 and 200.
   */
  @Test
  public void testIndexForTick_4() throws InvalidMidiDataException {
    System.out.println("testIndexForTick_4");
    Sequence sequence = new Sequence(Sequence.PPQ, 100, 3);
    Track track = sequence.getTracks()[0];
    track.add(new_30BPM_TempoEvent(0)); // i = 0
    track.add(new_30BPM_TempoEvent(100)); // i = 1
    track.add(new_30BPM_TempoEvent(200)); // i = 2

    TempoTrack instance = new TempoTrack(sequence);
    assertEquals(0, instance.indexForTick(0));
    assertEquals(0, instance.indexForTick(50L));
    assertEquals(1, instance.indexForTick(100L));
    assertEquals(1, instance.indexForTick(150L));
    assertEquals(2, instance.indexForTick(200L));
    assertEquals(2, instance.indexForTick(123456L));
    assertEquals(0, instance.indexForTick(-123456L));
    
  
  }

  /**
   * Test of CreateTimeMap method, of class TempoTrack.
   */
  @Test
  public void testCreateTimeMap_2() throws InvalidMidiDataException {
    System.out.println("CreateTimeMap_2");
    // 100 Pulses per beat
    Sequence sequence = new Sequence(Sequence.PPQ, 100, 3);
    //try with default at the beginning
    // 120 beats per minute (default) -> 2 beats per second
    // -> 200 pulses (midi Ticks) per second
    //and at 1000 ticks
    // 30 beats per minute  -> one beats per two seconds
    // -> 50 pulses (midi Ticks) per second

    Track track = sequence.getTracks()[0];
    track.add(new_30BPM_TempoEvent(1000));
    TempoTrack instance = new TempoTrack(sequence);


    double startTick = 0;
    double elapse = 2.0000;
    double tempoFactor = 1.0D;
    TimeMap timeMap = instance.CreateTimeMap(startTick, elapse, tempoFactor);
    assertEquals(1.00000D, timeMap.getTimeOffset(startTick + 200), 1E-9);

    startTick = 2000.234567;
    timeMap = instance.CreateTimeMap(startTick, elapse, tempoFactor);
    assertEquals(4.0040000D, timeMap.getTimeOffset(startTick + 200.2D), 1E-9);

    startTick = 899;
    timeMap = instance.CreateTimeMap(startTick, elapse, tempoFactor);
    double result = timeMap.getTimeOffset(startTick + 201);
    assertEquals(0.505000 + 2.000000, result, 1E-9);

    startTick = 900;
    tempoFactor = 0.5D;

    timeMap = instance.CreateTimeMap(startTick, elapse, tempoFactor);
    result = timeMap.getTimeOffset(startTick + 200);
    assertEquals(5.000000D, result, 1E-9);

    double offset = 5.0000000D;
    double tickPos = timeMap.getTickForOffset(offset);
    assertEquals(startTick + 200D, tickPos, 1E-6);

    offset = 0.0D;
    tickPos = timeMap.getTickForOffset(offset);
    assertEquals(startTick, tickPos, 1E-6);

    offset = 1.000000D;
    tickPos = timeMap.getTickForOffset(offset);
    assertEquals(startTick + 100, tickPos, 1E-6);
  }

  private MidiEvent new_30BPM_TempoEvent(long pos) {
    // 30 (quarter)-Beats per Minute
    // -> two seconds per beat = 2,000,000 microseconds
    // -> in Hex 0x1E8480 or as three bytes 0x1E  0x84  0x80
    int tempoMeta = 0x51;
    MetaMessage message = new MetaMessage();
    try {
      message.setMessage(tempoMeta, //
              new byte[]{(byte) 0x1E, (byte) 0x84, (byte) 0x80}, //
              3); //data2)
    } catch (InvalidMidiDataException ex) {
      throw new RuntimeException(ex);
    }

    return new MidiEvent(message, pos);

  }

  private MidiEvent makeNoteOn(long tick) {
    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(ShortMessage.NOTE_ON, 0, 64, 64);
    } catch (InvalidMidiDataException ex) {
      Logger.getLogger(TempoTrackTest.class.getName()).log(Level.SEVERE, null, ex);
    }
    return new MidiEvent(message, tick);
  }
}
