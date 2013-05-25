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

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.util.NotImplementedException;

/**
 * A "zone" is depicted in the "songview" window as a rectangular area extending
 * from top to bottom and represents a region of the media starting at a certain
 * point in time (the left border) and ending at an other point in time (the
 * right border). One or both the borders can be dragged with the mouse. This
 * class is the abstract base class for LoopZone, Lead-in and lead-out and more;
 * everything that is specific to mouse movement must be implemented in these
 * descendent classes.
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
abstract class Zone implements Layer, PropertyChangeListener {

  protected final SongCanvas canvas;
  private Color color = Color.GRAY;
  private boolean draggingActivated = false;
  protected final int sweetspotWith = 8;
  private int y = 0;
  protected int resizingStartX = 0;
  protected int resizingStartY = 0;
  private boolean active = false;

  Zone(SongCanvas canvas) {
    this.canvas = canvas;
    canvas.getDimensions().addPropertyChangeListener(this);

  }

  /**
   * Get the value of height
   *
   * @return the value of height
   */
  public int getHeight() {
    return canvas.getDimensions().getViewportHeight();
  }

  /**
   * Set the value of height
   *
   * @param height new value of height
   */
  public void setHeight(int height) {
    throw new NotImplementedException();
  }

  /**
   * Get the value of y
   *
   * @return the value of y
   */
  public int getY() {
    return y;
  }

  /**
   * Set the value of y
   *
   * @param y new value of y
   */
  public void setY(int y) {
    this.y = y;
  }

  /**
   * An active zone may use a different color to paint itself.
   *
   * @return true if the object represented by this layer is active.
   */
  @Override
  public boolean isActive() {
    return active;
  }

  /**
   * Change the active status of this zone.
   *
   * @param value
   */
  @Override
  public void setActive(boolean value) {
    active = value;
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    Object eventTag = evt.getPropertyName();
    Object oldValue = evt.getOldValue();
    Object newValue = evt.getNewValue();
    processLeftBorderChanges(eventTag, oldValue, newValue);
    processRightBorderChanges(eventTag, oldValue, newValue);
  }

  /**
   * This method is called every time a property has changed. Descendant classes
   * must filter those events that affect the position of the left border and
   * react accordingly.
   *
   * @param eventTag
   * @param oldValue
   * @param newValue
   */
  protected abstract void processLeftBorderChanges(Object eventTag,
          Object oldValue, Object newValue);

  /**
   * This method is called every time a property has changed. Descendant classes
   * must filter those events that affect the position of the right border and
   * react accordingly.
   *
   * @param eventTag
   * @param oldValue
   * @param newValue
   */
  protected abstract void processRightBorderChanges(Object eventTag,
          Object oldValue, Object newValue);

  /**
   * Get the position of leftBorder
   *
   * @return the position of leftBorder in canvas pixels.
   */
  public abstract int getLeftBorder();

  /**
   * This function is called every time this object has been dragged to a new
   * place.
   *
   * @param newPosition the new position in pixels.
   */
  public abstract void setDraggedBorder(int newPosition);

  /**
   * Invoked when the mouse button has been clicked (pressed and released) on
   * the canvas. this implementation does nothing.
   *
   * @param x_canvas x coordinate of the mouse position in pixels
   * @param y_canvas y coordinate of the mouse position in pixels
   */
  @Override
  public void mouseClicked(int x_canvas, int y_canvas) {
  }

  /**
   * Set the position of LeftBorder
   *
   * @param oldLeftBorder previous X-position of LeftBorder in canvas pixels.
   * @param newLeftBorder new X-position of LeftBorder in canvas pixels.
   */
  protected void adjustLeftBorder(int oldLeftBorder, int newLeftBorder) {
    // redraw the part that has moved
    canvas.repaintStripe(oldLeftBorder, newLeftBorder);
    redrawSweetspot();

    // redraw the interior (new gradient)
    canvas.repaintStripe(newLeftBorder, this.getRightBorder());
    redrawSweetspot();

  }

  /**
   * Get the position of rightBorder
   *
   * @return the position of rightBorder in canvas pixels.
   */
  public abstract int getRightBorder();

  /**
   * Set the position of rightBorder
   *
   * @param oldRightBorder previous X-position of rightBorder
   * @param rightBorder new X-position of rightBorder
   */
  protected void adjustRightBorder(int oldRightBorder, int newRightBorder) {

    // redraw the part that will move
    canvas.repaintStripe(oldRightBorder, newRightBorder);
    redrawSweetspot();

    // redraw the interior (new gradient)
    canvas.repaintStripe(getLeftBorder(), newRightBorder);
    redrawSweetspot();
  }

  /**
   * This function is called by the canvas whenever the mouse has moved.
   *
   * @param x the new X position of the mouse in the canvas coordinate system
   * (note that the offset to the viewport coordinate system is taken into
   * account)
   * @param y the new Y position of the mouse in the canvas coordinate system
   * (note that the offset to the viewport coordinate system is taken into
   * account)
   */
  public void mouseMoved(int x, int y) {
    if (isSweetspot(x, y)) {
      if (canvas.getDraggingActivatedLayer() == canvas.getDefaultDraggingLayer()) {
        setDraggingActivated(true, x, y);
      }
    } else {
      setDraggingActivated(false, x, y);
    }
  }

  /**
   * This function is called by the canvas whenever the mouse is being dragged
   * to a new position.
   *
   * @param x the new X position of the mouse in the canvas coordinate system
   * (note that the offset to the viewport coordinate system is taken into
   * account)
   */
  @Override
  public final void mouseDragged(int x) {
    if (isDraggingActivated()) {
      setDraggedBorder(x);
      int viewPortRight = canvas.getDimensions().getViewportLeftPixel() + canvas.getDimensions().getViewportWidthPixel();
      if (viewPortRight < x) {
        canvas.getDimensions().setViewportLeftPixel(x - canvas.getDimensions().getViewportWidthPixel());
      }
      if (x < canvas.getDimensions().getViewportLeftPixel()) {
        canvas.getDimensions().setViewportLeftPixel(x);
      }
    }
  }

  /**
   * Indicates whether a point with the given x coordinate belongs to the
   * sweet-spot or not.
   *
   * @param x
   * @return
   */
  protected abstract boolean isSweetspot(int x, int y);

  /**
   * Get the value of color
   *
   * @deprecated use make paint
   * @return the value of color
   */
  @Deprecated
  public Color getColor() {
    return color;
  }

  /**
   * Set the value of color
   *
   * @param color new value of color
   */
  public void setColor(Color color) {
    this.color = color;
  }

  @Override
  public void draw(Graphics2D g) {
    int viewPortWidth = canvas.getDimensions().getViewportWidthPixel();
    int viewPortLeft = canvas.getDimensions().getViewportLeftPixel();
    if (draggingActivated) {
      highLightSweetspot(g);
    }
    // do not paint, if region is completely outside on the left side of the view port
    if (getLeftBorder() > viewPortLeft + viewPortWidth) {
      return;
    }
    // do not paint, if region is completely outside on the left side of the view port
    if (getRightBorder() < viewPortLeft) {
      return;
    }

    int width = getRightBorder() - getLeftBorder();

    // set the paint (color and gradient)
    Paint originalPaint = g.getPaint();
    Stroke originalStroke = g.getStroke();
    g.setPaint(makePaint());


    Rectangle rect = new Rectangle(getLeftBorder(), getY(), width, getHeight());
    g.fill(rect);
    g.setPaint(Color.black);
    g.draw(rect);


    g.setStroke(originalStroke);
    g.setPaint(originalPaint);
  }

  /**
   * Determine the paint that shall be used to the song- region.
   *
   * @return a Paint object that will be used to fill the Song- region.
   */
  protected Paint makePaint() {
    return getColor();
  }

  /**
   * Indicate whether the zone participates in dragging action.
   *
   * @param startDragging true if we start a dragging action. False if we end
   * the dragging action.
   * @param mouseX the start X-coordinate of the dragging action.
   * @param mouseY the start Y-coordinate of the dragging action.
   */
  @Override
  public void setDraggingActivated(boolean startDragging, int mouseX, int mouseY) {
    if (draggingActivated == startDragging) {
      return;
    }
    draggingActivated = startDragging;
    redrawSweetspot();
    if (draggingActivated) {
      resizingStartX = mouseX;
      resizingStartY = mouseY;
      canvas.setMousePointer(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
      canvas.setDraggingActivatedLayer(this);
    } else {
      canvas.setMousePointer(Cursor.getDefaultCursor());
      canvas.setDraggingActivatedLayer(null);
    }
  }

  /**
   * Indicate whether this zone is currently performing a dragging action.
   *
   * @return true if the "Resizing" mode is active.
   */
  public boolean isDraggingActivated() {
    return draggingActivated;
  }

  /**
   * Perform the highlighting of the sweet-spot area.
   *
   * @param g
   */
  protected abstract void highLightSweetspot(Graphics2D g);

  /**
   * Activate "canvas.repaintRectangle()" for the sweet-spot area.
   */
  protected abstract void redrawSweetspot();

  @Override
  public void mouseDown(int x_canvas, int y_canvas) {
  }

  @Override
  public void mouseReleased(int x_canvas, int y_canvas) {
  }
}
