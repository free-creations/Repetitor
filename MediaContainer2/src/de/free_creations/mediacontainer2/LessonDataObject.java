/*
 * Copyright 2013 harald.
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
import java.util.Properties;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

@Messages({
  "LBL_Lesson_LOADER=Files of Lesson"
})
@MIMEResolver.ExtensionRegistration(
    displayName = "#LBL_Lesson_LOADER",
mimeType = "text/lesson",
extension = {"lesson"})
@DataObject.Registration(
    mimeType = "text/lesson",
iconBase = "de/free_creations/mediacontainer2/resources/lesson16.png",
displayName = "#LBL_Lesson_LOADER",
position = 300)
@ActionReferences({
  @ActionReference(
        path = "Loaders/text/lesson/Actions",
  id =
  @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
  position = 100,
  separatorAfter = 200),
  @ActionReference(
        path = "Loaders/text/lesson/Actions",
  id =
  @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
  position = 300),
  @ActionReference(
        path = "Loaders/text/lesson/Actions",
  id =
  @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
  position = 400,
  separatorAfter = 500),
  @ActionReference(
        path = "Loaders/text/lesson/Actions",
  id =
  @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
  position = 600),
  @ActionReference(
        path = "Loaders/text/lesson/Actions",
  id =
  @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
  position = 700,
  separatorAfter = 800),
  @ActionReference(
        path = "Loaders/text/lesson/Actions",
  id =
  @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
  position = 900,
  separatorAfter = 1000),
  @ActionReference(
        path = "Loaders/text/lesson/Actions",
  id =
  @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
  position = 1100,
  separatorAfter = 1200),
  @ActionReference(
        path = "Loaders/text/lesson/Actions",
  id =
  @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
  position = 1300),
  @ActionReference(
        path = "Loaders/text/lesson/Actions",
  id =
  @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
  position = 1400)
})
public class LessonDataObject extends MultiDataObject {

  public LessonDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
    super(pf, loader);
    registerEditor("text/lesson", true);

    /* Note: this solution is a hack to get the lesson handling implemented
     * quickly. The Song Session Manager is miss-used to have a central point that
     * collects all lessons. 
     */
    File newFile = new File(pf.getPath());
    // has this lesson already been recorded?
    if (SongSessionManager.hasLesson(newFile)) {
      return;
    }
    LessonProperties lesson = new LessonProperties(newFile);
    SongSessionManager.addLesson(newFile, lesson);
  }

  @Override
  protected int associateLookup() {
    return 1;
  }

  @MultiViewElement.Registration(
        displayName = "#LBL_Lesson_EDITOR",
  iconBase = "de/free_creations/mediacontainer2/resources/lesson16.png",
  mimeType = "text/lesson",
  persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
  preferredID = "Lesson",
  position = 1000)
  @Messages("LBL_Lesson_EDITOR=Source")
  public static MultiViewEditorElement createEditor(Lookup lkp) {
    return new MultiViewEditorElement(lkp);
  }
}
