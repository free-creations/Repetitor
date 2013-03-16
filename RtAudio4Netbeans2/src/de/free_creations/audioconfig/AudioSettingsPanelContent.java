/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.free_creations.audioconfig;

import de.free_creations.audioconfig.StoredConfig.ConfigRecord;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.OverlayLayout;
import javax.swing.Timer;
import rtaudio4java.AudioSystem.StreamParameters;

/**
 * The audio AudioSettingsPanelshows the configuration all Audio Architectures
 * currently available.
 *
 * @author Harald Postner
 */
public class AudioSettingsPanelContent extends javax.swing.JPanel implements ConfigChangedListener {

  private static final Logger logger = Logger.getLogger(AudioSettingsPanelContent.class.getName());
  private Recorder recorder;
  private Timer animationTimer = null;
  private final ImageIcon warningIcon = new ImageIcon(getClass().getResource("/de/free_creations/audioconfig/resources/warning32.png"));
  private final ImageIcon alertIcon = new ImageIcon(getClass().getResource("/de/free_creations/audioconfig/resources/alert32.png"));
  private final ImageIcon micro_off = new ImageIcon(getClass().getResource("/de/free_creations/audioconfig/resources/microOff.png"));
  private final ImageIcon[] micro_animated = new ImageIcon[]{
    new ImageIcon(getClass().getResource("/de/free_creations/audioconfig/resources/microLevel0.png")),
    new ImageIcon(getClass().getResource("/de/free_creations/audioconfig/resources/microLevel1.png")),
    new ImageIcon(getClass().getResource("/de/free_creations/audioconfig/resources/microLevel2.png")),
    new ImageIcon(getClass().getResource("/de/free_creations/audioconfig/resources/microLevel3.png")),
    new ImageIcon(getClass().getResource("/de/free_creations/audioconfig/resources/microLevel4.png")),};
  private final ImageIcon[] phone_animated = new ImageIcon[]{
    new ImageIcon(getClass().getResource("/de/free_creations/audioconfig/resources/phonePhase0.png")),
    new ImageIcon(getClass().getResource("/de/free_creations/audioconfig/resources/phonePhase1.png")),
    new ImageIcon(getClass().getResource("/de/free_creations/audioconfig/resources/phonePhase2.png")),
    new ImageIcon(getClass().getResource("/de/free_creations/audioconfig/resources/phonePhase3.png")),};

  public interface AudioSettingsPanelListener {

    /**
     * Instructs the listener that the setup has executed.
     */
    public void setupDone();
  }
  //
  private final StoredConfig storedConfig;
  private AudioArchitecturePane[] architecurePanes;
  private volatile AssocModel audioArchitectureModel;
  private AudioSettingsPanelListener audioSettingsPanelListener;
  private volatile boolean setupOK = false;
  private volatile AudioSystemInfo audioSystemInfo = null;
  private SineGenerator sineGenerator = null;

  public boolean isSetupOK() {
    return setupOK;
  }

  private void setSetupOK(boolean setupOK) {
    this.setupOK = setupOK;
  }

  public AudioSettingsPanelListener getAudioSettingsPanelListener() {
    return audioSettingsPanelListener;
  }

  public void setAudioSettingsPanelListener(AudioSettingsPanelListener audioSettingsPanelListener) {
    this.audioSettingsPanelListener = audioSettingsPanelListener;
  }

  private class PannelPopulator implements Runnable {

    private final AudioSettingsPanelContent audioSettingsPanel;

    public PannelPopulator(AudioSettingsPanelContent audioSettingsPanel) {
      this.audioSettingsPanel = audioSettingsPanel;
    }

    @Override
    public void run() {
      String localErrorMessage = null;
      try {
        audioSystemInfo = new AudioSystemInfo();
      } catch (Throwable ex) {
        audioSystemInfo = null;
        localErrorMessage = ex.getMessage();
      }
      final String finalErrorMessage = localErrorMessage;
      /* Populate the form in the AWT thread*/
      EventQueue.invokeLater(new Runnable() {
        @Override
        public void run() {
          if (audioSystemInfo != null) {
            audioSettingsPanel.populateSettingsPanel(audioSystemInfo);
            audioSettingsPanel.setSetupOK(true);
            audioSettingsPanel.showPreferedConfiguration();
            AudioSettingsPanelListener listener = audioSettingsPanel.getAudioSettingsPanelListener();
            if (listener != null) {
              listener.setupDone();
            }
          }
          audioSettingsPanel.setAlertMessage(finalErrorMessage);
        }
      });
    }
  }

  private class MicrophoneUpdater implements ActionListener {

    private final Recorder recorder;

    public MicrophoneUpdater(Recorder recorder) {
      this.recorder = recorder;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      int level = recorder.getPeakLevelAndClear();
      switch (level) {
        case 0:
          lblTestInput.setIcon(micro_animated[0]);
          return;
        case 1:
          lblTestInput.setIcon(micro_animated[1]);
          return;
        case 2:
          lblTestInput.setIcon(micro_animated[2]);
          return;
        case 3:
          lblTestInput.setIcon(micro_animated[3]);
          return;
        case 4:
          lblTestInput.setIcon(micro_animated[4]);
          return;

        default:
          lblTestInput.setIcon(micro_animated[0]);
      }
    }
  };

  private class HeadphoneUpdater implements ActionListener {

    private int count = 0;

    @Override
    public void actionPerformed(ActionEvent e) {
      if (count > phone_animated.length - 1) {
        count = 1;
      }
      lblTestOutput.setIcon(phone_animated[count]);
      count++;
    }
  };

  /**
   * Creates a new OutputDevicePanel. At this point the Panel is disabled. To
   * display the hard- and soft- ware currently available, the user must call
   * setup().
   */
  public AudioSettingsPanelContent() {
    storedConfig = new StoredConfig();
    initComponents();
    disableAllComponents();
    architecurePanes = new AudioArchitecturePane[]{defaultSystemPanel};
    audioSystemPane.setLayout(new OverlayLayout(audioSystemPane));
    setAlertMessage(null);
  }

  final public void setWarningMessage(String message) {
    if (message != null) {
      lblErrorMessages.setEnabled(true);
      lblErrorMessages.setVisible(true);
      lblErrorMessages.setIcon(warningIcon);
      lblErrorMessages.setText(message);
    } else {
      clearMessage();
    }
  }

  final public void setAlertMessage(String message) {
    if (message != null) {
      lblErrorMessages.setEnabled(true);
      lblErrorMessages.setVisible(true);
      lblErrorMessages.setIcon(alertIcon);
      lblErrorMessages.setText(message);
    } else {
      clearMessage();
    }
  }

  final public void clearMessage() {
    lblErrorMessages.setVisible(false);
  }

  /**
   * Populates the panel with values corresponding to the available audio
   * hardware and than tries to re-establish a configuration stored in the
   * preferences.
   *
   * This function returns immediately, the inspection of the audio hardware is
   * done in a separate thread. Use an AudioSettingsPanelListener to get
   * informed when the panel is ready to be used.
   */
  public void setup() {

    PannelPopulator pannelPopulator = new PannelPopulator(this);
    Thread pannelPopulatorThread = new Thread(pannelPopulator, "FreeCreationsAudioSettings");
    pannelPopulatorThread.start();
  }

  /**
   * Try to adjust the controls so they display a configuration corresponding to
   * the one obtained from "StoredConfig.java".
   *
   * This function shall only be invoked after the AudioSettingsPanel has been
   * successfully populated. The calling thread must be the AWT thread.
   */
  private void showPreferedConfiguration() {
    if (!setupOK) {
      throw new RuntimeException("AudioSettingsPanel not yet set-up.");
    }
    if (!EventQueue.isDispatchThread()) {
      throw new RuntimeException("Called on wrong thread.");
    }
    int storedConfigNumber = storedConfig.getArchitectureNumber();
    boolean success = audioArchitectureModel.setSelectedNumber(storedConfigNumber);
    //
    if (success) {
      cbxAudioSystemActionPerformed(null);
      AudioArchitecturePane selectedPane = getSelectedArchitecturePane();
      if (selectedPane != null) {
        selectedPane.showPreferedConfiguration();
      }
    }

  }

  /**
   * Save the chosen configuration into the preferences.
   *
   * This function shall only be invoked after the AudioSettingsPanel has been
   * successfully populated. The calling thread must be the AWT thread.
   */
  public void savePreferedConfiguration() throws BackingStoreException {
    getConfig();
    //......
    storedConfig.flush();
  }

  public StoredConfig getConfig() {
    if (!setupOK) {
      throw new RuntimeException("AudioSettingsPanel not yet set-up.");
    }
    if (!EventQueue.isDispatchThread()) {
      throw new RuntimeException("Called on wrong thread.");
    }

    //
    AudioArchitecturePane selectedPane = getSelectedArchitecturePane();
    if (selectedPane != null) {
      if (!selectedPane.hasConfiguration()) {
        return null;
      }
      selectedPane.savePreferedConfiguration();
    }
    int architecture = audioArchitectureModel.getSelectedNumber();
    storedConfig.putArchitectureNumber(architecture);
    //......
    return storedConfig;
  }

  private AudioArchitecturePane getSelectedArchitecturePane() {

    int selectedIndex = cbxAudioSystem.getSelectedIndex();
    if (selectedIndex < 0) {
      return null;
    }
    if (selectedIndex >= architecurePanes.length) {
      return null;
    }
    return architecurePanes[selectedIndex];
  }

  private void populateSettingsPanel(AudioSystemInfo systemInfo) {
    if (systemInfo == null) {
      disableAllComponents();
      return;
    }
    if (systemInfo.isEmpty()) {
      disableAllComponents();
      return;
    }
    audioSystemPane.removeAll();
    cbxAudioSystem.removeAllItems();
    int architectureCount = systemInfo.size();
    architecurePanes = new AudioArchitecturePane[architectureCount];
    for (int i = 0; i < architectureCount; i++) {
      AudioArchitecturePane systemPannel = new AudioArchitecturePane(storedConfig);
      systemPannel.populateArchitecturePane(systemInfo.get(i).getDeviceInfos(), this);
      architecurePanes[i] = systemPannel;
      audioSystemPane.add(systemPannel, javax.swing.JLayeredPane.DEFAULT_LAYER);
    }
    audioArchitectureModel = new AssocModel();
    for (int i = 0; i < architectureCount; i++) {
      audioArchitectureModel.addPair(
              systemInfo.get(i).getApiNumber(),
              systemInfo.get(i).getApiDescription());
    }
    cbxAudioSystem.setModel(audioArchitectureModel);
    cbxAudioSystem.setSelectedIndex(0);
    setEnabledOnComponent(this, true);
    cbxAudioSystemActionPerformed(null);
  }

  public final void disableAllComponents() {
    setEnabledOnComponentAndChildren(this, false);
    //... but not the error messages
    setEnabledOnComponentAndChildren(errorMessagePage, true);
  }

  public final void enableAllComponents() {
    setEnabledOnComponentAndChildren(this, true);
  }

  /**
   * A utility function that permits to recursively enable or disable a
   * component and all its children. This function is useful disable all
   * controls.
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

    cbxAudioSystem = new javax.swing.JComboBox();
    lblAdioSystem = new javax.swing.JLabel();
    audioSystemPane = new javax.swing.JLayeredPane();
    defaultSystemPanel = new de.free_creations.audioconfig.AudioArchitecturePane();
    outputTestPanel = new javax.swing.JPanel();
    lblTestOutput = new javax.swing.JLabel();
    btnTestOutput = new javax.swing.JButton();
    btnStopOutput = new javax.swing.JButton();
    inputTestPane = new javax.swing.JPanel();
    lblTestInput = new javax.swing.JLabel();
    btnTestInput = new javax.swing.JButton();
    btnStopInput = new javax.swing.JButton();
    errorMessagePage = new javax.swing.JPanel();
    lblErrorMessages = new javax.swing.JLabel();

    cbxAudioSystem.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "none" }));
    cbxAudioSystem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cbxAudioSystemActionPerformed(evt);
      }
    });

    lblAdioSystem.setText("Audio System:");

    audioSystemPane.setMinimumSize(new java.awt.Dimension(100, 0));
    defaultSystemPanel.setBounds(0, 0, 616, 380);
    audioSystemPane.add(defaultSystemPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

    outputTestPanel.setMinimumSize(new java.awt.Dimension(60, 30));
    outputTestPanel.setPreferredSize(new java.awt.Dimension(300, 30));

    lblTestOutput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/free_creations/audioconfig/resources/phonePhase0.png"))); // NOI18N
    lblTestOutput.setLabelFor(btnTestOutput);
    lblTestOutput.setText("Test:");
    lblTestOutput.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

    btnTestOutput.setText("Test");
    btnTestOutput.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnTestOutputActionPerformed(evt);
      }
    });

    btnStopOutput.setText("Stop");
    btnStopOutput.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnStopOutputActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout outputTestPanelLayout = new javax.swing.GroupLayout(outputTestPanel);
    outputTestPanel.setLayout(outputTestPanelLayout);
    outputTestPanelLayout.setHorizontalGroup(
      outputTestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(outputTestPanelLayout.createSequentialGroup()
        .addGap(0, 0, 0)
        .addComponent(lblTestOutput)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(btnTestOutput)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(btnStopOutput)
        .addContainerGap(98, Short.MAX_VALUE))
    );
    outputTestPanelLayout.setVerticalGroup(
      outputTestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(outputTestPanelLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(outputTestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(lblTestOutput)
          .addComponent(btnTestOutput)
          .addComponent(btnStopOutput))
        .addContainerGap(9, Short.MAX_VALUE))
    );

    inputTestPane.setMinimumSize(new java.awt.Dimension(60, 30));
    inputTestPane.setPreferredSize(new java.awt.Dimension(300, 0));

    lblTestInput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/free_creations/audioconfig/resources/microOff.png"))); // NOI18N
    lblTestInput.setLabelFor(btnTestInput);
    lblTestInput.setText("Test:");
    lblTestInput.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);

    btnTestInput.setText("Test");
    btnTestInput.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnTestInputActionPerformed(evt);
      }
    });

    btnStopInput.setText("Stop");
    btnStopInput.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnStopInputActionPerformed(evt);
      }
    });

    javax.swing.GroupLayout inputTestPaneLayout = new javax.swing.GroupLayout(inputTestPane);
    inputTestPane.setLayout(inputTestPaneLayout);
    inputTestPaneLayout.setHorizontalGroup(
      inputTestPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(inputTestPaneLayout.createSequentialGroup()
        .addContainerGap()
        .addComponent(lblTestInput)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(btnTestInput)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(btnStopInput)
        .addContainerGap(92, Short.MAX_VALUE))
    );
    inputTestPaneLayout.setVerticalGroup(
      inputTestPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(inputTestPaneLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(inputTestPaneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(lblTestInput)
          .addComponent(btnTestInput)
          .addComponent(btnStopInput))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    errorMessagePage.setMinimumSize(new java.awt.Dimension(100, 48));
    errorMessagePage.setPreferredSize(new java.awt.Dimension(141, 48));

    lblErrorMessages.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/free_creations/audioconfig/resources/warning32.png"))); // NOI18N
    lblErrorMessages.setText("Messages");

    javax.swing.GroupLayout errorMessagePageLayout = new javax.swing.GroupLayout(errorMessagePage);
    errorMessagePage.setLayout(errorMessagePageLayout);
    errorMessagePageLayout.setHorizontalGroup(
      errorMessagePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, errorMessagePageLayout.createSequentialGroup()
        .addContainerGap()
        .addComponent(lblErrorMessages)
        .addContainerGap(511, Short.MAX_VALUE))
    );
    errorMessagePageLayout.setVerticalGroup(
      errorMessagePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(errorMessagePageLayout.createSequentialGroup()
        .addContainerGap()
        .addComponent(lblErrorMessages)
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(errorMessagePage, javax.swing.GroupLayout.DEFAULT_SIZE, 616, Short.MAX_VALUE)
          .addGroup(layout.createSequentialGroup()
            .addComponent(lblAdioSystem)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
            .addComponent(cbxAudioSystem, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
          .addComponent(audioSystemPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addGroup(layout.createSequentialGroup()
            .addComponent(outputTestPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(inputTestPane, javax.swing.GroupLayout.DEFAULT_SIZE, 305, Short.MAX_VALUE)))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(cbxAudioSystem, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(lblAdioSystem))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(audioSystemPane, javax.swing.GroupLayout.PREFERRED_SIZE, 185, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(outputTestPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE)
          .addComponent(inputTestPane, javax.swing.GroupLayout.DEFAULT_SIZE, 63, Short.MAX_VALUE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(errorMessagePage, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap())
    );
  }// </editor-fold>//GEN-END:initComponents

  private void cbxAudioSystemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxAudioSystemActionPerformed
    stopTesting();
    int apiIdx = cbxAudioSystem.getSelectedIndex();
    if (apiIdx < 0) {
      return;
    }
    if (apiIdx >= architecurePanes.length) {
      return;
    }
    for (AudioArchitecturePane pane : architecurePanes) {
      pane.setVisible(false);
    }
    architecurePanes[apiIdx].setVisible(true);
    audioSystemPane.moveToFront(architecurePanes[apiIdx]);

    boolean configOK = architecurePanes[apiIdx].hasConfiguration();
    btnTestOutput.setEnabled(configOK);
    lblTestOutput.setEnabled(configOK);
    btnStopOutput.setEnabled(configOK);

    btnTestInput.setEnabled(configOK);
    lblTestInput.setEnabled(configOK);
    btnStopInput.setEnabled(configOK);

  }//GEN-LAST:event_cbxAudioSystemActionPerformed

  private void btnTestOutputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTestOutputActionPerformed
    logger.log(Level.INFO, "Test output");
    clearMessage();
    if (sineGenerator != null) {
      setWarningMessage("Please stop the running Output test.");
      logger.log(Level.WARNING, "sineGenerator is not null");
      return;
    }
    if (recorder != null) {
      setWarningMessage("Please stop the running Input test.");
      logger.log(Level.WARNING, "recorder is not null");
      return;
    }

    if (audioSystemInfo == null) {
      setAlertMessage("There is a severe problem (audioSystemInfo is null).");
      logger.log(Level.WARNING, "audioSystemInfo is null");
      return;
    }

    StoredConfig config = getConfig();
    if (config == null) {
      setAlertMessage("There is a severe problem (config is null).");
      logger.log(Level.WARNING, "config is null");
      return;
    }
    ConfigRecord runnigConfig = config.match(audioSystemInfo);
    if (runnigConfig == null) {
      setAlertMessage("There is a severe problem (runnigConfig is null).");
      logger.log(Level.WARNING, "runnigConfig is null");
      return;
    }
    StreamParameters outputParameters = runnigConfig.getOutputParameters();
    if (outputParameters == null) {
      setWarningMessage("The Output device is not valid.");
      logger.log(Level.WARNING, "outputParameters are null");
      return;
    }
    sineGenerator = new SineGenerator(runnigConfig);
    animationTimer = new Timer(300, new HeadphoneUpdater());
    animationTimer.start();
    btnTestOutput.setEnabled(false);
    btnTestInput.setEnabled(false);

  }//GEN-LAST:event_btnTestOutputActionPerformed

  @Override
  public void subpaneChanged() {
    clearMessage();
    stopTesting();
  }

  private void stopTesting() {
    if (sineGenerator != null) {
      sineGenerator.stopPlaying();
    }
    if (recorder != null) {
      recorder.stopRecording();
    }
    if (animationTimer != null) {
      animationTimer.stop();
    }
    sineGenerator = null;
    recorder = null;
    animationTimer = null;

    lblTestInput.setIcon(micro_off);
    lblTestOutput.setIcon(phone_animated[0]);
    btnTestInput.setEnabled(true);
    btnTestOutput.setEnabled(true);

  }

  private void btnStopOutputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStopOutputActionPerformed
    stopTesting();
  }//GEN-LAST:event_btnStopOutputActionPerformed

  private void btnTestInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTestInputActionPerformed
    clearMessage();
    logger.log(Level.INFO, "Test Input");
    if (sineGenerator != null) {
      setWarningMessage("Please stop the running Output test.");
      logger.log(Level.WARNING, "sineGenerator is not null");
      return;
    }
    if (recorder != null) {
      setWarningMessage("Please stop the running Input test.");
      logger.log(Level.WARNING, "recorder is not null");
      return;
    }

    if (audioSystemInfo == null) {
      setAlertMessage("There is a severe problem (audioSystemInfo is null).");
      logger.log(Level.WARNING, "audioSystemInfo is null");
      return;
    }

    StoredConfig config = getConfig();
    if (config == null) {
      setAlertMessage("There is a severe problem (config is null).");
      logger.log(Level.WARNING, "config is null");
      return;
    }
    ConfigRecord runnigConfig = config.match(audioSystemInfo);
    if (runnigConfig == null) {
      setAlertMessage("There is a severe problem (runnigConfig is null).");
      logger.log(Level.WARNING, "runnigConfig is null");
      return;
    }
    StreamParameters outputParameters = runnigConfig.getOutputParameters();
    StreamParameters inputParameters = runnigConfig.getInputParameters();
    if (outputParameters == null) {
      setWarningMessage("The Output device is not valid.");
      logger.log(Level.WARNING, "outputParameters are null");
      return;
    }
    if (inputParameters == null) {
      setWarningMessage("The Input device is not valid.");
      logger.log(Level.WARNING, "inputParameters are null");
      return;
    }
    recorder = new Recorder(runnigConfig);
    animationTimer = new Timer(125, new MicrophoneUpdater(recorder));
    animationTimer.start();
    btnTestInput.setEnabled(false);
    btnTestOutput.setEnabled(false);

  }//GEN-LAST:event_btnTestInputActionPerformed

  private void btnStopInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStopInputActionPerformed
    stopTesting();
  }//GEN-LAST:event_btnStopInputActionPerformed
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JLayeredPane audioSystemPane;
  private javax.swing.JButton btnStopInput;
  private javax.swing.JButton btnStopOutput;
  private javax.swing.JButton btnTestInput;
  private javax.swing.JButton btnTestOutput;
  private javax.swing.JComboBox cbxAudioSystem;
  private de.free_creations.audioconfig.AudioArchitecturePane defaultSystemPanel;
  private javax.swing.JPanel errorMessagePage;
  private javax.swing.JPanel inputTestPane;
  private javax.swing.JLabel lblAdioSystem;
  private javax.swing.JLabel lblErrorMessages;
  private javax.swing.JLabel lblTestInput;
  private javax.swing.JLabel lblTestOutput;
  private javax.swing.JPanel outputTestPanel;
  // End of variables declaration//GEN-END:variables
}
