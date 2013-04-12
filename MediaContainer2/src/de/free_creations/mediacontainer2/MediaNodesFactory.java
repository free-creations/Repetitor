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
package de.free_creations.mediacontainer2;

import de.free_creations.midisong.LessonProperties;
import de.free_creations.netBeansSong.SongSessionManager;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 * The Nodes Factory for the MediaRootNode class. The factory maintains a list
 * file paths, these are the files to be shown in the root node.
 *
 * @author Harald Postner
 */
class MediaNodesFactory extends ChildFactory.Detachable<File> {

  /**
   * A list of all currently visited Media containers.
   */
  private static final ArrayList<File> pathes = new ArrayList<File>();

  /**
   * Add a list of file-paths. File that do not really exist on disk, are
   * ignored. Files that are already in the list, are ignored.
   *
   * @param newFiles an array of files to be added
   */
  public void replacePathList(File[] newFiles) {
    pathes.clear();
    for (File f : newFiles) {
      addPath(f);
    }
  }

  /**
   * Add a file-path. If the file does not really exist on disk, the call is
   * ignored. If the file is already in the list, the call is ignored.
   *
   * @param newFile the file to be added
   */
  public void addPath(File newFile) {
    // does this path really exist on disk ?
    if (!newFile.exists()) {
      return;
    }
    updateLessons(newFile);
    // do we already have this path?
    if (pathes.contains(newFile.getAbsoluteFile())) {
      return;
    }
    // is it worth displaying?
    if (!isMediaPath(newFile)) {
      return;
    }
    pathes.add(newFile.getAbsoluteFile());
    refresh(false);
  }

  /**
   * if the given file is a lesson file add it to the collection lessons held by
   * the SongSession manager.
   *
   * Note: this solution is a hack to get the lesson handling implemented
   * quickly. The Song Session Manager is miss-used to have a central point that
   * collects all lessons. The MediaNodesFactory in package MediaContainer2 adds
   * the lessons whenever it discovers one.
   *
   * @param newFile
   */
  private void updateLessons(File newFile) {
    // is the filename extension ".lesson" ?
    if (!newFile.getName().toLowerCase().endsWith(".lesson")) {
      return;
    }
    // has this lesson already been recorded?
    if (SongSessionManager.hasLesson(newFile)) {
      return;
    }

    LessonProperties lesson = new LessonProperties(newFile);
    SongSessionManager.addLesson(newFile, lesson);
  }

  /**
   * Called immediately before the first call to createKeys(). Override to set
   * up listening for changes, allocating expensive-to-create resources, etc.
   */
  @Override
  protected void addNotify() {
  }

  /**
   * Called when this child factory is no longer in use, to dispose of
   * resources, detach listeners, etc. Does nothing by default; override if you
   * need notification when not in use anymore.
   */
  @Override
  protected void removeNotify() {
  }

  @Override
  protected boolean createKeys(List<File> toPopulate) {

    toPopulate.addAll(pathes);
    return true;
  }

  @Override
  protected Node createNodeForKey(File path) {
    try {
      FileObject fo = FileUtil.toFileObject(path);
      DataObject mediaContainer = DataObject.find(fo);
      return mediaContainer.getNodeDelegate();
    } catch (DataObjectNotFoundException ex) {
      Exceptions.printStackTrace(ex);
      return null;
    }
  }

  /**
   * Check whether the given path is worth to be shown.
   *
   * @param file the path to be inspected
   * @return true if it is a media file that can handled.
   */
  private boolean isMediaPath(File file) {
    // is it a directory
    if (file.isDirectory()) {
      return true;
    }
    // is the filename extension ".fmc" ?
    if (file.getName().toLowerCase().endsWith(".fmc")) {
      return true;
    }


    // all other cases
    return false;
  }
}
