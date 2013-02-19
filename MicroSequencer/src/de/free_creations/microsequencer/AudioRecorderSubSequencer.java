/*
 * Copyright 2013 Harald Postner.
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
package de.free_creations.microsequencer;

import de.free_creations.microsequencer.MasterSequencer.PlayingMode;
import javax.sound.midi.MidiUnavailableException;



/**
 *
 * @author Harald Postner
 */
class AudioRecorderSubSequencer implements 
        MasterSequencer.SubSequencer, 
        AudioProducer, AudioConsumer{

  @Override
  public void preparePlaying(double startTick, PlayingMode mode) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void stopPlaying() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void openOut(int samplingRate, int nFrames, int outputChannelCount, boolean noninterleaved) throws MidiUnavailableException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void closeOut() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void startOut() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void stopOut() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public float[] processOut(double streamTime) throws Exception {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void openIn(int samplingRate, int nFrames, int inputChannelCount, boolean noninterleaved) throws MidiUnavailableException {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void closeIn() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void startIn() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void stopIn() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public void processIn(double streamTime, float[] samples) throws Exception {
    throw new UnsupportedOperationException("Not supported yet.");
  }


  
}
