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

import java.awt.Graphics2D;

/**
 * All visible elements of the songview are layers. A layer shows the visible
 * representation of some element of a media. The {@link SongCanvasImpl} is
 * responsible to draw all layers.
 *
 * @author Harald Postner
 */
interface Layer {

  /**
   * Draw the layers visible representation on the given graphics.
   *
   * @param g
   */
  public void draw(Graphics2D g);

  /**
   * The exact meaning of an active or inactive layer depends on what the layer
   * represents, in general a layer is shown less colorfully when inactive.
   *
   * @return true if the object represented by this layer is active.
   */
  public boolean isActive();

  /**
   * Change the active status of this layer.
   *
   * @param value
   */
  public void setActive(boolean value);

  /**
   * Invoked when the mouse button has been clicked (pressed and released) on
   * the canvas.
   *
   * @param x_canvas x coordinate of the mouse position on the canvas expressed
   * in pixels.
   * @param y_canvas y coordinate of the mouse position on the canvas expressed
   * in pixels.
   */
  public void mouseClicked(int x_canvas, int y_canvas);

  /**
   * Invoked when the mouse button has been pressed (but not released) on the
   * canvas.
   *
   * @param x_canvas x coordinate of the mouse position on the canvas expressed
   * in pixels.
   * @param y_canvas y coordinate of the mouse position on the canvas expressed
   * in pixels.
   */
  public void mouseDown(int x_canvas, int y_canvas);

  /**
   * Invoked when the mouse button has been released on the canvas.
   *
   * @param x_canvas x coordinate of the mouse position on the canvas expressed
   * in pixels.
   * @param y_canvas y coordinate of the mouse position on the canvas expressed
   * in pixels.
   */
  public void mouseReleased(int x_canvas, int y_canvas);

  /**
   * This function is called by the canvas whenever the mouse is being dragged
   * to a new position.
   *
   * @param x the new X position of the mouse in the canvas coordinate system
   * (note that the offset to the viewport coordinate system is taken into
   * account)
   */
  public void mouseDragged(int x);

  /**
   * Indicate whether the layer participates in a dragging action.
   *
   * @param startDragging true if we start a dragging action. False if we end
   * the dragging action.
   * @param mouseX the start X-coordinate of the dragging action in the canvas
   * coordinate system.
   * @param mouseY the start Y-coordinate of the dragging action in the canvas
   * coordinate system.
   */
  public void setDraggingActivated(boolean startDragging, int mouseX, int mouseY);
}
