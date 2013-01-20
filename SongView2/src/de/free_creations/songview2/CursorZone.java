/*
 * Copyright 2011 Harald Postner.
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
package de.free_creations.songview2;

import de.free_creations.songview.artwork.ArrowHighSVG;
import de.free_creations.songview.artwork.ArrowSVG;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import org.openide.util.NotImplementedException;

/**
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
class CursorZone extends Zone {

  private double halfWidth;
  private int height;
  private final BufferedImage animatedArrowImage;

  CursorZone(SongCanvas canvas) throws IOException {
    super(canvas);
    URL arrowUrl = getResourceURL("artwork/arrowAnimated.png");
    animatedArrowImage = ImageIO.read(arrowUrl);
    setColor(Color.RED);
    halfWidth = ArrowSVG.getOrigWidth() / 2.0D;
    height = ArrowSVG.getOrigHeight();
    setY(Band.PREF_BANDHEIGHT - height);
  }

  private URL getResourceURL(String path) {
    URL result = StartPointZone.class.getResource(path);
    if (result == null) {
      throw new RuntimeException("Resource not found:" + path);
    }
    return result;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public void setHeight(int height) {
    throw new NotImplementedException();
  }

  @Override
  protected boolean isSweetspot(int x, int y) {

    if (((getLeftBorder()) <= x)
            && ((getRightBorder()) >= x)
            && (y >= getY())) {
      return true;
    }
    return false;
  }

  @Override
  public void draw(Graphics2D g) {
    int viewPortWidth = canvas.getDimensions().getViewportWidthPixel();
    int viewPortLeft = canvas.getDimensions().getViewportLeftPixel();
    // do not paint, if region is completely outside of the right side of the view port
    if (getLeftBorder() > viewPortLeft + viewPortWidth) {
      return;
    }
    // do not paint, if region is completely outside of the left side of the view port
    if (getRightBorder() < viewPortLeft) {
      return;
    }

    int x = canvas.getDimensions().getCursorPixel();


    // save the original settings
    Paint originalPaint = g.getPaint();
    Stroke originalStroke = g.getStroke();
    AffineTransform originalTransform = g.getTransform();

    if (isDraggingActivated()) {
      g.setStroke(new BasicStroke(3.0f));
      g.setPaint(Color.LIGHT_GRAY);
      g.draw(new Line2D.Float(x, getY() + getHeight() / 2, x, canvas.getDimensions().getViewportHeight()));

    }
    g.setStroke(new BasicStroke(1.0f));
    g.setPaint(Color.BLACK);
    g.draw(new Line2D.Float(x, getY() + getHeight() / 2, x, canvas.getDimensions().getViewportHeight()));

    double left = x - halfWidth - ArrowSVG.getOrigX();
    double top = getY() - ArrowSVG.getOrigY();
    AffineTransform transform = (AffineTransform) originalTransform.clone();
    transform.translate(left, top);
    g.setTransform(transform);

    if (isAnimated()) {
      g.drawImage(animatedArrowImage, 0, 0, null);
    } else {
      if (!isDraggingActivated()) {
        ArrowSVG.paint(g);
      } else {
        ArrowHighSVG.paint(g);
      }
    }




    g.setTransform(originalTransform);
    g.setStroke(originalStroke);
    g.setPaint(originalPaint);
  }

  @Override
  protected void highLightSweetspot(Graphics2D g) {
    //see  "void draw(Graphics2D g)"
  }

  @Override
  protected void redrawSweetspot() {
    canvas.repaintStripe(getLeftBorder(), getRightBorder());
  }

  @Override
  public int getLeftBorder() {
    return canvas.getDimensions().getCursorPixel() - (int) halfWidth;
  }

  @Override
  public int getRightBorder() {
    return canvas.getDimensions().getCursorPixel() + (int) halfWidth;
  }

  @Override
  protected void processLeftBorderChanges(Object eventTag, Object oldValue, Object newValue) {
    if (eventTag == Prop.CURSOR_PIXEL) {
      adjustLeftBorder((Integer) oldValue - (int) halfWidth, (Integer) newValue - (int) halfWidth);
    }
  }

  @Override
  protected void processRightBorderChanges(Object eventTag, Object oldValue, Object newValue) {
    if (eventTag == Prop.CURSOR_PIXEL) {
      adjustRightBorder((Integer) oldValue + (int) halfWidth, (Integer) newValue + (int) halfWidth);
    }
  }

  @Override
  public void setDraggedBorder(int newPosition) {
    int oldPosition = resizingStartX;
    int snapMidi = canvas.getDimensions().calulateSnapMidiTick(oldPosition, newPosition);
    canvas.getDimensions().setCursorMidi(snapMidi);
    canvas.getDimensions().setStartPointMidi(snapMidi);

  }

  /**
   * A cursor is animated when the song is played. The colour of an animated
   * arrow-head is changed to orange in order to attract the eye.
   *
   * @return true if the cursor is in animated state.
   */
  public boolean isAnimated() {
    return canvas.isAnimated();
  }
}
