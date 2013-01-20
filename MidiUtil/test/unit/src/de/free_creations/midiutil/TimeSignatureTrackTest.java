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

import javax.sound.midi.Track;
import javax.sound.midi.Sequence;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class TimeSignatureTrackTest {

  /**
   * Test the constructor of the class {@link TimeSignatureTrack}
   */
  @Test
  public void testCreation() throws InvalidMidiDataException {
    System.out.println("testCreation");
    Sequence sequence = new Sequence(Sequence.PPQ, 240, 3);
    //try with the empty sequnce.
    TimeSignatureTrack instance = new TimeSignatureTrack(sequence);
    //and verify for the default event
    assertEquals(1, instance.size());
    assertEquals(4, instance.get(0).getDenominator());
    assertEquals(4, instance.get(0).getNumerator());

    //try again with a sequence with an explict time signature at 0
    Track track = sequence.getTracks()[0];
    track.add(new_3_4TimeSigEvent(0));
    instance = new TimeSignatureTrack(sequence);
    //and verify that this time there is the given 3/4 Time signature at 0.
    assertEquals(1, instance.size());
    assertEquals(3, instance.get(0).getNumerator());
    assertEquals(4, instance.get(0).getDenominator());

    //and finally let's try with a displaced tempo event
    sequence = new Sequence(Sequence.PPQ, 240, 3);
    track = sequence.getTracks()[2];
    track.add(new_3_4TimeSigEvent(1000));
    instance = new TimeSignatureTrack(sequence);
    //and verify that an additional default appears on pos 0
    assertEquals(2, instance.size());
    assertEquals(4, instance.get(0).getNumerator());
    assertEquals(3, instance.get(1).getNumerator());


  }

  @Test
  public void testTimesignatureForTick() throws InvalidMidiDataException {

    System.out.println("testTimesignatureForTick");
    Sequence sequence = new Sequence(Sequence.PPQ, 240, 3);
    Track track = sequence.getTracks()[0];
    track.add(new_3_4TimeSigEvent(1000));
    TimeSignatureTrack instance = new TimeSignatureTrack(sequence);
    //and verify that an additional default appears on pos 0
    assertEquals(2, instance.size());
    assertEquals(instance.timesignatureForTick(100), instance.get(0));
    assertEquals(instance.timesignatureForTick(1100), instance.get(1));



  }

  /**
   * Test the method {@link TimeSignatureTrack#getBeatPosition(long)  }
   * for the case of default time signature.
   */
  @Test
  public void testGetBeatPosition_1() throws InvalidMidiDataException {
    System.out.println("testGetBeatPosition_1");
    Sequence sequence = new Sequence(Sequence.PPQ, 240);
    TimeSignatureTrack instance = new TimeSignatureTrack(sequence);
    BeatPosition beatPosition = instance.getBeatPosition(0);
    assertEquals(4, beatPosition.getDenominator());
    assertEquals(4, beatPosition.getNumerator());
    assertEquals(0, beatPosition.getMeasure());
    assertEquals(0.0F, beatPosition.getBeat(), 1E-9F);

    beatPosition = instance.getBeatPosition(240);
    assertEquals(4, beatPosition.getDenominator());
    assertEquals(4, beatPosition.getNumerator());
    assertEquals(0, beatPosition.getMeasure());
    assertEquals(1.0F, beatPosition.getBeat(), 1E-9F);

    beatPosition = instance.getBeatPosition(3.12345 * 240D);
    assertEquals(4, beatPosition.getDenominator());
    assertEquals(4, beatPosition.getNumerator());
    assertEquals(0, beatPosition.getMeasure());
    assertEquals(3.12345D, beatPosition.getBeat(), 1E-9D);

    beatPosition = instance.getBeatPosition(4.12345 * 240D);
    assertEquals(4, beatPosition.getDenominator());
    assertEquals(4, beatPosition.getNumerator());
    assertEquals(1, beatPosition.getMeasure());
    assertEquals(0.12345D, beatPosition.getBeat(), 1E-9D);

  }

  /**
   * Test the method {@link TimeSignatureTrack#getBeatPosition(long)  }
   * for the case of a sequence of 4/3 than 3/4 and again 4/4 measures
   */
  @Test
  public void testGetBeatPosition_2() throws InvalidMidiDataException {
    System.out.println("testGetBeatPosition_2");
    Sequence sequence = new Sequence(Sequence.PPQ, 240, 1);
    Track track = sequence.getTracks()[0];
    track.add(new_3_4TimeSigEvent(4 * 240));
    track.add(new_4_4TimeSigEvent((4 + 3 + 3) * 240));

    TimeSignatureTrack instance = new TimeSignatureTrack(sequence);
    BeatPosition beatPosition = instance.getBeatPosition(0);
    assertEquals(4, beatPosition.getNumerator());
    assertEquals(4, beatPosition.getDenominator());
    assertEquals(0, beatPosition.getMeasure());
    assertEquals(0.0F, beatPosition.getBeat(), 1E-9F);

    beatPosition = instance.getBeatPosition(4 * 240);
    assertEquals(3, beatPosition.getNumerator());
    assertEquals(4, beatPosition.getDenominator());
    assertEquals(1, beatPosition.getMeasure());
    assertEquals(0.0F, beatPosition.getBeat(), 1E-9F);

    beatPosition = instance.getBeatPosition(4.12345 * 240D);
    assertEquals(3, beatPosition.getNumerator());
    assertEquals(4, beatPosition.getDenominator());
    assertEquals(1, beatPosition.getMeasure());
    assertEquals(0.12345D, beatPosition.getBeat(), 1E-9D);

    beatPosition = instance.getBeatPosition(7 * 240);
    assertEquals(3, beatPosition.getNumerator());
    assertEquals(4, beatPosition.getDenominator());
    assertEquals(2, beatPosition.getMeasure());
    assertEquals(0.0F, beatPosition.getBeat(), 1E-9F);

    beatPosition = instance.getBeatPosition((4 + 3 + 3 + 4) * 240);
    assertEquals(4, beatPosition.getNumerator());
    assertEquals(4, beatPosition.getDenominator());
    assertEquals(4, beatPosition.getMeasure());
    assertEquals(0.0F, beatPosition.getBeat(), 1E-9F);


  }

  /**
   * Test the method {@link TimeSignatureTrack#getTickPosition }
   * for the case of a 3/4 time signature placed after a full 4/4 measure.
   */
  @Test
  public void testGetTickPosition() throws InvalidMidiDataException {
    System.out.println("testGetTickPosition");
    Sequence sequence = new Sequence(Sequence.PPQ, 240, 1);
    Track track = sequence.getTracks()[0];
    track.add(new_3_4TimeSigEvent(4 * 240));

    TimeSignatureTrack instance = new TimeSignatureTrack(sequence);

    double expectedTickPos = 7 * 240;
    BeatPosition beatPosition = instance.getBeatPosition(expectedTickPos);

    double actualTickpos = instance.getTickPosition(beatPosition);

    assertEquals(expectedTickPos, actualTickpos, 1E-9F);


  }

  /**
   * utility function to  create a 3/4 time signature event.
   * @param pos midi tick where the event should be placed
   * @return a new midi event
   */
  private MidiEvent new_3_4TimeSigEvent(long pos) {
    int timeSigMeta = 0x58;
    MetaMessage message = new MetaMessage();
    try {
      message.setMessage(timeSigMeta, //
              new byte[]{
                (byte) 0x03, //numerator
                (byte) 0x02, //denominator 2^2=4
                (byte) 24, //MIDI clocks in one metronome click
                (byte) 32}, //number of notated 32nd notes in one MIDI quarter note
              4); //data2)
    } catch (InvalidMidiDataException ex) {
      throw new RuntimeException(ex);
    }

    return new MidiEvent(message, pos);

  }

  /**
   * utility function to  create a 4/4 time signature event.
   * @param pos midi tick where the event should be placed
   * @return a new midi event
   */
  private MidiEvent new_4_4TimeSigEvent(long pos) {
    int timeSigMeta = 0x58;
    MetaMessage message = new MetaMessage();
    try {
      message.setMessage(timeSigMeta, //
              new byte[]{
                (byte) 0x04, //numerator
                (byte) 0x02, //denominator 2^2=4
                (byte) 24, //MIDI clocks in one metronome click (?)
                (byte) 32}, //number of notated 32nd notes in one MIDI quarter note
              4); //data2 (the lenght in bytes)
    } catch (InvalidMidiDataException ex) {
      throw new RuntimeException(ex);
    }

    return new MidiEvent(message, pos);

  }
}
