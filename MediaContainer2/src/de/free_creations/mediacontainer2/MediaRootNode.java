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

import java.awt.Event;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Harald Postner
 */
@Messages({
  "LBL_RootNode=Media",
  "HINT_RootNode=Shows all available Media."
})
public class MediaRootNode extends AbstractNode {

  private MediaNodesFactory mediaNodesFactory;
  private File mediaDirectory = null;

  private MediaRootNode(Children children) {
    super(children);
    setDisplayName(Bundle.LBL_RootNode());
    setShortDescription(Bundle.HINT_RootNode());
    setIconBaseWithExtension("resources/folderOpen.png");
  }

  /**
   * Factory method to create new root nodes.
   *
   * @return a new root node.
   */
  public static MediaRootNode create() {
    MediaNodesFactory mnf = new MediaNodesFactory();
    MediaRootNode mrn = new MediaRootNode(Children.create(mnf, false));
    mrn.mediaNodesFactory = mnf;
    return mrn;
  }

  @Override
  public HelpCtx getHelpCtx() {
    return HelpCtx.DEFAULT_HELP;
  }

  @Override
  public Node cloneNode() {
    return create();
  }



  /**
   * Add a list of file-paths. File that do not really exist on disk, are
   * ignored. Files that are already in the list, are ignored.
   *
   * @param newFiles an array of files to be added
   */
  private void replacePathList(File[] newFiles) {
    mediaNodesFactory.replacePathList(newFiles);
  }

  private class ReScanAction extends AbstractAction {

    public ReScanAction() {
      putValue(NAME, "Rescan");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
      scanMediaDirectory( mediaDirectory);
    }
  }

  @Override
  public Action[] getActions(boolean context) {
    return new Action[]{new ReScanAction()};
  }

  public void scanMediaDirectory(File mediaDirectory) {

    if (mediaDirectory == null) {

      return;
    }
    this.mediaDirectory = mediaDirectory;

    File[] mediaFiles = mediaDirectory.listFiles();
    replacePathList(mediaFiles);
  }
}
