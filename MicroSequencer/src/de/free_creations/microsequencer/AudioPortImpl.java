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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import javax.sound.midi.MidiUnavailableException;

/**
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
class AudioPortImpl implements AudioPort {

  /**
   * Sudden changes in volume (in attenuation) should not be
   * effectuated from one frame to the next, because this would
   * produce ugly cracking sounds. Instead the current attenuation will
   * exponentially reach its target value. The time this takes is defined by
   * the RELAXATIONTIME constant (in seconds).
   */
  private final double RELAXATIONTIME = 0.1; // in seconds
  private final float MINSIGNAL = 1E-10F; //-200 decibel
  private int framesPerCycle;
  private int outputChannelCount;
  private final int MAXCHANNELS = 16;
  private float[] currentAttenuationVolt = new float[MAXCHANNELS];
  private float[] targetAttenuationVolt = new float[MAXCHANNELS];
  private float[] targetAttenuationDecibel = new float[MAXCHANNELS];
  private float attn_f0 = 0F;
  private float attn_f1 = 1F - attn_f0;
  private float[] peakVu = new float[MAXCHANNELS];
  private AtomicReference<Future<float[]>> processResult = new AtomicReference<Future<float[]>>(null);
  private final AudioProducer audioProducer;
  private final ExecutorService executor;

  AudioPortImpl(AudioProducer audioProducer, ExecutorService executor) {
    this.audioProducer = audioProducer;
    Arrays.fill(targetAttenuationVolt, 0.0F);
    Arrays.fill(targetAttenuationDecibel, 120F);
    this.executor = executor;
  }

  /**
   * Get the value of peak Volume in Decibel for specified channel.
   *
   * @param channel a number between 0 and MAXCHANNELS indicating the
   * channel number for which the peak Volume is requested.
   * @return the value of peakVu at specified channel
   */
  @Override
  public float getPeakVuAndClear(int channel) {
    if (this.peakVu[channel] < MINSIGNAL) {
      return -200;
    }
    float result = 20F * (float) Math.log10(this.peakVu[channel]);
    this.peakVu[channel] = 0F;
    return result;
  }

  /**
   * The targetAttenuationVolt indicates how the signal of this port is damped.
   * The value is given in Decibel. A value of 0 indicates full value,
   * a value of 100 is full damping.
   * @return the value of the targetAttenuationVolt in Decibel.
   */
  @Override
  public float getAttenuation(int channel) {
    return targetAttenuationDecibel[channel];
  }

  @Override
  public float[] getAttenuations() {
    return targetAttenuationDecibel;
  }

  /**
   * Set the value of the attenuation
   * The attenuation indicates how the signal of this port is damped.
   * The value is given in Decibel. A value of 0 indicates full without value,
   * a value of 120 is full damping. (Negative values indicate amplification).
   * @param attenuation new value of attenuation in Decibel.
   */
  @Override
  public void setAttenuation(int channel, float attenuation) {
    final double F = -0.115129255D; //=-Math.log(10)/20D;
    this.targetAttenuationDecibel[channel] = attenuation;
    if (attenuation > 100F) {
      this.targetAttenuationVolt[channel] = 0F;
    } else {
      this.targetAttenuationVolt[channel] = (float) Math.exp(attenuation * F);
    }
  }

  public void open(int samplingRate, int framesPerCycle, int outputChannelCount, boolean noninterleaved) throws MidiUnavailableException {
    if (noninterleaved) {
      throw new IllegalArgumentException("This version is not able to handle noninterleaved channels.");
    }
    if (outputChannelCount > MAXCHANNELS) {
      throw new IllegalArgumentException("Unexpected  number of channels.");
    }
    this.framesPerCycle = framesPerCycle;
    this.outputChannelCount = outputChannelCount;
    Arrays.fill(currentAttenuationVolt, 0F);

    attn_f0 = (float) Math.exp(Math.log(0.5) / (samplingRate * RELAXATIONTIME));
    attn_f1 = 1F - attn_f0;
    audioProducer.openOut(samplingRate, framesPerCycle, outputChannelCount, noninterleaved);
  }

  public void start() {
    Arrays.fill(currentAttenuationVolt, 0F);
    processResult.lazySet(null);
    audioProducer.startOut();
  }

  public void close() {
    audioProducer.closeOut();
  }

  public float[] process(double streamTime) throws Exception {
    float[] outputArray = audioProducer.processOut(streamTime);
    int i = 0;
    for (int frame = 0; frame < framesPerCycle; frame++) {
      for (int channel = 0; channel < outputChannelCount; channel++) {
        //calculate new values for the  currently-used targetAttenuationVolt
        //so that it eventually reaches the target value.
        currentAttenuationVolt[channel] = attn_f0 * currentAttenuationVolt[channel] + attn_f1 * targetAttenuationVolt[channel];
        outputArray[i] = currentAttenuationVolt[channel] * outputArray[i];
        peakVu[channel] = Math.max(peakVu[channel], Math.abs(outputArray[i]));
        i++;
      }
    }
    return outputArray;
  }

  void stop() {
    audioProducer.stopOut();
  }

  /**
   * Package private - for test only. 
   * @param channel
   * @return the absolute value of the attenuation.
   */
  float getTargetAttenuationVolt(int channel) {
    return targetAttenuationVolt[channel];
  }

  /**
   * Every port get an ExecutorService attached on creation.
   * This executor provides the thread in which all activities of this port are
   * executing.
   * @return 
   */
  public ExecutorService getExecutor() {
    return executor;
  }

  public void processLater(final double streamTime) {
    class AudioProcess implements Callable<float[]> {

      @Override
      public float[] call() throws Exception {
        return AudioPortImpl.this.process(streamTime);
      }
    }
    Future<float[]> futureResult = executor.submit(new AudioProcess());
    processResult.set(futureResult);

  }

  public float[] getProcessResult() throws InterruptedException, ExecutionException {
    return processResult.get().get();
  }
}
