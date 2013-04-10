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
import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.loaders.DataNode;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
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
    private final String song;

    private class LessonNode extends AbstractNode {

      LessonNode(String name) {
        super(Children.LEAF);
        setName(name);
        setDisplayName("Lesson: " + name);
      }
    }

    public SongNodeChildFactory(String song) {
      this.song = song;
    }



    @Override
    protected boolean createKeys(List<LessonProperties> toPopulate) {
      List<LessonProperties> lessons = SongSessionManager.getLessons();
      for(LessonProperties lesson:lessons){
        if(song.equalsIgnoreCase(lesson.getSong())) {
          toPopulate.add(lesson);
        }
      }
      return true;
    }

    @Override
    protected Node createNodeForKey(LessonProperties lesson) {
      logger.log(Level.INFO, ">>>>>createNodeForKey : {0}", lesson.getProperty("description"));
      return new LessonNode(lesson.getProperty("description", "unknown"));
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

  public SongNode(SongDataSupport dataSupport, Lookup lookup) {

    super(dataSupport, Children.create(new SongNodeChildFactory(dataSupport.getName()), true), lookup);
    this.dataSupport = dataSupport;

    fileObserver = new SongDataObserver();
    dataSupport.addPropertyChangeListener(WeakListeners.propertyChange(fileObserver, dataSupport));

    setIconBaseWithExtension(ICON_SONG_OPEN_ACTIVE);
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