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
  private final String song;
  private static String previousPhrase = "";
  private static String previousNumber = "0";
  private static String previousPage = "";

  /**
   * Creates new form SaveLessonDialog
   */
  public SaveLessonDialog(LessonProperties lesson, File lessonsDirectory) {
    initComponents();
    this.song = lesson.getSong();
    edFirstBar.setText(lesson.getFirstBar());
    edPhrase.setText(previousPhrase);
    edLessonNumber.setText(Long.toString(previousNumberAsLong()+1));
    edDescription.setText(lesson.getDescription());
    cbxCategory.setSelectedItem(lesson.getCategory());
    lblDirectory.setText(lessonsDirectory.getAbsolutePath());
    edPage.setText(previousPage);
    this.lesson = lesson;
    updateDescriptionAndFilename();
  }

  private long previousNumberAsLong() {
    try {
      return Long.valueOf(previousNumber);
    } catch (NumberFormatException ignored) {
      return 0;
    }
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
    edFirstBar = new javax.swing.JTextField();
    jLabel3 = new javax.swing.JLabel();
    cbxCategory = new javax.swing.JComboBox();
    jLabel4 = new javax.swing.JLabel();
    edDescription = new javax.swing.JTextField();
    jLabel5 = new javax.swing.JLabel();
    edFilename = new javax.swing.JTextField();
    jLabel6 = new javax.swing.JLabel();
    lblDirectory = new javax.swing.JLabel();
    jLabel7 = new javax.swing.JLabel();
    edLessonNumber = new javax.swing.JTextField();
    jLabel8 = new javax.swing.JLabel();
    edPhrase = new javax.swing.JTextField();

    org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(SaveLessonDialog.class, "SaveLessonDialog.jLabel1.text")); // NOI18N

    edPage.setText(org.openide.util.NbBundle.getMessage(SaveLessonDialog.class, "SaveLessonDialog.edPage.text")); // NOI18N
    edPage.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        edPageActionPerformed(evt);
      }
    });
    edPage.addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusLost(java.awt.event.FocusEvent evt) {
        edPageFocusLost(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(SaveLessonDialog.class, "SaveLessonDialog.jLabel2.text")); // NOI18N

    edFirstBar.setText(org.openide.util.NbBundle.getMessage(SaveLessonDialog.class, "SaveLessonDialog.edFirstBar.text")); // NOI18N
    edFirstBar.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        edFirstBarActionPerformed(evt);
      }
    });
    edFirstBar.addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusLost(java.awt.event.FocusEvent evt) {
        edFirstBarFocusLost(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(SaveLessonDialog.class, "SaveLessonDialog.jLabel3.text")); // NOI18N

    cbxCategory.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Sopran", "Alt", "Tenor", "Bass" }));
    cbxCategory.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        cbxCategoryActionPerformed(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(SaveLessonDialog.class, "SaveLessonDialog.jLabel4.text")); // NOI18N

    edDescription.setText(org.openide.util.NbBundle.getMessage(SaveLessonDialog.class, "SaveLessonDialog.edDescription.text")); // NOI18N
    edDescription.setEnabled(false);

    org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(SaveLessonDialog.class, "SaveLessonDialog.jLabel5.text")); // NOI18N

    edFilename.setText(org.openide.util.NbBundle.getMessage(SaveLessonDialog.class, "SaveLessonDialog.edFilename.text")); // NOI18N
    edFilename.setEnabled(false);

    org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(SaveLessonDialog.class, "SaveLessonDialog.jLabel6.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(lblDirectory, org.openide.util.NbBundle.getMessage(SaveLessonDialog.class, "SaveLessonDialog.lblDirectory.text")); // NOI18N

    org.openide.awt.Mnemonics.setLocalizedText(jLabel7, org.openide.util.NbBundle.getMessage(SaveLessonDialog.class, "SaveLessonDialog.jLabel7.text")); // NOI18N

    edLessonNumber.setText(org.openide.util.NbBundle.getMessage(SaveLessonDialog.class, "SaveLessonDialog.edLessonNumber.text")); // NOI18N
    edLessonNumber.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        edLessonNumberActionPerformed(evt);
      }
    });
    edLessonNumber.addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusLost(java.awt.event.FocusEvent evt) {
        edLessonNumberFocusLost(evt);
      }
    });

    org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(SaveLessonDialog.class, "SaveLessonDialog.jLabel8.text")); // NOI18N

    edPhrase.setText(org.openide.util.NbBundle.getMessage(SaveLessonDialog.class, "SaveLessonDialog.edPhrase.text")); // NOI18N
    edPhrase.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        edPhraseActionPerformed(evt);
      }
    });
    edPhrase.addFocusListener(new java.awt.event.FocusAdapter() {
      public void focusLost(java.awt.event.FocusEvent evt) {
        edPhraseFocusLost(evt);
      }
    });

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
          .addComponent(jLabel6)
          .addComponent(jLabel7)
          .addComponent(jLabel8))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
          .addComponent(lblDirectory, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
          .addComponent(edDescription)
          .addComponent(edFilename)
          .addGroup(layout.createSequentialGroup()
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
              .addComponent(edPage)
              .addComponent(edFirstBar, javax.swing.GroupLayout.DEFAULT_SIZE, 82, Short.MAX_VALUE)
              .addComponent(edLessonNumber)
              .addComponent(cbxCategory, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGap(0, 231, Short.MAX_VALUE))
          .addComponent(edPhrase))
        .addContainerGap())
    );
    layout.setVerticalGroup(
      layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
      .addGroup(layout.createSequentialGroup()
        .addGap(3, 3, 3)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel7)
          .addComponent(edLessonNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel1)
          .addComponent(edPage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel2)
          .addComponent(edFirstBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel8)
          .addComponent(edPhrase, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
          .addComponent(jLabel3)
          .addComponent(cbxCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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

  private void edLessonNumberActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_edLessonNumberActionPerformed
    updateDescriptionAndFilename();
  }//GEN-LAST:event_edLessonNumberActionPerformed

  private void edPageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_edPageActionPerformed
    updateDescriptionAndFilename();
  }//GEN-LAST:event_edPageActionPerformed

  private void edFirstBarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_edFirstBarActionPerformed
    updateDescriptionAndFilename();
  }//GEN-LAST:event_edFirstBarActionPerformed

  private void cbxCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbxCategoryActionPerformed
    updateDescriptionAndFilename();
  }//GEN-LAST:event_cbxCategoryActionPerformed

  private void edLessonNumberFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_edLessonNumberFocusLost
    updateDescriptionAndFilename();
  }//GEN-LAST:event_edLessonNumberFocusLost

  private void edPageFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_edPageFocusLost
    updateDescriptionAndFilename();
  }//GEN-LAST:event_edPageFocusLost

  private void edFirstBarFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_edFirstBarFocusLost
    updateDescriptionAndFilename();
  }//GEN-LAST:event_edFirstBarFocusLost

  private void edPhraseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_edPhraseActionPerformed
    updateDescriptionAndFilename();
  }//GEN-LAST:event_edPhraseActionPerformed

  private void edPhraseFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_edPhraseFocusLost
    updateDescriptionAndFilename();
  }//GEN-LAST:event_edPhraseFocusLost
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JComboBox cbxCategory;
  private javax.swing.JTextField edDescription;
  private javax.swing.JTextField edFilename;
  private javax.swing.JTextField edFirstBar;
  private javax.swing.JTextField edLessonNumber;
  private javax.swing.JTextField edPage;
  private javax.swing.JTextField edPhrase;
  private javax.swing.JLabel jLabel1;
  private javax.swing.JLabel jLabel2;
  private javax.swing.JLabel jLabel3;
  private javax.swing.JLabel jLabel4;
  private javax.swing.JLabel jLabel5;
  private javax.swing.JLabel jLabel6;
  private javax.swing.JLabel jLabel7;
  private javax.swing.JLabel jLabel8;
  private javax.swing.JLabel lblDirectory;
  // End of variables declaration//GEN-END:variables

  public String getFilename() {
    updateDescriptionAndFilename();
    return edFilename.getText();
  }

  public LessonProperties getLessonProperties() {
    updateDescriptionAndFilename();
    lesson.setFirstBar(edFirstBar.getText());
    lesson.setDescription(edDescription.getText());
    lesson.setPhrase(edPhrase.getText());
    previousPhrase = edPhrase.getText();
    lesson.setLessonNumber(edLessonNumber.getText());
    lesson.setCategory(cbxCategory.getSelectedItem().toString());
    previousNumber = edLessonNumber.getText();
    previousPage = edPage.getText();
    return lesson;
  }

  private void updateDescriptionAndFilename() {
    int lessonNumber = 0;
    try {
      lessonNumber = Integer.parseInt(edLessonNumber.getText());
    } catch (NumberFormatException ignore) {
    }


    String desc = String.format("%s(%2d) Seite %s Takt %s \"%s\"",
            cbxCategory.getSelectedItem(),
            lessonNumber,
            edPage.getText(),
            edFirstBar.getText(),
            edPhrase.getText());
    edDescription.setText(desc);

    String filename = String.format("%s_%s%02d",
            song,
            cbxCategory.getSelectedItem(),
            lessonNumber);
    edFilename.setText(filename);

  }
}
