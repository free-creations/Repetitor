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
package de.free_creations.mediacontainer2;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.LocalFileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
 *
 * @author Harald Postner
 */
public class LessonDataObjectTest extends NbTestCase {

  private FileObject lessonFile;

  public LessonDataObjectTest(java.lang.String testName) {
    super(testName);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    clearWorkDir();
    LocalFileSystem fs = new LocalFileSystem();

    File testRoot = getGoldenFile("");
    assertTrue("test-data-directory must exist", testRoot.exists());
    assertTrue("file must be a directory", testRoot.isDirectory());
    fs.setRootDirectory(testRoot);

    lessonFile = fs.findResource("Example.lesson");
    assertNotNull(lessonFile);

  }

  /**
   * This test verifies that the MIME-resolver and the Data-loader do their job
   * for the lesson files.
   *
   * @throws DataObjectNotFoundException if this exception is thrown, than the
   * test-setup is buggy.
   */
  public void testDataLoader() throws DataObjectNotFoundException {
    DataObject lesson = DataObject.find(lessonFile);
    assertNotNull(lesson);
    assertEquals(LessonDataObject.class, lesson.getClass());
  }
}
