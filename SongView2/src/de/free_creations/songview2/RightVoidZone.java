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
import java.awt.Graphics2D;
import java.awt.Paint;

/**
 * The void zones represent the parts outside the loop area, the RightVoidZone
 * is the goes from the end of the loop to the end of the song.
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
class RightVoidZone extends Zone {

  RightVoidZone(SongCanvas canvas) {
    super(canvas);
  }

  /**
   * Decides which color is to be used to fill the area at the right of the
   * loop.
   *
   * @return a Paint object that will be used to fill the Song- region.
   */
  @Override
  protected Paint makePaint() {
    if (isActive()) {
      return ColorManager.getVoidZoneActiveColor();
    } else {
      return ColorManager.getVoidZoneInactiveColor();
    }
  }

  /**
   * make sure the layer is redrawn when the active status changes.
   *
   * @param value
   */
  @Override
  public void setActive(boolean value) {
    if (isActive() == value) {
      return;
    }
    super.setActive(value);
    canvas.repaintStripe(getLeftBorder() - sweetspotWith - 1, getRightBorder());
  }

  @Override
  public int getLeftBorder() {
    return canvas.getDimensions().getLoopEndPixel();
  }

  @Override
  public int getRightBorder() {
    return canvas.getDimensions().getMaximumPixel();
  }

  @Override
  protected boolean isSweetspot(int x, int y) {
    int sw_right = getLeftBorder() - 1;
    int sw_left = sw_right - sweetspotWith;
    if ((sw_right >= x)
            && (sw_left <= x)) {
      return true;
    }
    return false;
  }

  @Override
  protected void highLightSweetspot(Graphics2D g) {
    int x = getLeftBorder();
    Color transparentHighColor = ColorManager.DerivedColor(0, -0.1F, 0.0F, .5F);
    g.setPaint(transparentHighColor);

    g.fillRect(//
            x - sweetspotWith, // X
            0, // Y
            sweetspotWith, // width
            canvas.getDimensions().getViewportHeight()); // height

  }

  @Override
  protected void redrawSweetspot() {
    canvas.repaintStripe(getLeftBorder() - sweetspotWith - 1, getLeftBorder() + 1);
  }

  @Override
  public void setDraggedBorder(int newPosition) {
    int oldPosition = resizingStartX;
    int snapMidi = canvas.getDimensions().calulateSnapMidiTick(oldPosition, newPosition);
    canvas.getDimensions().setLoopEndMidi(snapMidi);
  }

  @Override
  protected void processLeftBorderChanges(Object eventTag, Object oldValue, Object newValue) {
    if (eventTag == Prop.LOOPEND_PIXEL) {
      adjustLeftBorder((Integer) oldValue, (Integer) newValue);
    }
  }

  @Override
  protected void processRightBorderChanges(Object eventTag, Object oldValue, Object newValue) {
    if (eventTag == Prop.MAXIMUM_PIXEL) {
      adjustLeftBorder((Integer) oldValue, (Integer) newValue);
    }
  }
}
