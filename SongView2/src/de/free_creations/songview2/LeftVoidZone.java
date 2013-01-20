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
 * The void zones represent the parts outside the loop area, the LeftVoidZone is
 * the goes from the start of the song to the start of the loop.
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
class LeftVoidZone extends Zone {

  LeftVoidZone(SongCanvas canvas) {
    super(canvas);

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
    canvas.repaintStripe(getLeftBorder(), getRightBorder() + sweetspotWith + 1);
  }

  /**
   * Decides which color is to be used to fill the area at the left of the loop.
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

  @Override
  public int getLeftBorder() {
    return canvas.getDimensions().getMinimumPixel();
  }

  @Override
  public int getRightBorder() {
    return canvas.getDimensions().getLoopStartPixel();
  }

  @Override
  protected boolean isSweetspot(int x, int y) {
    int sw_left = getRightBorder() + 1;
    int sw_right = sw_left + sweetspotWith;
    if (((sw_left) <= x)
            && ((sw_right) >= x)) {
      return true;
    }
    return false;
  }

  @Override
  protected void highLightSweetspot(Graphics2D g) {
    int x = getRightBorder();
    Color transparentHighColor = ColorManager.DerivedColor(0, -0.1F, 0.0F, .5F);
    g.setPaint(transparentHighColor);

    g.fillRect(//
            x, // X
            0, // Y
            sweetspotWith, // width
            canvas.getDimensions().getViewportHeight()); // height

  }

  @Override
  protected void redrawSweetspot() {
    canvas.repaintStripe(getRightBorder() + 1, getRightBorder() - sweetspotWith - 1);
  }

  @Override
  public void setDraggedBorder(int newPosition) {
    int oldPosition = resizingStartX;
    int snapMidi = canvas.getDimensions().calulateSnapMidiTick(oldPosition, newPosition);
    canvas.getDimensions().setLoopStartMidi(snapMidi);
  }

  @Override
  protected void processLeftBorderChanges(Object eventTag, Object oldValue, Object newValue) {
    if (eventTag == Prop.MINIMUM_PIXEL) {
      adjustLeftBorder((Integer) oldValue, (Integer) newValue);
    }
  }

  @Override
  protected void processRightBorderChanges(Object eventTag, Object oldValue, Object newValue) {
    if (eventTag == Prop.LOOPSTART_PIXEL) {
      adjustLeftBorder((Integer) oldValue, (Integer) newValue);
    }
  }
}
