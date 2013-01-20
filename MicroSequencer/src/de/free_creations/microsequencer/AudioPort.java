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

/**
 * The audio-ports are the input-plugs into the {@link AudioMixer}.
 * An audio port permits to control and to query the sound volume on each channel.
 * @author Harald Postner <Harald at H-Postner.de>
 */
public interface AudioPort {


  /**
   * Get the value of peak Volume in Decibel for specified audio-channel.
   *
   * @param channel the audio channel (for a stereo port there are 
   * two channel, left and right numbered 0 and 1).
   * @return the value of peakVu at specified channel
   */
  public float getPeakVuAndClear(int channel);

  /**
   * The targetAttenuationVolt indicates how the signal of this port is damped.
   * The value is given in Decibel. A value of 0 indicates full value,
   * a value of 100 is full damping.
   * @param channel the audio channel (for a stereo port there are 
   * two channel, left and right numbered 0 and 1).
   * @return the value of the targetAttenuationVolt in Decibel.
   */
  public float getAttenuation(int channel) ;

    /**
   * The targetAttenuationVolt indicates how the signal of this port is damped.
   * The value is given in Decibel. A value of 0 indicates full value,
   * a value of 100 is full damping.
   * @return an array of 16 entries holding the attenuation of all channels.
   * Note, the array has more entries than there are channels used. Not used
   * entries are set to zero.
   */
  public float[] getAttenuations() ;

  /**
   * Set the value of the attenuation
   * The attenuation indicates how the signal of this port is damped.
   * The value is given in Decibel. A value of 0 indicates full without value,
   * a value of 120 is full damping. (Negative values indicate amplification).
   * @param channel the audio channel (for a stereo port there are 
   * two channel, left and right numbered 0 and 1).
   * @param attenuation new value of attenuation in Decibel.
   */
  public void setAttenuation(int channel, float attenuation);
}
