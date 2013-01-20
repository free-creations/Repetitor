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
package de.free_creations.conductor;

import de.free_creations.midisong.SongSession;
import de.free_creations.midiutil.RPositionEx;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.Timer;

/**
 *
 * @author Harald Postner
 */
public class ConductorPanel extends JPanel {

  static final private Logger logger = Logger.getLogger(ConductorPanel.class.getName());
  final int imageWidth = 256;
  final int imageHeight = 256;
  public static final int MAXIMAGECOUNT = 120;
  public static final int BEATTYPES = 4;
  public static final int BEAT_1 = 0;
  public static final int BEAT_2 = 1;
  public static final int BEAT_3 = 2;
  public static final int BEAT_4 = 3;
  public static final int BEAT_1_IMAGECOUNT = 60;
  public static final int BEAT_2_IMAGECOUNT = 60;
  public static final int BEAT_3_IMAGECOUNT = 60;
  public static final int BEAT_4_IMAGECOUNT = 120;
  public static final int BEAT_1_START = 20;
  public static final int BEAT_2_START = 10;
  public static final int BEAT_3_START = 5;
  public static final int BEAT_4_START = 10;
  private BufferedImage[][] loadedImages = new BufferedImage[BEATTYPES][MAXIMAGECOUNT];
  private BufferedImage defaultImage = null;
  private volatile boolean imagesReady = false;
  private volatile SongSession session = null;
  private LoaderTask loaderTask = null;
  private int currentBeatType = 0;
  private long currentMeasure = -1;
  /**
   * the time-duration the conductor advances on the performance
   */
  final double totalAdvance = 0.050; // in seconds

  private class LoaderTask extends SwingWorker<Object, Object> {

    @Override
    protected Object doInBackground() throws Exception {
      String filename = "resources/default.png";
      URL imageUrl = ConductorPanel.class.getResource(filename);
      if (imageUrl == null) {
        throw new RuntimeException("Image not found " + filename);
      }
      defaultImage = ImageIO.read(imageUrl);

      for (int b = 0; b < BEATTYPES; b++) {
        for (int i = 0; i < MAXIMAGECOUNT; i++) {
          filename = getFilename(b, i);
          imageUrl = ConductorPanel.class.getResource(filename);
          if (imageUrl != null) {
            /*
             * Load and copy into Black- and- White BufferedImage
             */
            BufferedImage img = ImageIO.read(imageUrl);
            int w = img.getWidth(null);
            int h = img.getHeight(null);
            BufferedImage gray_Image = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
            Graphics g = gray_Image.getGraphics();
            g.drawImage(img, 0, 0, null);
            loadedImages[b][i] = gray_Image;
            g.dispose();
          } else {
            //image not found (that's OK provided i>59)
            if (i > 59) {
              loadedImages[b][i] = null;
            } else {
              throw new RuntimeException("Image not found " + filename);
            }
          }
        }
      }
      System.gc();
      imagesReady = true;
      return null;
    }

    @Override
    protected void done() {
      repaint();
    }

    private String getFilename(int beatType, int image) {
      switch (beatType) {
        case BEAT_1:
          return String.format("resources/_1_beat/%04d.png", image);
        case BEAT_2:
          return String.format("resources/_2_beat/%04d.png", image);
        case BEAT_3:
          return String.format("resources/_3_beat/%04d.png", image);
        case BEAT_4:
          return String.format("resources/_4_beat/%04d.png", image);
      }
      throw new RuntimeException("unknown beatType " + beatType);
    }
  }
  private static final int frameRate = 20;
  private static final int timerDelay = 1000 / frameRate;
  private Timer timer = new Timer(timerDelay, new ActionListener() {

    @Override
    public void actionPerformed(ActionEvent ae) {
      repaint();
    }
  });

  public ConductorPanel() {
    //setBorder(BorderFactory.createLineBorder(Color.black));
    setOpaque(true);
    setBackground(Color.white);
    loaderTask = new LoaderTask();
    loaderTask.execute();
  }

  /**
   * This procedure should only be called by the AWT thread...
   *
   * @param session
   */
  protected void sessionStarted(SongSession session) {
    this.session = session;
    timer.start();
  }

  /**
   * This procedure should only be called by the AWT thread...
   */
  protected void sessionStopped() {
    timer.stop();
    this.session = null;
    currentMeasure = -1;
    repaint();
  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(imageWidth, imageHeight);
  }

  private int getImageNumber(RPositionEx pos, int beatType) {
    double phase = pos.getBeat() / pos.getNumerator();
    switch (beatType) {
      case BEAT_1:
        return ((int) Math.round(phase * BEAT_1_IMAGECOUNT) + BEAT_1_START) % BEAT_1_IMAGECOUNT;
      case BEAT_2:
        return ((int) Math.round(phase * BEAT_2_IMAGECOUNT) + BEAT_2_START) % BEAT_2_IMAGECOUNT;
      case BEAT_3:
        return ((int) Math.round(phase * BEAT_3_IMAGECOUNT) + BEAT_3_START) % BEAT_3_IMAGECOUNT;
      case BEAT_4:
        return ((int) Math.round(phase * BEAT_4_IMAGECOUNT) + BEAT_4_START) % BEAT_4_IMAGECOUNT;
    }
    throw new RuntimeException("unknown beatType " + beatType);

  }

  private int getBeatType(RPositionEx pos, SongSession currentSession, double tick) {
    if (currentMeasure == pos.getMeasure()) {
      return currentBeatType;
    }
    currentMeasure = pos.getMeasure();
    double speed = currentSession.tickToEffectiveBPM(tick);
    if (speed > 180D) {
      currentBeatType = BEAT_1; // one beat per measure
      return currentBeatType;
    }

    switch (pos.getNumerator()) {
      case 1:
        currentBeatType = BEAT_1; // one beat per measure
        break;
      case 2:
        currentBeatType = BEAT_2; // two beats per measure
        break;
      case 3:
        currentBeatType = BEAT_3; // three beats per measure
        break;
      case 4:
        if (speed < 100) {
          currentBeatType = BEAT_4; // four beats per measure
        } else {
          currentBeatType = BEAT_2; // two beats per measure
        }
        break;
      case 6:
        currentBeatType = BEAT_2; // two beats per measure
        break;
      case 12:
        currentBeatType = BEAT_2; // two beats per measure ??
        break;
      default:
        currentBeatType = BEAT_1;
    }

    return currentBeatType;
  }

  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);

    if (imagesReady) {
      if (session == null) {
        g.drawImage(defaultImage, (getWidth() - imageWidth) / 2, (getHeight() - imageHeight) / 2, null);
      } else {

        double tick = session.getTickPosition(totalAdvance);
        RPositionEx pos = session.tickToRPositionEx(tick);
        int beatType = getBeatType(pos, session, tick);
        g.drawImage(loadedImages[beatType][getImageNumber(pos, beatType)], (getWidth() - imageWidth) / 2, (getHeight() - imageHeight) / 2, null);
      }
    } else {
      if (loaderTask.isDone()) {
        try {
          loaderTask.get();
        } catch (InterruptedException ex) {
          logger.log(Level.WARNING, "Error in loading images.", ex);
        } catch (ExecutionException ex) {
          logger.log(Level.WARNING, "Error in loading images.", ex);
        }
      } else {
        g.drawString("Loading...", 30, 30);
      }
    }

  }
}
