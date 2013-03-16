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

import de.free_creations.microsequencer.MicroSequencer;
import de.free_creations.microsequencer.MicroSequencerManager;
import de.free_creations.midisong.EInvalidSongFile;
import de.free_creations.midisong.Song;
import de.free_creations.midisong.SongSession;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
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
 * should also be used as the factory class to create new
 * {@link SongSession SongSessions}.
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class SongSessionManager {

  private static final Logger logger = Logger.getLogger(SongSessionManager.class.getName());
  static private final SongSessionManager instance = new SongSessionManager();
  public static final String PROP_ACTIVESONGSESSION = "activeSongSession";
  private static volatile SessionActivationTask currentSessionActivationTask = null;
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

  private static class SessionActivationTask extends SwingWorker<SongSession, Void> {

    /**
     * the SongSession that shall become the new active session *
     */
    private final SongSession newSongSession;
    private final SongSession oldSongSession;
    private final ProgressHandle progressHandle;
    private MicroSequencer microSequencer;

    public SessionActivationTask(SongSession newSongSession, SongSession oldSongSession) {
      super();
      progressHandle = ProgressHandleFactory.createHandle("Connecting to Audio Hardware.");

      this.newSongSession = newSongSession;
      this.oldSongSession = oldSongSession;

    }

    @Override
    protected SongSession doInBackground() throws EInvalidSongFile, MidiUnavailableException  {
      progressHandle.start();

      microSequencer = initializeAudioSystem();
      if (microSequencer == null) {
        logger.warning("The Audio System is not correctly initialized.");
        return null;
      }

      // if the new session is already active there is nothing to be done.
      if (oldSongSession == newSongSession) {
        return oldSongSession;
      }
      // desactivate the currently active song
      if (oldSongSession != null) {
        oldSongSession.setPlaying(false);
        oldSongSession.detachSequencer();
      }
      if (newSongSession != null) {
        newSongSession.attachSequencer(microSequencer);
      }
      return newSongSession;
    }

    @Override
    protected void done() {
      progressHandle.finish();
      if (oldSongSession != newSongSession) {
        instance.propertyChangeSupport.firePropertyChange(PROP_ACTIVESONGSESSION, oldSongSession, newSongSession);
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
   * Note1: if the session could not be activated (because the audio system is
   * not correctly configured), this function will silently fail.
   *
   * Note2: if this function is called while an other session is being activated
   * this function will block until the other session is ready.
   *
   * @param songSession the song session to be activated (if null, the currently
   * active session is deactivated)
   */
  public static void activate(SongSession songSession) throws InterruptedException, ExecutionException {
    SongSession previousSession = null;
    if (currentSessionActivationTask != null) {
      if (!currentSessionActivationTask.isDone()) {
        logger.warning("Attempt to activate two sessions in parallel.");
      }
      previousSession = currentSessionActivationTask.get();
    }
    currentSessionActivationTask = new SessionActivationTask(songSession, previousSession);
    currentSessionActivationTask.execute();
  }

  /**
   * make sure that there is no more active session.
   *
   * @param ignored this parameter is ignored
   */
  public static void deactivate(SongSession ignored) {
    try {
      activate(null);
    } catch (InterruptedException ex) {
      Exceptions.printStackTrace(ex);
    } catch (ExecutionException ex) {
      Exceptions.printStackTrace(ex);
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

  /**
   * Tries to open the unique instance of the microSequencer.
   *
   * @return the unique instance of the micro sequencer. Note: if the
   * instantiation failed (because no suitable audio configuration could be
   * found), this function returns null.
   * @throws MidiUnavailableException
   */
  private static MicroSequencer initializeAudioSystem() {
    MicroSequencer microSequencer = MicroSequencerManager.getInstance();
    if (microSequencer == null) {
      return null;
    }
    if (!microSequencer.isOpen()) {
      try {
        microSequencer.open();
      } catch (MidiUnavailableException ex) {
        Exceptions.printStackTrace(ex);
      }
    }
    if (!microSequencer.isOpen()) {
      logger.warning("Could not open the sequencer.");
      return null;
    }
    return microSequencer;
  }

  public static void closeAudioSystem() {
    MicroSequencerManager.closeInstance();
  }

  /**
   * Returns the currently active session.
   *
   * Note: if this function is called while a session is being
   * activated this function will block until the session gets active.
   *
   * @return the currently active session.
   * @throws InterruptedException
   * @throws ExecutionException
   */
  public static SongSession getActiveSongSession() throws InterruptedException, ExecutionException {
    SongSession currentSession = null;
    if (currentSessionActivationTask != null) {
      currentSession = currentSessionActivationTask.get();
    }
    return currentSession;
  }
}
