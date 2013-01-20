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
package de.free_creations.audioconfig;

import de.free_creations.audioconfig.Audioconfig.ConfigDialogEndListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * Provides the dialog to manage the
 * audio- configuration.
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
@OptionsPanelController.TopLevelRegistration(id = "AudioSettings",
categoryName = "#OptionsCategory_Name_AudioSettings",
iconBase = "de/free_creations/audioconfig/phone.png",
keywords = "#OptionsCategory_Keywords_AudioSettings",
keywordsCategory = "AudioSettings")
public final class AudioSettingsOptionsPanelController extends OptionsPanelController {

  private static String message = null;
  private AudioSettingsPanel panel;
  private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
  private boolean changed;
  static private ConfigDialogEndListener endListener = null;

  static public void setConfigDialogEndListener(ConfigDialogEndListener listnener) {
    endListener = listnener;
  }

  static public void setMessage(String s) {
    message = s;
  }

  @Override
  public void update() {
    getPanel().load();
    changed = false;
  }

  @Override
  public void applyChanges() {
    getPanel().store();
    getPanel().stopPlaying();
    changed = false;
    if (endListener != null) {
      endListener.dialogClosed();
      endListener = null;
    }
  }

  @Override
  public void cancel() {
        getPanel().stopPlaying();
  }

  @Override
  public boolean isValid() {
    return getPanel().valid();
  }

  @Override
  public boolean isChanged() {
    return changed;
  }

  @Override
  public HelpCtx getHelpCtx() {
    return null; // new HelpCtx("...ID") if you have a help set
  }

  @Override
  public JComponent getComponent(Lookup masterLookup) {
    return getPanel();
  }

  @Override
  public void addPropertyChangeListener(PropertyChangeListener l) {
    pcs.addPropertyChangeListener(l);
  }

  @Override
  public void removePropertyChangeListener(PropertyChangeListener l) {
    pcs.removePropertyChangeListener(l);
  }

  private AudioSettingsPanel getPanel() {
    if (panel == null) {
      panel = new AudioSettingsPanel(this);
    }
    if(message != null){
      panel.setMessage(message);
      message = null;
    }

    return panel;
  }

  void changed() {
    if (!changed) {
      changed = true;
      pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
    }
    pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
  }
}
