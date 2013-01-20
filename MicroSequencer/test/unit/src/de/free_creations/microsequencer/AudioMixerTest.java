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

import de.free_creations.midiutil.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Soundbank;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class AudioMixerTest {

  /**
   * Test of process method, of class AudioMixer.
   * We simulate a AudioMixer with three attached
   * synthesisers. We let it process thousand cycles
   * and then we verify that the functions of the synthesisers
   * have been called in the right order and in the right number.
   */
  @Test
  public void testProcess() throws Exception, Throwable {
    System.out.println("testProcess");

    MasterSequencerMockup sequencer = new MasterSequencerMockup();

    AudioMixer instance =
            new AudioMixer(sequencer);

    float[] resultBuffer = null;
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    AudioProducerMockup audioProducer1 = new AudioProducerMockup();
    AudioPort port1 = instance.createPort(audioProducer1, executorService);
    port1.setAttenuation(0, 0.0F);
    port1.setAttenuation(1, 0.0F);

    int framesPerCycle = 128;
    int outputChannelCount = 2;
    instance.onOpenStream(44100, framesPerCycle, 0, outputChannelCount, false);
    instance.onStartStream();

    // now we are simulating the work of the sound-system
    int cycleCount = 123;
    for (int i = 0; i < cycleCount; i++) {
      resultBuffer = instance.process(null, 0, 0);
    }

    //Part 1, verify that sequencer and audio producers have been called for each cycle
    assertEquals(cycleCount, sequencer.prepareCycle_Called);
    assertEquals(cycleCount, audioProducer1.process_Called);
    //verify that the output buffer has been filled correctly (see AudioProducerMockup.process)
    assertEquals(framesPerCycle * outputChannelCount, resultBuffer.length);
    assertEquals(1.0F, resultBuffer[resultBuffer.length - 1], 1E-1);

    //Part 2, add an audio producer
    AudioProducerMockup audioProducer2 = new AudioProducerMockup();
    AudioPort port2 = instance.createPort(audioProducer2, executorService);
    port2.setAttenuation(0, 0.0F);
    port2.setAttenuation(1, 0.0F);
    for (int i = 0; i < cycleCount; i++) {
      resultBuffer = instance.process(null, 0, 0);
    }

    assertEquals(2 * cycleCount, sequencer.prepareCycle_Called);
    assertEquals(2 * cycleCount, audioProducer1.process_Called);
    assertEquals(cycleCount, audioProducer2.process_Called);
    assertEquals(cycleCount, audioProducer2.process_Called);
    //verify that the output buffer has been filled correctly (see AudioProducerMockup.process)
    assertEquals(framesPerCycle * outputChannelCount, resultBuffer.length);
    assertEquals(2.0F, resultBuffer[resultBuffer.length - 1], 1E-1);

    //Part 3, remove the audio producers
    instance.removeAllPorts();
    assertTrue(audioProducer1.state == State.CLOSED);
    assertTrue(audioProducer2.state == State.CLOSED);
    for (int i = 0; i < cycleCount; i++) {
      resultBuffer = instance.process(null, 0, 0);
    }
    assertEquals(3 * cycleCount, sequencer.prepareCycle_Called);
    //the cycle count for the audioProducers should not have changed since Part 2.
    assertEquals(2 * cycleCount, audioProducer1.process_Called);
    assertEquals(cycleCount, audioProducer2.process_Called);
    //verify that the output buffer has been filled correctly (see AudioProducerMockup.process)
    assertEquals(framesPerCycle * outputChannelCount, resultBuffer.length);
    assertEquals(0.0F, resultBuffer[resultBuffer.length - 1], 1E-1);


    AudioProducerMockup audioProducer4 = new AudioProducerMockup();
    AudioPort port4 = instance.createPort(audioProducer4, executorService);
    port4.setAttenuation(0, 0.0F);
    port4.setAttenuation(1, 0.0F);
    for (int i = 0; i < cycleCount; i++) {
      resultBuffer = instance.process(null, 0, 0);
    }
    assertEquals(4 * cycleCount, sequencer.prepareCycle_Called);
    assertEquals(1 * cycleCount, audioProducer4.process_Called);
    assertEquals(framesPerCycle * outputChannelCount, resultBuffer.length);
    assertEquals(1.0F, resultBuffer[resultBuffer.length - 1], 1E-1);

    instance.onStopStream();
    instance.onCloseStream();
    assertTrue(audioProducer4.state == State.CLOSED);
    executorService.shutdownNow();

  }

  public enum State {

    OPENED, CLOSED, STARTED, STOPPED
  }

  private class AudioProducerMockup implements AudioProducer {

    public int open_Called = 0;
    public int close_Called = 0;
    public int start_Called = 0;
    public int stop_Called = 0;
    public int process_Called = 0;
    private float[] resultBuffer;
    public State state;

    @Override
    public void open(int samplingRate, int nFrames, int outputChannelCount, boolean noninterleaved) {
      open_Called++;
      state = State.OPENED;
      resultBuffer = new float[outputChannelCount * nFrames];
    }

    @Override
    public void close() {
      assertTrue(state == State.STOPPED);
      state = State.CLOSED;
      close_Called++;
    }

    @Override
    public void start() {
      assertTrue(state == State.OPENED);
      state = State.STARTED;
      start_Called++;
    }

    @Override
    public void stop() {
      assertTrue(state == State.STARTED);
      state = State.STOPPED;
      stop_Called++;
    }

    @Override
    public float[] process(double streamTime) throws Exception {
      assertTrue(state == State.STARTED);
      process_Called++;
      // as a marker,  we set the last item of the result buffer to one
      resultBuffer[resultBuffer.length - 1] = 1.0F;
      return resultBuffer;
    }
  }

  private class MasterSequencerMockup implements MasterSequencer {

    public int prepareCycle_Called = 0;

    @Override
    public double getTempoFactor() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setTempoFactor(double tempoFactor) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getCurrentTickPosition(double streamTime) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BeatPosition getCurrentBeatPosition(double streamTime) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void prepareCycle(double streamTime, double cycleLength) {
      prepareCycle_Called++;
    }

    @Override
    public void startMidi() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void stopMidi() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setTickPosition(double tick) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isRunning() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeAllSubsequncers() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long getTickLength() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setLoopCount(int count) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getLoopCount() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setLoopStartPoint(double tick) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setLoopEndPoint(double tick) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getLoopEndPoint() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double getLoopStartPoint() {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setMasterTrack(TempoTrack tempoTrack, TimeSignatureTrack timeSignatureTrack, long tickLength) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public BeatPosition tickToBeatPosition(double tickPosition) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

 
    @Override
    public SubSequencer createSubSequencer(String name, Soundbank soundbank) throws MidiUnavailableException {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double beatPositionToTick(RPosition position) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public RPositionEx tickToRPositionEx(double tickPosition) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void add(SequencerEventListener listener) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void remove(SequencerEventListener listener) {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public double tickToEffectiveBPM(double tickPosition) {
      throw new UnsupportedOperationException("Not supported yet.");
    }
  }
}
