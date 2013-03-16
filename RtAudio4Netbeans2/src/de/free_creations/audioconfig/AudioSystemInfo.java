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

import de.free_creations.audioconfig.AudioSystemInfo.ArchitectureInfo;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import rtaudio4java.AudioSystem;
import rtaudio4java.AudioSystemFactory;
import rtaudio4java.DeviceInfo;
import rtaudio4java.RtError;

/**
 * This class describes the audio hardware and software locally available.
 *
 * @author Harald Postner
 */
public class AudioSystemInfo implements Iterable<ArchitectureInfo> {

  /**
   * Describes one specific audio architecture.
   */
  public class ArchitectureInfo {

    private final String apiDescription;
    private final int apiNumber;
    private final DeviceInfo[] deviceInfos;

    private ArchitectureInfo(String apiDescription, int apiNumber, DeviceInfo[] info) {
      this.apiDescription = apiDescription;
      this.apiNumber = apiNumber;
      this.deviceInfos = info;
    }

    public String getApiDescription() {
      return apiDescription;
    }

    public int getApiNumber() {
      return apiNumber;
    }

    public DeviceInfo[] getDeviceInfos() {
      return deviceInfos;
    }
  }
  private final List<ArchitectureInfo> architectureInfos;

  /**
   * Opens the Audio system and collects information about all available
   * software and hardware resources. Note: this operation is blocking and might
   * take a long time, do not call this procedure from within the AWT thread.
   *
   * @throws Throwable if the audio hardware or software could not be accessed.
   */
  public AudioSystemInfo() throws Throwable {
    architectureInfos = prepareArchtectureInfos();
  }

  private List<ArchitectureInfo> prepareArchtectureInfos() throws Throwable {
    Set<Integer> apis = AudioSystemFactory.getCompiledApi();
    ArrayList<ArchitectureInfo> audioSystemsInfo = new ArrayList<>();
    for (int api : apis) {
      audioSystemsInfo.add(describeApi(api));
    }
    return audioSystemsInfo;
  }

  private ArchitectureInfo describeApi(int api) {
    return new ArchitectureInfo(
            AudioSystemFactory.apiTypeToString(api),
            api,
            describeDevices(api));
  }

  private DeviceInfo[] describeDevices(int api) {
    AudioSystem instance = AudioSystemFactory.getRtAudioInstance(api);
    int devCount = instance.getDeviceCount();
    DeviceInfo[] deviceInfos = new DeviceInfo[devCount];
    for (int i = 0; i < devCount; i++) {
      try {
        deviceInfos[i] = instance.getDeviceInfo(i).get();
      } catch (InterruptedException | ExecutionException | RtError ex) {
        deviceInfos[i] = null;
      }
    }
    return deviceInfos;
  }

  @Override
  public Iterator<ArchitectureInfo> iterator() {
    return architectureInfos.iterator();
  }

  public boolean isEmpty() {
    return architectureInfos.isEmpty();
  }

  public int size() {
    return architectureInfos.size();
  }

  ArchitectureInfo get(int i) {
    return architectureInfos.get(i);
  }
  /**
   * 
   * @param libDir 
   */
   public static void loadNativeLibray(File libDir) {
     AudioSystemFactory.loadNativeLibray(libDir);
  }
}
