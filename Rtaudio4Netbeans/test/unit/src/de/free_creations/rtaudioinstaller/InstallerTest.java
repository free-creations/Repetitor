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

package de.free_creations.rtaudioinstaller;

import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import rtaudio4java.AudioSystemFactory;


/**
 * This class tests whether the RtAudio Library is correctly integrated
 * into the NetBeans framework.
 * We assume that the RtAudio Library by itself has been  thoroughly tested
 * in its own project. Here, we just make sure that the RtAudio class is
 * correctly linked with the native library.
 * We do this by verifying that the "getNativeBuildRevison()" function
 * can be called without throwing an UnsatisfiedLinkError.
 * For more information on how to test
 * NetBeans platform modules see also
 * {@link http://platform.netbeans.org/tutorials/nbm-test.html
 * http://platform.netbeans.org/tutorials/nbm-test.html}
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class InstallerTest extends NbTestCase {

  public InstallerTest(String s) {
    super(s);
  }

  /**
   * This method will create a
   * test that sets up a proper NetBeans Runtime Container environment,
   * especially it will load the RtAudio4Java module and invoke the installer.
   * @return
   */
  public static Test suite() {
    return NbModuleSuite.create(InstallerTest.class, "ide|java", "rtaudio4java");
  }

  public void testLoading() {
    String version = AudioSystemFactory.getNativeBuildRevison();
    assertNotNull(version);
    System.out.println("RtAudio4Java version "+version);
  }
}