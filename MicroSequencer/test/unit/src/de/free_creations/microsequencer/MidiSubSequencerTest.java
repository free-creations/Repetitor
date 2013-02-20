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

import com.sun.media.sound.AudioSynthesizer;
import com.sun.media.sound.AudioSynthesizerPropertyInfo;
import de.free_creations.midiutil.MidiUtil;
import de.free_creations.midiutil.TempoTrack;
import de.free_creations.midiutil.TempoTrack.TimeMap;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Patch;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Track;
import javax.sound.midi.Transmitter;
import javax.sound.midi.VoiceStatus;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.SourceDataLine;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class MidiSubSequencerTest {

  private static final Logger logger = Logger.getLogger(MidiSubSequencerTest.class.getName());

  public MidiSubSequencerTest() {
  }
  private TempoTrack tempoTrack;
  Track[] tracks;
  double duration;// in seconds
  double midiTicksPerSecond;

  @Before
  public void initialize() throws Exception {
    Sequence sequence = new Sequence(Sequence.PPQ, 360, 16);
    Track[] newTracks = sequence.getTracks();
    newTracks[0].add(new_30BPM_TempoEvent(0)); //180 Midi-Ticks per second
    midiTicksPerSecond = 180;
    tempoTrack = new TempoTrack(sequence);


    newTracks[1].add(newNoteOnEvent(360)); //event A at 2 seconds 
    newTracks[1].add(newNoteOffEvent(720)); // event B at 4 seconds

    newTracks[1].add(newNoteOnEvent(1080)); // event C at 6 seconds
    newTracks[1].add(newNoteOffEvent(108000)); //event D the last event at 600 seconds

    tracks = new Track[]{newTracks[1]};
    duration = 610.0D;


  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  /**
   * Test of setTracks method, of class MidiSubSequencer.
   */
  @Test
  public void testSetTracks() throws InvalidMidiDataException, MidiUnavailableException {
    System.out.println("setTracks");
    MidiSubSequencer instance = new MidiSubSequencer("test", new SynthMockup(), null);
    instance.setTracks(tracks);
    assertArrayEquals(tracks, instance.getTracks());
  }

  /**
   * Run the MidiSubSequencer for a number of cycles and verify that controller
   * events are correctly used to initialize the synthesizer.
   */
  @Test
  public void testTrackInitialistion() throws InvalidMidiDataException, MidiUnavailableException, Exception {
    System.out.println("testTrackInitialistion");

    Sequence sequence = new Sequence(Sequence.PPQ, 360, 2);
    Track[] newTracks = sequence.getTracks();
    MidiEvent testPitchbend = newPitchBendEvent(50); //this is the event we will be looking for
    newTracks[1].add(testPitchbend);
    newTracks[1].add(newNoteOffEvent(108000)); //just to shift the End-of-Track far away
    tracks = new Track[]{newTracks[1]};

    duration = 10.0D;
    int samplingRate = 44100;
    int framesPerCycle = 33;
    int outputChannelCount = 2;
    boolean noninterleaved = false;
    double cycleDuration = (double) framesPerCycle / (double) samplingRate;
    // we startOut playing after the pitch bend..
    double thisCycleStartTick = 100D;
    double nextCycleStartTick = 100D;
    double framesInTotal = samplingRate * duration;
    int cycleCount = (int) Math.ceil(framesInTotal / framesPerCycle);

    SynthMockup synth = new SynthMockup();
    MidiSubSequencer instance = new MidiSubSequencer("test", synth, null);


    instance.setTracks(tracks);
    instance.openOut(samplingRate, framesPerCycle, outputChannelCount, noninterleaved);
    instance.prepareSession(thisCycleStartTick, null);
    double streamTimeSeconds = 24 * 60 * 60; //assume that the stream has allready run a whole day
    // now we are simulating a number cycles
    for (int i = 0; i < cycleCount; i++) {

      streamTimeSeconds += cycleDuration;
      thisCycleStartTick = nextCycleStartTick;
      TimeMap timeMap = tempoTrack.CreateTimeMap(thisCycleStartTick, 1.1 * cycleDuration, 1.0);
      nextCycleStartTick = timeMap.getTickForOffset(cycleDuration);

      // this would be done by the master sequencer
      instance.prepareNormalCycle(timeMap, thisCycleStartTick, nextCycleStartTick);
      // this shall be done by the processor
      instance.processOut(streamTimeSeconds);
    }

    // now verify that the events (prepared in initialize) have been send
    //we expect 17 events (16 "reset-controllers" and the "test pitch bend" intoduced above")
    assertEquals(17, synth.timedEvents.size());
    // let's search for the test pitch bend
    Collection<SynthMockup.TimedEvent> collectedEvents = synth.timedEvents.values();
    boolean found = false;
    for (SynthMockup.TimedEvent e : collectedEvents) {
      if (e.message.equals(testPitchbend.getMessage())) {
        found = true;
      }
    }
    assertTrue(found);
  }

  /**
   * Run the MidiSubSequencer for a number of normal cycles and verify that 
   * it sends the correctly timed events to the synthesiser.
   * This test starts at the beginning of the sequence.
   */
  @Test
  public void testCycles() throws InvalidMidiDataException, MidiUnavailableException, Exception {
    System.out.println("testCycles");


    int samplingRate = 44100;
    int framesPerCycle = 33;
    int outputChannelCount = 2;
    boolean noninterleaved = false;
    double cycleDuration = (double) framesPerCycle / (double) samplingRate;
    double thisCycleStartTick = 0D;
    double nextCycleStartTick = 0D;
    double framesInTotal = samplingRate * duration;
    int cycleCount = (int) Math.ceil(framesInTotal / framesPerCycle);

    SynthMockup synth = new SynthMockup();
    MidiSubSequencer instance = new MidiSubSequencer("test", synth, null);
    instance.setTracks(tracks);
    instance.openOut(samplingRate, framesPerCycle, outputChannelCount, noninterleaved);
    instance.prepareSession(thisCycleStartTick, null);
    double streamTimeSeconds = 24 * 60 * 60; //assume that the stream has allready run a whole day
    // now we are simulating a cycling for about 10 minutes
    long startTime = System.nanoTime();
    for (int i = 0; i < cycleCount; i++) {

      streamTimeSeconds += cycleDuration;
      thisCycleStartTick = nextCycleStartTick;
      TimeMap timeMap = tempoTrack.CreateTimeMap(thisCycleStartTick, 1.1 * cycleDuration, 1.0);
      nextCycleStartTick = timeMap.getTickForOffset(cycleDuration);

      // this would be done by the master sequencer
      instance.prepareNormalCycle(timeMap, thisCycleStartTick, nextCycleStartTick);
      // this shall be done by the processor
      instance.processOut(streamTimeSeconds);
    }
    double usedCPUTime = (System.nanoTime() - startTime) / 1E9;
    double efficiency = duration / usedCPUTime;
    System.out.println("... Time = " + usedCPUTime + "; efficiency = " + efficiency);


    // now verify that the events (prepared in initialize) have been send
    // (remember there are 16 reset-all-controller messages comming before)
    assertEquals(16 + 4, synth.timedEvents.size());

    Long[] timeStamps = synth.timedEvents.keySet().toArray(new Long[]{});

    assertTrue("expected 2000000L, but was " + timeStamps[16 + 0] + ".", 2000000L + 2 > timeStamps[0 + 16]);
    assertTrue("expected 2000000L, but was " + timeStamps[16 + 0] + ".", 2000000L - 2 < timeStamps[0 + 16]);

    assertTrue("expected 4000000L, but was " + timeStamps[16 + 1] + ".", 4000000L + 4 > timeStamps[1 + 16]);
    assertTrue("expected 4000000L, but was " + timeStamps[16 + 1] + ".", 4000000L - 4 < timeStamps[1 + 16]);

    assertTrue("expected 6000000L, but was " + timeStamps[16 + 2] + ".", 6000000L + 6 > timeStamps[2 + 16]);
    assertTrue("expected 6000000L, but was " + timeStamps[16 + 2] + ".", 6000000L - 6 < timeStamps[2 + 16]);

    assertTrue("expected 600000000L, but was " + timeStamps[16 + 3] + ".", 600000000L + 100 > timeStamps[3 + 16]);
    assertTrue("expected 600000000L, but was " + timeStamps[16 + 3] + ".", 600000000L - 100 < timeStamps[3 + 16]);
  }

  /**
   * Run the MidiSubSequencer for a number of normal cycles and verify that 
   * it sends the correctly timed events to the synthesiser.
   * This test differs from the previous one in the fact that
   * we startOut in near the end of the sequence.
   */
  @Test
  public void testCyclesStartingLater() throws InvalidMidiDataException, MidiUnavailableException, Exception {
    System.out.println("testCyclesStartingLater");



    int samplingRate = 44100;
    int framesPerCycle = 33;
    int outputChannelCount = 2;
    boolean noninterleaved = false;
    double cycleDuration = (double) framesPerCycle / (double) samplingRate;
    double thisCycleStartTick = 1080D; // at 6 seconds 
    double nextCycleStartTick = 1080D;
    double framesInTotal = samplingRate * 595.000000;
    int cycleCount = (int) Math.ceil(framesInTotal / framesPerCycle);

    SynthMockup synth = new SynthMockup();
    MidiSubSequencer instance = new MidiSubSequencer("test", synth, null);
    instance.setTracks(tracks);
    instance.openOut(samplingRate, framesPerCycle, outputChannelCount, noninterleaved);
    instance.prepareSession(thisCycleStartTick, null);
    double streamTimeSeconds = 24 * 60 * 60; //assume that the stream has allready run a whole day
    // now we are simulating a number of cycles
    for (int i = 0; i < cycleCount; i++) {

      streamTimeSeconds += cycleDuration;
      thisCycleStartTick = nextCycleStartTick;
      TimeMap timeMap = tempoTrack.CreateTimeMap(thisCycleStartTick, 1.1 * cycleDuration, 1.0);
      nextCycleStartTick = timeMap.getTickForOffset(cycleDuration);

      // this would be done by the master sequencer
      instance.prepareNormalCycle(timeMap, thisCycleStartTick, nextCycleStartTick);
      // this shall be done by the processor
      instance.processOut(streamTimeSeconds);
    }

    // now verify that the events (prepared in initialize) have been send
    // (remember there are 16 reset-all-controller messages comming before)
    assertEquals(16 + 2, synth.timedEvents.size());

    Long[] keys = synth.timedEvents.keySet().toArray(new Long[]{});


    SynthMockup.TimedEvent timedEvent = synth.timedEvents.get(keys[16 + 0]);
    long timeStamp = timedEvent.microsecondsTimestamp;
    assertTrue("expected 0L, but was " + timeStamp + ".", 0L + 1 > timeStamp);
    assertTrue("expected 0L, but was " + timeStamp + ".", 0L - 1 < timeStamp);

    timedEvent = synth.timedEvents.get(keys[16 + 1]);
    timeStamp = timedEvent.microsecondsTimestamp;
    assertTrue("expected 594000000L, but was " + timeStamp + ".", 594000000L + 1 > timeStamp);
    assertTrue("expected 594000000L, but was " + timeStamp + ".", 594000000L - 1 < timeStamp);


  }

  /**
   * Run the MidiSubSequencer for a number of cycles and verify that 
   * it sends the correctly timed events to the synthesiser.
   * This test differs from the previous one in the fact that
   * we effectue one loop.
   */
  @Test
  public void testCyclesLooping() throws InvalidMidiDataException, MidiUnavailableException, Exception {
    System.out.println("testCyclesLooping");



    int samplingRate = 44100;
    int framesPerCycle = 4410;
    int outputChannelCount = 2;
    boolean noninterleaved = false;
    double cycleDuration = (double) framesPerCycle / (double) samplingRate;
    double midiTicksPerCycle = cycleDuration * midiTicksPerSecond;
    double thisCycleStartTick = 0;
    double nextCycleStartTick = 0;
    double approxDurationInSeconds = 7.0D;
    double framesInTotal = samplingRate * approxDurationInSeconds;
    int cycleCount = (int) Math.ceil(framesInTotal / framesPerCycle);
    long exactDurationInMicrosec = (long) ((1E06D * cycleCount * framesPerCycle) / (double) samplingRate);

    SynthMockup synth = new SynthMockup();
    MidiSubSequencer instance = new MidiSubSequencer("test", synth, null);
    instance.setTracks(tracks);
    instance.openOut(samplingRate, framesPerCycle, outputChannelCount, noninterleaved);
    instance.prepareSession(thisCycleStartTick, null);
    double streamTimeInSeconds = 24 * 60 * 60; //assume that the stream has allready run a whole day
    // now we are simulating 7 seconds of cycling (until after event C)
    for (int i = 0; i < cycleCount; i++) {

      streamTimeInSeconds += cycleDuration;
      thisCycleStartTick = nextCycleStartTick;
      TimeMap timeMap = tempoTrack.CreateTimeMap(thisCycleStartTick, 1.1 * cycleDuration, 1.0);
      nextCycleStartTick = timeMap.getTickForOffset(cycleDuration);

      // this would be done by the master sequencer
      instance.prepareNormalCycle(timeMap, thisCycleStartTick, nextCycleStartTick);
      // this shall be done by the processor
      instance.processOut(streamTimeInSeconds);
    }

    // jump backwards to a position one second after the beginning (before event A)
    streamTimeInSeconds += cycleDuration;
    thisCycleStartTick = nextCycleStartTick;
    double loopStartTick = 180D;

    double loopEndTick = thisCycleStartTick + 9;//9 ticks => 50msec
    long loopEndTimestamp = exactDurationInMicrosec + 50000;
    TimeMap timeMap_1 = tempoTrack.CreateTimeMap(thisCycleStartTick, 1.1 * cycleDuration, 1.0);
    TimeMap timeMap_2 = tempoTrack.CreateTimeMap(loopStartTick, 1.1 * cycleDuration, 1.0);
    nextCycleStartTick = timeMap_2.getTickForOffset(cycleDuration - 0.05);

    assertEquals(midiTicksPerCycle, (loopEndTick - thisCycleStartTick) + (nextCycleStartTick - loopStartTick), 1E-3);


    instance.prepareLoopEndCycle(timeMap_1, timeMap_2, thisCycleStartTick, nextCycleStartTick, loopStartTick, loopEndTick);
    instance.processOut(streamTimeInSeconds);


    // and again we are simulating 7 seconds of cycling (until after event C)
    for (int i = 0; i < cycleCount; i++) {

      streamTimeInSeconds += cycleDuration;
      thisCycleStartTick = nextCycleStartTick;
      TimeMap timeMap = tempoTrack.CreateTimeMap(thisCycleStartTick, 1.1 * cycleDuration, 1.0);
      nextCycleStartTick = timeMap.getTickForOffset(cycleDuration);

      // this would be done by the master sequencer
      instance.prepareNormalCycle(timeMap, thisCycleStartTick, nextCycleStartTick);
      // this shall be done by the processor
      instance.processOut(streamTimeInSeconds);
    }


    // now verify that the events have been send.
    // We expect:
    // 0) sixteen reset-controller messages sent before starting 
    // 1) the three events A to C sent in the first round
    // 2) sixteen reset-controller messages sent before starting the second round
    // 3) the three events A to C sent again in the second round
    assertEquals(16 + 3 + 16 + 3, synth.timedEvents.size());

    Long[] keys = synth.timedEvents.keySet().toArray(new Long[]{});

    //events A to C the first time (the key value equals the microsecond timestamp)
    assertTrue("expected 2000000L, but was " + keys[16 + 0] + ".", 2000000L + 2 > keys[16 + 0]);
    assertTrue("expected 2000000L, but was " + keys[16 + 0] + ".", 2000000L - 2 < keys[16 + 0]);

    assertTrue("expected 4000000L, but was " + keys[16 + 1] + ".", 4000000L + 4 > keys[16 + 1]);
    assertTrue("expected 4000000L, but was " + keys[16 + 1] + ".", 4000000L - 4 < keys[16 + 1]);

    assertTrue("expected 6000000L, but was " + keys[16 + 2] + ".", 6000000L + 6 > keys[16 + 2]);
    assertTrue("expected 6000000L, but was " + keys[16 + 2] + ".", 6000000L - 6 < keys[16 + 2]);

    // the sixteen reset controller
    for (int i = 3 + 16; i < 3 + 16 + 16; i++) {
      SynthMockup.TimedEvent timedEvent = synth.timedEvents.get(keys[i]);

      long timeStamp = timedEvent.microsecondsTimestamp;

      assertTrue("expected timestamp in event " + i + " should be " + loopEndTimestamp + ", but was " + timeStamp + ".", loopEndTimestamp + 2 > timeStamp);
      assertTrue("expected timestamp in event " + i + " should be " + loopEndTimestamp + ", but was " + timeStamp + ".", loopEndTimestamp - 2 < timeStamp);
    }

    //events A to C the second time
    SynthMockup.TimedEvent timedEvent = synth.timedEvents.get(keys[19 + 16]);
    long timeStamp = timedEvent.microsecondsTimestamp;
    long expectedTimestamp = loopEndTimestamp + 1000000L;
    assertTrue("expected timestamp in event 19 should be " + expectedTimestamp + ", but was " + timeStamp + ".", expectedTimestamp + 2 > timeStamp);
    assertTrue("expected timestamp in event 19 should be " + expectedTimestamp + ", but was " + timeStamp + ".", expectedTimestamp - 2 < timeStamp);

  }

  @Test
  public void testSendMidiMessages() throws InvalidMidiDataException, MidiUnavailableException, Exception {
    System.out.println("testSendMidiMessages");


    double testDuration = 10; //in seconds
    int samplingRate = 44100;
    int framesPerCycle = 33;
    int outputChannelCount = 2;
    boolean noninterleaved = false;
    double cycleDuration = (double) framesPerCycle / (double) samplingRate;
    double thisCycleStartTick = 0D;
    double nextCycleStartTick = 0D;
    double framesInTotal = samplingRate * testDuration;
    int cycleCount = (int) Math.ceil(framesInTotal / framesPerCycle);

    SynthMockup synth = new SynthMockup();
    MidiSubSequencer instance = new MidiSubSequencer("test", synth, null);
    instance.openOut(samplingRate, framesPerCycle, outputChannelCount, noninterleaved);
    double msecStreamTime = 24 * 60 * 60; //assume that the stream has allready run a whole day

    // add two messages for this test
    instance.send(newNoteOnMessage(), msecStreamTime + 1.0);
    instance.send(newNoteOnMessage(), msecStreamTime + 9.0);

    // now we are simulating a number cycles
    for (int i = 0; i < cycleCount; i++) {

      thisCycleStartTick = nextCycleStartTick;
      TimeMap timeMap = tempoTrack.CreateTimeMap(thisCycleStartTick, 1.1 * cycleDuration, 1.0);
      nextCycleStartTick = timeMap.getTickForOffset(cycleDuration);

      // this would be done by the master sequencer
      instance.prepareNormalCycle(timeMap, thisCycleStartTick, nextCycleStartTick);
      // this shall be done by the processor
      instance.processOut(msecStreamTime);
      msecStreamTime += cycleDuration;
    }

    // now verify that the two events (prepared above) have been send
    assertEquals(2, synth.timedEvents.size());

    Long[] timeStamps = synth.timedEvents.keySet().toArray(new Long[]{});

    assertTrue("expected 1000000L, but was " + timeStamps[0] + ".", 1000000L + 2 > timeStamps[0]);
    assertTrue("expected 1000000L, but was " + timeStamps[0] + ".", 1000000L - 2 < timeStamps[0]);

    assertTrue("expected 9000000L, but was " + timeStamps[1] + ".", 9000000L + 2 > timeStamps[1]);
    assertTrue("expected 9000000L, but was " + timeStamps[1] + ".", 9000000L - 2 < timeStamps[1]);

  }

  /**
   * Verify that when StopPlaying() has been called,
   * the allSoundsOff message gets distributed to all subsequencers
   * @throws InvalidMidiDataException
   * @throws MidiUnavailableException
   * @throws Exception 
   */
  @Test
  public void testStopPlaying() throws InvalidMidiDataException, MidiUnavailableException, Exception {
    System.out.println("testStopPlaying");


    double testDuration = 2; //in seconds
    int samplingRate = 44100;
    int framesPerCycle = 33;
    int outputChannelCount = 2;
    boolean noninterleaved = false;
    double cycleDuration = (double) framesPerCycle / (double) samplingRate;
    double thisCycleStartTick = 0D;
    double nextCycleStartTick = 0D;
    double framesInTotal = samplingRate * testDuration;
    int cycleCount = (int) Math.ceil(framesInTotal / framesPerCycle);

    SynthMockup synth = new SynthMockup();
    MidiSubSequencer instance = new MidiSubSequencer("test", synth, null);
    instance.openOut(samplingRate, framesPerCycle, outputChannelCount, noninterleaved);
    double msecStreamTime = 0;

    instance.prepareSession(0, null);
    // now we are simulating a number cycles
    for (int i = 0; i < cycleCount; i++) {

      thisCycleStartTick = nextCycleStartTick;
      TimeMap timeMap = tempoTrack.CreateTimeMap(thisCycleStartTick, 1.1 * cycleDuration, 1.0);
      nextCycleStartTick = timeMap.getTickForOffset(cycleDuration);

      // this would be done by the master sequencer
      instance.prepareNormalCycle(timeMap, thisCycleStartTick, nextCycleStartTick);
      // this shall be done by the processor
      instance.processOut(msecStreamTime);
      msecStreamTime += cycleDuration;
    }
    //--- here we tell the sub synth to stopOut playing
    instance.stopSession();

    // now we are simulating again a number cycles
    for (int i = 0; i < cycleCount; i++) {

      thisCycleStartTick = nextCycleStartTick;
      TimeMap timeMap = tempoTrack.CreateTimeMap(thisCycleStartTick, 1.1 * cycleDuration, 1.0);
      nextCycleStartTick = timeMap.getTickForOffset(cycleDuration);

      // this would be done by the master sequencer
      instance.prepareNormalCycle(timeMap, thisCycleStartTick, nextCycleStartTick);
      // this shall be done by the processor
      instance.processOut(msecStreamTime);
      msecStreamTime += cycleDuration;
    }

    // now verify that allSoundsOff have been send
    // we expect the 16 reset-controller at the beginning plus 16 allSoundsOff at the end.
    assertEquals(16 + 16, synth.timedEvents.size());


  }

  /**
   * utility function to  create a "note on" message.
   * @return a new midi message
   */
  private MidiMessage newNoteOnMessage() {
    int channel = 0;
    int pitch = 64;
    int velocity = 64;
    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(ShortMessage.NOTE_OFF, channel, pitch, velocity);
    } catch (InvalidMidiDataException ex) {
      logger.log(Level.SEVERE, null, ex);
    }
    return message;
  }

  /**
   * utility function to  create a "note on" event.
   * @param tick midi tick where the event should be placed
   * @return a new midi event
   */
  private MidiEvent newNoteOnEvent(long tick) {

    return new MidiEvent(newNoteOnMessage(), tick);
  }

  /**
   * utility function to  create a "note off" event.
   * @param tick midi tick where the event should be placed
   * @return a new midi event
   */
  private MidiEvent newNoteOffEvent(long tick) {
    int channel = 0;
    int pitch = 64;
    int velocity = 64;
    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(ShortMessage.NOTE_OFF, channel, pitch, velocity);
    } catch (InvalidMidiDataException ex) {
      logger.log(Level.SEVERE, null, ex);
    }
    return new MidiEvent(message, tick);
  }

  /**
   * utility function to  create an arbitrary  "pitch bend" event.
   * @param tick midi tick where the event should be placed
   * @return a new midi event
   */
  private MidiEvent newPitchBendEvent(long tick) {
    int channel = 0;
    int firstByte = 0x20;
    int secondByte = 0x00;
    ShortMessage message = new ShortMessage();
    try {
      message.setMessage(ShortMessage.PITCH_BEND, channel, firstByte, secondByte);
    } catch (InvalidMidiDataException ex) {
      logger.log(Level.SEVERE, null, ex);
    }
    return new MidiEvent(message, tick);
  }

  /**
   * utility function to  create a "tempo" event.
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

  private class SynthMockup implements AudioSynthesizer {

    public class TimedEvent {

      final MidiMessage message;
      final long microsecondsTimestamp;

      public TimedEvent(MidiMessage message, long microsecondsTimestamp) {
        this.message = message;
        this.microsecondsTimestamp = microsecondsTimestamp;
      }
    }
    public TreeMap<Long, TimedEvent> timedEvents = new TreeMap<Long, TimedEvent>();
    public ArrayList<MidiMessage> immediateEvents = new ArrayList<MidiMessage>();
    private Receiver receiver = new Receiver() {

      @Override
      public void send(MidiMessage message, long timeStamp) {
        if (isEndOfTrackMessage(message)) {
          return;
        }
        if (timeStamp >= 0) {
          TimedEvent timedEvent = new TimedEvent(message, timeStamp);
          long index = timeStamp;
          while (timedEvents.containsKey(index)) {
            index++;
          }
          timedEvents.put(index, timedEvent);
        } else {
          immediateEvents.add(message);
        }
      }

      @Override
      public void close() {
      }
    };
    public boolean openStreamCalled = false;
    private boolean opened = false;

    public void printImmediateEvents() {
      for (MidiMessage entry : immediateEvents) {
        System.out.println(".... Event " + entry + ".");
      }
    }

    @Override
    public AudioFormat getFormat() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AudioSynthesizerPropertyInfo[] getPropertyInfo(Map<String, Object> map) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void open(SourceDataLine sdl, Map<String, Object> map) throws MidiUnavailableException {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AudioInputStream openStream(AudioFormat af, Map<String, Object> map) throws MidiUnavailableException {
      openStreamCalled = true;
      InputStream istream = new InputStream() {

        @Override
        public int read() throws IOException {
          return 0;
        }
      };
      AudioInputStream audioStream = new AudioInputStream(istream, af, 256);
      opened = true;
      return audioStream;
    }

    @Override
    public int getMaxPolyphony() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getLatency() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MidiChannel[] getChannels() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public VoiceStatus[] getVoiceStatus() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isSoundbankSupported(Soundbank soundbank) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean loadInstrument(Instrument instrument) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void unloadInstrument(Instrument instrument) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean remapInstrument(Instrument from, Instrument to) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Soundbank getDefaultSoundbank() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Instrument[] getAvailableInstruments() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Instrument[] getLoadedInstruments() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean loadAllInstruments(Soundbank soundbank) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void unloadAllInstruments(Soundbank soundbank) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean loadInstruments(Soundbank soundbank, Patch[] patchList) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void unloadInstruments(Soundbank soundbank, Patch[] patchList) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Info getDeviceInfo() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void open() throws MidiUnavailableException {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() {
      opened = false;
    }

    @Override
    public boolean isOpen() {
      return opened;
    }

    @Override
    public long getMicrosecondPosition() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getMaxReceivers() {
      return 1;
    }

    @Override
    public int getMaxTransmitters() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Receiver getReceiver() throws MidiUnavailableException {
      return receiver;
    }

    @Override
    public List<Receiver> getReceivers() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Transmitter getTransmitter() throws MidiUnavailableException {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Transmitter> getTransmitters() {
      throw new UnsupportedOperationException("Not supported yet.");
    }
  }

  boolean isEndOfTrackMessage(MidiMessage message) {
    if (message instanceof MetaMessage) {
      int type = ((MetaMessage) message).getType();
      if (type == MidiUtil.endOfTrackMeta) {
        return true;
      }
    }
    return false;
  }
}
