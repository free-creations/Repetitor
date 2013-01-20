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

import javax.sound.midi.MidiUnavailableException;


/**
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
interface AudioProducer {

  public void open(int samplingRate, int nFrames, int outputChannelCount, boolean noninterleaved) throws MidiUnavailableException;

  public void close();

  public void start();

  public void stop();

  public abstract float[] process(double streamTime) throws Exception;

}
