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
package de.free_creations.netBeansSong;

import de.free_creations.midisong.LessonProperties;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Harald Postner
 */
public class LessonNode extends AbstractNode implements PropertyChangeListener {

  private static final Logger logger = Logger.getLogger(LessonNode.class.getName());
  private static final String ICON_LESSON = "de/free_creations/netBeansSong/artwork/lessonClosed.png";
  private static final String ICON_LESSON_OPEN = "de/free_creations/netBeansSong/artwork/lessonOpen.png";
  private static final Image iconLesson = ImageUtilities.loadImage(ICON_LESSON);
  private static final Image iconLessonOpen = ImageUtilities.loadImage(ICON_LESSON_OPEN);
  private final LessonProperties lesson;
  private final SongDataSupport songDataSupport;
  private final Action lessonOpenAction = new AbstractAction("open") {
    @Override
    public void actionPerformed(ActionEvent e) {
      songDataSupport.applyLessonProperties(lesson);
      setOpen(true);
    }
  };
  private final Action[] lessonActions = new Action[]{lessonOpenAction};
  private boolean open;
  private final long lessonId;

  public LessonNode(LessonProperties lesson, SongDataSupport songDataSupport) {
    super(Children.LEAF);
    setName(lesson.getDescription());
    setDisplayName(lesson.getDescription());
    this.lesson = lesson;
    this.songDataSupport = songDataSupport;
    open = false;
    lessonId = lesson.getIdentity();
    SongSessionManager.addPropertyChangeListener(this);
  }

  @Override
  public Action[] getActions(boolean context) {
    return lessonActions;
  }

  @Override
  public Action getPreferredAction() {
    return lessonOpenAction;
  }

  @Override
  public Image getIcon(int type) {
    if (open) {
      return iconLessonOpen;
    } else {
      return iconLesson;
    }
  }

  private void setOpen(boolean value) {
    boolean oldValue = this.open;
    if(oldValue == value){
      return;
    }
    open = value;
    if(open){
      SongSessionManager.setActiveLesson(lessonId);
    }
    fireIconChange();
  }

  @Override
  public Image getOpenedIcon(int type) {
    return iconLessonOpen;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    try {
      if (SongSessionManager.PROP_ACTIVELESSON.equals(evt.getPropertyName())) {

        if ((Long) evt.getNewValue() == lessonId) {
          setOpen(true);
        } else {
          setOpen(false);
        }
      }
    } catch (Throwable ignored) {
    }
  }
}
