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
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class MasterSequencerImplTest {

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

  @Test
  public void testTickToEffectiveBPM() throws InvalidMidiDataException {
    System.out.println("testTickToEffectiveBPM");
    MasterSequencerImpl instance = new MasterSequencerImpl(MidiSubsequencerMockFactory, null);
    //if no tempo track, it should always report the default tempo of 120 BPM.
    assertEquals(120D, instance.tickToEffectiveBPM(123D), 1E-9D);

    //prepare a test sequence with default  tempo at start and 30BPM after tick 1000
    Sequence sequence = new Sequence(Sequence.PPQ, 360, 16);
    Track masterTrack = sequence.getTracks()[0];
    masterTrack.add(new_30BPM_TempoEvent(1000));
    masterTrack.add(new_3_4TimeSigEvent(108000)); //the last event at 600 seconds

    tempoTrack = new TempoTrack(sequence);
    timeSignatureTrack = new TimeSignatureTrack(sequence);
    tickLength = sequence.getTickLength();
    instance.setMasterTrack(tempoTrack, timeSignatureTrack, tickLength);

    // verify....
    assertEquals(120D, instance.tickToEffectiveBPM(0D), 1E-9D);
    assertEquals(30D, instance.tickToEffectiveBPM(1001D), 1E-9D);

    // what about tempo factor...
    instance.setTempoFactor(2.0);
    assertEquals(240D, instance.tickToEffectiveBPM(0D), 1E-9D);
    assertEquals(60D, instance.tickToEffectiveBPM(1001D), 1E-9D);

  }

  @Test
  public void testGetTickPosition() {
    System.out.println("testGetTickPosition");
    MasterSequencerImpl instance = new MasterSequencerImpl(MidiSubsequencerMockFactory, null);
    //if no tempo track, it should always report to be at position zero.
    assertEquals(0D, instance.getCurrentTickPosition(123D), 1E-9D);
    instance.setMasterTrack(tempoTrack, timeSignatureTrack, tickLength);
    //with a tempo track, but no cycles done so far still at position zero.
    assertEquals(0D, instance.getCurrentTickPosition(0D), 1E-9D);
    double startPosition = 100;
    instance.setTickPosition(startPosition);

    // shifting the start position, should also shift the reported tick position
    assertEquals(startPosition, instance.getCurrentTickPosition(0D), 1E-9D);

    instance.prepareCycle(0D, 1D);

    // doing a cycle in not started state should not change the current tick position
    assertEquals(startPosition, instance.getCurrentTickPosition(0D), 1E-9D);

    instance.startMidi();
    instance.prepareCycle(1D, 1D);
    instance.prepareCycle(2D, 1D);
    //now the tick position should have moved by one second -> 180 Ticks
    assertEquals(startPosition + 180D, instance.getCurrentTickPosition(2D), 1E-9D);
    assertEquals(startPosition + 180D + 90D, instance.getCurrentTickPosition(2D + 0.5D), 1E-9D);

  }

  /**
   * Same as testGetTickPosition but with a tempo factor.
   */
  @Test
  public void testGetTickPositionWithTempoFactor() {
    System.out.println("testGetTickPositionWithTempoFactor");
    MasterSequencerImpl instance = new MasterSequencerImpl(MidiSubsequencerMockFactory, null);
    instance.setTempoFactor(2D);
    //if no tempo track, it should always report to be at position zero.
    assertEquals(0D, instance.getCurrentTickPosition(123D), 1E-9D);
    instance.setMasterTrack(tempoTrack, timeSignatureTrack, tickLength);
    //with a tempo track, but no cycles done so far still at position zero.
    assertEquals(0D, instance.getCurrentTickPosition(0D), 1E-9D);
    double startPosition = 100;
    instance.setTickPosition(startPosition);

    // shifting the start position, should also shift the reported tick position
    assertEquals(startPosition, instance.getCurrentTickPosition(0D), 1E-9D);

    instance.prepareCycle(0D, 1D);

    // doing a cycle in not started state should not change the current tick position
    assertEquals(startPosition, instance.getCurrentTickPosition(0D), 1E-9D);

    instance.startMidi();
    instance.prepareCycle(1D, 1D);
    instance.prepareCycle(2D, 1D);
    //now the tick position should have moved by one second -> 180 Ticks
    assertEquals(startPosition + 360D, instance.getCurrentTickPosition(2D), 1E-9D);
    assertEquals(startPosition + 360D + 180D, instance.getCurrentTickPosition(2D + 0.5D), 1E-9D);

  }

  @Test
  public void testGetBeatPosition() {
    System.out.println("testGetBeatPosition");
    MasterSequencerImpl instance = new MasterSequencerImpl(MidiSubsequencerMockFactory, null);
    //if no tempo track, it should always report to be at measure 0 in beat 0
    BeatPosition position = instance.getCurrentBeatPosition(123);
    assertEquals(0, position.getMeasure());
    assertEquals(0F, position.getBeat(), 1E-9F);
    assertEquals(4, position.getNumerator());
    assertEquals(4, position.getDenominator());


    instance.setMasterTrack(tempoTrack, timeSignatureTrack, tickLength);
    //with a tempo track, but no cycles done so far still at position zero.
    //but now we have a 3/4 rythm
    position = instance.getCurrentBeatPosition(0);
    assertEquals(0, position.getMeasure());
    assertEquals(0F, position.getBeat(), 1E-9F);
    assertEquals(3, position.getNumerator());
    assertEquals(4, position.getDenominator());


    //shift the startpos by one measure
    double startPosition = 3 * 360;
    instance.setTickPosition(startPosition);

    // shifting the start position, should also shift the reported beat pos
    position = instance.getCurrentBeatPosition(0);
    assertEquals(1, position.getMeasure());
    assertEquals(0F, position.getBeat(), 1E-9F);
    assertEquals(3, position.getNumerator());
    assertEquals(4, position.getDenominator());



    //let's do some cycles

    instance.startMidi();
    instance.prepareCycle(0D, 6D);
    instance.prepareCycle(6D, 6D);
    //now the tick position should have moved by one measure
    position = instance.getCurrentBeatPosition(0);
    assertEquals(2, position.getMeasure());
    assertEquals(0F, position.getBeat(), 1E-9F);
    assertEquals(3, position.getNumerator());
    assertEquals(4, position.getDenominator());

  }

  /**
   * same as testGetBeatPosition() but mixed rhythms
   */
  @Test
  public void testGetBeatPosition2() throws InvalidMidiDataException {
    System.out.println("testGetBeatPosition2");
    MasterSequencerImpl instance = new MasterSequencerImpl(MidiSubsequencerMockFactory, null);
    Sequence sequence = new Sequence(Sequence.PPQ, 360, 16);

    Track[] newTracks = sequence.getTracks();

    // 2 seconds per quarter beat
    // 180 Ticks per second
    newTracks[0].add(new_30BPM_TempoEvent(0));

    // 6 seconds per measure
    newTracks[0].add(new_3_4TimeSigEvent(4 * 360)); // 3/4 (walz)
    newTracks[0].add(new_3_4TimeSigEvent(108000)); //the last event at 600 seconds
    tempoTrack = new TempoTrack(sequence);
    timeSignatureTrack = new TimeSignatureTrack(sequence);


    instance.setMasterTrack(tempoTrack, timeSignatureTrack, tickLength);

    BeatPosition position = instance.getCurrentBeatPosition(0);
    assertEquals(0, position.getMeasure());
    assertEquals(0F, position.getBeat(), 1E-9F);
    assertEquals(4, position.getNumerator());
    assertEquals(4, position.getDenominator());


    //shift the startpos by one measure
    double startPosition = 4 * 360;
    instance.setTickPosition(startPosition);

    // shifting the start position, should also shift the reported beat pos
    position = instance.getCurrentBeatPosition(0);
    assertEquals(1, position.getMeasure());
    assertEquals(0F, position.getBeat(), 1E-9F);
    assertEquals(3, position.getNumerator());
    assertEquals(4, position.getDenominator());

    startPosition = startPosition + 4 * 360;
    instance.setTickPosition(startPosition);

    // shifting the start position, should also shift the reported beat pos
    position = instance.getCurrentBeatPosition(0);
    assertEquals(2, position.getMeasure());
    assertEquals(1F, position.getBeat(), 1E-9F);
    assertEquals(3, position.getNumerator());
    assertEquals(4, position.getDenominator());



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
  public void testSequentialProcessing() throws InvalidMidiDataException, MidiUnavailableException {
    System.out.println("testSequentialProcessing");

    //create a test-candidate and add the sequence created in the initialize() step
    MasterSequencerImpl instance = new MasterSequencerImpl(MidiSubsequencerMockFactory, null);
    instance.setMasterTrack(tempoTrack, timeSignatureTrack, tickLength);

    SubsequencerMock subsequencer = (SubsequencerMock) instance.createMidiSubSequencer(null, null);

    double cycleLength = 0.5D; //half a second, 90 ticks per cycle (see initialize)
    double streamTime = 123.0D; //arbitrary start time

    double startTick = 456.0D; //arbitrary start tick (within the given sequence)    


    instance.setTickPosition(startTick);


    instance.startMidi();
    assertTrue(subsequencer.started);

    //--do the first cycle
    instance.prepareCycle(streamTime, cycleLength);
    assertNotNull(subsequencer.timeMap_1);
    assertEquals(startTick, subsequencer.thisCycleStartTick, 1E-10D);
    assertEquals(startTick + 90.0D, subsequencer.nextCycleStartTick, 1E-10D);

    //--do an other cycle
    streamTime = streamTime + cycleLength;
    instance.prepareCycle(streamTime, cycleLength);
    assertNotNull(subsequencer.timeMap_1);
    assertEquals(startTick + 90.0D, subsequencer.thisCycleStartTick, 1E-10D);
    assertEquals(startTick + 180.0D, subsequencer.nextCycleStartTick, 1E-10D);


    instance.stopMidi();
    assertFalse(subsequencer.started);

  }

  /**
   * This test verifies that the master-sequencer calls the sub-sequencers
   * prepareNormalCycle() method and also the prepareLoopEndCycle() This is a
   * case with looping, where loop is longer than one cycle.
   */
  @Test
  public void testLoopingProcessing_1() throws InvalidMidiDataException, MidiUnavailableException {
    System.out.println("testLoopingProcessing_1");

    //create a test-candidate and add the sequence created in the initialize() step
    MasterSequencerImpl instance = new MasterSequencerImpl(MidiSubsequencerMockFactory, null);
    instance.setMasterTrack(tempoTrack, timeSignatureTrack, tickLength);

    SubsequencerMock subsequencer = (SubsequencerMock) instance.createMidiSubSequencer(null, null);

    double cycleLength = 0.5D; //half a second, 90 ticks per cycle (see initialize)
    double streamTime = 123.0D; //arbitrary start time

    double startTick = 456.0D; //arbitrary start tick (within the given sequence)   
    double loopStartTick = startTick + 10D;    //the loop start lies in the first cycle
    double loopEndTick = loopStartTick + 100D; //the loop end lies in the next cycle

    instance.setTickPosition(startTick);
    instance.setLoopStartPoint(loopStartTick);
    instance.setLoopEndPoint(loopEndTick);
    instance.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);

    instance.startMidi();
    assertTrue(subsequencer.started);

    //--the first cycle is like in testSequentialProcessing() above.
    instance.prepareCycle(streamTime, cycleLength);
    assertNotNull(subsequencer.timeMap_1);
    assertEquals(startTick, subsequencer.thisCycleStartTick, 1E-10D);
    assertEquals(startTick + 90.0D, subsequencer.nextCycleStartTick, 1E-10D);

    //--the next cycle is a looping cycle with a jump-back to loopStartTick
    streamTime = streamTime + cycleLength;
    instance.prepareCycle(streamTime, cycleLength);
    assertTrue(subsequencer.prepareLoopEndCycleCount > 0);
    assertNotNull(subsequencer.timeMap_1);
    assertNotNull(subsequencer.timeMap_2);
    double cycleStartTick = startTick + 90.0D;
    assertEquals(cycleStartTick, subsequencer.timeMap_1.getTickForOffset(0D), 1E-10D);
    assertEquals(loopStartTick, subsequencer.timeMap_2.getTickForOffset(0D), 1E-10D);
    assertEquals(cycleStartTick, subsequencer.thisCycleStartTick, 1E-10D);
    assertEquals(loopEndTick, subsequencer.loopEndTick, 1E-10D);
    assertEquals(loopStartTick, subsequencer.loopStartTick, 1E-10D);
    assertEquals(loopStartTick + 70.0D, subsequencer.nextCycleStartTick, 1E-10D);


    instance.stopMidi();
    assertFalse(subsequencer.started);

  }
  private int loopEventListenerCount = 0;
  private boolean propPlaying;

  /**
   * This test verifies that the master-sequencer correctly counts the loops.
   */
  @Test
  public void testLoopingProcessingLoopCount() throws InvalidMidiDataException, MidiUnavailableException {
    System.out.println("testLoopingProcessingLoopCount()");

    //create a test-candidate and add the sequence created in the initialize() step
    MasterSequencerImpl instance = new MasterSequencerImpl(MidiSubsequencerMockFactory, null);
    instance.setMasterTrack(tempoTrack, timeSignatureTrack, tickLength);

    SubsequencerMock subsequencer = (SubsequencerMock) instance.createMidiSubSequencer(null, null);

    loopEventListenerCount = 0;

    instance.add(new SequencerEventListener() {
      @Override
      public void loopDone(int newLoopCount) {
        loopEventListenerCount++;
      }

      @Override
      public void notifyPlaying(boolean isPlaying) {
        propPlaying = isPlaying;
      }
    });

    double cycleLength = 0.5D; //half a second, 90 ticks per cycle (see initialize)
    double streamTime = 123.0D; //arbitrary start time

    double startTick = 456.0D; //arbitrary start tick (within the given sequence)   
    double loopStartTick = startTick + 10D;    //the loop start lies in the first cycle
    double loopEndTick = loopStartTick + 100D; //the loop end lies in the next cycle

    instance.setTickPosition(startTick);
    instance.setLoopStartPoint(loopStartTick);
    instance.setLoopEndPoint(loopEndTick);
    final int LOOPCOUNT = 3;
    instance.setLoopCount(LOOPCOUNT);
    propPlaying = false;

    instance.startMidi();
    assertTrue(subsequencer.started);
    assertTrue(propPlaying);

    //-- Test starts here: execute some cycles and verify that 
    // prepareLoopEndCycle() has been called the right number of times.
    for (int i = 0; i < 10; i++) {
      instance.prepareCycle(streamTime, cycleLength);
      streamTime = streamTime + cycleLength;
    }

    // now the prepareLoopEndCycle() procedure should have been called 
    // "LOOPCOUNT" times.
    assertEquals(LOOPCOUNT, subsequencer.prepareLoopEndCycleCount);
    assertEquals(LOOPCOUNT, loopEventListenerCount);



    instance.stopMidi();
    assertFalse(propPlaying);


  }

  /**
   * This test verifies that the master-sequencer correctly calculates the cycle
   * start-tick and the cycle-end tick for a simple case.
   */
  @Test
  public void testCycleLength_1() throws InvalidMidiDataException, MidiUnavailableException {
    System.out.println("testCycleLength_1()");

    //create a test-candidate and a sequence
    MasterSequencerImpl instance = new MasterSequencerImpl(MidiSubsequencerMockFactory, null);

    Sequence sequence = new Sequence(Sequence.PPQ, 360, 16);
    Track[] newTracks = sequence.getTracks();

    // At Start: 1 seconds per quarter beat => 360 Ticks per second 
    newTracks[0].add(new_60BPM_TempoEvent(0));


    newTracks[0].add(new_3_4TimeSigEvent(108000)); //the last event at 600 seconds
    tempoTrack = new TempoTrack(sequence);
    timeSignatureTrack = new TimeSignatureTrack(sequence);
    tickLength = sequence.getTickLength();

    instance.setMasterTrack(tempoTrack, timeSignatureTrack, tickLength);

    SubsequencerMock subsequencer = (SubsequencerMock) instance.createMidiSubSequencer(null, null);

    double cycleLength = 2.0D; // cycle lenght of 2 seconds (is not realistic but easy to calculate)
    double streamTime = 123.0D; //arbitrary start time

    double startTick = 456; // arbitray start position
    double expectedCycleEnd = startTick + 2 * 360; // two seconds after start (because cycle length is 2 seconds) 


    instance.setTickPosition(startTick);
    instance.setLoopCount(0);
    instance.startMidi();
    assertTrue(subsequencer.started);

    instance.prepareCycle(streamTime, cycleLength);

    assertEquals(startTick, subsequencer.thisCycleStartTick, 1E-6);
    assertEquals(expectedCycleEnd, subsequencer.nextCycleStartTick, 1E-6);

  }

  /**
   * This test verifies that the master-sequencer correctly calculates the cycle
   * start-tick and the cycle-end tick after a tempo change.
   */
  @Test
  public void testCycleLength_2() throws InvalidMidiDataException, MidiUnavailableException {
    System.out.println("testCycleLength_2()");

    //create a test-candidate and a sequence
    MasterSequencerImpl instance = new MasterSequencerImpl(MidiSubsequencerMockFactory, null);

    Sequence sequence = new Sequence(Sequence.PPQ, 360, 16);
    Track[] newTracks = sequence.getTracks();

    // At Start: 2 seconds per quarter beat => 180 Ticks per second 
    newTracks[0].add(new_30BPM_TempoEvent(0));
    // At pos 1800 tempo-change : 1 second per quarter beat => 360 Ticks per second 
    long tempoChangePos = 1800;
    newTracks[0].add(new_60BPM_TempoEvent(tempoChangePos));


    newTracks[0].add(new_3_4TimeSigEvent(108000)); //the last event at 600 seconds
    tempoTrack = new TempoTrack(sequence);
    timeSignatureTrack = new TimeSignatureTrack(sequence);
    tickLength = sequence.getTickLength();

    instance.setMasterTrack(tempoTrack, timeSignatureTrack, tickLength);

    SubsequencerMock subsequencer = (SubsequencerMock) instance.createMidiSubSequencer(null, null);

    double cycleLength = 2.0D; // cycle lenght of 2 seconds (is not realistic but easy to calculate)
    double streamTime = 123D; //arbitrary 

    double startTick = tempoChangePos + 123.0D; //arbitrary start time after the tempo change
    double expectedCycleEnd = startTick + 2 * 360; // two seconds after start (because cycle length is 2 seconds) 


    instance.setTickPosition(startTick);
    instance.setLoopCount(0);
    instance.startMidi();
    assertTrue(subsequencer.started);

    instance.prepareCycle(streamTime, cycleLength);

    assertEquals(startTick, subsequencer.thisCycleStartTick, 1E-6);
    assertEquals(expectedCycleEnd, subsequencer.nextCycleStartTick, 1E-6);

  }

  /**
   * This test verifies that the master-sequencer correctly calculates the cycle
   * start-tick and the cycle end tick even if a change in tempo occurs within
   * the cycle.
   */
  @Test
  public void testCycleLength_3() throws InvalidMidiDataException, MidiUnavailableException {
    System.out.println("testCycleLength_3()");

    //create a test-candidate and a sequence
    MasterSequencerImpl instance = new MasterSequencerImpl(MidiSubsequencerMockFactory, null);

    Sequence sequence = new Sequence(Sequence.PPQ, 360, 16);
    Track[] newTracks = sequence.getTracks();

    // At Start: 2 seconds per quarter beat => 180 Ticks per second 
    newTracks[0].add(new_30BPM_TempoEvent(0));
    // At pos 1800: 1 second per quarter beat => 360 Ticks per second 
    long tempoChangePos = 1800;
    newTracks[0].add(new_60BPM_TempoEvent(tempoChangePos));

    newTracks[0].add(new_3_4TimeSigEvent(108000)); //the last event at 600 seconds
    tempoTrack = new TempoTrack(sequence);
    timeSignatureTrack = new TimeSignatureTrack(sequence);
    tickLength = sequence.getTickLength();

    instance.setMasterTrack(tempoTrack, timeSignatureTrack, tickLength);

    SubsequencerMock subsequencer = (SubsequencerMock) instance.createMidiSubSequencer(null, null);

    double cycleLength = 2.0D; // cycle lenght of 2 seconds (is not realistic but easy to calculate)
    double streamTime = 123.0D; //arbitrary start time

    double startTick = tempoChangePos - 180; // one second before the tempo change 
    double expectedCycleEnd = tempoChangePos + 360; // one second after the tempo change (because cycle length is 2 seconds) 


    instance.setTickPosition(startTick);
    instance.setLoopCount(0);
    instance.startMidi();
    assertTrue(subsequencer.started);

    instance.prepareCycle(streamTime, cycleLength);

    assertEquals(startTick, subsequencer.thisCycleStartTick, 1E-6);
    assertEquals(expectedCycleEnd, subsequencer.nextCycleStartTick, 1E-6);

  }

  /**
   * This test verifies that the master-sequencer correctly calculates the cycle
   * start-tick and the cycle-end tick for a loop jump.
   */
  @Test
  public void testCycleLoopLength_1() throws InvalidMidiDataException, MidiUnavailableException {
    System.out.println("testLoopingProcessingLoopCount()");

    //create a test-candidate and a sequence
    MasterSequencerImpl instance = new MasterSequencerImpl(MidiSubsequencerMockFactory, null);

    Sequence sequence = new Sequence(Sequence.PPQ, 360, 16);
    Track[] newTracks = sequence.getTracks();

    // At Start: 2 seconds per quarter beat => 180 Ticks per second 
    newTracks[0].add(new_30BPM_TempoEvent(0));

    // At pos 1800: 1 second per quarter beat => 360 Ticks per second 
    long tempoChangePos = 1800;
    newTracks[0].add(new_60BPM_TempoEvent(tempoChangePos));

    newTracks[0].add(new_3_4TimeSigEvent(108000)); //the last event at 600 seconds
    tempoTrack = new TempoTrack(sequence);
    timeSignatureTrack = new TimeSignatureTrack(sequence);
    tickLength = sequence.getTickLength();

    instance.setMasterTrack(tempoTrack, timeSignatureTrack, tickLength);
    SubsequencerMock subsequencer = (SubsequencerMock) instance.createMidiSubSequencer(null, null);

    double cycleLength = 2.0D; // cycle lenght of 2 seconds (is not realistic but easy to calculate)
    double streamTime = 123.0D; //arbitrary start time

    double startTick = tempoChangePos + 1234; // arbitray after the tempo change 
    double loopStartTick = 100;    //arbirary before the tempo change 
    double loopEndTick = startTick + 360; //from start-tick, run one second than jump to lopp-start-tick
    double expectedCycleEnd = loopStartTick + 180;

    instance.setTickPosition(startTick);
    instance.setLoopStartPoint(loopStartTick);
    instance.setLoopEndPoint(loopEndTick);
    final int LOOPCOUNT = 3;
    instance.setLoopCount(LOOPCOUNT);


    instance.startMidi();
    assertTrue(subsequencer.started);

    instance.prepareCycle(streamTime, cycleLength);

    assertEquals(startTick, subsequencer.thisCycleStartTick, 1E-6);
    assertEquals(expectedCycleEnd, subsequencer.nextCycleStartTick, 1E-6);

  }

  /**
   * utility function to create a "Note-on" event.
   *
   * @param pos midi tick where the event should be placed
   * @return a new midi event
   */
  private MidiEvent new_NoteOnEvent(long pos) {

    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(0x9,//command = note on
              0,// channel = 0
              0x3C,// note = middle C
              0x40);//velocity

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

  private class SubsequencerMock implements MasterSequencer.MidiSubSequencer {

    public boolean started = false;
    public TimeMap timeMap_1 = null;
    public double thisCycleStartTick = -1;
    public double nextCycleStartTick = -1;
    public int prepareNormalCycleCount = 0;
    public int prepareLoopEndCycleCount = 0;
    public TimeMap timeMap_2 = null;
    public double loopStartTick = 1;
    public double loopEndTick = -1;

    @Override
    public void prepareNormalCycle(TimeMap timeMap, double thisCycleStartTick, double nextCycleStartTick) {
      this.prepareNormalCycleCount++;
      this.timeMap_1 = timeMap;
      this.thisCycleStartTick = thisCycleStartTick;
      this.nextCycleStartTick = nextCycleStartTick;
    }

    @Override
    public void prepareLoopEndCycle(TimeMap timeMap_1, TimeMap timeMap_2, double thisCycleStartTick, double nextCycleStartTick, double loopStartTick, double loopEndTick) {
      this.prepareLoopEndCycleCount++;
      this.timeMap_1 = timeMap_1;
      this.timeMap_2 = timeMap_2;
      this.thisCycleStartTick = thisCycleStartTick;
      this.nextCycleStartTick = nextCycleStartTick;
      this.loopStartTick = loopStartTick;
      this.loopEndTick = loopEndTick;
    }

    @Override
    public void prepareSession(double startPosition, MasterSequencer.PlayingMode mode) {
      started = true;
    }

    @Override
    public void stopSession() {
      started = false;
    }
  }
  private MasterSequencer.SubSequencerFactory MidiSubsequencerMockFactory =
          new MasterSequencer.SubSequencerFactory() {
            @Override
            public MidiSubSequencer make(String name, Soundbank soundbank) throws MidiUnavailableException {
              return new SubsequencerMock();
            }

            @Override
            public SubSequencer makeAudioRecorder(String name) throws IOException {
              throw new RuntimeException("Not allowed.");
            }
          };
}
