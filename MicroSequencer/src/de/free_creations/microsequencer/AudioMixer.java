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

import java.util.Arrays;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import javax.sound.midi.MidiUnavailableException;
import rtaudio4java.AudioProcessor_Float32;

/**
 * The audio-mixer takes several steams of audio data
 * (form several {@link AudioPort audioPorts}) and adds them together
 * into one stream which is than output through the sound-card.
 * 
 */
class AudioMixer extends AudioProcessor_Float32 {

  private double cycleLength;
  private volatile boolean streamOpen = false;
  private volatile int samplingRate;
  private volatile int framesPerCycle;
  private volatile int outputChannelCount;
  private volatile boolean noninterleaved;
  private volatile boolean streamStarted = false;
  private volatile double maxLoad;


  class ProcessThreadFactory implements ThreadFactory {

    private int threadCount = 0;

    @Override
    public Thread newThread(Runnable r) {
      threadCount++;
      Thread thread = new Thread(r);
      thread.setPriority(Thread.MAX_PRIORITY - 1);
      thread.setName("AudioWorker_" + threadCount);
      return thread;
    }
  }
  private float[] resultBuffer;
  private final CopyOnWriteArrayList<AudioPortImpl> audioPorts = new CopyOnWriteArrayList<AudioPortImpl>();
  private final MasterSequencer masterSequencer;


  AudioMixer(MasterSequencer masterSequencer) {
    if (masterSequencer == null) {
      throw new IllegalArgumentException("argument \"sequencer\" is null.");
    }
    this.masterSequencer = masterSequencer;
    streamOpen = false;
  }

  @Override
  public void onOpenStream(int samplingRate,
          int framesPerCycle,
          int notUsed,
          int outputChannelCount,
          boolean noninterleaved) throws Throwable {

    this.samplingRate = samplingRate;
    this.framesPerCycle = framesPerCycle;
    this.outputChannelCount = outputChannelCount;
    this.noninterleaved = noninterleaved;

    cycleLength = (double) framesPerCycle / (double) samplingRate;
    resultBuffer = new float[framesPerCycle * outputChannelCount];
    Arrays.fill(resultBuffer, 0F);

    ListIterator<AudioPortImpl> portIter = audioPorts.listIterator();
    while (portIter.hasNext()) {
      portIter.next().open(samplingRate, framesPerCycle, outputChannelCount, noninterleaved);
    }
    streamOpen = true;
  }

  @Override
  public void onStartStream() {

    ListIterator<AudioPortImpl> portIter = audioPorts.listIterator();
    while (portIter.hasNext()) {
      portIter.next().start();
    }
    streamStarted = true;
  }

  @Override
  public void onStopStream() {
    streamStarted = false;
    ListIterator<AudioPortImpl> portIter = audioPorts.listIterator();
    while (portIter.hasNext()) {
      portIter.next().stop();
    }

  }

  @Override
  public void onCloseStream() {
    streamOpen = false;

    ListIterator<AudioPortImpl> portIter = audioPorts.listIterator();
    while (portIter.hasNext()) {
      portIter.next().close();
    }


  }

  @Override
  public float[] process(float[] notUsed, double streamTime, int status) throws InterruptedException, ExecutionException {
    long startNano = System.nanoTime();
    masterSequencer.prepareCycle(streamTime, cycleLength);


    for (AudioPortImpl audioPort : audioPorts) {
      audioPort.processLater(streamTime);
    }

    Arrays.fill(resultBuffer, 0F);

    for (AudioPortImpl audioPort : audioPorts) {

      float[] producerBuffer = audioPort.getProcessResult();
      for (int i = 0; i < resultBuffer.length; i++) {
        resultBuffer[i] += producerBuffer[i];
      }
    }
    long elapseNano = System.nanoTime() - startNano;
    double load = (1E-9 * elapseNano) / cycleLength;
    maxLoad = Math.max(load, maxLoad);
    return resultBuffer;
  }

  public AudioPort createPort(AudioProducer producer, ExecutorService executorService) throws MidiUnavailableException {
    AudioPortImpl port = new AudioPortImpl(producer, executorService);
    if (streamOpen) {
      port.open(samplingRate, framesPerCycle, outputChannelCount, noninterleaved);
    }
    if (streamStarted) {
      port.start();
    }
    audioPorts.add(port);
    return port;
  }

  public void removeAllPorts() {
    ListIterator<AudioPortImpl> portIter = audioPorts.listIterator();
    while (portIter.hasNext()) {
      AudioPortImpl port = portIter.next();
      port.stop();
      port.close();
    }
    audioPorts.clear();
  }

  public double getMaxLoadAndClear() {
    double result = maxLoad;
    maxLoad = 0D;
    return result;
  }


}
