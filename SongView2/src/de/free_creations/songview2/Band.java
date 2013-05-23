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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.Track;

/**
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
abstract class Band implements PropertyChangeListener, Layer {

  static final private Logger logger = Logger.getLogger(Band.class.getName());
  protected final SongCanvas canvas;
  public static final int PREF_BANDHEIGHT = 48;
  protected Track track = null;
  protected int resolution = 240;//ticks per quarter note.
  private int y;
  private int bandHeight = PREF_BANDHEIGHT;
  private boolean active = false;

  public Band(SongCanvas canvas) {
    this.canvas = canvas;
    canvas.getDimensions().addPropertyChangeListener(this);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    Object eventTag = evt.getPropertyName();
    if (eventTag == Prop.PIXELTOMIDIFACTOR) {
      adjustMaximum();
    }
  }

  /**
   * Get the value of bandHeight
   *
   * @return the value of bandHeight
   */
  public int getBandHeight() {
    return bandHeight;
  }

  public abstract int getTotalHeight();

  /**
   * Set the value of bandHeight
   *
   * @param bandHeight new value of bandHeight
   */
  public void setBandHeight(int height) {
    if (this.bandHeight == height) {
      return;
    }
    int oldHeight = this.bandHeight;
    this.bandHeight = height;
    processHeightChanged(oldHeight, height);

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

  public void setTrack(Track newTrack, int resolution) {
    track = newTrack;
    if (resolution > 0) {
      this.resolution = resolution;
    } else {
      resolution = 240;
    }
    processTrack(newTrack);
    adjustMaximum();
    invalidate();
  }

  protected void invalidate() {
    int x = canvas.getDimensions().getMinimumPixel();
    int width = canvas.getDimensions().getMaximumPixel() - canvas.getDimensions().getMinimumPixel();
    canvas.repaintRectangle(x, getY(), width, getTotalHeight());

  }

  protected abstract void processTrack(Track newTrack);

  /**
   * This routine must react on a change of the height.
   *
   * @param oldHeight
   * @param height
   */
  protected void processHeightChanged(int oldHeight, int height) {
  }

  private void adjustMaximum() {
    if (track != null) {
      long oldMax = canvas.getDimensions().getMaximumMidi();
      if (track.ticks() > oldMax) {
        logger.log(Level.FINER, "adjustMaximum():setting MaximumMidi to {0} .", track.ticks());
        canvas.getDimensions().setMaximumMidi(track.ticks());
      }
    }
  }

  /**
   * An active band may use a different color to paint itself.
   *
   * @return true if the object represented by this layer is active.
   */
  @Override
  public boolean isActive() {
    return active;
  }

  /**
   * Change the active status of this band.
   *
   * @param value
   */
  @Override
  public void setActive(boolean value) {
    active = value;
  }

  /**
   * Invoked when the mouse button has been clicked (pressed and released) on
   * the canvas. this implementation does nothing.
   *
   * @param x_canvas X position of the mouse in the canvas coordinate system
   * (note that the offset to the viewport coordinate system is taken into
   * account)
   * @param y_canvas Y position of the mouse in the canvas coordinate system
   * (note that the offset to the viewport coordinate system is taken into
   * account)
   */
  @Override
  public void mouseClicked(int x_canvas, int y_canvas) {
  }

  /**
   * This function is called by the canvas whenever the mouse is being dragged
   * to a new position.
   *
   * The default implementation ignores these events. Can be overwritten as in
   * Director band.
   *
   * @param x the new X position of the mouse in the canvas coordinate system
   * (note that the offset to the viewport coordinate system is taken into
   * account)
   */
  @Override
  public void mouseDragged(int x) {
  }

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
  @Override
  public void setDraggingActivated(boolean startDragging, int mouseX, int mouseY) {
  }
}
