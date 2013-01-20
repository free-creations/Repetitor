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
package de.free_creations.netBeansSong;

import de.free_creations.audioconfig.Audioconfig;
import de.free_creations.microsequencer.MicroSequencer;
import de.free_creations.microsequencer.MicroSequencerManager;
import de.free_creations.midisong.EInvalidSongFile;
import de.free_creations.midisong.Song;
import de.free_creations.midisong.SongSession;
import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.SwingWorker;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Exceptions;

/**
 * This class controls the creation and activation of SongSessions. Especially
 * it makes sure that, at a given time, only one single song has access to the
 * sequencer. Controller-elements can subscribe to get informed about the
 * currently active session. When the first session is activated, the sound
 * system is booted. Booting the sound system is done in a background thread.
 * The publishing (firePropertyChange) is done in the AWT thread. This class
 * should also be used as the factory class to create new {@link SongSession SongSessions}.
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class SongSessionManager {

  static private final SongSessionManager instance = new SongSessionManager();
  /**
   * The currently active song session. This variable is protected by the
   * activeSongSessionLock.
   */
  static private SongSession activeSongSession = null;
  /**
   * Lock that protects the active song session and makes access to the active
   * song session sequential.
   */
  static private final Object activeSongSessionLock = new Object();
  public static final String PROP_ACTIVESONGSESSION = "activeSongSession";
  private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  /**
   * Create a new {@link SongSession SongSession}.
   *
   * @param id the identification of the new song session (will be used as the
   * name of the new session).
   * @return a new {@link SongSession SongSession} with the given name.
   */
  public static SongSession getSongSession(Song song, String id) throws EInvalidSongFile {
    SongSession songSession = song.createSession();
    if (id != null) {
      songSession.setName(id);
    }
    return songSession;
  }

  private static class SessionActivationTask_1 implements Audioconfig.ConfigDialogEndListener {

    private final SongSession toBeActivated;
    private final Component component;

    public SessionActivationTask_1(SongSession toBeActivated, Component component) {
      this.toBeActivated = toBeActivated;
      this.component = component;
    }

    /**
     * This method is invoked when the user has selected a valid Audio
     * configuration
     */
    @Override
    public void dialogClosed() {
      synchronized (activeSongSessionLock) {
        if (activeSongSession != toBeActivated) {
          SessionActivationTask_2 task = new SessionActivationTask_2(toBeActivated, component);
          task.execute();
        }
      }
    }
  }

  private static class SessionActivationTask_2 extends SwingWorker<Void, Void> {

    /**
     * the songsession that shall become the new active session *
     */
    private final SongSession newSongSession;
    private SongSession oldSongSession;
    private final ProgressHandle progressHandle;
    private MicroSequencer microSequencer;
    private final Component component;

    public SessionActivationTask_2(SongSession newSongSession) {
      super();
      progressHandle = ProgressHandleFactory.createHandle("Connecting to Audio Hardware.");

      this.newSongSession = newSongSession;
      component = null;
    }

    /**
     *
     * @param newSongSession
     * @param component a call to redraw this component will be executed when
     * the session is activated (this parameter is optional, if not used set it
     * to null)
     */
    private SessionActivationTask_2(SongSession newSongSession, Component component) {
      super();
      progressHandle = ProgressHandleFactory.createHandle("Connecting to Audio Hardware.");

      this.newSongSession = newSongSession;
      this.component = component;
    }

    @Override
    protected Void doInBackground() {
      progressHandle.start();
      try {
        microSequencer = initializeAudioSystem();
        synchronized (activeSongSessionLock) {
          oldSongSession = activeSongSession;
          // if the new session is already active there is nothing to be done.
          if (newSongSession == activeSongSession) {
            return null;
          }
          // desactivate the currently active song
          if (activeSongSession != null) {
            activeSongSession.setPlaying(false);
            activeSongSession.detachSequencer();
          }
          if (newSongSession != null) {
            newSongSession.attachSequencer(microSequencer);
          }
          activeSongSession = newSongSession;
        }
      } catch (Exception ex) {
        activeSongSession = null;
        Exceptions.printStackTrace(ex);
      }
      return null;
    }

    @Override
    protected void done() {
      progressHandle.finish();
      /**
       * @TODO calling "firePropertyChange" here can lead to trouble because the
       * active session could have changed again when this function is
       * scheduled... On the other hand, calling propertyChangeSupport outside
       * the AWT thread is also dangerous...
       */
      if (oldSongSession != activeSongSession) {
        instance.propertyChangeSupport.firePropertyChange(PROP_ACTIVESONGSESSION, oldSongSession, newSongSession);
      }
      if (component != null) {
        component.repaint();
      }
    }
  }

  /**
   * Asynchronously activates a song session. An active song session has access
   * to the sequencer and the sound system. There can only be one session active
   * at the same time. The SongSessionManager first deactivates the currently
   * active session and then tries to activate the given session. It is assumed
   * that the calling thread is the AWT thread, therefore the reconnecting work
   * is done in a separate thread.
   *
   * @param songSession the song session to be activated (if null, the currently
   * active session is deactivated)
   */
  public static void activate(SongSession songSession) {
    /**
     * @ToDo Instead of synchronising on one active Session it might be a better
     * to queue the activation requests.
     */
    SessionActivationTask_1 task = new SessionActivationTask_1(songSession, null);
    if (Audioconfig.isProbed()) {
      task.dialogClosed();
    } else {
      Audioconfig.showConfigDialog(task, "Please configure the audio system.");
    }
  }

  /**
   * Asynchronously activates a song session. An active song session has access
   * to the sequencer and the sound system. There can only be one session active
   * at the same time. The SongSessionManager first deactivates the currently
   * active session and then tries to activate the given session. It is assumed
   * that the calling thread is the AWT thread, therefore the reconnecting work
   * is done in a separate thread.
   *
   * @param songSession the song session to be activated (if null, the currently
   * active session is deactivated)
   * @param component a call to redraw this component will be executed when the
   * session is activated (this parameter is optional, if not used set it to
   * null)
   * @deprecated doing a callback on the calling component was not a good idea
   */
  @Deprecated
  public static void activate(SongSession songSession, Component component) {
    /**
     * @ToDo Remove this and all calls on it.
     */
    SessionActivationTask_1 task = new SessionActivationTask_1(songSession, component);
    if (Audioconfig.isProbed()) {
      task.dialogClosed();
    } else {
      Audioconfig.showConfigDialog(task, "Please configure the audio system.");
    }
  }

  /**
   * make sure that the given session is no more active.
   *
   * @param session
   */
  public static void deactivate(SongSession session) {
    if (activeSongSession == session) {
      activate(null);
    }
  }

  /**
   * Add PropertyChangeListener.
   *
   * @param listener
   */
  public static void addPropertyChangeListener(PropertyChangeListener listener) {
    instance.propertyChangeSupport.addPropertyChangeListener(listener);
  }

  /**
   * Remove PropertyChangeListener.
   *
   * @param listener
   */
  public static void removePropertyChangeListener(PropertyChangeListener listener) {
    instance.propertyChangeSupport.removePropertyChangeListener(listener);
  }

  private static MicroSequencer initializeAudioSystem() throws MidiUnavailableException {
    MicroSequencer microSequencer = MicroSequencerManager.getInstance();
    if (!microSequencer.isOpen()) {
      microSequencer.open();
    }
    return microSequencer;
  }

  public static void closeAudioSystem() {
    MicroSequencerManager.closeInstance();
  }

  public static SongSession getActiveSongSession() {
    return activeSongSession;
  }
}
