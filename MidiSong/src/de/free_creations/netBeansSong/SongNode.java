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

import de.free_creations.midisong.LessonProperties;
import de.free_creations.midisong.SongSession;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.loaders.DataNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;

/**
 * This class integrates the {@link Song Song class} into the NetBeans Nodes
 * framework. The SongNode manages the iconised representation of a Song in
 * NetBeans user interface.
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class SongNode extends DataNode {

  private static final Logger logger = Logger.getLogger(SongNode.class.getName());

  private static class SongNodeChildFactory extends ChildFactory<LessonProperties> {

    private final SongDataSupport songDataSupport;

    public SongNodeChildFactory(SongDataSupport dataSupport) {
      this.songDataSupport = dataSupport;
    }

    @Override
    protected boolean createKeys(List<LessonProperties> toPopulate) {
      Set<LessonProperties> lessons = SongSessionManager.getLessons();
      String songname = songDataSupport.getName();
      for (LessonProperties lesson : lessons) {
        if (songname.equalsIgnoreCase(lesson.getSong())) {
          toPopulate.add(lesson);
        }
      }
      return true;
    }

    @Override
    protected Node createNodeForKey(LessonProperties lesson) {
      return new LessonNode(lesson, songDataSupport);
    }
  }

  private class SongDataObserver implements PropertyChangeListener {

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      SongNode.this.songDataPropertyChange(evt);
    }
  }
  private final SongDataObserver fileObserver;
  private final SongDataSupport dataSupport;
  private static final String ICON_SONG = "de/free_creations/netBeansSong/artwork/songNode.png";
  private static final String ICON_SONG_OPEN = "de/free_creations/netBeansSong/artwork/songNodeOpen.png";
  private static final String ICON_SONG_OPEN_ACTIVE = "de/free_creations/netBeansSong/artwork/songNodeOpenActive.png";
  private static final String ICON_SONG32 = "de/free_creations/netBeansSong/artwork/songNode32.png";
  private static final String ICON_SONG_OPEN32 = "de/free_creations/netBeansSong/artwork/songNodeOpen32.png";
  private static final String ICON_SONG_OPEN_ACTIVE32 = "de/free_creations/netBeansSong/artwork/songNodeOpenActive32.png";
  private final Action songOpenAction = new AbstractAction("open") {
    @Override
    public void actionPerformed(ActionEvent e) {
      SongOpenSupport songOpenSupport = new SongOpenSupport(dataSupport);
      songOpenSupport.open();
    }
  };
  private final SaveLessonAction saveLessonAction;

  private class SaveLessonAction extends AbstractAction {

    private final SongDataSupport dataSupport;

    public SaveLessonAction(SongDataSupport dataSupport) {
      super("save lesson");
      this.dataSupport = dataSupport;
    }

    @Override
    public boolean isEnabled() {
      return dataSupport.isSessionActive();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      try {
        SongSession session = dataSupport.getSession();
        File lessonsDirectory = dataSupport.getLessonsDirectory();
        if (lessonsDirectory == null) {
          lessonsDirectory = new File(System.getProperty("user.home"));
        }
        LessonProperties lesson = session.getLessonProperties();
        lesson.setSong(dataSupport.getName());
        SaveLessonDialog form = new SaveLessonDialog(dataSupport.getName(), lesson, lessonsDirectory);
        String msg = "Save Lesson...";
    
        
        DialogDescriptor dd = new DialogDescriptor(form, msg);
        boolean done = false;
        while (!done) {
          Object result = DialogDisplayer.getDefault().notify(dd);
          if (result != NotifyDescriptor.OK_OPTION) {
            break;
          }
          File verifiedFile = verifyFilename(form.getFilename(), lessonsDirectory);
          if (verifiedFile != null) {

            LessonProperties resultProperties = form.getLessonProperties();
            resultProperties.writeToFile(verifiedFile);
            break;
          }
        }

      } catch (Exception ex) {
        Exceptions.printStackTrace(ex);
      }
    }

    File verifyFilename(String Filename, File lessonsDirectory) {
      if (Filename.length() < 1) {
        NotifyDescriptor nd = new NotifyDescriptor.Message("You must specify a filename.", NotifyDescriptor.INFORMATION_MESSAGE);
        DialogDisplayer.getDefault().notify(nd);
        return null;
      }

      String FilenameExt = Filename + ".lesson";
      File lessonFile = new File(lessonsDirectory, FilenameExt);

      if (lessonFile.exists()) {
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(Filename + " already exists, OK to overwrite?.", NotifyDescriptor.YES_NO_OPTION);
        Object okToOverwrite = DialogDisplayer.getDefault().notify(nd);
        if (NotifyDescriptor.YES_OPTION == okToOverwrite) {
          return lessonFile;
        } else {
          return null;
        }
      }
      return lessonFile;
    }
  };
  private final Action[] songActions;

  public SongNode(SongDataSupport dataSupport, Lookup lookup) {

    super(dataSupport, Children.create(new SongNodeChildFactory(dataSupport), true), lookup);
    this.dataSupport = dataSupport;
    saveLessonAction = new SaveLessonAction(dataSupport);
    songActions = new Action[]{songOpenAction, saveLessonAction};

    fileObserver = new SongDataObserver();
    dataSupport.addPropertyChangeListener(WeakListeners.propertyChange(fileObserver, dataSupport));

    setIconBaseWithExtension(ICON_SONG_OPEN_ACTIVE);
  }

  @Override
  public Action[] getActions(boolean context) {
    return songActions;
  }

  @Override
  public Action getPreferredAction() {
    return songOpenAction;
  }

  @Override
  public Image getIcon(int type) {
    if (dataSupport.isOpen()) {
      if (dataSupport.isSessionActive()) {
        return getActiveIcon(type);
      } else {
        return getOpenedIcon(type);
      }
    }
    return getClosedIcon(type);
  }

  private Image getActiveIcon(int type) {
    switch (type) {
      case java.beans.BeanInfo.ICON_COLOR_16x16:
        return ImageUtilities.loadImage(ICON_SONG_OPEN_ACTIVE);
      case java.beans.BeanInfo.ICON_COLOR_32x32:
        return ImageUtilities.loadImage(ICON_SONG_OPEN_ACTIVE32);
    }
    return null;
  }

  public Image getClosedIcon(int type) {
    switch (type) {
      case java.beans.BeanInfo.ICON_COLOR_16x16:
        return ImageUtilities.loadImage(ICON_SONG);
      case java.beans.BeanInfo.ICON_COLOR_32x32:
        return ImageUtilities.loadImage(ICON_SONG32);
    }
    return null;
  }

  @Override
  public Image getOpenedIcon(int type) {
    switch (type) {
      case java.beans.BeanInfo.ICON_COLOR_16x16:
        return ImageUtilities.loadImage(ICON_SONG_OPEN);
      case java.beans.BeanInfo.ICON_COLOR_32x32:
        return ImageUtilities.loadImage(ICON_SONG_OPEN32);
    }
    return null;
  }

  /**
   * Reacts on a change of a property in the SongDataSupport object.
   *
   * @param evt
   */
  private void songDataPropertyChange(PropertyChangeEvent evt) {
    if (SongDataSupport.PROP_SESSION_OPENED.equals(evt.getPropertyName())) {
      fireIconChange();
    }
    if (SongDataSupport.PROP_SESSION_ACTIVE.equals(evt.getPropertyName())) {
      fireIconChange();
    }
  }
}
