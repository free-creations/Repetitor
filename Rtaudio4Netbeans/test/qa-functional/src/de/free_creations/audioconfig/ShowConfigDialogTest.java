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

import de.free_creations.audioconfig.Audioconfig.ConfigDialogEndListener;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbModuleSuite.Configuration;

/**
 * This test tries to verify if the
 * {@link Audioconfig#showConfigDialog } function works correctly.
 * first the config dialog is displayed and a message
 * "------ dialog opened" is printed then the
 * test waits for 20 seconds. 
 * 
 * When you close the dialog with OK during these 20 seconds,
 * the message "------ dialog closed" should be printed, showing
 * that the callback has worked.
 *
 * When you let the 20 seconds expire the test should be shutdown
 * without printing any further message.
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class ShowConfigDialogTest extends JellyTestCase {

  /** Constructor required by JUnit */
  public ShowConfigDialogTest(String name) {
    super(name);
  }

  /** Creates suite from particular test cases. You can define order of testcases here. */
  public static Test suite() {

    Configuration testConfig = NbModuleSuite.createConfiguration(ShowConfigDialogTest.class);
    //testConfig.addTest("testBrushSize", "testPainting", "testClear", "testColorChooser");
    testConfig.clusters(".*").enableModules(".*");
    testConfig.gui(true);

    return NbModuleSuite.create(testConfig);

  }

  /** Called before every test case. */
  @Override
  public void setUp() {
    System.out.println("########  " + getName() + "  #######");
  }

  /** Test brush size setting. */
  public void testConfigDialog() throws InterruptedException {
    ConfigDialogEndListener listener = new ConfigDialogEndListener() {

      @Override
      public void dialogClosed() {
        System.out.println("------ dialog closed");
      }
    };
    System.out.println("------ dialog opened");
    Audioconfig.showConfigDialog(listener,"This is a test");
    Thread.sleep(20000);
  }
}
