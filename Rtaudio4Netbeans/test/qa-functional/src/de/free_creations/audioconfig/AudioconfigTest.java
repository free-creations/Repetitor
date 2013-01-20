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
package de.free_creations.audioconfig;

import java.io.*;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import rtaudio4java.AudioSystemFactory;

/**
 * This class tests whether the Audiconfig class works correctly.
 * Especially we test whether the function "isProbed" works.
 * We'll assume that the configuration stored in the user settings is a valid
 * configuration.
 * For more information on how to test
 * NetBeans platform modules see also
 * {@link http://platform.netbeans.org/tutorials/nbm-test.html
 * http://platform.netbeans.org/tutorials/nbm-test.html}
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class AudioconfigTest extends NbTestCase {

  private final String originalSetting = "OriginalSettings.xml";

  public AudioconfigTest(String s) {
    super(s);
  }

  /**
   * This method will create a
   * test that sets up a proper NetBeans Runtime Container environment,
   * especially it will load the RtAudio4Java module and invoke the installer.
   * @return
   */
  public static Test suite() {
    return NbModuleSuite.create(AudioconfigTest.class, "ide|java", "rtaudio4java");
  }

  @Override
  protected void setUp() throws java.lang.Exception {
    // save the original settings
    System.out.println("Export settings to XML");
    File xmlFile = new File(getWorkDir(), originalSetting);
    OutputStream os = new FileOutputStream(xmlFile);

    Audioconfig.exportToXML(os);
  }

  @Override
  protected void tearDown() throws java.lang.Exception {
    System.out.println("Restore settings from XML");
    // restore the original settings
    File xmlFile = new File(getWorkDir(), originalSetting);
    InputStream is = new FileInputStream(xmlFile);
    Audioconfig.importFromXML(is);
  }

  /**
   * we assume that the original configuration is valid.
   * To this end, before running this test run once the whole
   * module and open open the  menu Tools | options
   * in the dialog choose audio settings and set the configuration to a valid
   * configuration on your system.
   */
  public void testOriginalConfig() {
    System.out.println("  testOriginalConfig");
    assertTrue("Run the module once, and set the audio config to a valid one.", Audioconfig.isProbed());
  }

  public void testInvalidSystemNumber() {
    System.out.println("  testInvalidSystemNumber");
    Audioconfig.putSystemNumber(AudioSystemFactory.APITYPE_UNSPECIFIED);
    assertFalse(Audioconfig.isProbed());
    System.out.println("     Message: " + Audioconfig.getProbeMessage());
  }

  public void testInvalidDeviceNumber() {
    System.out.println("  testInvalidDeviceNumber");
    Audioconfig.putOutputDeviceNumber(0xFFFFFFF);
    boolean result = Audioconfig.isProbed();
    assertFalse(result);
    System.out.println("     Message: " + Audioconfig.getProbeMessage());
  }

  public void testInvalidDeviceDescription() {
    System.out.println("  testInvalidDeviceDescription");
    Audioconfig.putOutputDeviceDescription("XXXX");
    boolean result = Audioconfig.isProbed();
    assertFalse(result);
    System.out.println("     Message: " + Audioconfig.getProbeMessage());
  }

  public void testInvalidFirstChannel() {
    System.out.println("  testInvalidFirstChannel");
    Audioconfig.putFirstChannel(0xFFFFFFF);
    boolean result = Audioconfig.isProbed();
    assertFalse(result);
    System.out.println("     Message: " + Audioconfig.getProbeMessage());
  }

  public void testInvalidNumberOfChannels() {
    System.out.println("  testInvalidNumberOfChannels");
    Audioconfig.putNumberOfChannels(0xFFFFFFF);
    boolean result = Audioconfig.isProbed();
    assertFalse(result);
    System.out.println("     Message: " + Audioconfig.getProbeMessage());
  }

  public void testInvalidSampleRate() {
    System.out.println("  testInvalidSampleRate");
    Audioconfig.putSampleRate(0xFFFFFFF);
    boolean result = Audioconfig.isProbed();
    assertFalse(result);
    System.out.println("     Message: " + Audioconfig.getProbeMessage());
  }
}
