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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JComponent;
import rtaudio4java.DeviceInfo;

/**
 * The AudioArchitecturePane shows the configuration related to one specific
 * Audio Architecture such as "Microsoft Multimedia" or "Linux-Alsa". It shows
 * the selected buffer sizes and two sub-panes the OutputDevicesPane and the
 * InputDevicesPane.
 *
 * @author Harald Postner
 */
public class AudioArchitecturePane extends javax.swing.JPanel
        implements DeviceChangedListener, ConfigChangedListener {

  private DeviceInfo inputDeviceInfo = null;
  private DeviceInfo outputDeviceInfo = null;
  private AssocModel bufferSizeModel;
  private AssocModel sampleRateModel;
  private boolean isPopulated = false;
  private final StoredConfig storedConfig;
  private ConfigChangedListener master = null;

  /**
   * Creates new form AudioArchitecturePane
   *
   * @deprecated only for the NetBeans Swing GUI Builder (Matisse)
   */
  public AudioArchitecturePane() {
    this(null);
  }

  AudioArchitecturePane(StoredConfig storedConfig) {
    this.storedConfig = storedConfig;
    initComponents();
    cbBufferCount.setValue(8);
    bufferSizeModel = new AssocModel();
    bufferSizeModel.addPair(64, "64");
    bufferSizeModel.addPair(128, "128");
    bufferSizeModel.addPair(256, "256");
    bufferSizeModel.addPair(512, "512");
    bufferSizeModel.addPair(1024, "1024");
    bufferSizeModel.addPair(2048, "2048");
    bufferSizeModel.setSelectedNumber(1024);
    cbBufferSize.setModel(bufferSizeModel);
    disableAllComponents();
  }

  void populateArchitecturePane(DeviceInfo[] deviceInfos, ConfigChangedListener master) {
    this.master = master;
    if (deviceInfos.length > 0) {
      inputDevicePane.populateInputDevicePane(deviceInfos, this, this);
      outputDevicePane.populateOutputDevicePane(deviceInfos, this, this);
      setEnabledOnComponent(this, true);
      isPopulated = true;
    } else {
      disableAllComponents();
    }
  }

  protected void showPreferedConfiguration() {
    if (!isPopulated) {
      return;
    }
    inputDevicePane.showPreferedConfiguration();
    outputDevicePane.showPreferedConfiguration();
    //
    bufferSizeModel.setSelectedNumber(storedConfig.getBufferSize());
    sampleRateModel.setSelectedNumber(storedConfig.getSampleRate());
    cbBufferCount.setValue(storedConfig.getBufferCount());
    cbBufferSizeActionPerformed(null);
    cbSampleRateActionPerformed(null);
    cbBufferCountStateChanged(null);

  }

  protected void savePreferedConfiguration() {
    if (!isPopulated) {
      return;
    }
    storedConfig.putBufferSize(bufferSizeModel.getSelectedNumber());
    storedConfig.putSampleRate(sampleRateModel.getSelectedNumber());
    storedConfig.putBufferCount((Integer) cbBufferCount.getValue());
    inputDevicePane.savePreferedConfiguration();
    outputDevicePane.savePreferedConfiguration();
  }

  /**
   * This method must be called whenever a device has been selected. Its task is
   * to adjust the set of available sample rates, as to match this set with the
   * input and the output device.
   *
   * @param info
   * @param input
   */
  @Override
  public void newDevice(DeviceInfo info, boolean input) {
    if (input) {
      inputDeviceInfo = info;
    } else {
      outputDeviceInfo = info;
    }
    DeviceInfo[] newInfos = new DeviceInfo[]{inputDeviceInfo, outputDeviceInfo};
    populateSampleRate(newInfos);
  }

  /**
   * Populates the SampleRate combo box with a set of sample rates that is
   * common to all devices.
   *
   * @param deviceInfos
   */
  void populateSampleRate(DeviceInfo[] deviceInfos) {
    Set<Integer> sampleRates = commonSampleRates(deviceInfos);
    sampleRateModel = new AssocModel();
    if (sampleRates.isEmpty()) {
      sampleRateModel.addPair(44100, "Impossible!!!");
    }
    for (Integer sr : sampleRates) {
      sampleRateModel.addPair(sr, sr.toString());
    }
    sampleRateModel.setSelectedNumber(44100);
    cbSampleRate.setModel(sampleRateModel);
    cbSampleRateActionPerformed(null);
  }

  private Set<Integer> commonSampleRates(DeviceInfo[] deviceInfos) {
    Set<Integer> intersection = new HashSet<>();
    ArrayList<DeviceInfo> notNullDevices = new ArrayList<>();
    if (deviceInfos != null) {
      for (DeviceInfo info : deviceInfos) {
        if (info != null) {
          if (info.isProbed()) {
            notNullDevices.add(info);
          }
        }
      }
    }
    if (!notNullDevices.isEmpty()) {
      intersection.addAll(notNullDevices.get(0).getSampleRates());
    }
    for (int i = 1; i < notNullDevices.size(); i++) {
      intersection.retainAll(notNullDevices.get(i).getSampleRates());
    }
    return intersection;
  }

  private void disableAllComponents() {
    setEnabledOnComponentAndChildren(this, false);
  }

  private void displayNewLatency() {
    float latency = (getBufferSize() * getBufferCount() * 1000F) / (float) getSampleRate();

    lblLatency.setText(String.format("Latency: %.1f ms", latency));
  }

  public int getSampleRate() {
    try {
      String sampleRateStr = (String) cbSampleRate.getSelectedItem();
      return Integer.parseInt(sampleRateStr);
    } catch (Exception e) {
      return 44100;
    }
  }

  public int getBufferSize() {
    try {
      return bufferSizeModel.getSelectedNumber();
    } catch (Exception e) {
      return 1024;
    }
  }

  public int getBufferCount() {
    try {
      return (Integer) cbBufferCount.getValue();
    } catch (Exception e) {
      return 8;
    }
  }

  /**
   * A utility function that permits to recursively enable or disable a
   * component and all its children. This function is useful disable all
   * controls for a window that has no song session.
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
   * A utility function that permits to enable or disable a component and its
   * direct children.
   *
   * @param component
   * @param enabled
   */
  protected final void setEnabledOnComponent(JComponent component, boolean enabled) {
    for (Component awtComponent : component.getComponents()) {
      if (awtComponent instanceof JComponent) {
        awtComponent.setEnabled(enabled);
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

    jLabel1 = new javax.swing.JLabel();
    cbBufferSize = new javax.swing.JComboBox();
    jLabel2 = new javax.swing.JLabel();
    cbBufferCount = new javax.swing.JSpinner();
    jLabel3 = new javax.swing.JLabel();
    cbSampleRate = new javax.swing.JComboBox();
    lblLatency = new javax.swing.JLabel();
    outputDevicePane = new de.free_creations.audioconfig.OutputDevicesPane(storedConfig);
    inputDevicePane = new de.free_creations.audioconfig.InputDevicesPane(storedConfig);

    setPreferredSize(new java.awt.Dimension(616, 380));

    jLabel1.setText("Buffer Size:");

    cbBufferSize.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "128", "256", "512", "1024", "2048" }));
    cbBufferSize.setSelectedIndex(3);
    cbBufferSize.setToolTipText("");
    cbBufferSize.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cbBufferSizeActionPerformed(evt);
      }
    });

    jLabel2.setText("x");

    cbBufferCount.setModel(new javax.swing.SpinnerNumberModel(2, 1, 8, 1));
    cbBufferCount.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        cbBufferCountStateChanged(evt);
      }
    });

    jLabel3.setText("Sample Rate:");

    cbSampleRate.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "none" }));
    cbSampleRate.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cbSampleRateActionPerformed(evt);
      }
    });

    lblLatency.setText("Latency: 100 ms");

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addComponent(jLabel1)
              .addComponent(jLabel3))
            .addGap(18, 18, 18)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createSequentialGroup()
                .addComponent(cbBufferSize, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cbBufferCount, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(lblLatency))
              .addComponent(cbSampleRate, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)))
          .addGroup(layout.createSequentialGroup()
            .addComponent(outputDevicePane, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
            .addGap(18, 18, 18)
            .addComponent(inputDevicePane, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)))
        .addGap(4, 4, 4))
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel3)
          .addComponent(cbSampleRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel1)
          .addComponent(cbBufferSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel2)
          .addComponent(cbBufferCount, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(lblLatency))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(outputDevicePane, javax.swing.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE)
          .addComponent(inputDevicePane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
        .addContainerGap())
    );
  }// </editor-fold>//GEN-END:initComponents

  private void cbSampleRateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbSampleRateActionPerformed
    displayNewLatency();
    notifyMaster();
  }//GEN-LAST:event_cbSampleRateActionPerformed

  private void cbBufferSizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbBufferSizeActionPerformed
    displayNewLatency();
    notifyMaster();
  }//GEN-LAST:event_cbBufferSizeActionPerformed

  private void cbBufferCountStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_cbBufferCountStateChanged
    displayNewLatency();
    notifyMaster();
  }//GEN-LAST:event_cbBufferCountStateChanged
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JSpinner cbBufferCount;
  private javax.swing.JComboBox cbBufferSize;
  private javax.swing.JComboBox cbSampleRate;
  private de.free_creations.audioconfig.InputDevicesPane inputDevicePane;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel lblLatency;
  private de.free_creations.audioconfig.OutputDevicesPane outputDevicePane;
  // End of variables declaration//GEN-END:variables

  boolean hasConfiguration() {
    return isPopulated;
  }

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
