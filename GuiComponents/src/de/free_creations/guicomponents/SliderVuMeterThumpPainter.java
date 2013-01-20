/*
 * Source Downloaded at:
 */
package de.free_creations.guicomponents;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.Painter;

/**
 */
//public final class SliderVuMeterTrackPainter extends AbstractRegionPainter {
public final class SliderVuMeterThumpPainter implements Painter<JComponent> {

  private final BufferedImage thumpImage;
  private final int srcWidth;
  private final int srcHeight;

  public SliderVuMeterThumpPainter(boolean prelight, boolean enabled, boolean active) throws IOException {
    URL thumpUrl;

    if (enabled) {
      if (active) {
        thumpUrl = getResourceURL("artwork/thumpActive.png");
      } else if (prelight) {
        thumpUrl = getResourceURL("artwork/thumpPrelight.png");
      } else {
        thumpUrl = getResourceURL("artwork/thump.png");
      }
    } else {
      thumpUrl = getResourceURL("artwork/thumpDisabled.png");
    }
    thumpImage = ImageIO.read(thumpUrl);


    srcWidth = thumpImage.getWidth();
    srcHeight = thumpImage.getHeight();
  }
  
  private URL getResourceURL(String path){
    URL  result = SliderVuMeterThumpPainter.class.getResource(path);
    if(result == null){
      throw new RuntimeException("Resource not found:"+path);
    }
    return result;
  }

  @Override
  public void paint(Graphics2D g, JComponent t, int width, int height) {

    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


    //draw streched version of track
    g.drawImage(thumpImage, //
            0, 0, width, height, //destination recangle
            0, 0, srcWidth, srcHeight, //source recangle
            null);

  }
}