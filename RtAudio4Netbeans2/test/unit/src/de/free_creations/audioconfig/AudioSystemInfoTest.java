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
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import rtaudio4java.DeviceInfo;

/**
 *
 * @author Harald Postner
 */
public class AudioSystemInfoTest {

  public AudioSystemInfoTest() {
  }

  @BeforeClass
  public static void loadLib() throws Exception {
    String osName = System.getProperty("os.name").toLowerCase();
    // this is the path to the RtAudio library on my Linux machine
    File libDir = new File("/home/harald/NetBeansProjects/"
            + "Repetitor/RtAudio4Netbeans2/"
            + "release/modules/lib");
    if (osName.indexOf("windows") > -1) {
      // this is the path to the RtAudio library on my Windows machine
      libDir = new File("C:\\Dokumente und Einstellungen\\harald\\Eigene Dateien\\"
              + "NetBeansTutorials\\RtAudio4NetbeansTutorial\\"
              + "Rtaudio4Netbeans\\release\\modules\\lib");
    }
    if (!libDir.exists()) {
      throw new RuntimeException("Please set the path to the RtAudio Library.");
    }
    AudioSystemInfo.loadNativeLibray(libDir);
  }

  /**
   * Test of iterator method, of class AudioSystemInfo.
   */
  @Test
  public void testIterator() throws Throwable {


    AudioSystemInfo instance = new AudioSystemInfo();
    System.out.println("#### prepareAudioSystemsInfo start ##################");

    assertNotNull(instance);

    for (ArchitectureInfo arch : instance) {
      System.out.println("\n ... ----------------"
              + arch.getApiNumber() + ") "
              + arch.getApiDescription()
              + "----------------");
      DeviceInfo[] devInfos = arch.getDeviceInfos();
      for (int i = 0; i < devInfos.length; i++) {
        DeviceInfo info = devInfos[i];
        if (info != null) {
          System.out.println(" ... " + i + ") " + info.getName());
          if (info.isProbed() == false) {
            System.out.println(" ...    Probe Status = UNsuccessful");
          } else {
            System.out.println(" ...    Probe Status = Successful");
            System.out.println(" ...    Output Channels = " + info.getOutputChannels());
            System.out.println(" ...    Input Channels = " + info.getInputChannels());
            System.out.println(" ...    Duplex Channels = " + info.getDuplexChannels());
            System.out.print(" ...    Supported sample rates = ");
            for (int sampleRate : info.getSampleRates()) {
              System.out.print(sampleRate + " ");
            }
            System.out.println("");
          }
        } else {
          System.out.println(" ... " + i + ") invalid device!!!!");
        }
      }
    }
    System.out.println("#### prepareAudioSystemsInfo end ##################");

  }

  /**
   * Test of isEmpty method, of class AudioSystemInfo.
   */
  @Test
  @Ignore("Trivial")
  public void testIsEmpty() {
  }

  /**
   * Test of size method, of class AudioSystemInfo.
   */
  @Test
  @Ignore("Trivial")
  public void testSize() {
  }

  /**
   * Test of get method, of class AudioSystemInfo.
   */
  @Test
  @Ignore("Trivial")
  public void testGet() {
  }

  /**
   * Test of loadNativeLibray method, of class AudioSystemInfo.
   */
  @Test
  @Ignore("Trivial")
  public void testLoadNativeLibray() {
  }
}
