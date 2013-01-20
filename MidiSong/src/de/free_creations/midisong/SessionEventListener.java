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
package de.free_creations.midisong;

/**
 * The SessionEventListener interface should be implemented by classes whose 
 * instances need to be notified when some event happens in the session
 * (other than simple parameter changes). 
 * To register a SessionEventListener object to receive such notifications, pass 
 * it as the argument to the addEventListener method of SongSession. Please note
 * that the thread who is notifying is usually not the AWT thread.
 * @author Harald Postner
 */
public interface SessionEventListener {
   /**
   * Invoked when the Sequencer has encountered and processed a The loopEndPoint in the 
   * Sequence.
   * @param newLoopCount a positive number indicates the number of loops 
   * that remain to be done until the looping stops. A negative number indicates
   * the negative count of loops done.
   */
  void loopDone(int newLoopCount);  
  
}
