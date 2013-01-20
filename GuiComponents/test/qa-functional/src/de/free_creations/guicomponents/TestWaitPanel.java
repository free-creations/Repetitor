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
package de.free_creations.guicomponents;

import java.awt.BorderLayout;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * see also:
 *
 * @author Harald Postner
 */
public class TestWaitPanel extends JFrame {

  /**
   * Creates new form MainFrame
   */
  public TestWaitPanel() {
    initComponents();
  }

  private void initComponents() {

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

    final JCheckBox chbVisible = new JCheckBox("Visible");
    final WaitPanel waitPanel = new WaitPanel();

    waitPanel.setMessage("This is the Message: Hello World.");
    getContentPane().add(waitPanel, BorderLayout.CENTER);
    getContentPane().add(chbVisible, BorderLayout.SOUTH);
    chbVisible.setSelected(true);
    chbVisible.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(ChangeEvent e) {
        waitPanel.setVisible(chbVisible.isSelected());
      }
    });

    setLocationRelativeTo(null); // center it
    pack();
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    //configure Java.Util.Logging so that we see all messages from the WaitPanel.
    Logger logger = Logger.getLogger(WaitPanel.class.getName());
    logger.setLevel(Level.ALL);
    Handler handler = new ConsoleHandler();
    handler.setLevel(Level.ALL);
    logger.addHandler(handler);
    logger.setUseParentHandlers(false);
    /*
     * Set the Nimbus look and feel
     */
    //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
     * If Nimbus (introduced in Java SE 6) is not available, stay with the
     * default look and feel. For details see
     * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
     */
    try {
      for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
          javax.swing.UIManager.setLookAndFeel(info.getClassName());
          break;
        }
      }
    } catch (ClassNotFoundException ex) {
      java.util.logging.Logger.getLogger(TestWaitPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (InstantiationException ex) {
      java.util.logging.Logger.getLogger(TestWaitPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      java.util.logging.Logger.getLogger(TestWaitPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (javax.swing.UnsupportedLookAndFeelException ex) {
      java.util.logging.Logger.getLogger(TestWaitPanel.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
    //</editor-fold>

    /*
     * Create and display the form
     */
    final TestWaitPanel mainFrame = new TestWaitPanel();
    java.awt.EventQueue.invokeLater(new Runnable() {

      @Override
      public void run() {
        //SwingUtilities.updateComponentTreeUI(mainFrame);

        mainFrame.setVisible(true);

      }
    });
  }
}
