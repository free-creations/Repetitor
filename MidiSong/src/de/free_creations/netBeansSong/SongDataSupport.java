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

import de.free_creations.midisong.EInvalidSongFile;
import de.free_creations.midisong.Song;
import de.free_creations.midisong.SongSession;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.swing.SwingWorker;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.windows.CloneableTopComponent;

/**
 * This class integrates the {@link Song Song class} into the NetBeans actions
 * framework. The SongDataSupport manages the activities when a Song is opened.
 * A SongDataSupport can attach one session and one sessionView.
 */
public class SongDataSupport extends MultiDataObject {

  protected boolean open = false;
  public static final String PROP_SESSION_OPENED = "sessionopened";
  public static final String PROP_SESSION_ACTIVE = "sessionactive";
  public static final String PROP_SESSION = "session";
  private CloneableTopComponent sessionView;
  private final FileObject primaryFileObject;
  private Song song = null;
  private int sessionCount = 0;
  /**
   * This SwingWorker reads the song file outside the Swing thread so that the
   * user interface does not get blocked.
   */
  private SwingWorker<SongSession, Void> sessionOpeningTask = new SwingWorker<SongSession, Void>() {

    private SongSession newSongSession = null;

    @Override
    protected SongSession doInBackground() throws FileStateInvalidException, EInvalidSongFile {
      URL xmlFileUrl = primaryFileObject.getURL();
      Song song = Song.createFromFile(xmlFileUrl);
      String sessionName = song.getName();
      newSongSession = SongSessionManager.getSongSession(song, sessionName);
      return newSongSession;
    }

    @Override
    protected void done() {
      setOpen(newSongSession != null);
      newSongSession.addPropertyChangeListener(songSessionListener);
      SongDataSupport.this.firePropertyChange(PROP_SESSION, null, newSongSession);
    }
  };
  /**
   * This class shall be attached to the songSession-object and shall be
   * informed when the songSession-object has become the active one.
   */
  private final PropertyChangeListener songSessionListener =
          new PropertyChangeListener() {

            /**
             * Forwards changes in the active property to the clients of this
             * object.
             */
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
              if (SongSession.PROP_ACTIVE.equals(evt.getPropertyName())) {
                SongDataSupport.this.firePropertyChange(PROP_SESSION_ACTIVE,
                        evt.getOldValue(), evt.getNewValue());
              }
            }
          };

  public SongDataSupport(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
    super(pf, loader);
    primaryFileObject = pf;
    CookieSet cookies = getCookieSet();
    SongOpenSupport actionSupport = new SongOpenSupport(this);
    cookies.assign(OpenCookie.class, (OpenCookie) actionSupport);
  }

  @Override
  protected Node createNodeDelegate() {
    return new SongNode(this, getLookup());
  }

  @Override
  public Lookup getLookup() {
    return getCookieSet().getLookup();
  }

  boolean isSessionActive() {
    if (!isOpen()) {
      return false;
    }
    try {
      return getSession().isActive();
    } catch (Exception ex) {
      return false;
    }

  }

  /**
   * Indicates the state of the attached file.
   *
   * @return true if the file has been loaded into memory and a corresponding
   * SongSession object has been created.
   */
  public boolean isOpen() {
    return open;
  }

  /**
   * Set the value of open
   *
   * @param open new value of open
   */
  public void setOpen(boolean open) {
    if (this.open == open) {
      return;
    }
    boolean oldOpen = this.open;
    this.open = open;
    firePropertyChange(PROP_SESSION_OPENED, oldOpen, open);
  }

  /**
   * The top component that shall be used to show this song.
   *
   * @return
   */
  public CloneableTopComponent getSessionView() throws IOException {
    if (sessionView != null) {
      return sessionView;
    }
    SongSessionViewProvider provider =
            Lookup.getDefault().lookup(
            SongSessionViewProvider.class);

    if (provider != null) {
      sessionView = provider.getView(this);
      return sessionView;
    }
    throw new IOException("Cannot open this Song. You must install a View Provider.");
  }

  /**
   * Do not use this function in the context of the AWT thread, because it takes
   * too much time.
   *
   * @deprecated
   * @return
   * @throws FileStateInvalidException
   * @throws EInvalidSongFile
   */
  @Deprecated
  public SongSession createSession() throws FileStateInvalidException, EInvalidSongFile {
    if (song == null) {
      URL xmlFileUrl = primaryFileObject.getURL();
      song = Song.createFromFile(xmlFileUrl);
    }
    String sessionName = song.getName();
    sessionCount++;
    if (sessionCount > 1) {
      sessionName = song.getName() + " (" + sessionCount + ")";
    }
    setOpen(true);
    return SongSessionManager.getSongSession(song, sessionName);
  }

  /**
   * Asynchronously creates a session and attaches it to this object. Clients
   * must listen to the PROP_SESSION_OPENED property in order to discover when
   * the session becomes available. Note: this function is only designed to be
   * executed once
   */
  public void openSession() {
    sessionOpeningTask.execute();
  }

  /**
   * Get the SongSession object belonging to this data object.
   *
   * @return the SongSession object belonging to this data object.
   * @throws InterruptedException if the loading of this SongSession has been
   * interrupted
   * @throws ExecutionException if something went wrong while loading this
   * SongSession
   * @throws TimeoutException if this SongSession is still loading.
   */
  public SongSession getSession() throws InterruptedException, ExecutionException, TimeoutException {
    return sessionOpeningTask.get(0, TimeUnit.MILLISECONDS);
  }

  /**
   * Session-views that have been created outside of the function
   * "getSessionView()" must be registered through this function.
   *
   * @param sessionView
   */
  public void registerSessionView(CloneableTopComponent sessionView) {
    this.sessionView = sessionView;
  }
}
