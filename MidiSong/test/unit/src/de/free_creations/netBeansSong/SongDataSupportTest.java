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

import org.openide.nodes.Node;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.LocalFileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class SongDataSupportTest {

  private FileObject songFile;

  @Before
  public void setUp() throws URISyntaxException, IOException, PropertyVetoException {

    URL dataDirURL = this.getClass().getResource("../midisong/resources");
    File dataDir = new File(dataDirURL.toURI());
    assertTrue(dataDir.exists());

    LocalFileSystem fs = new LocalFileSystem();
    fs.setRootDirectory(dataDir);

    songFile = fs.findResource("sanctusAll.xml");
    assertNotNull(songFile);

  }

  /**
   * This test verifies that the MIME-resolver and the Data-loader do their job
   * for the song files.
   *
   * @throws DataObjectNotFoundException if this exception is thrown, than the
   * test-setup is buggy.
   */
  @Test
  public void testDataLoader() throws DataObjectNotFoundException {
    DataObject songFileSupport = DataObject.find(songFile);
    assertNotNull(songFileSupport);
    assertEquals(SongDataSupport.class, songFileSupport.getClass());
  }

  /**
   * Make sure that a song file is represented by the correct kind of node.
   */
  @Test
  public void testGetNodeDelegate() throws DataObjectNotFoundException {
    DataObject songFileSupport = DataObject.find(songFile);
    Node node = songFileSupport.getNodeDelegate();
    assertNotNull(node);
    assertEquals(SongNode.class, node.getClass());
  }
}
