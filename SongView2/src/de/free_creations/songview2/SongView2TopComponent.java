/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.free_creations.songview2;

import de.free_creations.guicomponents.SongTopComponent;
import de.free_creations.guicomponents.StartStopButton;
import de.free_creations.midisong.*;
import de.free_creations.midiutil.RPositionEx;
import de.free_creations.netBeansSong.SongDataSupport;
import de.free_creations.netBeansSong.SongSessionManager;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.Track;
import javax.swing.SwingUtilities;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.Exceptions;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//org.myorg.songview//SongView//EN", autostore = false)
@TopComponent.Description(preferredID = SongView2TopComponent.PREFERRED_ID,
iconBase = "de/free_creations/songview/artwork/noteSheet.png",
//persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED)
persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "editor", openAtStartup = false, position = 100)
public class SongView2TopComponent extends SongTopComponent {

  public static final String PREFERRED_ID = "SongView2TopComponent";
  static final private Logger logger = Logger.getLogger(SongView2TopComponent.class.getName());
  // private WaitPanel waitPanel = new WaitPanel();
  private SongDataSupport songDataSupport;
  private volatile SongSession session = null;
  private volatile boolean sessionIsActive = false;
  /**
   * This class shall be attached to the songDataSupport-object and shall be
   * informed when the songDataSupport-object has opened the song session.
   */
  private final PropertyChangeListener songDataSupportListener =
          new PropertyChangeListener() {

            /**
             * this function will be triggered when songDataSupport-object has
             * finished loading the song-session.
             */
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
              if (SongDataSupport.PROP_SESSION.equals(pce.getPropertyName())) {
                try {
                  songDataSupport.setOpen(true);
                  session = songDataSupport.getSession();
                  assert (session != null);
                  setDisplayName(session.getName());
                  setPreferedSession(session);
                  attachRequiemSong(session);
                  SongSessionManager.activate(session);
//                  waitPanel.setMessage("Connecting to Soundcard...");
                } catch (EInvalidSongFile ex) {
                  Exceptions.printStackTrace(ex);
                } catch (InterruptedException ex) {
                  Exceptions.printStackTrace(ex);
                } catch (ExecutionException ex) {
                  Exceptions.printStackTrace(ex);
                } catch (TimeoutException ex) {
                  Exceptions.printStackTrace(ex);
                }
              }
            }
          };

  /**
   * Creates new form SongViewTopComponent and connects to the given song.
   *
   * @param songData the song that this window should display.
   */
  SongView2TopComponent(final SongDataSupport songData) {
    this();
    if (songData != null) {
      initData(songData);
    }
  }

  /**
   * This no-argument constructor is used by the auto-restore feature.
   */
  public SongView2TopComponent() {
    initComponents();
    setEnabledOnComponentAndChildren(controlContainer, false);
    scrollBarHorizontal.setModel(songPanel.getHorizontalScrollModel());
    setName("Song View");
    setToolTipText(null);
//**    add(waitPanel);
//**    setComponentZOrder(waitPanel, 0);
//**    waitPanel.setBounds(0, 0, 752, 304);
    //**   waitPanel.setVisible(true);
    setEnabledOnComponentAndChildren(controlContainer, false);
    addComponentListener(new ComponentListener() {

      @Override
      public void componentResized(ComponentEvent e) {
//**        waitPanel.setBounds(0, 0, getWidth(), getHeight());
      }

      @Override
      public void componentMoved(ComponentEvent e) {
      }

      @Override
      public void componentShown(ComponentEvent e) {
      }

      @Override
      public void componentHidden(ComponentEvent e) {
      }
    });
  }

  /**
   * A special version of attachSong used for a pre-alpha version that could
   * only play "Faure Requiem Opus 48".
   *
   * @deprecated
   */
  @Deprecated
  private void attachRequiemSong(SongSession session) throws EInvalidSongFile {

    if (!SwingUtilities.isEventDispatchThread()) {
      throw new RuntimeException("Must be called from EventDispatchThread.");
    }

    Song activeSong = session.getActiveSong();

    //climb down the hierarchy and collect all elements that shall be displayed
    MasterTrack mastertrack = activeSong.getMastertrack();
    int resolution = mastertrack.getSequence().getResolution();


    MidiSynthesizerTrack voicesTrack = (MidiSynthesizerTrack) mastertrack.getSubtracks()[1];

    GenericTrack[] voicesSubTracks = voicesTrack.getSubtracks();

    // collect the tracks that shall be displayed in the Song view window
    Track[] displayTracks = new Track[voicesSubTracks.length + 1];
    GenericTrack[] tracksDescriptions = new GenericTrack[voicesSubTracks.length + 1];
    displayTracks[0] = mastertrack.getMidiTrack();
    tracksDescriptions[0] = null;
    int index = 1;
    for (GenericTrack voiceTrack : voicesSubTracks) {
      if (index < displayTracks.length) {
        MidiTrack midiTrack = (MidiTrack) voiceTrack;


        displayTracks[index] = midiTrack.getMidiTrack();
        tracksDescriptions[index] = voiceTrack;
      }
      index++;
    }
    songPanel.setRequiemTracks(displayTracks, resolution, tracksDescriptions);
    songPanel.connectSession(session);
//    int minimumPixelToMidiFactor = (int) (songPanel.getDefaultPixelToMidiFactor() / 5);
//    if (minimumPixelToMidiFactor < 1) {
//      minimumPixelToMidiFactor = 1;
//    }
//    sliderZoom.setMinimum(minimumPixelToMidiFactor);
//    sliderZoom.setMaximum((int) (songPanel.getDefaultPixelToMidiFactor() * 5));
//    sliderZoom.setValue((int) (songPanel.getDefaultPixelToMidiFactor()));

  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        trackViewContainer = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        songPanel = new de.free_creations.songview2.SongPanel();
        sliderZoom = new javax.swing.JSlider();
        scrollBarHorizontal = new javax.swing.JScrollBar();
        controlContainer = new javax.swing.JPanel();
        btnStartStop = new de.free_creations.guicomponents.StartStopButton();
        edStartpoint = new de.free_creations.guicomponents.RPositionSpinner();
        btnLoop = new javax.swing.JToggleButton();
        edLoopStart = new de.free_creations.guicomponents.RPositionSpinner();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        spacer = new javax.swing.JPanel();
        edLoopEnd = new de.free_creations.guicomponents.RPositionSpinner();

        setLayout(new java.awt.BorderLayout());

        trackViewContainer.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SongView2TopComponent.class, "SongView2TopComponent.jLabel2.text")); // NOI18N

        javax.swing.GroupLayout songPanelLayout = new javax.swing.GroupLayout(songPanel);
        songPanel.setLayout(songPanelLayout);
        songPanelLayout.setHorizontalGroup(
            songPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        songPanelLayout.setVerticalGroup(
            songPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 230, Short.MAX_VALUE)
        );

        sliderZoom.setMaximum(200);
        sliderZoom.setMinimum(5);
        sliderZoom.setValue(100);
        sliderZoom.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderZoomStateChanged(evt);
            }
        });

        scrollBarHorizontal.setOrientation(javax.swing.JScrollBar.HORIZONTAL);
        scrollBarHorizontal.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                scrollBarHorizontalMouseWheelMoved(evt);
            }
        });

        javax.swing.GroupLayout trackViewContainerLayout = new javax.swing.GroupLayout(trackViewContainer);
        trackViewContainer.setLayout(trackViewContainerLayout);
        trackViewContainerLayout.setHorizontalGroup(
            trackViewContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(trackViewContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollBarHorizontal, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sliderZoom, javax.swing.GroupLayout.PREFERRED_SIZE, 194, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(14, 14, 14))
            .addComponent(songPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        trackViewContainerLayout.setVerticalGroup(
            trackViewContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(trackViewContainerLayout.createSequentialGroup()
                .addComponent(songPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addGroup(trackViewContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollBarHorizontal, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sliderZoom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)))
        );

        add(trackViewContainer, java.awt.BorderLayout.CENTER);

        java.awt.GridBagLayout controlContainerLayout = new java.awt.GridBagLayout();
        controlContainerLayout.columnWidths = new int[] {0, 5, 0, 5, 0, 5, 0, 5, 0, 5, 0};
        controlContainerLayout.rowHeights = new int[] {0, 2, 0};
        controlContainer.setLayout(controlContainerLayout);

        btnStartStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartStopActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_END;
        controlContainer.add(btnStartStop, gridBagConstraints);

        edStartpoint.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                edStartpointStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 3, 1);
        controlContainer.add(edStartpoint, gridBagConstraints);

        btnLoop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/free_creations/songview2/resources/loop24.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(btnLoop, org.openide.util.NbBundle.getMessage(SongView2TopComponent.class, "SongView2TopComponent.btnLoop.text")); // NOI18N
        btnLoop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLoopActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.PAGE_END;
        controlContainer.add(btnLoop, gridBagConstraints);

        edLoopStart.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                edLoopStartStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 1);
        controlContainer.add(edLoopStart, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SongView2TopComponent.class, "SongView2TopComponent.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        controlContainer.add(jLabel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(SongView2TopComponent.class, "SongView2TopComponent.jLabel3.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        controlContainer.add(jLabel3, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(SongView2TopComponent.class, "SongView2TopComponent.jLabel4.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        controlContainer.add(jLabel4, gridBagConstraints);

        spacer.setPreferredSize(new java.awt.Dimension(20, 20));

        javax.swing.GroupLayout spacerLayout = new javax.swing.GroupLayout(spacer);
        spacer.setLayout(spacerLayout);
        spacerLayout.setHorizontalGroup(
            spacerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        spacerLayout.setVerticalGroup(
            spacerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.ipadx = 8;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        controlContainer.add(spacer, gridBagConstraints);

        edLoopEnd.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                edLoopEndStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 10;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.ipadx = 10;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 1, 0, 1);
        controlContainer.add(edLoopEnd, gridBagConstraints);

        add(controlContainer, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

  private void sliderZoomStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderZoomStateChanged
    if (sessionIsActive) {
      songPanel.setPixelToMidiFactor(sliderZoom.getValue());
    }
  }//GEN-LAST:event_sliderZoomStateChanged

  private void btnStartStopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartStopActionPerformed
    if (session != null) {
      switch (btnStartStop.getState()) {
        case STARTING:
          session.setPlaying(true);
          break;
        case STOPPING:
          session.setPlaying(false);
          break;
      }
    }
  }//GEN-LAST:event_btnStartStopActionPerformed

  private void edStartpointStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_edStartpointStateChanged
    if (session == null) {
      return;
    }
    double tickPos = session.beatPositionToTick(edStartpoint.getValue());
    if (tickPos > session.getTickLength()) {
      tickPos = session.getTickLength();
      // correct the displayed value
      edStartpoint.setValue(session.tickToRPositionEx(tickPos));
    }
    if (tickPos < 0D) {
      tickPos = 0;
      // correct the displayed value
      edStartpoint.setValue(session.tickToRPositionEx(tickPos));
    }
    session.setStartPoint(Math.round(tickPos));
  }//GEN-LAST:event_edStartpointStateChanged

  private void edLoopStartStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_edLoopStartStateChanged
    if (session == null) {
      return;
    }
    double tickPos = session.beatPositionToTick(edLoopStart.getValue());
    if (tickPos > session.getTickLength()) {
      tickPos = session.getTickLength();
      // correct the displayed value
      edLoopStart.setValue(session.tickToRPositionEx(tickPos));
    }
    if (tickPos < 0D) {
      tickPos = 0;
      // correct the displayed value
      edLoopStart.setValue(session.tickToRPositionEx(tickPos));
    }
    session.setLoopStartPoint(Math.round(tickPos));
  }//GEN-LAST:event_edLoopStartStateChanged

  private void edLoopEndStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_edLoopEndStateChanged
    if (session == null) {
      return;
    }
    if (!sessionIsActive) {
      return;
    }
    double tickPos = session.beatPositionToTick(edLoopEnd.getValue());
    if (tickPos > session.getTickLength()) {
      tickPos = session.getTickLength();
      // correct the displayed value
      edLoopEnd.setValue(session.tickToRPositionEx(tickPos));
    }
    if (tickPos < 0D) {
      tickPos = 0;
      // correct the displayed value
      edLoopEnd.setValue(session.tickToRPositionEx(tickPos));
    }
    session.setLoopEndPoint(Math.round(tickPos));
  }//GEN-LAST:event_edLoopEndStateChanged

  private void btnLoopActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLoopActionPerformed
    if (session != null) {
      session.setLooping(btnLoop.isSelected());
    }
  }//GEN-LAST:event_btnLoopActionPerformed

  private float mouseWheelFactor(int sliderMin, int sliderMax) {
    final float mouseWheelTicksFullRange = 16; //the number of ticks required to do a full range move
    float range = sliderMax - sliderMin;
    float result = range / mouseWheelTicksFullRange;
    if (result < 1.0F) {
      result = 1.0F;
    }
    return result;
  }

  private void scrollBarHorizontalMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_scrollBarHorizontalMouseWheelMoved
    int delta = (int) mouseWheelFactor(scrollBarHorizontal.getMinimum(), scrollBarHorizontal.getMaximum()) * evt.getWheelRotation();
    int newValue = scrollBarHorizontal.getValue() - delta;
    if (newValue < scrollBarHorizontal.getMinimum()) {
      newValue = scrollBarHorizontal.getMinimum();
    }
    if (newValue > scrollBarHorizontal.getMaximum()-scrollBarHorizontal.getVisibleAmount()) {
      newValue = scrollBarHorizontal.getMaximum()-scrollBarHorizontal.getVisibleAmount();
    }
    scrollBarHorizontal.setValue(newValue);
  }//GEN-LAST:event_scrollBarHorizontalMouseWheelMoved
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton btnLoop;
    private de.free_creations.guicomponents.StartStopButton btnStartStop;
    private javax.swing.JPanel controlContainer;
    private de.free_creations.guicomponents.RPositionSpinner edLoopEnd;
    private de.free_creations.guicomponents.RPositionSpinner edLoopStart;
    private de.free_creations.guicomponents.RPositionSpinner edStartpoint;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JScrollBar scrollBarHorizontal;
    private javax.swing.JSlider sliderZoom;
    private de.free_creations.songview2.SongPanel songPanel;
    private javax.swing.JPanel spacer;
    private javax.swing.JPanel trackViewContainer;
    // End of variables declaration//GEN-END:variables

  @Override
  public void componentOpened() {
    super.componentOpened();
    if (songDataSupport != null) {
      songDataSupport.setOpen(true);
    }
  }

  @Override
  public void componentClosed() {
    songDataSupport.removePropertyChangeListener(songDataSupportListener);
    SongSessionManager.deactivate(session);
    super.componentClosed();
  }

  void writeProperties(java.util.Properties p) {
    // better to version settings since initial version as advocated at
    // http://wiki.apidesign.org/wiki/PropertyFiles
    p.setProperty("version", "1.0");
    // TODO store your settings
  }

  void readProperties(java.util.Properties p) {
    String version = p.getProperty("version");
    // TODO read your settings according to their version
  }

  private void initData(SongDataSupport songData) {

    this.songDataSupport = songData;
    associateLookup(songData.getLookup());
    setDisplayName(songData.getName());
//**    waitPanel.setMessage(
//            org.openide.util.NbBundle.getMessage(
//            SongView2TopComponent.class,
//            "WaitMessage"));

    songDataSupport.addPropertyChangeListener(songDataSupportListener);
    songDataSupport.openSession();

  }

  @Override
  protected void songSessionChanged(SongSession oldSession, SongSession newSession) {
    if (newSession != null) {
      if (newSession == session) {
        // waitPanel.setVisible(false);
        sessionIsActive = true;
        updateStartpoint(newSession, newSession.getStartPoint());
        updateLoopStartpoint(newSession, newSession.getLoopStartPoint());
        updateLoopEndpoint(newSession, newSession.getLoopEndPoint());
        updateLoopStatus(newSession, newSession.isLooping());
        setEnabledOnComponentAndChildren(controlContainer, true);
        int minimumPixelToMidiFactor = (int) (songPanel.getDefaultPixelToMidiFactor() / 5);
        if (minimumPixelToMidiFactor < 1) {
          minimumPixelToMidiFactor = 1;
        }
        sliderZoom.setMinimum(minimumPixelToMidiFactor);
        sliderZoom.setMaximum((int) (songPanel.getDefaultPixelToMidiFactor() * 5));
        sliderZoom.setValue((int) (songPanel.getDefaultPixelToMidiFactor()));
      } else {
        //jLabel1.setText("OOPS! I am actived on " + newSession.getName());
        sessionIsActive = false;
        setEnabledOnComponentAndChildren(controlContainer, false);
      }


    } else {
      //jLabel1.setText("I NOT active");
      sessionIsActive = false;
      setEnabledOnComponentAndChildren(controlContainer, false);
    }
  }

  private void updatePlaying(SongSession session, Object newValue) {
    if (newValue instanceof Boolean) {
      boolean isPlaying = (Boolean) newValue;
      if (isPlaying) {
        btnStartStop.setState(StartStopButton.State.STOPPING);
      } else {
        btnStartStop.setState(StartStopButton.State.STARTING);
      }
    }
  }

  private void updateStartpoint(SongSession session, Object newValue) {
    if (newValue instanceof Long) {
      long newTick = (Long) newValue;
      edStartpoint.setValue(
              session.tickToRPositionEx(newTick));
    }
  }

  private void updateLoopStartpoint(SongSession session, Object newValue) {
    if (newValue instanceof Long) {
      long newTick = (Long) newValue;
      edLoopStart.setValue(
              session.tickToRPositionEx(newTick));
    }
  }

  private void updateLoopEndpoint(SongSession session, Object newValue) {
    if (newValue instanceof Long) {
      long newTick = (Long) newValue;
      edLoopEnd.setValue(
              session.tickToRPositionEx(newTick));
    }
  }

  private void updateLoopStatus(SongSession session, Object newValue) {
    if (newValue instanceof Boolean) {
      boolean newStatus = (Boolean) newValue;
      btnLoop.setSelected(newStatus);
    }
  }

  @Override
  protected void songPropertyChange(SongSession session, PropertyChangeEvent evt) {
    String prop = evt.getPropertyName();
    if (SongSession.PROP_STARTPOINT.equals(prop)) {
      updateStartpoint(session, evt.getNewValue());
    } else if (SongSession.PROP_PLAYING.equals(prop)) {
      updatePlaying(session, evt.getNewValue());
    } else if (SongSession.PROP_LOOPSTARTPOINT.equals(prop)) {
      updateLoopStartpoint(session, evt.getNewValue());
    } else if (SongSession.PROP_LOOPENDPOINT.equals(prop)) {
      updateLoopEndpoint(session, evt.getNewValue());
    } else if (SongSession.PROP_LOOPING.equals(prop)) {
      updateLoopStatus(session, evt.getNewValue());

    }
  }

  @Override
  protected void componentActivated() {
    super.componentActivated();
    SongSessionManager.activate(session);
  }

  @Override
  protected boolean closeLast() {
    if (songDataSupport != null) {
      songDataSupport.setOpen(false);
    }
    return super.closeLast();
  }
}
