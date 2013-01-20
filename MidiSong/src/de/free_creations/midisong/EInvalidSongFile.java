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

package de.free_creations.midisong;

/**
 * This exception indicates that an error has occurred while 
 * reading a Song file.
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class EInvalidSongFile extends Exception{
  EInvalidSongFile() {
    super();
  }
  EInvalidSongFile(Throwable cause) {
    super(cause);
  }
  EInvalidSongFile(String message) {
    super(message);
  }
  EInvalidSongFile(String message, Throwable cause) {
    super(message, cause);
  }
}
