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

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Image;
import java.awt.image.ImageObserver;

/**
 * This interface represents the surface on which the different Layers (like
 * tracks, regions etc.) are painted.
 *
 * @author Harald Postner <Harald at H-Postner.de>
 * @see {@link Layer}
 */
interface SongCanvas extends ImageObserver {

  /**
   * The background color of the songview window.
   *
   * @return the background color
   */
  public Color getBackground();

  /**
   * Repaints the specified rectangle of the viewport.
   *
   * @param x the x coordinate of the left top edge in canvas space.
   * @param y the y coordinate of the left top edge in canvas space.
   * @param width the width of the area to be repainted
   * @param height the height of the area to be repainted
   */
  public void repaintRectangle(int x,
          int y,
          int width,
          int height);

  /**
   * Repaint a stripe extending from top to bottom of the canvas. (X1, X2 need
   * not to be in ascending order)
   *
   * @param x1 one edge of the stripe (in canvas coordinates)
   * @param x2 the other edge of the stripe (in canvas coordinates)
   */
  public void repaintStripe(int x1, int x2);

  /**
   * Sets the mouse pointer to the specified icon.
   *
   * @param cursor
   */
  public void setMousePointer(Cursor mousePointer);

  public Dimensions getDimensions();

  public void repaint();

  /**
   * Creates an off-screen drawable image to be used for double buffering.
   *
   * @param width the specified width
   * @param height the specified height
   * @return an off-screen drawable image, which can be used for double
   * buffering. The return value may be null if the component is not
   * displayable. This will always happen if GraphicsEnvironment.isHeadless()
   * returns true.
   */
  public Image createImage(int width,
          int height);

  /**
   * At a given time only one layer can be dragged by the mouse.
   *
   * @return the layer that currently has the mouse focus or null if there is
   * none.
   */
  public Layer getDraggingActivatedLayer();

  /**
   * At a given time only one layer can be dragged by the mouse.
   *
   * @param layer the layer that shall currently gain the mouse focus or null if
   * there is none.
   */
  public void setDraggingActivatedLayer(Layer layer);

  /**
   * The SongCanvas is animated when the song is played. When animated the arrow
   * of the cursor is changed to orange in order to attract the eye.
   *
   * @return true if the SongCanvas is in animated state.
   */
  public boolean isAnimated();

  /**
   * The SongCanvas is animated when the song is played. When animated the arrow
   * of the cursor is changed to orange in order to attract the eye.
   *
   * @param animated the new state of the canvas.
   */
  public void setAnimated(boolean animated);
}
