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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * A panel that shows a simple animation to show the user that he has to wait
 * for some time.
 *
 * @author Harald Postner
 */
public class WaitPanel extends JPanel {

  static final private Logger logger = Logger.getLogger(WaitPanel.class.getName());
  final int imageWidth = 200;
  final int imageHeight = 200;
  public static final int IMAGECOUNT = 8;
  private static final ImageIcon[] images = new ImageIcon[IMAGECOUNT];
  private String message = null;

  {
    for (int i = 0; i < IMAGECOUNT; i++) {
      URL imageUrl = WaitPanel.class.getResource(String.format("resources/waitAnimation%d.png", (i) % IMAGECOUNT));
      images[i] = new ImageIcon(imageUrl);
    }
  }

  /**
   * Get the value of message
   *
   * @return the value of message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Set the value of message
   *
   * @param message new value of message
   */
  public void setMessage(String message) {
    this.message = message;
  }
  private static final int timerDelay = 750; // milliseconds
  private int currentImage = 0;
  private Timer timer = new Timer(timerDelay, new ActionListener() {

    @Override
    public void actionPerformed(ActionEvent ae) {
      currentImage++;
      currentImage = currentImage % IMAGECOUNT;
      repaint();
    }
  });

  public WaitPanel() {

    super();
    setBackground(Color.white);
    setOpaque(true);
    setVisible(true);
    timer.start();
    addComponentListener(new ComponentListener() {

      @Override
      public void componentResized(ComponentEvent ce) {
      }

      @Override
      public void componentMoved(ComponentEvent ce) {
      }

      @Override
      public void componentShown(ComponentEvent ce) {
        timer.start();
      }

      @Override
      public void componentHidden(ComponentEvent ce) {
        timer.stop();
      }
    });
  }

  /**
   * Paints the animation.
   *
   * @param g
   */
  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;
    logger.log(Level.FINEST, "### painting image {0}.", currentImage);
    ImageIcon image = images[currentImage];
    if (image != null) {
      image.paintIcon(this, g, (getWidth() - imageWidth) / 2, (getHeight() - imageHeight) / 2);
    }
    if (message != null) {
      Rectangle2D bounds = g2d.getFontMetrics().getStringBounds(message, g2d);
      int messageWidth = (int) bounds.getWidth();
      int messageHeight = (int) bounds.getHeight();
      g2d.drawString(message, (getWidth() - messageWidth) / 2, (getHeight() - messageHeight));
    }

  }
}
