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
 * The ChannelPane shows the configuration related to one specific device.
 *
 * @author Harald Postner
 */
public class ChannelPane extends javax.swing.JPanel {

  private DeviceInfo deviceInfo;
  /**
   * The firstStereoChannelModel permits to select the first channel in case
   * Stereo-Play has been selected; it shows only the lower channel of a pair of
   * channels.
   */
  private AssocModel firstStereoChannelModel = new AssocModel();
  /**
   * The firstStereoChannelModel permits to select the first channel in case
   * Mono-Play has been selected; it shows all available channels.
   */
  private AssocModel firstMonoChannelModel = new AssocModel();
  private boolean isInput;
  AssocModel channelCountModel;
  private boolean isPopulated = false;
  private final StoredConfig storedConfig;
  private ConfigChangedListener master = null;

  /**
   * Creates new form ChannelPane
   *
   * @deprecated only for the NetBeans Swing GUI Builder (Matisse)
   */
  @Deprecated
  public ChannelPane() {
    this(null);
  }

  ChannelPane(StoredConfig storedConfig) {
    this.storedConfig = storedConfig;
    initComponents();
  }

  void populateChannnelPane(DeviceInfo deviceInfo, boolean input, ConfigChangedListener master) {
    this.master = master;

    this.deviceInfo = deviceInfo;
    this.isInput = input;

    int channelCount;
    if (input) {
      channelCount = deviceInfo.getInputChannels();
    } else {
      channelCount = deviceInfo.getOutputChannels();
    }
    // populate the firstMonoChannelModel with all available channels
    firstMonoChannelModel = new AssocModel();
    for (int i = 0; i < channelCount; i++) {
      firstMonoChannelModel.addPair(i, "" + (i + 1));
    }
    firstMonoChannelModel.setSelectedNumber(0);
    // populate the firstStereoChannelModel with the first channel of a pair of channels
    firstStereoChannelModel = new AssocModel();
    for (int i = 0; i < channelCount - 1; i = i + 2) {
      firstStereoChannelModel.addPair(i, "" + (i + 1));
    }
    firstStereoChannelModel.setSelectedNumber(0);

    // populate the model with mono/strereo if there is more than one channel available
    channelCountModel = new AssocModel();
    channelCountModel.addPair(1, "mono");
    if (channelCount > 1) {
      channelCountModel.addPair(2, "stereo");
    }
    cbxChannelCount.setModel(channelCountModel);

    //default to stereo on first channel (if possible)
    channelCountModel.setSelectedNumber(2);
    adjustFirstChannelModel();
    isPopulated = true;
  }

  DeviceInfo getDeviceInfo() {
    return deviceInfo;
  }

  protected void showPreferedConfiguration() {
    if (!isPopulated) {
      return;
    }
    if (storedConfig == null) {
      return;
    }
    if (isInput) {
      int channelCount = storedConfig.getNumberOfInputChannels();
      channelCountModel.setSelectedNumber(channelCount);
      firstStereoChannelModel.setSelectedNumber(storedConfig.getFirstInputChannel());
      firstMonoChannelModel.setSelectedNumber(storedConfig.getFirstInputChannel());
    } else {
      int channelCount = storedConfig.getNumberOfOutputChannels();
      channelCountModel.setSelectedNumber(channelCount);
      firstStereoChannelModel.setSelectedNumber(storedConfig.getFirstOutputChannel());
      firstMonoChannelModel.setSelectedNumber(storedConfig.getFirstOutputChannel());
    }
    cbxChannelCountActionPerformed(null);
  }

  protected void savePreferedConfiguration() {
    if (!isPopulated) {
      return;
    }
    if (storedConfig == null) {
      return;
    }
    int channelCount = channelCountModel.getSelectedNumber();
    if (isInput) {
      storedConfig.putNumberOfInputChannels(channelCount);
      if (channelCount > 1) {
        storedConfig.putFirstInputChannel(firstStereoChannelModel.getSelectedNumber());
      } else {
        storedConfig.putFirstInputChannel(firstMonoChannelModel.getSelectedNumber());
      }
    } else {
      storedConfig.putNumberOfOutputChannels(channelCount);
      if (channelCount > 1) {
        storedConfig.putFirstOutputChannel(firstStereoChannelModel.getSelectedNumber());
      } else {
        storedConfig.putFirstOutputChannel(firstMonoChannelModel.getSelectedNumber());
      }
    }
  }

  /**
   * Depending whether the user has chosen "stereo" or "mono" play, select the
   * appropriate model for the cbxFirstChannel combo box.
   */
  private void adjustFirstChannelModel() {
    int channelCount = 2;
    try {
      AssocModel model = (AssocModel) cbxChannelCount.getModel();
      channelCount = model.getSelectedNumber();
    } catch (Throwable ignored) {
    }
    if (channelCount == 1) {
      cbxFirstChannel.setModel(firstMonoChannelModel);
    } else {
      cbxFirstChannel.setModel(firstStereoChannelModel);
    }
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jLabel1 = new javax.swing.JLabel();
    cbxFirstChannel = new javax.swing.JComboBox();
    jLabel2 = new javax.swing.JLabel();
    cbxChannelCount = new javax.swing.JComboBox();

    jLabel1.setText("Channel:");

    cbxFirstChannel.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1" }));
    cbxFirstChannel.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cbxFirstChannelActionPerformed(evt);
      }
    });

    jLabel2.setText("Stereo / Mono:");

    cbxChannelCount.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "stereo" }));
    cbxChannelCount.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cbxChannelCountActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jLabel1)
          .addComponent(jLabel2))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
          .addComponent(cbxFirstChannel, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(cbxChannelCount, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        .addGap(0, 86, Short.MAX_VALUE))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGap(0, 0, 0)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel2)
          .addComponent(cbxChannelCount, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(cbxFirstChannel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel1))
        .addContainerGap(16, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents

  private void cbxChannelCountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxChannelCountActionPerformed
    adjustFirstChannelModel();
    notifyMaster();
  }//GEN-LAST:event_cbxChannelCountActionPerformed

  private void cbxFirstChannelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxFirstChannelActionPerformed
    notifyMaster();
  }//GEN-LAST:event_cbxFirstChannelActionPerformed
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JComboBox cbxChannelCount;
  private javax.swing.JComboBox cbxFirstChannel;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  // End of variables declaration//GEN-END:variables

  private void notifyMaster() {
    if (master != null) {
      master.subpaneChanged();
    }
  }
}
