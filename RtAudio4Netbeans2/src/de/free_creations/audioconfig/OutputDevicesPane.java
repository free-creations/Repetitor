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

import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.OverlayLayout;
import rtaudio4java.DeviceInfo;

/**
 * The OutputDevicesPane shows the configuration related to all output devices
 * available in a given Audio Architecture. For each device it has a separate
 * Channel-sub-pane.
 *
 * @author Harald Postner
 */
public class OutputDevicesPane extends javax.swing.JPanel
        implements ConfigChangedListener {

  private ChannelPane[] channelPanes;
  private final boolean isInput = false;
  private DeviceChangedListener deviceChangedListener = null;
  AssocModel deviceModel;
  private final StoredConfig storedConfig;
  private ConfigChangedListener master = null;

  /**
   * Creates new form OutputDevicesPane
   *
   * @deprecated only for the NetBeans Swing GUI Builder (Matisse)
   */
  @Deprecated
  public OutputDevicesPane() {
    this(null);
  }

  OutputDevicesPane(StoredConfig storedConfig) {
    this.storedConfig = storedConfig;
    initComponents();
    disableAllComponents();
    channelPanes = new ChannelPane[]{defaultChannelPane};
    channelContainerPane.setLayout(new OverlayLayout(channelContainerPane));
  }

  void populateOutputDevicePane(DeviceInfo[] deviceInfos, DeviceChangedListener listener, ConfigChangedListener master) {
    this.master = master;
    
    deviceChangedListener = listener;

    deviceModel = populateComboBoxModel(deviceInfos);
    int deviceCount = deviceModel.getSize();
    if (deviceCount < 1) {
      disableAllComponents();
      return;
    }
    cbxDevice.setModel(deviceModel);
    channelContainerPane.removeAll();
    channelPanes = new ChannelPane[deviceCount];
    for (int i = 0; i < deviceCount; i++) {
      ChannelPane channelPannel = new ChannelPane(storedConfig);
      channelPannel.populateChannnelPane(deviceInfos[deviceModel.getNumberOfItem(i)], isInput, this);
      channelPanes[i] = channelPannel;
      channelContainerPane.add(channelPannel, javax.swing.JLayeredPane.DEFAULT_LAYER);
    }


    enableAllComponents();
    cbxDevice.setSelectedIndex(0);
    cbxDeviceActionPerformed(null);
  }

  private AssocModel populateComboBoxModel(DeviceInfo[] deviceInfos) {
    deviceModel = new AssocModel();
    if (deviceInfos != null) {
      for (int i = 0; i < deviceInfos.length; i++) {
        DeviceInfo info = deviceInfos[i];
        if (info != null) {
          if (info.isProbed()) {
            if (isInput) {
              if (info.getInputChannels() > 0) {
                deviceModel.addPair(i, info.getName());
              }
            } else {
              if (info.getOutputChannels() > 0) {
                deviceModel.addPair(i, info.getName());
              }
            }
          }
        }
      }
    }
    return deviceModel;
  }

  protected void showPreferedConfiguration() {
    String storedOutputDevice = storedConfig.getOutputDeviceDescription();
    boolean success = deviceModel.setSelectedDescription(storedOutputDevice);
    //
    if (success) {
      cbxDeviceActionPerformed(null);
      ChannelPane selectedPane = getSelectedDevicePane();
      if (selectedPane != null) {
        selectedPane.showPreferedConfiguration();
      }
    }
  }

  protected void savePreferedConfiguration() {
    String outputDevice = deviceModel.getSelectedDescription();
    storedConfig.putOutputDeviceDescription(outputDevice);
    //
    ChannelPane selectedPane = getSelectedDevicePane();
    if (selectedPane != null) {
      selectedPane.savePreferedConfiguration();
    }
  }

  private ChannelPane getSelectedDevicePane() {

    int selectedIndex = cbxDevice.getSelectedIndex();
    if (selectedIndex < 0) {
      return null;
    }
    if (selectedIndex >= channelPanes.length) {
      return null;
    }
    return channelPanes[selectedIndex];
  }

  public final void disableAllComponents() {
    setEnabledOnComponentAndChildren(this, false);
  }

  public final void enableAllComponents() {
    setEnabledOnComponentAndChildren(this, true);
  }

  /**
   * A utility function that permits to recursively enable or disable a
   * component and all its children.
   *
   * @param component the root component. This component and all its children
   * will change state.
   * @param enabled set this to true if the components should be enabled. Set
   * this to False if the components should be disabled.
   *
   */
  protected final void setEnabledOnComponentAndChildren(JComponent component, boolean enabled) {
    for (Component awtComponent : component.getComponents()) {
      if (awtComponent instanceof JComponent) {
        setEnabledOnComponentAndChildren((JComponent) awtComponent, enabled);
      }
    }
    component.setEnabled(enabled);
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    cbxDevice = new javax.swing.JComboBox();
    jLabel1 = new javax.swing.JLabel();
    channelContainerPane = new javax.swing.JLayeredPane();
    defaultChannelPane = new de.free_creations.audioconfig.ChannelPane();

    setPreferredSize(new java.awt.Dimension(300, 300));

    cbxDevice.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Not available" }));
    cbxDevice.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cbxDeviceActionPerformed(evt);
      }
    });

    jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/free_creations/audioconfig/resources/headPhone24.png"))); // NOI18N
    jLabel1.setText("Output Device:");
    jLabel1.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

    defaultChannelPane.setBounds(0, 0, 255, 72);
    channelContainerPane.add(defaultChannelPane, javax.swing.JLayeredPane.DEFAULT_LAYER);

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addComponent(jLabel1)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(cbxDevice, 0, 177, Short.MAX_VALUE))
      .addGroup(layout.createSequentialGroup()
        .addComponent(channelContainerPane)
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(cbxDevice, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel1))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(channelContainerPane, javax.swing.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
        .addContainerGap())
    );
  }// </editor-fold>//GEN-END:initComponents

  private void cbxDeviceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxDeviceActionPerformed
    int deviceIdx = cbxDevice.getSelectedIndex();
    if (deviceIdx < 0) {
      return;
    }
    if (deviceIdx >= channelPanes.length) {
      return;
    }
    for (ChannelPane pane : channelPanes) {
      pane.setVisible(false);
    }
    channelPanes[deviceIdx].setVisible(true);
    channelContainerPane.moveToFront(channelPanes[deviceIdx]);

    if (deviceChangedListener != null) {
      deviceChangedListener.newDevice(channelPanes[deviceIdx].getDeviceInfo(), isInput);
    }
    
    notifyMaster();


  }//GEN-LAST:event_cbxDeviceActionPerformed
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JComboBox cbxDevice;
  private javax.swing.JLayeredPane channelContainerPane;
  private de.free_creations.audioconfig.ChannelPane defaultChannelPane;
  private javax.swing.JLabel jLabel1;
  // End of variables declaration//GEN-END:variables

  @Override
  public void subpaneChanged() {
    notifyMaster();
  }

  private void notifyMaster() {
    if (master != null) {
      master.subpaneChanged();
    }
  }
}
