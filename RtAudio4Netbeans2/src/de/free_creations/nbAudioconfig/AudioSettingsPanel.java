/*
 * Copyright 2013 harald.
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
package de.free_creations.nbAudioconfig;

import de.free_creations.audioconfig.AudioSettingsPanelContent;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;

final class AudioSettingsPanel extends javax.swing.JPanel {

  private final AudioSettingsOptionsPanelController controller;
  private final AudioSettingsPanelContent content;
  private boolean setupOK = false;


  AudioSettingsPanel(final AudioSettingsOptionsPanelController controller) {
    this.controller = controller;
    initComponents();
    content = new AudioSettingsPanelContent();
    content.setAudioSettingsPanelListener(
            new AudioSettingsPanelContent.AudioSettingsPanelListener() {
              @Override
              public void setupDone() {
                setupOK = true;
                controller.changed();
              }
            });
    add(content, java.awt.BorderLayout.CENTER);

  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    setLayout(new java.awt.BorderLayout());
  }// </editor-fold>//GEN-END:initComponents

  void load() {
    // read settings and initialize GUI
     content.setup();
  }

  void store() {
    // store modified settings
    try {
      content.savePreferedConfiguration();
      Logger.getLogger(this.getClass().getName()).log(Level.INFO, "New Audio settings saved.");
    } catch (BackingStoreException ex) {
      Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
      content.setAlertMessage(ex.getMessage());
    }
  }

  boolean valid() {
    return setupOK;
  }
  // Variables declaration - do not modify//GEN-BEGIN:variables
  // End of variables declaration//GEN-END:variables
}
