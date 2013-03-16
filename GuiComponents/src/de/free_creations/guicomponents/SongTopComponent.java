/*
 * Copyright 2012 Harald Postner.
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
package de.free_creations.guicomponents;

import de.free_creations.midisong.SongSession;
import de.free_creations.netBeansSong.SongSessionManager;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JComponent;
import org.openide.util.Exceptions;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

/**
 * An {@link org.openide.windows.TopComponent } window that automatically
 * connects to the currently active {@link SongSession}. @Note Do not forget to
 * call super.componentOpened() and super.componentClosed() when overriding
 * these functions.
 *
 * @author Harald Postner
 */
public abstract class SongTopComponent extends CloneableTopComponent {

  /**
   * This method must be implemented to react when an other song becomes the
   * active song. It is guaranteed that this method is invoked in the AWT
   * thread.
   *
   * @param oldSession the previous session or null
   * @param newSession the current session or null
   */
  protected abstract void songSessionChanged(SongSession oldSession, SongSession newSession);

  /**
   * This method must be implemented to react when a property of the active song
   * has changed. It is guaranteed that this method is invoked in the AWT
   * thread.
   *
   * @param session the currently active session (never null)
   * @param evt the property change event (never null)
   */
  protected abstract void songPropertyChange(SongSession session, PropertyChangeEvent evt);

  /**
   * If the PreferedSession is set to a value different from null this Window
   * will exclusively connect to the given session.
   *
   * @param value the SongSession this window should connect to ( a null value
   * will make this window to connect to any active session)
   */
  public void setPreferedSession(SongSession value) {
    managerToWindowConnector.setPreferedSession(value);
  }

  public SongSession getPreferedSession() {
    return managerToWindowConnector.getPreferedSession();
  }

  /**
   * This is the AWT runner for changes in the SongSession.
   */
  private final class PropertyUpdater implements Runnable {

    private final PropertyChangeEvent evt;
    private final SongSession session;

    /**
     *
     * @param session the session is guaranteed not to be null
     * @param evt the event is guaranteed not to be null
     */
    public PropertyUpdater(SongSession session, PropertyChangeEvent evt) {
      this.evt = evt;
      this.session = session;
    }

    @Override
    public void run() {
      songPropertyChange(this.session, this.evt);
    }
  }

  /**
   * This class listens on the currently connected SongSession and forwards all
   * Property-Change-Events to the propertyUpdater. The propertyUpdater is
   * guaranteed to be executed in the AWT thread.
   */
  private final class SessionToTopComponentConnector implements PropertyChangeListener {

    private final SongSession songSession;

    SessionToTopComponentConnector(SongSession songSession) {
      assert (songSession != null);
      this.songSession = songSession;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      SwingExecutor.instance().execute(
              new PropertyUpdater(songSession, evt));
    }
  };

  /**
   * This class listens on the SongSessionManager. Whenever the active song
   * changes, the "SongUpdater" task is called.
   */
  private class ManagerToWindowConnector implements PropertyChangeListener {

    private final AtomicReference<SongSession> preferedSession =
            new AtomicReference<SongSession>(null);
    private SongSession oldSession = null;
    private SessionToTopComponentConnector oldConnector = null;

    /**
     * This procedure reacts whenever the songManager changes the active song.
     * Note: the new active song is determined from the "newValue" of the
     * PropertyChangeEvent. The previous values (oldSession) are locally cached,
     * therefore this procedure must be marked "synchronised" in order to
     * sequence calls to this procedure.
     */
    @Override
    public synchronized void propertyChange(PropertyChangeEvent evt) {

      if (SongSessionManager.PROP_ACTIVESONGSESSION.equals(evt.getPropertyName())) {
        synchronized (preferedSession) {
          SongSession newSession = null;
          SessionToTopComponentConnector newConnector = null;
          if (evt.getNewValue() instanceof SongSession) {
            newSession = (SongSession) evt.getNewValue();
            if (preferedSession.get() != null) {
              if (newSession != preferedSession.get()) {
                newSession = null;
              }
            }
          }
          if (newSession == oldSession) {
            return;
          }
          if (newSession != null) {
            newConnector = new SessionToTopComponentConnector(newSession);
          }
          SwingExecutor.instance().execute(
                  new SongUpdater(newSession, oldSession, newConnector, oldConnector));
          oldSession = newSession;
          oldConnector = newConnector;
        }
      }
    }

    public void setPreferedSession(SongSession value) {
      synchronized (preferedSession) {
        SongSession previouslyPrefered = preferedSession.get();
        // if the PreferedSession has not changed there is nothing to do
        if (previouslyPrefered == value) {
          return;
        }
        // if we were connected but not to the newly PreferedSession,
        // then disconnect from the now "un-preferd" session.
        if (oldSession != null) {
          if (oldSession != value) {
            SwingExecutor.instance().execute(
                    new SongUpdater(null, oldSession, null, oldConnector));
            oldSession = null;
            oldConnector = null;
          }
        }
        preferedSession.set(value);
      }
    }

    private SongSession getPreferedSession() {
      synchronized (preferedSession) {
        return preferedSession.get();
      }
    }
  };
  private final ManagerToWindowConnector managerToWindowConnector =
          new ManagerToWindowConnector();

  /**
   * This class implements the activities to be done when an other session
   * becomes the currently active session.
   */
  private final class SongUpdater implements Runnable {

    private final SongSession newSession;
    private final SongSession oldSession;
    private final SessionToTopComponentConnector newConnector;
    private final SessionToTopComponentConnector oldConnector;

    public SongUpdater(SongSession newSession, SongSession oldSession,
            SessionToTopComponentConnector newConnector, SessionToTopComponentConnector oldConnector) {
      this.newSession = newSession;
      this.newConnector = newConnector;
      this.oldSession = oldSession;
      this.oldConnector = oldConnector;
    }

    @Override
    public void run() {
      if (newSession == oldSession) {
        return;
      }

      if (oldSession != null) {
        oldSession.removePropertyChangeListener(oldConnector);
      }
      if (newSession != null) {
        newSession.addPropertyChangeListener(newConnector);

      }
      songSessionChanged(oldSession, newSession);
    }
  }

  @Override
  public void componentOpened() {
    super.componentOpened();
    
    SongSession activeSongSession = null;
    try {
      activeSongSession = SongSessionManager.getActiveSongSession();
    } catch (InterruptedException ex) {
      Exceptions.printStackTrace(ex);
    } catch (ExecutionException ex) {
      Exceptions.printStackTrace(ex);
    }

    // simulate a "PROP_ACTIVESONGSESSION" message in order to connect to the currenly active 
    // song.
    managerToWindowConnector.propertyChange(
            new PropertyChangeEvent(
            this, //source
            SongSessionManager.PROP_ACTIVESONGSESSION, //property name
            null, //oldValue
            activeSongSession)); // newValue

    //connect to the SongSessionManager, as to get notification when
    //the currently active song changes.
    SongSessionManager.addPropertyChangeListener(managerToWindowConnector);

  }

  @Override
  public void componentClosed() {
    //disconnect from the SongSessionManager, so to stop to be notified.
    SongSessionManager.removePropertyChangeListener(managerToWindowConnector);
    // simulate a "PROP_ACTIVESONGSESSION" message in order to disconnect from the currenly active 
    // song.
    managerToWindowConnector.propertyChange(
            new PropertyChangeEvent(
            this, //source
            SongSessionManager.PROP_ACTIVESONGSESSION, //property name
            null, //"oldValue". We don't know at this point....do not use this value!!!
            null)); // newValue

    super.componentClosed();
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
}
