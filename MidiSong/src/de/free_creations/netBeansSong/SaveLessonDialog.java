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
package de.free_creations.netBeansSong;

import de.free_creations.midisong.LessonProperties;
import java.io.File;

/**
 *
 * @author Harald Postner
 */
public class SaveLessonDialog extends javax.swing.JPanel {

  private final LessonProperties lesson;

  /**
   * Creates new form SaveLessonDialog
   */
  public SaveLessonDialog(LessonProperties lesson, File lessonsDirectory) {
    initComponents();
    edDescription.setText(lesson.getDescription());
    lblDirectory.setText(lessonsDirectory.getAbsolutePath());
    this.lesson = lesson;
  }

  /**
   * This method is called from within the constructor to initialize the form.
   * WARNING: Do NOT modify this code. The content of this method is always
   * regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {

    jLabel1 = new javax.swing.JLabel();
    edPage = new javax.swing.JTextField();
    jLabel2 = new javax.swing.JLabel();
    jTextField1 = new javax.swing.JTextField();
    jLabel3 = new javax.swing.JLabel();
    jComboBox1 = new javax.swing.JComboBox();
    jLabel4 = new javax.swing.JLabel();
    edDescription = new javax.swing.JTextField();
    jLabel5 = new javax.swing.JLabel();
    edFilename = new javax.swing.JTextField();
    jLabel6 = new javax.swing.JLabel();
    lblDirectory = new javax.swing.JLabel();

    org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SaveLessonDialog.class, "SaveLessonDialog.jLabel1.text")); // NOI18N

    edPage.setText(org.openide.util.NbBundle.getMessage(SaveLessonDialog.class, "SaveLessonDialog.edPage.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SaveLessonDialog.class, "SaveLessonDialog.jLabel2.text")); // NOI18N

    jTextField1.setText(org.openide.util.NbBundle.getMessage(SaveLessonDialog.class, "SaveLessonDialog.jTextField1.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(SaveLessonDialog.class, "SaveLessonDialog.jLabel3.text")); // NOI18N

    jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Sopran", "Alt", "Tenor", "Bass" }));

    org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(SaveLessonDialog.class, "SaveLessonDialog.jLabel4.text")); // NOI18N

    edDescription.setText(org.openide.util.NbBundle.getMessage(SaveLessonDialog.class, "SaveLessonDialog.edDescription.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(SaveLessonDialog.class, "SaveLessonDialog.jLabel5.text")); // NOI18N

    edFilename.setText(org.openide.util.NbBundle.getMessage(SaveLessonDialog.class, "SaveLessonDialog.edFilename.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(SaveLessonDialog.class, "SaveLessonDialog.jLabel6.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(lblDirectory, org.openide.util.NbBundle.getMessage(SaveLessonDialog.class, "SaveLessonDialog.lblDirectory.text")); // NOI18N

    javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
    this.setLayout(layout);
    layout.setHorizontalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(jLabel2)
          .addComponent(jLabel1)
          .addComponent(jLabel3)
          .addComponent(jLabel4)
          .addComponent(jLabel5)
          .addComponent(jLabel6))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(lblDirectory, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(edDescription)
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
              .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                .addComponent(edPage)
                .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE))
              .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGap(0, 225, Short.MAX_VALUE))
          .addComponent(edFilename))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addContainerGap()
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel1)
          .addComponent(edPage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel2)
          .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel3)
          .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel4)
          .addComponent(edDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel5)
          .addComponent(edFilename, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel6)
          .addComponent(lblDirectory))
        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
    );
  }// </editor-fold>//GEN-END:initComponents
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JTextField edDescription;
  private javax.swing.JTextField edFilename;
  private javax.swing.JTextField edPage;
  private javax.swing.JComboBox jComboBox1;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JLabel jLabel5;
  private javax.swing.JLabel jLabel6;
  private javax.swing.JTextField jTextField1;
  private javax.swing.JLabel lblDirectory;
  // End of variables declaration//GEN-END:variables

  public String getFilename() {
    return edFilename.getText();
  }

  public LessonProperties getLessonProperties() {
    lesson.setDescription(edDescription.getText());
    return lesson;
  }
}
