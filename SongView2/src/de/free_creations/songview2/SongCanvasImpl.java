/*
 * Copyright 2011 Harald Postner .
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

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.BoundedRangeModel;
import javax.swing.JPanel;

/**
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
class SongCanvasImpl extends JPanel
        implements SongCanvas {

  private final Dimensions dimensions = new Dimensions();
  private HorizontalScrollModel horizontalScrollModel;
  static final int PREFEREDHEIGHT = 300;
  static final int PREFEREDWIDTH = 800;
  private ArrayList<Zone> zones = new ArrayList<Zone>();
  private ArrayList<Layer> layers = new ArrayList<Layer>();
  /**
   * The one and only layer that is currently being dragged (null if there is
   * none).
   */
  private Layer draggingActivatedLayer = null;
  private boolean animated = false;
  /**
   * A layer that might be chosen for dragging if no other has priority.
   */
  private Layer defaultDraggingLayer = null;
  private boolean defaultDraggingLayerIsDragged = false;

  public SongCanvasImpl() {

    dimensions.addPropertyChangeListener(new PropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent evt) {
        Object eventTag = evt.getPropertyName();
        if (eventTag == Prop.VIEWPORTLEFT_PIXEL) {
          repaint();
        }
      }
    });

    horizontalScrollModel = new HorizontalScrollModel(dimensions);

    setBackground(Color.white);
    setOpaque(true);

    addMouseListener(new MouseAdapter() {
      /**
       * Invoked when the mouse button has been clicked (pressed and released)
       * on the canvas.
       */
      @Override
      public void mouseClicked(MouseEvent e) {
        //transform the mouse coordinates to canvas coordinates
        int x_canvas = e.getX() + dimensions.getViewportLeftPixel();
        int y_canvas = e.getY();
        //and inform all layers 
        for (Layer layer : layers) {
          layer.mouseClicked(x_canvas, y_canvas);

        }
      }

      @Override
      public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
          int x_canvas = e.getX() + dimensions.getViewportLeftPixel();
          int y_canvas = e.getY();
          for (Layer layer : layers) {
            layer.mouseDown(x_canvas, y_canvas);
          }
          if (draggingActivatedLayer != null) {
            if (draggingActivatedLayer == defaultDraggingLayer) {
              defaultDraggingLayerIsDragged = true;
              setMousePointer(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

              draggingActivatedLayer.setDraggingActivated(true, x_canvas, y_canvas);
            }
          }

        }

      }

      @Override
      public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
          int x_canvas = e.getX() + dimensions.getViewportLeftPixel();
          int y_canvas = e.getY();
          for (Layer layer : layers) {
            layer.mouseReleased(x_canvas, y_canvas);
          }
          if (defaultDraggingLayerIsDragged) {
            defaultDraggingLayerIsDragged = false;
            setMousePointer(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            draggingActivatedLayer.setDraggingActivated(false, x_canvas, y_canvas);
          }
        }
      }
    });

    addMouseMotionListener(new MouseMotionListener() {
      /**
       * Invoked when the mouse cursor has been moved onto the canvas but no
       * buttons have been pushed.
       */
      @Override
      public void mouseMoved(MouseEvent e) {
        //transform the mouse coordinates to canvas coordinates
        int x_canvas = e.getX() + dimensions.getViewportLeftPixel();
        int y_canvas = e.getY();
        //and inform all zones until the first zone has consumed the the event
        for (Zone zone : zones) {
          zone.mouseMoved(x_canvas, y_canvas);
          if (zone.isDraggingActivated()) {
            break;
          }
        }
      }

      /**
       * Invoked when a mouse button is pressed on the canvas and then dragged.
       */
      @Override
      public void mouseDragged(MouseEvent e) {
        //transform the mouse coordinates to canvas coordinates
        int x_canvas = e.getX() + dimensions.getViewportLeftPixel();
        for (Layer layer : layers) {
          if (SongCanvasImpl.this.draggingActivatedLayer == layer) {
            layer.mouseDragged(x_canvas);
          }
        }
      }
    });

    addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        dimensions.setViewportHeight(SongCanvasImpl.this.getHeight());
        dimensions.setViewportWidthPixel(SongCanvasImpl.this.getWidth());
      }
    });

    dimensions.setViewportHeight(SongCanvasImpl.this.getHeight());
    dimensions.setViewportWidthPixel(SongCanvasImpl.this.getWidth());
  }

  @Override
  public void repaintRectangle(int x, int y, int width, int height) {
    repaint(x - dimensions.getViewportLeftPixel(), y, width, height);
  }

  @Override
  public final Dimensions getDimensions() {
    return dimensions;
  }

  @Override
  public void repaintStripe(int x1, int x2) {
    long left;
    long width;
    if (x1 > x2) {
      left = x2 - dimensions.getViewportLeftPixel();
      width = x1 - x2;
    } else {
      left = x1 - dimensions.getViewportLeftPixel();
      width = x2 - x1;
    }
    repaint((int) left - 8, 0, (int) width + 16, getHeight());

  }

  @Override
  public Dimension getPreferredSize() {
    return new Dimension(PREFEREDWIDTH, PREFEREDHEIGHT);
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2d = (Graphics2D) g;
    AffineTransform originalTransform = g2d.getTransform();
    RenderingHints originalRenderingHints = g2d.getRenderingHints();
    Paint originalPaint = g2d.getPaint();
    Stroke originalStroke = g2d.getStroke();
    Font originalFont = g2d.getFont();


    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

    AffineTransform translateAt = AffineTransform.getTranslateInstance(-dimensions.getViewportLeftPixel(), 0.0d);
    g2d.transform(translateAt);


    for (Layer layer : layers) {
      layer.draw(g2d);
    }

    // reset the transformation to what it was before.
    g2d.setFont(originalFont);
    g2d.setPaint(originalPaint);
    g2d.setStroke(originalStroke);
    g2d.setRenderingHints(originalRenderingHints);
    g2d.setTransform(originalTransform);

  }

  public BoundedRangeModel getHorizontalScrollModel() {
    return horizontalScrollModel;
  }

  /**
   * Add a zone to this Canvas. The Z-oder is determined by the sequence the
   * zones are added. The lastly added element is the element on top.
   *
   * @param newZone
   */
  public void addZone(Zone newZone) {
    zones.add(newZone);
    layers.add(newZone);
  }

  /**
   * Add a band to this Canvas. The Z-oder is determined by the sequence the
   * bands are added. The lastly added element is the element on top.
   *
   * @param newBand
   */
  public void addBand(Band newBand) {
    layers.add(newBand);
  }

  /**
   * Change the shape of the mouse pointer.
   *
   * @param mousePointer
   */
  @Override
  public void setMousePointer(Cursor mousePointer) {
    setCursor(mousePointer);
  }

  /**
   * {@inheritDoc }
   */
  @Override
  public Layer getDraggingActivatedLayer() {
    return draggingActivatedLayer;
  }

  /**
   * {@inheritDoc }
   */
  @Override
  public void setDraggingActivatedLayer(Layer layer) {
    if (this.draggingActivatedLayer != layer) {
      if (layer == null) {
        draggingActivatedLayer = defaultDraggingLayer;
      } else {
        draggingActivatedLayer = layer;
      }
    }
  }

  /**
   * Completely scratches all elements from this canvas.
   */
  public void clear() {
    draggingActivatedLayer = null;
    defaultDraggingLayer = null;
    //dimensions = new Dimensions();
    // horizontalScrollModel = new HorizontalScrollModel(dimensions);
    dimensions.setMaximumPixel(0);
    dimensions.setMinimumPixel(0);
    zones.clear();
    layers.clear();
  }

  @Override
  public boolean isAnimated() {
    return animated;
  }

  @Override
  public void setAnimated(boolean animated) {
    if (this.animated == animated) {
      return;
    }
    this.animated = animated;
    repaint();
  }

  @Override
  public Layer getDefaultDraggingLayer() {
    return defaultDraggingLayer;
  }

  @Override
  public void setDefaultDraggingLayer(Layer layer) {
    this.defaultDraggingLayer = layer;
    if (draggingActivatedLayer == null) {
      draggingActivatedLayer = layer;
    }
  }
}
