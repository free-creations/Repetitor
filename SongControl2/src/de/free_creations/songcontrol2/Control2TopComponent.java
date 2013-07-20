/*
 * Copyright 2012 harald.
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
package de.free_creations.songcontrol2;

import Wii4Java.Manager;
import Wii4Java.WiiListener;
import de.free_creations.guicomponents.SliderVuMeter;
import de.free_creations.guicomponents.SongTopComponent;
import de.free_creations.midisong.*;
import de.free_creations.midisong.GenericTrack.EventHandler;
import de.free_creations.midiutil.RPositionEx;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//de.free_creations.songcontrol2//Control2//EN",
        autostore = false)
@TopComponent.Description(preferredID = "Control2TopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "output", openAtStartup = true)
@ActionID(category = "Window", id = "de.free_creations.songcontrol2.Control2TopComponent")
@ActionReference(path = "Menu/Window" /*
         * , position = 333
         */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_Control2Action",
        preferredID = "Control2TopComponent")
@Messages({
  "CTL_Control2Action=Control",
  "CTL_Control2TopComponent=Control Window", //  "HINT_Control2TopComponent="
})
public final class Control2TopComponent extends SongTopComponent {

  /**
   * Internal identifier for the MS-Windows operating system.
   */
  private static final String WIN = "win";
  /**
   * Internal identifier for the Linux operating system.
   */
  private static final String LINUX = "linux";
  /**
   * Internal identifier for the Mac OS X operating system.
   */
  private static final String MAC = "mac";
  static final private Logger logger = Logger.getLogger(Control2TopComponent.class.getName());
  static final private String disabledDisplayName = "...";
  static final private String noVoice = "...";
  private volatile SongSession activeSongSession = null;
  private static final int pollingDelay = 200;
  private static final int doubleClickDelay = 500;
  private BuiltinSynthesizer oSynth = null;
  private BuiltinSynthesizer vSynth = null;
  private MidiSynthesizerTrack orchestraTrack;
  private volatile boolean wiiConnected = false;
  private volatile boolean feedbackOn = true;
  private volatile int feedbackOnAttenuation = -10;
  private boolean btnAdown = false;
  private final WiiListener wiiListener = new WiiListener() {
    @Override
    public void connectionChanged(int status) {
      switch (status) {
        case WiiListener.ABORTED:
          lblWiiMessage.setText("<html>Connection broken.</html>");
          wiiConnected = false;
          return;
        case WiiListener.CONNECTED:
          lblWiiMessage.setText("<html>Wii is connected.</html>");
          btnWii.setText("Disconnect Wii");
          wiiConnected = true;
          return;
        case WiiListener.ENDED:
          lblWiiMessage.setText("<html>Connection terminated.</html>");
          btnWii.setText("Wii");
          wiiConnected = false;
          return;
        default:
          lblWiiMessage.setText("");
      }
    }

    @Override
    public void buttonAChanged(boolean down) {
      btnAdown = down;
    }

    @Override
    public void buttonBChanged(boolean down) {
      if (down) {
        if (doubleClickTimer.isRunning()) {
          // this is the second click of a double click
          doubleClickTimer.stop();
          if (activeSongSession == null) {
            return;
          }
          if (!activeSongSession.isPlaying()) {
            putStartPointToCurrentPosition();
          }

        } else {
          // this is the first click, a second click might follow this one.
          if (activeSongSession.isPlaying()) {
            activeSongSession.setPlaying(false);
            doubleClickTimer.stop();
          } else {
            // We start the timer, if the delay elapses without a second click
            // we will execute a single click (see singleClickTask)
            doubleClickTimer.setRepeats(false);
            if (btnAdown) {
              doubleClickTimer.setActionCommand("RecordAudio");
            } else {
              doubleClickTimer.setActionCommand("PlayAudio");
            }
            doubleClickTimer.start();
          }
        }
      }
    }

    @Override
    public void buttonEvent(int i, int i1) {
    }
  };
  private final ActionListener pollingTask = new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
      //Control2TopComponent.this.updateCursorPos();
      Control2TopComponent.this.updateVuMeters();
    }
  };
  /**
   * This task is executed if after a first click no second click follows.
   */
  private final ActionListener singleClickTask = new ActionListener() {
    @Override
    public void actionPerformed(ActionEvent e) {
      if (activeSongSession == null) {
        return;
      }
      if (activeSongSession.isPlaying()) {
        // this should never happen! Just ignore it.
        return;
      }
      String command = e.getActionCommand();
      if (command != null) {
        activeSongSession.setPlayingModeStr(command);
      }
      activeSongSession.setPlaying(true);
    }
  };
  private final Timer pollTimer = new Timer(pollingDelay, pollingTask);
  private final Timer doubleClickTimer = new Timer(doubleClickDelay, singleClickTask);
  private MidiSynthesizerTrack voicesTrack;
  private GenericTrack[] voicesSubTracks;

  private static class TrackAttenuationConnector implements EventHandler {

    private final SliderVuMeter slider;

    public TrackAttenuationConnector(SliderVuMeter slider) {
      this.slider = slider;
    }

    @Override
    public void onMuteChange(boolean value) {
    }

    @Override
    public void onAttenuationChange(float value) {
      slider.setValue((int) value);
    }
  }

  private static class TrackMuteConnector implements EventHandler {

    private final JCheckBox checkbox;

    public TrackMuteConnector(JCheckBox checkbox) {

      this.checkbox = checkbox;
    }

    @Override
    public void onMuteChange(boolean value) {
      checkbox.setSelected(!value);
    }

    @Override
    public void onAttenuationChange(float value) {
    }
  }

  @SuppressWarnings("LeakingThisInConstructor")
  public Control2TopComponent() {
    initComponents();
    setName(Bundle.CTL_Control2TopComponent());
    //setToolTipText(Bundle.HINT_Control2TopComponent());
    putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);
    putClientProperty(TopComponent.PROP_DRAGGING_DISABLED, Boolean.TRUE);
    putClientProperty(TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE);
    putClientProperty(TopComponent.PROP_UNDOCKING_DISABLED, Boolean.TRUE);
    setDisplayName(disabledDisplayName);
    setEnabledOnComponentAndChildrenEx(this, false);
    sliderOrchestra.setVuValue(sliderOrchestra.getMinVuValue());
    sliderVoices.setVuValue(sliderOrchestra.getMinVuValue());
    sliderFeedback.setVuValue(sliderFeedback.getMinVuValue());
    sliderFeedback.setValue(-10);
    lblWiiMessage.setText("");

  }

  /**
   * Overwrites the inherited version of setEnabledOnComponentAndChildren in
   * order to leave the Wii controls always enabled
   *
   * @param component
   * @param enabled
   */
  private void setEnabledOnComponentAndChildrenEx(JComponent component, boolean enabled) {
    super.setEnabledOnComponentAndChildren(component, enabled);
    // currently, the Wii stuff is only available in Linux
    if (isLinux()) {
      lblWiiMessage.setEnabled(true);
      btnWii.setEnabled(true);
    } else {
      // on windows we hide the Wii controlls to avoid confusing the user
      lblWiiMessage.setVisible(false);
      btnWii.setVisible(false);
    }
  }

  private void putStartPointToCurrentPosition() {

    if (activeSongSession == null) {
      return;
    }
    if (activeSongSession.isPlaying()) {
      activeSongSession.setPlaying(false);
    }

    double tickPosition = activeSongSession.getLastStopingTickPosition();
    RPositionEx currentRPos = activeSongSession.tickToRPositionEx(tickPosition);
    RPositionEx newRPos = new RPositionEx(currentRPos.getNumerator(),
            currentRPos.getDenominator(), currentRPos.getMeasure(), 0);
    long newPosition = (long) activeSongSession.beatPositionToTick(newRPos);
    activeSongSession.setStartPoint(newPosition);

  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    pnlSingstimmen = new javax.swing.JPanel();
    sliderVoices = new de.free_creations.guicomponents.SliderVuMeter();
    pnlVoices = new javax.swing.JPanel();
    voice3 = new javax.swing.JCheckBox();
    voice6 = new javax.swing.JCheckBox();
    voice2 = new javax.swing.JCheckBox();
    voice4 = new javax.swing.JCheckBox();
    voice1 = new javax.swing.JCheckBox();
    voice5 = new javax.swing.JCheckBox();
    jLabel2 = new javax.swing.JLabel();
    pnlOrchestra = new javax.swing.JPanel();
    sliderOrchestra = new de.free_creations.guicomponents.SliderVuMeter();
    jLabel1 = new javax.swing.JLabel();
    pnlFeedback = new javax.swing.JPanel();
    jPanel1 = new javax.swing.JPanel();
    sliderFeedback = new de.free_creations.guicomponents.SliderVuMeter();
    jLabel3 = new javax.swing.JLabel();
    btnWii = new javax.swing.JButton();
    lblWiiMessage = new javax.swing.JLabel();

    setLayout(new java.awt.GridBagLayout());

    pnlSingstimmen.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(Control2TopComponent.class, "Control2TopComponent.pnlSingstimmen.border.title"))); // NOI18N
    pnlSingstimmen.setPreferredSize(new java.awt.Dimension(362, 400));

    sliderVoices.setInverted(true);
    sliderVoices.setMaximum(50);
    sliderVoices.setMinimum(-10);
    sliderVoices.setOrientation(1);
    sliderVoices.setValue(0);
    sliderVoices.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        sliderVoicesStateChanged(evt);
      }
    });

    pnlVoices.setLayout(new java.awt.GridBagLayout());

    org.openide.awt.Mnemonics.setLocalizedText(voice3, org.openide.util.NbBundle.getMessage(Control2TopComponent.class, "Control2TopComponent.voice3.text")); // NOI18N
    voice3.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        voice3ActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 0.333;
    pnlVoices.add(voice3, gridBagConstraints);

    org.openide.awt.Mnemonics.setLocalizedText(voice6, org.openide.util.NbBundle.getMessage(Control2TopComponent.class, "Control2TopComponent.voice6.text")); // NOI18N
    voice6.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        voice6ActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 0.333;
    pnlVoices.add(voice6, gridBagConstraints);

    org.openide.awt.Mnemonics.setLocalizedText(voice2, org.openide.util.NbBundle.getMessage(Control2TopComponent.class, "Control2TopComponent.voice2.text")); // NOI18N
    voice2.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        voice2ActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 0.333;
    pnlVoices.add(voice2, gridBagConstraints);

    org.openide.awt.Mnemonics.setLocalizedText(voice4, org.openide.util.NbBundle.getMessage(Control2TopComponent.class, "Control2TopComponent.voice4.text")); // NOI18N
    voice4.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        voice4ActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 0.333;
    pnlVoices.add(voice4, gridBagConstraints);

    org.openide.awt.Mnemonics.setLocalizedText(voice1, org.openide.util.NbBundle.getMessage(Control2TopComponent.class, "Control2TopComponent.voice1.text")); // NOI18N
    voice1.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        voice1ActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 0.333;
    pnlVoices.add(voice1, gridBagConstraints);

    org.openide.awt.Mnemonics.setLocalizedText(voice5, org.openide.util.NbBundle.getMessage(Control2TopComponent.class, "Control2TopComponent.voice5.text")); // NOI18N
    voice5.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        voice5ActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.weighty = 0.333;
    pnlVoices.add(voice5, gridBagConstraints);

    org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(Control2TopComponent.class, "Control2TopComponent.Volume")); // NOI18N

    javax.swing.GroupLayout pnlSingstimmenLayout = new javax.swing.GroupLayout(pnlSingstimmen);
    pnlSingstimmen.setLayout(pnlSingstimmenLayout);
    pnlSingstimmenLayout.setHorizontalGroup(
      pnlSingstimmenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(pnlSingstimmenLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(pnlSingstimmenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jLabel2)
          .addGroup(pnlSingstimmenLayout.createSequentialGroup()
            .addComponent(sliderVoices, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(pnlVoices, javax.swing.GroupLayout.PREFERRED_SIZE, 282, javax.swing.GroupLayout.PREFERRED_SIZE)))
        .addContainerGap())
    );
    pnlSingstimmenLayout.setVerticalGroup(
      pnlSingstimmenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(pnlSingstimmenLayout.createSequentialGroup()
        .addGroup(pnlSingstimmenLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addGroup(pnlSingstimmenLayout.createSequentialGroup()
            .addGap(21, 21, 21)
            .addComponent(pnlVoices, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
          .addGroup(pnlSingstimmenLayout.createSequentialGroup()
            .addComponent(jLabel2)
            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
            .addComponent(sliderVoices, javax.swing.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)))
        .addGap(0, 0, 0))
    );

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.ipady = 30;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weighty = 0.1;
    add(pnlSingstimmen, gridBagConstraints);

    pnlOrchestra.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(Control2TopComponent.class, "Control2TopComponent.pnlOrchestra.border.title"))); // NOI18N
    pnlOrchestra.setPreferredSize(new java.awt.Dimension(97, 400));

    sliderOrchestra.setInverted(true);
    sliderOrchestra.setMaximum(50);
    sliderOrchestra.setMinimum(-10);
    sliderOrchestra.setOrientation(1);
    sliderOrchestra.setValue(0);
    sliderOrchestra.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        sliderOrchestraStateChanged(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(Control2TopComponent.class, "Control2TopComponent.Volume")); // NOI18N

    javax.swing.GroupLayout pnlOrchestraLayout = new javax.swing.GroupLayout(pnlOrchestra);
    pnlOrchestra.setLayout(pnlOrchestraLayout);
    pnlOrchestraLayout.setHorizontalGroup(
      pnlOrchestraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(pnlOrchestraLayout.createSequentialGroup()
        .addContainerGap()
        .addGroup(pnlOrchestraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(sliderOrchestra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
          .addComponent(jLabel1))
        .addContainerGap(26, Short.MAX_VALUE))
    );
    pnlOrchestraLayout.setVerticalGroup(
      pnlOrchestraLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlOrchestraLayout.createSequentialGroup()
        .addComponent(jLabel1)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(sliderOrchestra, javax.swing.GroupLayout.DEFAULT_SIZE, 508, Short.MAX_VALUE)
        .addGap(0, 0, 0))
    );

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.ipady = 30;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.1;
    gridBagConstraints.weighty = 0.1;
    add(pnlOrchestra, gridBagConstraints);

    jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(org.openide.util.NbBundle.getMessage(Control2TopComponent.class, "Control2TopComponent.jPanel1.border.title"))); // NOI18N
    jPanel1.setPreferredSize(new java.awt.Dimension(179, 400));

    sliderFeedback.setInverted(true);
    sliderFeedback.setMaximum(60);
    sliderFeedback.setMinimum(-60);
    sliderFeedback.setOrientation(1);
    sliderFeedback.setValue(0);
    sliderFeedback.addChangeListener(new javax.swing.event.ChangeListener() {
      public void stateChanged(javax.swing.event.ChangeEvent evt) {
        sliderFeedbackStateChanged(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(Control2TopComponent.class, "Control2TopComponent.jLabel3.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(btnWii, org.openide.util.NbBundle.getMessage(Control2TopComponent.class, "Control2TopComponent.btnWii.text")); // NOI18N
    btnWii.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        btnWiiActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(lblWiiMessage, org.openide.util.NbBundle.getMessage(Control2TopComponent.class, "Control2TopComponent.lblWiiMessage.text")); // NOI18N
    lblWiiMessage.setVerticalAlignment(javax.swing.SwingConstants.TOP);

    javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
    jPanel1.setLayout(jPanel1Layout);
    jPanel1Layout.setHorizontalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jLabel3)
          .addComponent(sliderFeedback, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(btnWii, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
          .addGroup(jPanel1Layout.createSequentialGroup()
            .addGap(7, 7, 7)
            .addComponent(lblWiiMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
        .addContainerGap())
    );
    jPanel1Layout.setVerticalGroup(
      jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addContainerGap()
        .addComponent(btnWii)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addComponent(lblWiiMessage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        .addContainerGap(360, Short.MAX_VALUE))
      .addGroup(jPanel1Layout.createSequentialGroup()
        .addComponent(jLabel3)
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addComponent(sliderFeedback, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );

    javax.swing.GroupLayout pnlFeedbackLayout = new javax.swing.GroupLayout(pnlFeedback);
    pnlFeedback.setLayout(pnlFeedbackLayout);
    pnlFeedbackLayout.setHorizontalGroup(
      pnlFeedbackLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(pnlFeedbackLayout.createSequentialGroup()
        .addGap(0, 0, 0)
        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 183, Short.MAX_VALUE)
        .addContainerGap())
    );
    pnlFeedbackLayout.setVerticalGroup(
      pnlFeedbackLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlFeedbackLayout.createSequentialGroup()
        .addGap(0, 0, 0)
        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 550, Short.MAX_VALUE)
        .addGap(0, 0, 0))
    );

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.ipady = 30;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
    gridBagConstraints.weightx = 0.2;
    gridBagConstraints.weighty = 0.1;
    add(pnlFeedback, gridBagConstraints);
    pnlFeedback.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(Control2TopComponent.class, "Control2TopComponent.pnlFeedback.AccessibleContext.accessibleName")); // NOI18N
  }// </editor-fold>//GEN-END:initComponents

  private void sliderVoicesStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderVoicesStateChanged
    if (voicesTrack != null) {
      voicesTrack.setAttenuation(sliderVoices.getValue());
    }
  }//GEN-LAST:event_sliderVoicesStateChanged

  private void sliderOrchestraStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderOrchestraStateChanged
    if (orchestraTrack != null) {
      orchestraTrack.setAttenuation(sliderOrchestra.getValue());
    }
  }//GEN-LAST:event_sliderOrchestraStateChanged

  private void voice1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_voice1ActionPerformed
    if (voicesSubTracks.length > 0) {
      voicesSubTracks[0].setMute(!voice1.isSelected());
    }
  }//GEN-LAST:event_voice1ActionPerformed

  private void voice2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_voice2ActionPerformed
    if (voicesSubTracks.length > 1) {
      voicesSubTracks[1].setMute(!voice2.isSelected());
    }
  }//GEN-LAST:event_voice2ActionPerformed

  private void voice3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_voice3ActionPerformed
    if (voicesSubTracks.length > 2) {
      voicesSubTracks[2].setMute(!voice3.isSelected());
    }
  }//GEN-LAST:event_voice3ActionPerformed

  private void voice4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_voice4ActionPerformed
    if (voicesSubTracks.length > 3) {
      voicesSubTracks[3].setMute(!voice4.isSelected());
    }
  }//GEN-LAST:event_voice4ActionPerformed

  private void voice5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_voice5ActionPerformed
    if (voicesSubTracks.length > 4) {
      voicesSubTracks[4].setMute(!voice5.isSelected());
    }
  }//GEN-LAST:event_voice5ActionPerformed

  private void voice6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_voice6ActionPerformed
    if (voicesSubTracks.length > 5) {
      voicesSubTracks[5].setMute(!voice6.isSelected());
    }
  }//GEN-LAST:event_voice6ActionPerformed

  private void sliderFeedbackStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderFeedbackStateChanged
    if (activeSongSession != null) {
      activeSongSession.setAudioAttenuation(sliderFeedback.getValue());
    }
  }//GEN-LAST:event_sliderFeedbackStateChanged

  private void btnWiiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnWiiActionPerformed

    if (!wiiConnected) {
      lblWiiMessage.setText("<html>Connecting to the Wii controller.<p><em>Press Buttons 1 and 2...</em></p></html>");
      Manager.connect(wiiListener);
    } else {
      lblWiiMessage.setText("");
      Manager.disconnect(wiiListener);

    }
  }//GEN-LAST:event_btnWiiActionPerformed
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton btnWii;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JPanel jPanel1;
  private javax.swing.JLabel lblWiiMessage;
  private javax.swing.JPanel pnlFeedback;
  private javax.swing.JPanel pnlOrchestra;
  private javax.swing.JPanel pnlSingstimmen;
  private javax.swing.JPanel pnlVoices;
  private de.free_creations.guicomponents.SliderVuMeter sliderFeedback;
  private de.free_creations.guicomponents.SliderVuMeter sliderOrchestra;
  private de.free_creations.guicomponents.SliderVuMeter sliderVoices;
  private javax.swing.JCheckBox voice1;
  private javax.swing.JCheckBox voice2;
  private javax.swing.JCheckBox voice3;
  private javax.swing.JCheckBox voice4;
  private javax.swing.JCheckBox voice5;
  private javax.swing.JCheckBox voice6;
  // End of variables declaration//GEN-END:variables

  @Override
  public void componentOpened() {
    super.componentOpened();
  }

  @Override
  public void componentClosed() {
    super.componentClosed();
  }
  static final String PROP_FEEDBACK_ATTENUATION = "feedbackAttenuation";

  void writeProperties(java.util.Properties p) {
    // better to version settings since initial version as advocated at
    // http://wiki.apidesign.org/wiki/PropertyFiles
    p.setProperty("version", "1.0");
    p.setProperty("PROP_FEEDBACK_ATTENUATION", Integer.toString(sliderFeedback.getValue()));

  }

  void readProperties(java.util.Properties p) {
    String version = p.getProperty("version");
    int feedbackAttenuation;
    String feedbackAttenuationStr = p.getProperty("PROP_FEEDBACK_ATTENUATION", "-10");
    try {
      feedbackAttenuation = Integer.parseInt(feedbackAttenuationStr);
    } catch (Throwable ignored) {
      feedbackAttenuation = -10;
    }
    sliderFeedback.setValue(feedbackAttenuation);
  }

  /**
   * This method must react when an other song becomes the active song. It is
   * guaranteed that this method is invoked in the AWT thread.
   *
   * @param oldSession the previous session or null
   * @param newSession the current session or null
   */
  @Override
  protected void songSessionChanged(SongSession oldSession, SongSession newSession) {
    activeSongSession = newSession;
    if (newSession == null) {
      setDisplayName(disabledDisplayName);
      setEnabledOnComponentAndChildrenEx(this, false);
    } else {
      setDisplayName(newSession.getName());
      setEnabledOnComponentAndChildrenEx(this, true);
      attachTracks();
    }
  }

  /**
   * This method must react when a property of the active song has changed. It
   * is guaranteed that this method is invoked in the AWT thread.
   *
   * @param session the currently active session
   * @param evt the property change event
   */
  @Override
  protected void songPropertyChange(SongSession session, PropertyChangeEvent evt) {
    String prop = evt.getPropertyName();
    if (SongSession.PROP_STARTPOINT.equals(prop)) {
      updateStartpoint(session, evt.getNewValue());
    } else if (SongSession.PROP_PLAYING.equals(prop)) {
      updatePlaying(session, evt.getNewValue());
    } else if (SongSession.PROP_LOOPSTARTPOINT.equals(prop)) {
      updateLoopStartpoint(evt.getNewValue());
    } else if (SongSession.PROP_LOOPENDPOINT.equals(prop)) {
      updateLoopEndpoint(evt.getNewValue());
    } else if (SongSession.PROP_LOOPING.equals(prop)) {
      updateLoopStatus(evt.getNewValue());

    }
  }

  private void updateStartpoint(SongSession session, Object newValue) {
  }

  private void updateLoopStartpoint(Object newValue) {
    //throw new UnsupportedOperationException("Not yet implemented");
  }

  private void updateLoopEndpoint(Object newValue) {
    //throw new UnsupportedOperationException("Not yet implemented");
  }

  private void updateLoopStatus(Object newValue) {
    //throw new UnsupportedOperationException("Not yet implemented");
  }

  private void updateVuMeters() {
    if (oSynth != null) {
      sliderOrchestra.setVuValue((int) oSynth.getVuLevel(0));
    }
    if (vSynth != null) {
      sliderVoices.setVuValue((int) vSynth.getVuLevel(0));
    }
    if (activeSongSession != null) {
      sliderFeedback.setVuValue((int) activeSongSession.getAudioVuLevel());
    }
  }

  private void updatePlaying(SongSession session, Object newValue) {
    if (newValue instanceof Boolean) {
      boolean isPlaying = (Boolean) newValue;
      if (isPlaying) {
        pollTimer.start();
        disableVoiceSelection();


      } else {
        pollTimer.stop();
        sliderOrchestra.setVuValue(sliderOrchestra.getMinVuValue());
        sliderVoices.setVuValue(sliderVoices.getMinVuValue());
        sliderFeedback.setVuValue(sliderFeedback.getMinVuValue());
        enableVoiceSelection();
      }
    }
  }

  private void attachTracks() {
    if (activeSongSession == null) {
      return;
    }
    if (!SwingUtilities.isEventDispatchThread()) {
      throw new RuntimeException("Must be called from EventDispatchThread.");
    }
    //climb down the hierarchy and verify that all elements are at their expected position
    Song activeSong = activeSongSession.getActiveSong();
    MasterTrack mastertrack = activeSong.getMastertrack();

    // hack to make all songs play in mode "PlayRecordAudio"
    activeSongSession.setPlayingModeStr("PlayRecordAudio");


    orchestraTrack = (MidiSynthesizerTrack) mastertrack.getSubtracks()[0];
    sliderOrchestra.setValue((int) orchestraTrack.getAttenuation());
    orchestraTrack.addNonAudioEventHandler(new TrackAttenuationConnector(sliderOrchestra));

    voicesTrack = (MidiSynthesizerTrack) mastertrack.getSubtracks()[1];
    sliderVoices.setValue((int) voicesTrack.getAttenuation());
    voicesTrack.addNonAudioEventHandler(new TrackAttenuationConnector(sliderVoices));

    voicesSubTracks = voicesTrack.getSubtracks();

    // collect the tracks that shall be displayed in the Song view window

    if (voicesSubTracks.length > 0) {
      voice1.setSelected(true);
      voice1.setText(voicesSubTracks[0].getName());
      voice1.setSelected(!voicesSubTracks[0].isMute());
      voicesSubTracks[0].addNonAudioEventHandler(new TrackMuteConnector(voice1));
      voice1.setEnabled(true);
    } else {
      voice1.setText(noVoice);
      voice1.setEnabled(false);
    }
    if (voicesSubTracks.length > 1) {
      voice2.setText(voicesSubTracks[1].getName());
      voice2.setSelected(!voicesSubTracks[1].isMute());
      voicesSubTracks[1].addNonAudioEventHandler(new TrackMuteConnector(voice2));
      voice2.setEnabled(true);
    } else {
      voice2.setText(noVoice);
      voice2.setEnabled(false);
    }
    if (voicesSubTracks.length > 2) {
      voice3.setText(voicesSubTracks[2].getName());
      voice3.setSelected(!voicesSubTracks[2].isMute());
      voicesSubTracks[2].addNonAudioEventHandler(new TrackMuteConnector(voice3));
      voice3.setEnabled(true);
    } else {
      voice3.setText(noVoice);
      voice3.setEnabled(false);
    }

    if (voicesSubTracks.length > 3) {
      voice4.setText(voicesSubTracks[3].getName());
      voice4.setSelected(!voicesSubTracks[3].isMute());
      voicesSubTracks[3].addNonAudioEventHandler(new TrackMuteConnector(voice4));
      voice4.setEnabled(true);
    } else {
      voice4.setText(noVoice);
      voice4.setEnabled(false);
    }
    if (voicesSubTracks.length > 4) {
      voice5.setText(voicesSubTracks[4].getName());
      voice5.setSelected(!voicesSubTracks[4].isMute());
      voicesSubTracks[4].addNonAudioEventHandler(new TrackMuteConnector(voice5));
      voice5.setEnabled(true);
    } else {
      voice5.setText(noVoice);
      voice5.setEnabled(false);
    }
    if (voicesSubTracks.length > 5) {
      voice6.setText(voicesSubTracks[5].getName());
      voice6.setSelected(!voicesSubTracks[5].isMute());
      voicesSubTracks[5].addNonAudioEventHandler(new TrackMuteConnector(voice6));
      voice6.setEnabled(true);
    } else {
      voice6.setText(noVoice);
      voice6.setEnabled(false);
    }
    oSynth = (BuiltinSynthesizer) orchestraTrack.getSynthesizer();
    vSynth = (BuiltinSynthesizer) voicesTrack.getSynthesizer();

    activeSongSession.setLooping(true);
    activeSongSession.setAudioAttenuation(sliderFeedback.getValue());

  }

  private void disableVoiceSelection() {

    voice1.setEnabled(false);
    voice2.setEnabled(false);
    voice3.setEnabled(false);
    voice4.setEnabled(false);
    voice5.setEnabled(false);
    voice6.setEnabled(false);
  }

  private void enableVoiceSelection() {
    //this is a hack. To indicate that
    //currently voices cannot be changed whenn playing

    if (voicesSubTracks.length > 0) {
      voice1.setText(voicesSubTracks[0].getName());
      voice1.setSelected(!voicesSubTracks[0].isMute());
      voice1.setEnabled(true);
    } else {
      voice1.setText(noVoice);
      voice1.setEnabled(false);
    }
    if (voicesSubTracks.length > 1) {
      voice2.setText(voicesSubTracks[1].getName());
      voice2.setSelected(!voicesSubTracks[1].isMute());
      voice2.setEnabled(true);
    } else {
      voice2.setText(noVoice);
      voice2.setEnabled(false);
    }
    if (voicesSubTracks.length > 2) {
      voice3.setText(voicesSubTracks[2].getName());
      voice3.setSelected(!voicesSubTracks[2].isMute());
      voice3.setEnabled(true);
    } else {
      voice3.setText(noVoice);
      voice3.setEnabled(false);
    }

    if (voicesSubTracks.length > 3) {
      voice4.setText(voicesSubTracks[3].getName());
      voice4.setSelected(!voicesSubTracks[3].isMute());
      voice4.setEnabled(true);
    } else {
      voice4.setText(noVoice);
      voice4.setEnabled(false);
    }
    if (voicesSubTracks.length > 4) {
      voice5.setText(voicesSubTracks[4].getName());
      voice5.setSelected(!voicesSubTracks[4].isMute());
      voice5.setEnabled(true);
    } else {
      voice5.setText(noVoice);
      voice5.setEnabled(false);
    }
    if (voicesSubTracks.length > 5) {
      voice6.setText(voicesSubTracks[5].getName());
      voice6.setSelected(!voicesSubTracks[5].isMute());
      voice6.setEnabled(true);
    } else {
      voice6.setText(noVoice);
      voice6.setEnabled(false);
    }

  }

  private static boolean isLinux() {
    return (getOsName() == null ? LINUX == null : getOsName().equals(LINUX));
  }

  /**
   * Determine the name of the operating system.
   *
   * @return either "win" or "linux" or "mac".
   */
  private static String getOsName() {
    String osName = System.getProperty("os.name").toLowerCase();
    if (osName.indexOf("windows") > -1) {
      return WIN;
    }
    if (osName.indexOf("linux") > -1) {
      return LINUX;
    }
    if (osName.indexOf("mac") > -1) {
      return MAC;
    }
    throw new RuntimeException("Unsupported Operating System \"" + System.getProperty("os.name") + "\".");
  }
}
