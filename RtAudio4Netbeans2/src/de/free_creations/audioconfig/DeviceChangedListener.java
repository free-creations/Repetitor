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
package de.free_creations.audioconfig;

import rtaudio4java.DeviceInfo;

/**
 *
 * @author Harald Postner
 */
public interface DeviceChangedListener {
  /**
   * Indicates the the user has selected a new device.
   * @param info the device info record of the new device.
   * @param input true if the device is used as input device.
   */
  public void newDevice(DeviceInfo info,boolean input);  
}
