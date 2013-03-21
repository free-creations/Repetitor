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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.MidiUnavailableException;
import rtaudio4java.AudioProcessor_Float32;

/**
 * The audio-mixer takes several steams of audio data (form several
 * {@link AudioPort audioPorts}) and adds them together into one stream which is
 * than output through the sound-card.
 *
 */
class AudioMixer extends AudioProcessor_Float32 {

  private static final Logger logger = Logger.getLogger(AudioMixer.class.getName());
  private double cycleDuration;
  private volatile boolean streamOpen = false;
  private volatile int samplingRate;
  private volatile int framesPerCycle;
  private volatile int outputChannelCount;
  private volatile int inputChannelCount;
  private volatile boolean noninterleaved;
  private volatile boolean streamStarted = false;
  private volatile double maxLoad;
  private int processCount = 0; // (debugging variable) the number of times process was called
  private int badStatusCount = 0; // (debugging variable) the number of times RtAudio reported a timeout

  // for debugging
  private void reportStatus() {
    if (processCount % 50 == 0) {
      double lastMaxLoad = getMaxLoadAndClear();
      if (lastMaxLoad > 0.8) {
        logger.log(Level.FINER, "### overload");
        logger.log(Level.FINER, " process count: {0}", processCount);
        logger.log(Level.FINER, " maximum load: {0}", lastMaxLoad);
      }
    }
    if (processCount % 500 == 0) {
      logger.log(Level.FINER, " Still alive - process count: {0}", processCount);
    }

  }

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
  private final CopyOnWriteArrayList<AudioPortImpl> audioPorts = new CopyOnWriteArrayList<>();
  private final Object audioPortsLock = new Object();
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
          int inputChannelCount,
          int outputChannelCount,
          boolean noninterleaved) throws Throwable {

    this.samplingRate = samplingRate;
    this.framesPerCycle = framesPerCycle;
    this.outputChannelCount = outputChannelCount;
    this.inputChannelCount = inputChannelCount;
    this.noninterleaved = noninterleaved;

    cycleDuration = (double) framesPerCycle / (double) samplingRate;
    resultBuffer = new float[framesPerCycle * outputChannelCount];
    Arrays.fill(resultBuffer, 0F);

    synchronized (audioPortsLock) {
      ListIterator<AudioPortImpl> portIter = audioPorts.listIterator();
      while (portIter.hasNext()) {
        portIter.next().open(samplingRate, framesPerCycle, inputChannelCount, outputChannelCount, noninterleaved);
      }
    }
    logger.log(Level.FINER, "### onOpenStream executed.");
    logger.log(Level.FINER, "... inputChannelCount: {0}", inputChannelCount);
    logger.log(Level.FINER, "... outputChannelCount: {0}", outputChannelCount);

    streamOpen = true;
  }

  @Override
  public void onStartStream() {

    synchronized (audioPortsLock) {
      ListIterator<AudioPortImpl> portIter = audioPorts.listIterator();
      while (portIter.hasNext()) {
        portIter.next().start();
      }
    }
    processCount = 0;
    badStatusCount = 0;
    logger.log(Level.FINER, "onStartStream executed.");
    streamStarted = true;
  }

  @Override
  public void onStopStream() {
    streamStarted = false;
    synchronized (audioPortsLock) {
      ListIterator<AudioPortImpl> portIter = audioPorts.listIterator();
      while (portIter.hasNext()) {
        portIter.next().stop();
      }
    }
    logger.log(Level.FINER, "### > onStopStream executed.");
    logger.log(Level.FINER, "### > process count: {0}", processCount);
    logger.log(Level.FINER, "### > bad status count: {0}", badStatusCount);
  }

  @Override
  public void onCloseStream() {
    streamOpen = false;

    synchronized (audioPortsLock) {
      ListIterator<AudioPortImpl> portIter = audioPorts.listIterator();
      while (portIter.hasNext()) {
        portIter.next().close();
      }
    }
    logger.log(Level.FINER, "onCloseStream executed.");
  }

  @Override
  public float[] process(float[] input, double streamTime, int status) throws InterruptedException, ExecutionException {
    long startNano = System.nanoTime();
    if (status != 0) {
      badStatusCount++;
    }
    reportStatus();

    masterSequencer.prepareCycle(streamTime, cycleDuration);

    synchronized (audioPortsLock) {
      for (AudioPortImpl audioPort : audioPorts) {
        audioPort.processLater(streamTime, input);
      }

      Arrays.fill(resultBuffer, 0F);

      for (AudioPortImpl audioPort : audioPorts) {

        float[] producerBuffer = audioPort.getProcessResult();
        if (producerBuffer != null) {
          for (int i = 0; i < resultBuffer.length; i++) {
            resultBuffer[i] += producerBuffer[i];
          }
        }
      }
    }
    long elapseNano = System.nanoTime() - startNano;
    double load = (1E-9 * elapseNano) / cycleDuration;
    maxLoad = Math.max(load, maxLoad);
    processCount++;
    return resultBuffer;
  }

  public AudioPort createPort(AudioProcessor producer, ExecutorService executorService) throws MidiUnavailableException {
    AudioPortImpl port = new AudioPortImpl(producer, executorService);
    if (streamOpen) {
      port.open(samplingRate, framesPerCycle, inputChannelCount, outputChannelCount, noninterleaved);
    }
    if (streamStarted) {
      port.start();
    }
    synchronized (audioPortsLock) {
      audioPorts.add(port);
    }
    return port;
  }

  public void removeAllPorts() {
    synchronized (audioPortsLock) {
      ListIterator<AudioPortImpl> portIter = audioPorts.listIterator();
      while (portIter.hasNext()) {
        AudioPortImpl port = portIter.next();
        port.stop();
        port.close();
      }
      audioPorts.clear();
    }
  }

  public double getMaxLoadAndClear() {
    double result = maxLoad;
    maxLoad = 0D;
    return result;
  }
}
