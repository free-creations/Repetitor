/*
 * Copyright 2012 harald.
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.*;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * An Object that represents an archive file (a ZIP file) that contains the
 * media files. see also
 * http://platform.netbeans.org/tutorials/nbm-filetype.html
 *
 * @author Harald Postner
 */
public class MediaContainerDataObject extends MultiDataObject {

  /**
   * Factory that is used to create Children-nodes attached to the node delegate
   * of a MediaContainerDataObject.
   */
  private class MediaContainerChildFactory extends ChildFactory<String> {

    @Override
    protected boolean createKeys(List<String> toPopulate) {
      // read the file META-INF/container.xml ......
      FileObject containerInfoFile = zipRoot.getFileObject("META-INF/container.xml");
      if (containerInfoFile == null) {
        throw new RuntimeException("Is not a valid Media container "
                + "(missing file \"META-INF/container.xml\")");
      }

      List<String> empty = Collections.emptyList();
      ContainerInfo containerInfo = new ContainerInfo(empty);//default initialisation (for the case that following statment failes)
      try {
        containerInfo = new ContainerInfo(containerInfoFile.getInputStream());
      } catch (JAXBException ex) {
        Exceptions.printStackTrace(ex);
      } catch (FileNotFoundException ex) {
        Exceptions.printStackTrace(ex);
      }
      List<String> FileURIs = containerInfo.getRootfiles();
      toPopulate.addAll(FileURIs);

      return true;
    }

    @Override
    protected Node createNodeForKey(String fileUri) {
      URI uri;
      try {
        uri = new URI(fileUri);
      } catch (URISyntaxException ex) {
        Node errorNode = new AbstractNode(Children.LEAF);
        errorNode.setDisplayName("Error 1: " + fileUri);
        Exceptions.printStackTrace(ex);
        return errorNode;
      }
      FileObject songFile = zipRoot.getFileObject(uri.getPath());
      if (songFile == null) {
        Node errorNode = new AbstractNode(Children.LEAF);
        errorNode.setDisplayName("Error 3: " + fileUri);
        Exceptions.printStackTrace(new RuntimeException("could not find" + fileUri + " in " + zipRoot));
        return errorNode;
      }
      DataObject songData;
      try {
        songData = DataObject.find(songFile);
      } catch (DataObjectNotFoundException ex) {
        Node errorNode = new AbstractNode(Children.LEAF);
        errorNode.setDisplayName("Error 2: " + fileUri);
        Exceptions.printStackTrace(ex);
        return errorNode;
      }
      Node node = songData.getNodeDelegate();
      return node;
    }
  }
  private final FileObject zipRoot;

  public MediaContainerDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
    super(pf, loader);
    // open the primary file as a ZIP file
    FileObject fo = pf;
    if (fo == null) {
      throw new RuntimeException("Primary file is null.");
    }
    if (!FileUtil.isArchiveFile(fo)) {
      throw new RuntimeException("File is not recognized as a valid archive.");
    }
    zipRoot = FileUtil.getArchiveRoot(fo);
    if (zipRoot == null) {
      throw new RuntimeException("File is not recognized as a valid archive(2).");
    }
  }

  @Override
  protected Node createNodeDelegate() {
    return new DataNode(
            this,
            Children.create(new MediaContainerChildFactory(), true),
            getLookup());
  }

  @Override
  public Lookup getLookup() {
    return getCookieSet().getLookup();
  }
}
