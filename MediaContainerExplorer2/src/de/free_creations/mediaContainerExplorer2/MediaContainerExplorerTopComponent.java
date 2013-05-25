/*
 * Copyright 2012 Harald Postner <Harald at free-creations.de>.
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
package de.free_creations.mediaContainerExplorer2;

import de.free_creations.mediacontainer2.MediaRootNode;
import de.free_creations.netBeansSong.SongNode;
import java.awt.Component;
import java.awt.Image;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.datatransfer.NewType;
import org.openide.util.datatransfer.PasteType;
import org.openide.windows.TopComponent;

/**
 * Explorer component that displays all currently open MediaContainers. see also
 * the JavaDoc for "org.openide.explorer.ExplorerUtils" and
 * http://platform.netbeans.org/tutorials/nbm-selection-2.html
 * http://platform.netbeans.org/tutorials/nbm-nodesapi.html
 *
 * @author Harald Postner <Harald at free-creations.de>
 */
@ConvertAsProperties(dtd = "-//de.free_creations.mediaContainerExplorer2//MediaContainerExplorer//EN",
        autostore = false)
@TopComponent.Description(preferredID = "MediaContainerExplorerTopComponent",
        iconBase = "de/free_creations/mediaContainerExplorer/folderClosed.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "explorer", openAtStartup = true)
@ActionID(category = "Window",
        id = "de.free_creations.mediaContainerExplorer2.MediaContainerExplorerTopComponent")
@ActionReference(path = "Menu/Window" /*
         * , position = 333
         */)
@TopComponent.OpenActionRegistration(displayName = "#CTL_MediaContainerExplorerAction",
        preferredID = "MediaContainerExplorerTopComponent")
@Messages({
  "CTL_MediaContainerExplorerAction=Media",
  "CTL_MediaContainerExplorerTopComponent=Media Catalogue",
  "HINT_MediaContainerExplorerTopComponent=Shows all the available media."
})
public final class MediaContainerExplorerTopComponent extends TopComponent
        implements ExplorerManager.Provider, Lookup.Provider {

  private static final Logger logger = Logger.getLogger(MediaContainerExplorerTopComponent.class.getName());
  static final String PREF_MEDIAFOLDER = "mediafolder";
  private final ExplorerManager explorerManager = new ExplorerManager();
  private final MediaRootNode rootNode = MediaRootNode.create();
  private final BeanTreeView treeView;
  private PropertyChangeListener nodesListener = new PropertyChangeListener() {
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
        Node[] nodes = explorerManager.getSelectedNodes();
        if (nodes != null) {
          if (nodes.length > 0) {
            Node node = nodes[0];
            if (node instanceof SongNode) {
              treeView.expandNode(node);
            }
          }
        }
      }
    }
  };

  public MediaContainerExplorerTopComponent() {
    initComponents();
    setName(Bundle.CTL_MediaContainerExplorerTopComponent());
    setToolTipText(Bundle.HINT_MediaContainerExplorerTopComponent());
    putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE);
    treeView = (BeanTreeView) treeViewScrollPanel;

    explorerManager.addPropertyChangeListener(nodesListener);

    ActionMap actionMap = getActionMap();
    //actionMap.put("delete", ExplorerUtils.actionDelete(explorerManager, true)); // or false
    //actionMap.put("rescan", new ReScanAction());
    // following line tells the top component which lookup should be associated with it
    associateLookup(ExplorerUtils.createLookup(explorerManager, actionMap));

    scanMediaDirectory();
    ExplorerUtils.activateActions(explorerManager, true);
    explorerManager.setRootContext(rootNode);
    treeView.setRootVisible(false);
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        treeViewScrollPanel = new BeanTreeView();

        setLayout(new java.awt.BorderLayout());

        treeViewScrollPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                treeViewScrollPanelMouseClicked(evt);
            }
        });
        add(treeViewScrollPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

  private void treeViewScrollPanelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_treeViewScrollPanelMouseClicked
    // TODO add your handling code here:
  }//GEN-LAST:event_treeViewScrollPanelMouseClicked
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane treeViewScrollPanel;
    // End of variables declaration//GEN-END:variables

  @Override
  public void componentOpened() {
    // TODO add custom code on component opening
  }

  @Override
  public void componentClosed() {
    // TODO add custom code on component closing
  }

  void writeProperties(java.util.Properties p) {
    // better to version settings since initial version as advocated at
    // http://wiki.apidesign.org/wiki/PropertyFiles
    p.setProperty("version", "1.0");
    // TODO store your settings
  }

  void readProperties(java.util.Properties p) {
    String version = p.getProperty("version");
    // TODO read your settings according to their version
  }

  @Override
  protected void componentActivated() {
    // It is (not?) agood idea to switch all listeners on when the
    // component is shown.
//    scanMediaDirectory();
//    ExplorerUtils.activateActions(explorerManager, true);
    //   logger.info(">>>>>>>  componentActivated()");
  }

  @Override
  protected void componentDeactivated() {
    // It is (not?) a good idea to switch all listeners off when the
    // component is hidden.
//    ExplorerUtils.activateActions(explorerManager, false);
//    logger.info(">>>>>>>  componentDeactivated()");
  }

  @Override
  public ExplorerManager getExplorerManager() {
    return explorerManager;
  }

  protected void scanMediaDirectory() {
    File mediaDirectory = findMediaDirectory();
    if (mediaDirectory == null) {
      DialogDisplayer.getDefault().notify(
              new NotifyDescriptor.Message("Media Directory not found.",
              NotifyDescriptor.WARNING_MESSAGE));
      return;
    }
    rootNode.scanMediaDirectory(mediaDirectory);
  }

  protected File findMediaDirectory() {

    String mediaDirName = NbPreferences.forModule(MediaContainerExplorerTopComponent.class).
            get(MediaContainerExplorerTopComponent.PREF_MEDIAFOLDER,
            System.getProperty("user.home") + "/RepetitorMedia");
    File mediaDir = new File(mediaDirName);
    if (!mediaDir.exists()) {
      logger.log(Level.WARNING, "\"{0}\" not found.", mediaDirName);
      return null;
    }
    if (!mediaDir.isDirectory()) {
      logger.log(Level.WARNING, "\"{0}\" is not a directory.", mediaDirName);
      return null;
    }
    return mediaDir;
  }
}
