/*
 * Copyright 2012 Harald Postner.
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

/**
 * The SequencerEventListener interface should be implemented by classes whose
 * instances need to be notified when a Sequencer has processed the
 * loopEndPoint. To register a SequencerEventListener object to receive such
 * notifications, pass it as the argument to the
 * {@link MicroSequencer#addSequencerEventListener } method.
 *
 * @author Harald Postner
 */
public interface SequencerEventListener {

  /**
   * Invoked when the Sequencer has encountered and processed a the loopEndPoint
   * in the Sequence it is processing.
   *
   * @param newLoopCount the number of loops that will still be done until the
   * looping stops or -1 if looping is indefinite.
   */
  void loopDone(int newLoopCount);

  /**
   * Invoked when the Sequencer is starting or stopping.
   *
   * @param isPlaying true when the sequencer is starting.
   */
  void notifyPlaying(boolean isPlaying);
}
