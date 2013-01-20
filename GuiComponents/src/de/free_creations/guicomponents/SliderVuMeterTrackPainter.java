/*
 * Source Downloaded at:
 */
package de.free_creations.guicomponents;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.Painter;

/**
 */
//public final class SliderVuMeterTrackPainter extends AbstractRegionPainter {
public final class SliderVuMeterTrackPainter implements Painter<JComponent> {

  private final static Logger logger = Logger.getLogger(SliderVuMeterTrackPainter.class.getName());
  private BufferedImage trackImage;
  private BufferedImage trackHighImage;
  private static final int insetX = 10;
  private final int srcWidth;
  private final int srcHeight;
  private static final Color SEMITRANSPARENT = new Color(0.0F, 0.0F, 0.0F, 0.25F);
  private static final Color TRANSPARENT = new Color(0.0F, 0.0F, 0.0F, 0.0F);
  private static final Color OPAQUE = new Color(0.0F, 0.0F, 0.0F, 1.0F);

  public SliderVuMeterTrackPainter() {
    URL trackUrl = SliderVuMeterTrackPainter.class.getResource("artwork/trackLargeLow.png");
    URL trackHighUrl = SliderVuMeterTrackPainter.class.getResource("artwork/trackLargeHigh.png");
    BufferedImage loadedTrackImage;
    try {
      loadedTrackImage = ImageIO.read(trackUrl);
    } catch (IOException ex) {
      logger.log(Level.SEVERE, null, ex);
      loadedTrackImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
    }
    BufferedImage loadedTrackHighImage;
    try {
      loadedTrackHighImage = ImageIO.read(trackHighUrl);
    } catch (IOException ex) {
      logger.log(Level.SEVERE, null, ex);
      loadedTrackHighImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
    }
    trackImage = loadedTrackImage;
    trackHighImage = loadedTrackHighImage;
    srcWidth = trackImage.getWidth();
    srcHeight = trackImage.getHeight();


  }

  @Override
  public void paint(Graphics2D g, JComponent t, int width, int height) {
    if (!(t instanceof JSlider)) {
      return;
    }
    SliderVuMeter slider = (SliderVuMeter) t;

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    //draw left inset
    g.drawImage(trackImage, //
            0, 0, insetX, srcHeight, //destination recangle
            0, 0, insetX, srcHeight, //source recangle
            null);
    //draw right inset
    g.drawImage(trackImage, //
            width - insetX, 0, width, srcHeight, //destination recangle
            srcWidth - insetX, 0, srcWidth, srcHeight, //source recangle
            null);
    //draw streched version of track
    g.drawImage(trackImage, //
            insetX, 0, width - insetX, srcHeight, //destination recangle
            insetX, 0, srcWidth - insetX, srcHeight, //source recangle
            null);

    int min = slider.getMinVuValue();
    int max = slider.getMaxVuValue();
    int vuValue = slider.getVuValue();

    if (vuValue > min) {
      float xHigh = (float) (vuValue - min) / (float) (max - min);
      if (slider.getOrientation() == JSlider.HORIZONTAL) {
        paintHighlight(g, width, height, xHigh);
      } else {
        paintInvertedHighlight(g, width, height, xHigh);
      }
    }


  }

  private void paintHighlight(Graphics2D g, int width, int height, float xHigh) {
    float left = .05F;
    xHigh = Math.max(xHigh, 0.0001F);

    float xlow = xHigh + left;
    xlow = Math.min(xlow, 1.0F);
    xHigh = Math.min(xHigh, xlow - 0.001F);

    BufferedImage offImg = new BufferedImage(srcWidth, srcHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = offImg.createGraphics();
    g2.drawRenderedImage(trackHighImage, null);
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN));


    //GradientPaint gp1 = new GradientPaint(-20, 0, new Color(0.0F, 0.0F, 0.0F, 0.3F), xHigh - 20, 0, new Color(0.0F, 0.0F, 0.0F, 1.0F));
    //g2.setPaint(gp1);
    Point2D start = new Point2D.Float(-3, 0);
    Point2D end = new Point2D.Float(srcWidth + 3, 0);
    float[] dist = {0.0f, xHigh, xlow};
    Color[] colors = {SEMITRANSPARENT, OPAQUE, TRANSPARENT};
    LinearGradientPaint gp1 =
            new LinearGradientPaint(start, end, dist, colors);
    g2.setPaint(gp1);
    g2.fillRect(0, 0, srcWidth, srcHeight);
    //draw left inset
    g.drawImage(offImg, //
            0, 0, insetX, srcHeight, //destination recangle
            0, 0, insetX, srcHeight, //source recangle
            null);
    //draw right inset
    g.drawImage(offImg, //
            width - insetX, 0, width, srcHeight, //destination recangle
            srcWidth - insetX, 0, srcWidth, srcHeight, //source recangle
            null);
    //draw streched version of track
    g.drawImage(offImg, //
            insetX, 0, width - insetX, srcHeight, //destination recangle
            insetX, 0, srcWidth - insetX, srcHeight, //source recangle
            null);


  }

  private void paintInvertedHighlight(Graphics2D g, int width, int height, float xHigh) {
    //draw highlight
    float left = .05F;
    xHigh = Math.max(xHigh, 0.0001F);

    float xlow = xHigh + left;
    xlow = Math.min(xlow, 1.0F);
    xHigh = Math.min(xHigh, xlow - 0.001F);

    BufferedImage offImg = new BufferedImage(srcWidth, srcHeight, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = offImg.createGraphics();
    g2.drawRenderedImage(trackHighImage, null);
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN));

    Point2D start = new Point2D.Float(-3, 0);
    Point2D end = new Point2D.Float(srcWidth + 3, 0);
    float[] dist = {0.0f, xHigh, xlow};
    Color[] colors = {SEMITRANSPARENT, OPAQUE, TRANSPARENT};
    LinearGradientPaint gp1 =
            new LinearGradientPaint(start, end, dist, colors);
    g2.setPaint(gp1);
    g2.fillRect(0, 0, srcWidth, srcHeight);
    //draw left inset
    g.drawImage(offImg, //
            width, 0, width - insetX, srcHeight, //destination recangle
            0, 0, insetX, srcHeight, //source recangle
            null);
    //draw right inset
    g.drawImage(offImg, //
            insetX, 0, 0, srcHeight, //destination recangle
            srcWidth - insetX, 0, srcWidth, srcHeight, //source recangle
            null);
    //draw streched version of track
    g.drawImage(offImg, //
            width - insetX, 0, insetX, srcHeight, //destination recangle
            insetX, 0, srcWidth - insetX, srcHeight, //source recangle
            null);


  }
}