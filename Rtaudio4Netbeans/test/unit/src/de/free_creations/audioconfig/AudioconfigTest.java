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

import java.io.FileOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import java.io.InputStream;
import java.io.OutputStream;

/**
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class AudioconfigTest extends NbTestCase {

  public AudioconfigTest(String s) throws IOException {
    super(s);
    clearWorkDir();
  }

  /**
   * Test of getSystemNumber method, of class Audioconfig.
   */
  public void testGetPutSystemNumber() {
    System.out.println("getPutSystemNumber");
    int expResult = 1;
    Audioconfig.putSystemNumber(expResult);
    int result = Audioconfig.getSystemNumber();
    assertEquals(expResult, result);
  }

  /**
   * Test of getSystemDescription method, of class Audioconfig.
   */
  public void testGetPutSystemDescription() {
    System.out.println("getPutSystemDescription");
    String expResult = "Test_2";
    Audioconfig.putSystemDescription(expResult);
    String result = Audioconfig.getSystemDescription();
    assertEquals(expResult, result);
  }

  /**
   * Test of getOutputDeviceNumber method, of class Audioconfig.
   */
  public void testGetPutOutputDeviceNumber() {
    System.out.println("getOutputDeviceNumber");
    int expResult = 3;
    Audioconfig.putOutputDeviceNumber(expResult);
    int result = Audioconfig.getOutputDeviceNumber();
    assertEquals(expResult, result);
  }

  /**
   * Test of getOutputDeviceDescription method, of class Audioconfig.
   */
  public void testGetPutOutputDeviceDescription() {
    System.out.println("getPutOutputDeviceDescription");
    String expResult = "Test_4";
    Audioconfig.putOutputDeviceDescription(expResult);
    String result = Audioconfig.getOutputDeviceDescription();
    assertEquals(expResult, result);
  }

  /**
   * Test of getFirstChannel method, of class Audioconfig.
   */
  public void testGetPutFirstChannel() {
    System.out.println("getPutFirstChannel");
    int expResult = 5;
    Audioconfig.putFirstChannel(expResult);
    int result = Audioconfig.getFirstChannel();
    assertEquals(expResult, result);
  }

  /**
   * Test of getNumberOfChannels method, of class Audioconfig.
   */
  public void testGetPutNumberOfChannels() {
    System.out.println("getNumberOfChannels");
    int expResult = 6;
    Audioconfig.putNumberOfChannels(expResult);
    int result = Audioconfig.getNumberOfChannels();
    assertEquals(expResult, result);
  }

  /**
   * Test of getSampleRate method, of class Audioconfig.
   */
  public void testGetPutSampleRate() {
    System.out.println("getSampleRate");
    int expResult = 7;
    Audioconfig.putSampleRate(expResult);
    int result = Audioconfig.getSampleRate();
    assertEquals(expResult, result);
  }

  /**
   * Test of getBufferSize method, of class Audioconfig.
   */
  public void testGetPutBufferSize() {
    System.out.println("getBufferSize");
    int expResult = 8;
    Audioconfig.putBufferSize(expResult);
    int result = Audioconfig.getBufferSize();
    assertEquals(expResult, result);
  }

  /**
   * Test of exportToXML method, of class Audioconfig.
   */
  public void testImportExportToFromXML() throws Exception {
    System.out.println("testImportExportToFromXML");
    File xmlFile = new File(getWorkDir(), "TestSettings.xml");
    OutputStream os = new FileOutputStream(xmlFile);
    // put some value to device description and export it to an XML file.
    String expResult = "Test_ImportExport";
    Audioconfig.putOutputDeviceDescription(expResult);
    Audioconfig.exportToXML(os);
    os.close();
    assertTrue(xmlFile.isFile());
    assertTrue(xmlFile.exists());

    // clear the value and make sure it is cleared
    Audioconfig.putOutputDeviceDescription("XXX");
    assertEquals("XXX", Audioconfig.getOutputDeviceDescription());

    // restore the value
    InputStream is = new FileInputStream(xmlFile);
    Audioconfig.importFromXML(is);

    String result = Audioconfig.getOutputDeviceDescription();
    assertEquals(expResult, result);

  }

  /**
   * Test of isProbed method, of class Audioconfig.
   * In fact there is not much to be tested here.
   * The test-runner does not load the RtAudio library,
   * therefore the probing will fail on a linkage error.
   * A more in depth testing will be done in the
   * functional tests.
   */
  public void testIsProbed() {
    System.out.println("testIsProbed");
    boolean result = Audioconfig.isProbed();
    assertFalse(result);
    assertTrue(Audioconfig.getProbeMessage().length() > 0);
    System.out.println("... This test is expected to fail with a linkager error.");
    System.out.println(".... The message is \"" + Audioconfig.getProbeMessage()+"\"");
  }

  public void testShowConfigDialog() {
    System.out.println("testShowConfigDialog");
    //Audioconfig.showConfigDialog();
  }
}
