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
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * see also:
 * http://docs.oracle.com/javase/tutorial/uiswing/lookandfeel/_nimbusDefaults.html
 * @author Harald Postner
 */
public class TestSliderVuMeter extends JFrame {

  /** Creates new form MainFrame */
  public TestSliderVuMeter() {
    initComponents();
  }

  private void initComponents() {
    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
    final SliderVuMeter sliderHorizonal = new SliderVuMeter(JSlider.HORIZONTAL);
    final SliderVuMeter sliderVertical = new SliderVuMeter(JSlider.VERTICAL);
    final JCheckBox chbEnabled = new JCheckBox("Enable");
    //sliderVertical.setInverted(true);
    getContentPane().add(sliderHorizonal, BorderLayout.NORTH);
    getContentPane().add(sliderVertical, BorderLayout.WEST);
    getContentPane().add(chbEnabled, BorderLayout.SOUTH);
    sliderHorizonal.setMinimum(-80);
    sliderHorizonal.setMaximum(0);
    sliderVertical.setMinimum(-80);
    sliderVertical.setMaximum(0);
    sliderVertical.setVuValue(sliderHorizonal.getValue());
    sliderHorizonal.setVuValue(sliderVertical.getValue());

    sliderHorizonal.addChangeListener(
            new ChangeListener() {

              @Override
              public void stateChanged(ChangeEvent e) {

                sliderVertical.setVuValue(sliderHorizonal.getValue());
              }
            });
    sliderVertical.addChangeListener(
            new ChangeListener() {

              @Override
              public void stateChanged(ChangeEvent e) {
                sliderHorizonal.setVuValue(sliderVertical.getValue());

              }
            });
    chbEnabled.setSelected(true);
    chbEnabled.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(ChangeEvent e) {
        sliderHorizonal.setEnabled(chbEnabled.isSelected());
        sliderVertical.setEnabled(chbEnabled.isSelected());
      }
    });
    setLocationRelativeTo(null); // center it
    pack();
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    /* Set the Nimbus look and feel */
    //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
     * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
     */
    try {
      for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
          javax.swing.UIManager.setLookAndFeel(info.getClassName());
          break;
        }
      }
    } catch (ClassNotFoundException ex) {
      java.util.logging.Logger.getLogger(TestSliderVuMeter.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (InstantiationException ex) {
      java.util.logging.Logger.getLogger(TestSliderVuMeter.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      java.util.logging.Logger.getLogger(TestSliderVuMeter.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (javax.swing.UnsupportedLookAndFeelException ex) {
      java.util.logging.Logger.getLogger(TestSliderVuMeter.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
    //</editor-fold>

    /* Create and display the form */
    final TestSliderVuMeter mainFrame = new TestSliderVuMeter();
    java.awt.EventQueue.invokeLater(new Runnable() {

      @Override
      public void run() {
        //SwingUtilities.updateComponentTreeUI(mainFrame);

        mainFrame.setVisible(true);

      }
    });
  }
}
