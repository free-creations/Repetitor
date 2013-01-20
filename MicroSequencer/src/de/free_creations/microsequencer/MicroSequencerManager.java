/*
 * Copyright 2011 admin.
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
 * This class manages the live-time of the microsequncer.
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class MicroSequencerManager {

  static private MicroSequencer theMicroSequencer = null;

  public static MicroSequencer getInstance() {
    if (theMicroSequencer == null) {
      theMicroSequencer = new MicroSequencerImpl();
    }
    return theMicroSequencer;
  }

  public static void closeInstance() {
    if (theMicroSequencer != null) {
      if (theMicroSequencer.isOpen()) {
        theMicroSequencer.close();
      }
    }
  }
}
