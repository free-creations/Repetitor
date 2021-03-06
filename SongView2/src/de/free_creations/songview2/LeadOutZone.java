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
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;

/**
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
class LeadOutZone extends Zone {

  LeadOutZone(SongCanvas canvas) {
    super(canvas);
    setColor(Color.LIGHT_GRAY);
  }

  /**
   * Fills the region with a gradient
   * @return a Paint object that will be used to fill the Song- region.
   */
  @Override
  protected Paint makePaint() {

    return new GradientPaint(getLeftBorder() - 30, 0, canvas.getBackground(),
            getRightBorder(), 0, getColor());
  }

  @Override
  protected boolean isSweetspot(int x, int y) {
    int sw_right = getLeftBorder();
    int sw_left = sw_right - sweetspotWith;
    if (((sw_left) <= x)
            && ((sw_right) >= x)) {
      return true;
    }
    return false;
  }

  @Override
  protected void highLightSweetspot(Graphics2D g) {
    int x = getLeftBorder() - sweetspotWith;
    //Color transparentHighColor = ColorManager.DerivedColor(0, -0.3F, 0.3F, 0.6F);
    Color transparentHighColor = ColorManager.DerivedColor(0, -0.1F, 0.0F, .5F);
    g.setPaint(transparentHighColor);
    g.fillRect(x, 0, sweetspotWith, canvas.getDimensions().getViewportHeight());

  }

  @Override
  protected void redrawSweetspot() {
    canvas.repaintStripe(getLeftBorder() - sweetspotWith - 1, getLeftBorder() + 1);
  }

  @Override
  public int getLeftBorder() {
    return canvas.getDimensions().getLeadOutStartPixel();
  }

  @Override
  public int getRightBorder() {
    return canvas.getDimensions().getLoopEndPixel();
  }

  @Override
  protected void processLeftBorderChanges(Object eventTag, Object oldValue, Object newValue) {
    if (eventTag == Prop.LEADOUTSTART_PIXEL) {
      adjustLeftBorder((Integer) oldValue, (Integer) newValue);
    }
  }

  @Override
  protected void processRightBorderChanges(Object eventTag, Object oldValue, Object newValue) {
    if (eventTag == Prop.LOOPEND_PIXEL) {
      adjustRightBorder((Integer) oldValue, (Integer) newValue);
    }
  }

  @Override
  public void setDraggedBorder(int newValue) {
    canvas.getDimensions().setLeadOutStartPixel(newValue);
  }
}
