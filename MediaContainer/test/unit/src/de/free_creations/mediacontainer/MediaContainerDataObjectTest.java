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
package de.free_creations.mediacontainer;

import de.free_creations.netBeansSong.SongNode;
import java.io.File;
import java.util.Enumeration;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Test the MediaContainerDataObject. Note in order to make the class FileUtil
 * work, you must add "org.netbeans.modules.masterfs" and
 * "org.netbeans.core.startup" and "org.netbeans.bootstrap" to the Unit test
 * libraries (see http://wiki.netbeans.org/UsingFileSystemsMasterfs and
 * http://netbeans.org/nonav/bugzilla/show_bug.cgi?id=84501) see also
 * http://platform.netbeans.org/tutorials/nbm-test.html
 *
 * @author Harald Postner
 */
public class MediaContainerDataObjectTest extends NbTestCase {

  private FileObject testContainer;

  public MediaContainerDataObjectTest(java.lang.String testName) {
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

    testContainer = fs.findResource("Boismortier.fmc");
    assertNotNull(testContainer);
    //make sure recognizes the media-container as a zip archive
    assertTrue(FileUtil.isArchiveFile(testContainer));
    assertNotNull(FileUtil.getArchiveRoot(testContainer));
  }

  /**
   * This test verifies that the MIME-resolver and the Data-loader do their job
   * for the mediaContainer files.
   *
   * @throws DataObjectNotFoundException if this exception is thrown, than the
   * test-setup is buggy.
   */
  public void testDataLoader() throws DataObjectNotFoundException {
    DataObject mediaContainer = DataObject.find(testContainer);
    assertNotNull(mediaContainer);
    assertEquals(MediaContainerDataObject.class, mediaContainer.getClass());
  }

  /**
   * Make sure that a mediaContainer file is represented by the correct kind of
   * nodes.
   */
  public void testGetNodeDelegate() throws DataObjectNotFoundException, InterruptedException {
    DataObject mediaContainer = DataObject.find(testContainer);
    Node node = mediaContainer.getNodeDelegate();
    assertNotNull(node);
    assertEquals(DataNode.class, node.getClass());
    // the golden test container has several rootfiles, these must
    // be represented as chidren of this node
    Children children = node.getChildren();
    assertTrue("Node should have some children.", children.getNodesCount() != 0);
    System.out.println("#### Nodes:");
    //check that the nodes are of correct type
    Node[] childrenArr = children.getNodes(true);
    assertTrue(childrenArr.length >0 );
    for (Node n : childrenArr) {
      assertEquals(SongNode.class, n.getClass());
      System.out.println("...." + n);
    }
    System.out.println("#### testGetNodeDelegate has run");
    Thread.sleep(250); // give it some time to do printout

  }
}
