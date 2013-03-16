/*
 *  Copyright 2011 harald.
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

import java.io.File;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInstall;
import rtaudio4java.AudioSystemFactory;

/**
 * Manages the life-cycle of the RtAudio4Java module.
 * Specifically it must make sure that the path to
 * the native DLL is on the java library path.
 * @see {@link http://wiki.netbeans.org/DevFaqModulesStartupActions }
 */
public class Installer extends ModuleInstall {

  /**
   * Called when the RtAudio- module is restored (during startup).
   * Loads the Native library.
   */
  @Override
  public void restored() {
    loadNativeRtAudioLib();
  }

  /**
   * Called when all modules agreed with closing and NetBeans will be closed. 
   */
  @Override
  public void close() {
    AudioSystemFactory.shutdown();
  }

  /**
   * Loads the Native library.
   * This function has been separated, so it can be used during test to
   * install the RtAudio library without having to load all NetBeans IDE modules.
   * But it still needs to have the NetBeans file system up and running.
   * @see {@link http://bits.netbeans.org/dev/javadoc/org-openide-modules/org/openide/modules/InstalledFileLocator.html }
   * @see {@link http://bits.netbeans.org/dev/javadoc/org-openide-modules/org/openide/modules/doc-files/api.html#jni }
   */
  public static void loadNativeRtAudioLib() {
    /* --- since version 7.0.1 the "modules/lib" path seems to be automatically added to the system path.
     * File myLibraryPath = InstalledFileLocator.getDefault().locate("modules/lib", "rtaudio4java", false);
     * if(myLibraryPath == null){
     *     throw new UnsatisfiedLinkError("\"org.openide.modules.InstalledFileLocator\" did not find the directory \"modules/lib\".");
     *  }
     * AudioSystemFactory.loadNativeLibray(myLibraryPath);
     * */
   // AudioSystemFactory.loadNativeLibray(null);
  }
}
