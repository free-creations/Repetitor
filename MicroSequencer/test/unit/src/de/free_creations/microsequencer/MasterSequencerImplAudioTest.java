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
package de.free_creations.microsequencer;

import de.free_creations.microsequencer.MasterSequencer.AudioRecorderSubSequencerInt;
import de.free_creations.microsequencer.MasterSequencer.MidiSubSequencer;
import de.free_creations.microsequencer.MasterSequencer.SubSequencer;
import de.free_creations.midiutil.BeatPosition;
import de.free_creations.midiutil.TempoTrack;
import de.free_creations.midiutil.TempoTrack.TimeMap;
import de.free_creations.midiutil.TimeSignatureTrack;
import java.io.IOException;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Track;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the MasterSequencerImpl for Audio processing.
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class MasterSequencerImplAudioTest {

  private TempoTrack tempoTrack;
  private TimeSignatureTrack timeSignatureTrack;
  private long tickLength;

  @Before
  public void initialize() throws Exception {
    Sequence sequence = new Sequence(Sequence.PPQ, 360, 16);

    Track[] newTracks = sequence.getTracks();

    // 2 seconds per quarter beat
    // 180 Ticks per second
    newTracks[0].add(new_30BPM_TempoEvent(0));

    // 6 seconds per measure
    newTracks[0].add(new_3_4TimeSigEvent(0)); // 3/4 (walz)
    newTracks[0].add(new_3_4TimeSigEvent(108000)); //the last event at 600 seconds
    tempoTrack = new TempoTrack(sequence);
    timeSignatureTrack = new TimeSignatureTrack(sequence);
    tickLength = sequence.getTickLength();
  }

  /**
   * This test verifies that the master-sequencer calls the sub-sequencer's
   * prepareNormalCycle() method for each cycle. This is the simplest case where
   * the sequence is traversed from start to end without looping.
   *
   * @throws InvalidMidiDataException
   * @throws MidiUnavailableException
   */
  @Test
  public void testSequentialProcessing() throws InvalidMidiDataException, MidiUnavailableException, IOException {
    System.out.println("testSequentialProcessing");

    //create a test-candidate and add the sequence created in the initialize() step
    MasterSequencerImpl instance = new MasterSequencerImpl(null, AudioSubsequencerMockFactory);
    instance.setMasterTrack(tempoTrack, timeSignatureTrack, tickLength);

    SubsequencerMock subsequencer = (SubsequencerMock) instance.createAudioRecorderSubSequencer("test");

    instance.startMidi(PlayingMode.MidiOnly);
    assertTrue(subsequencer.started);
    assertEquals(PlayingMode.MidiOnly, subsequencer.payingMode);
    instance.stopMidi();
    assertFalse(subsequencer.started);

    instance.startMidi(PlayingMode.PlayAudio);
    assertTrue(subsequencer.started);
    assertEquals(PlayingMode.PlayAudio, subsequencer.payingMode);
    instance.stopMidi();
    assertFalse(subsequencer.started);


    instance.startMidi(PlayingMode.PlayRecordAudio);
    assertTrue(subsequencer.started);
    assertEquals(PlayingMode.PlayRecordAudio, subsequencer.payingMode);
    instance.stopMidi();
    assertFalse(subsequencer.started);

    instance.startMidi(PlayingMode.RecordAudio);
    assertTrue(subsequencer.started);
    assertEquals(PlayingMode.RecordAudio, subsequencer.payingMode);
    instance.stopMidi();
    assertFalse(subsequencer.started);

  }

  /**
   * utility function to create a "tempo" event.
   *
   * @param pos midi tick where the event should be placed
   * @return a new midi event
   */
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

  /**
   * utility function to create a "tempo" event.
   *
   * @param pos midi tick where the event should be placed
   * @return a new midi event
   */
  private MidiEvent new_60BPM_TempoEvent(long pos) {
    // 60 (quarter)-Beats per Minute
    // -> one second per beat = 1,000,000 microseconds
    // -> in Hex 0x0F4240 or as three bytes 0x0F  0x42  0x40
    int tempoMeta = 0x51;
    MetaMessage message = new MetaMessage();
    try {
      message.setMessage(tempoMeta, //
              new byte[]{(byte) 0x0F, (byte) 0x42, (byte) 0x40}, //
              3); //data2)
    } catch (InvalidMidiDataException ex) {
      throw new RuntimeException(ex);
    }

    return new MidiEvent(message, pos);

  }

  /**
   * utility function to create a 3/4 time signature event.
   *
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

  private class SubsequencerMock implements MasterSequencer.AudioRecorderSubSequencerInt {

    public boolean started = false;
    PlayingMode payingMode;

    @Override
    public void prepareSession(double startTick, PlayingMode mode) {
      payingMode = mode;
      started = true;
    }

    @Override
    public void stopSession() {
      started = false;
    }

    @Override
    public void prepareSwitch(double switchPoint) {
      throw new UnsupportedOperationException("Not supported yet.");
    }
  }
  private MasterSequencer.SubSequencerFactory AudioSubsequencerMockFactory =
          new MasterSequencer.SubSequencerFactory() {
            @Override
            public MidiSubSequencer make(String name, Soundbank soundbank) throws MidiUnavailableException {
              throw new RuntimeException("Not allowed in this test.");
            }

            @Override
            public AudioRecorderSubSequencerInt makeAudioRecorder(String name) throws IOException {
              return new SubsequencerMock();
            }
          };
}
