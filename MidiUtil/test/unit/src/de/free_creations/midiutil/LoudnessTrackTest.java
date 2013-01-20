/*
 * Copyright 2011 harald.
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

import javax.sound.midi.Track;
import javax.sound.midi.Sequence;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.MidiEvent;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author harald
 */
public class LoudnessTrackTest {

  private final int channel = 0;

  @Test
  public void testCreation() throws InvalidMidiDataException {
    Sequence sequence = new Sequence(Sequence.PPQ, 240);
    Track track = sequence.createTrack();

    int volume = 50;
    track.add(new_VolumeEvent(0, volume));
    int expression = 60;
    track.add(new_ExpressionEvent(500, expression));

    LoudnessTrack instance = new LoudnessTrack(track);

    // we expect two loudness events in our test-instance
    // 1) the default at 0
    // 2) the expression event at 500
    assertEquals(2, instance.size());

    assertTrue(instance.get(0).isVolume());
    assertTrue(instance.get(1).isExpression());
    assertTrue(instance.get(0).isObsolete());
    assertTrue(instance.get(1).isObsolete());

    assertEquals(volume * 127 * 127, instance.get(0).getLoudnessLevel());
    assertEquals(volume * expression * 127, instance.get(1).getLoudnessLevel());

  }

  @Test
  public void testCreationEx() throws InvalidMidiDataException {
    Sequence sequence = new Sequence(Sequence.PPQ, 240);
    Track track = sequence.createTrack();

    int vel_1 = 50;
    track.add(new_NoteOn(100, 1, vel_1));
    track.add(new_NoteOff(200, 1));
    int vel_2 = 60;
    track.add(new_NoteOn(500, 2, vel_2));
    track.add(new_NoteOff(600, 2));

    LoudnessTrack instance = new LoudnessTrack(track, true);

    // we expect two loudness events in our test-instance
    // 1) the key-on event at 100    
    // 2) the key-on event at 500
    assertEquals(2, instance.size());


    assertTrue(instance.get(0).isKeyOn());
    assertTrue(instance.get(1).isKeyOn());

    assertFalse(instance.get(0).isObsolete());
    assertFalse(instance.get(1).isObsolete());

    assertEquals(vel_1 * 127 * 127, instance.get(0).getLoudnessLevel());
    assertEquals(vel_2 * 127 * 127, instance.get(1).getLoudnessLevel());

  }

  /**
   * Verify whether obsolete loudness- events are correctly detected.
   *
   * @throws InvalidMidiDataException
   */
  @Test
  public void testObsolete() throws InvalidMidiDataException {
    Sequence sequence = new Sequence(Sequence.PPQ, 240, 3);
    Track track = sequence.createTrack();

    track.add(new_VolumeEvent(0, 50)); // event  0 is obsolete because it is followed by an other event
    track.add(new_ExpressionEvent(10, 61)); // event 1 is not obsolete because it precedes a note on
    track.add(new_NoteOn(20, 1, 64));
    track.add(new_NoteOn(30, 2, 64));
    track.add(new_ExpressionEvent(40, 62)); // event 2 is not obsolete because it is on sounding notes
    track.add(new_ExpressionEvent(45, 63)); // event 3 is not obsolete because it is on sounding notes
    track.add(new_NoteOff(50, 2));
    track.add(new_NoteOff(60, 1));
    track.add(new_ExpressionEvent(70, 64)); // event 4 is  obsolete because it does not act on any note

    LoudnessTrack instance = new LoudnessTrack(track);
    assertEquals(5, instance.size());

    assertTrue(instance.get(0).isObsolete());
    assertFalse(instance.get(1).isObsolete());
    assertFalse(instance.get(2).isObsolete());
    assertFalse(instance.get(3).isObsolete());
    assertTrue(instance.get(4).isObsolete());


  }

  @Test
  public void testCreateNormalizedTrack() throws InvalidMidiDataException {
    Sequence sequence = new Sequence(Sequence.PPQ, 240);
    Track inputTrack = sequence.createTrack();
    Track outputTrack = sequence.createTrack();

    // her we consruct an input track and in comment we note how the output track is expected to be
    //........................................ event (0) shall be the track volume
    inputTrack.add(new_VolumeEvent(0, 50)); //obsolte
    inputTrack.add(new_VolumeEvent(10, 61)); // should be event(1) in output
    inputTrack.add(new_NoteOn(20, 1, 64)); // should be event(2) in output
    inputTrack.add(new_NoteOn(30, 2, 64)); // should be event(3) in output
    inputTrack.add(new_VolumeEvent(40, 64)); // should be event(4) in output
    inputTrack.add(new_VolumeEvent(45, 85)); // should be event(5) in output this is the loudest event
    inputTrack.add(new_NoteOff(50, 2)); // should be event(6) in output
    inputTrack.add(new_NoteOff(60, 1)); // should be event(7) in output
    inputTrack.add(new_VolumeEvent(70, 66)); //obsolete
    // .........end of track is event(8) in output

    LoudnessTrack instance = new LoudnessTrack(inputTrack);
    outputTrack = instance.createNormalizedTrack(inputTrack, outputTrack);

    assertEquals(9, outputTrack.size());
    assertTrue(LoudnessEvent.isVolumeEvent(outputTrack.get(0)));
    assertTrue(LoudnessEvent.isExpressionEvent(outputTrack.get(1)));
    assertFalse(LoudnessEvent.isLoudnessEvent(outputTrack.get(2)));
    assertFalse(LoudnessEvent.isLoudnessEvent(outputTrack.get(3)));
    assertTrue(LoudnessEvent.isExpressionEvent(outputTrack.get(4)));
    assertTrue(LoudnessEvent.isExpressionEvent(outputTrack.get(5)));
    assertFalse(LoudnessEvent.isLoudnessEvent(outputTrack.get(3)));
    assertFalse(LoudnessEvent.isLoudnessEvent(outputTrack.get(3)));

    // the volume is expected to have the value of the loudest event (event(5) in above)
    assertEquals(85, ((ShortMessage) outputTrack.get(0).getMessage()).getMessage()[2]);
    // event(5) should now be 100%
    assertEquals(127, ((ShortMessage) outputTrack.get(5).getMessage()).getMessage()[2]);

  }

  private MidiEvent new_ExpressionEvent(long pos, int val) {
    final int expressionMSB = 11;
    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(ShortMessage.CONTROL_CHANGE,
              channel, expressionMSB, val);


    } catch (InvalidMidiDataException ex) {
      throw new RuntimeException(ex);
    }
    return new MidiEvent(message, pos);
  }

  private MidiEvent new_VolumeEvent(long pos, int val) {
    final int volumeMSB = 7;
    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(ShortMessage.CONTROL_CHANGE,
              channel, volumeMSB, val);


    } catch (InvalidMidiDataException ex) {
      throw new RuntimeException(ex);
    }
    return new MidiEvent(message, pos);
  }

  private MidiEvent new_NoteOn(long pos, int pitch, int velocity) {
    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(ShortMessage.NOTE_ON, channel, pitch, velocity);
    } catch (InvalidMidiDataException ex) {
      throw new RuntimeException(ex);
    }
    return new MidiEvent(message, pos);
  }

  private MidiEvent new_NoteOff(long pos, int pitch) {
    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(ShortMessage.NOTE_OFF, channel, pitch, 64);
    } catch (InvalidMidiDataException ex) {
      throw new RuntimeException(ex);
    }
    return new MidiEvent(message, pos);
  }
}
