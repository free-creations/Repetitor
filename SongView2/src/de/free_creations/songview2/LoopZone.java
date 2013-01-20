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
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
class LoopZone extends Zone {

  private int height;
  public static final int PREFERED_HEIGHT = Band.PREF_BANDHEIGHT / 8;

  LoopZone(SongCanvas canvas) {
    super(canvas);
    setColor(ColorManager.DerivedColor(0.0F, -0.2F, 0.0F, 1.0F));
    setHeight(PREFERED_HEIGHT);
    setY(Band.PREF_BANDHEIGHT);
  }

  /**
   * Get the value of height
   *
   * @return the value of height
   */
  @Override
  public int getHeight() {
    return height;
  }

  /**
   * Set the value of height
   *
   * @param height new value of height
   */
  @Override
  public void setHeight(int height) {
    this.height = height;
  }

  /**
   * Fills the region with a gradient
   *
   * @return a Paint object that will be used to fill the Song- region.
   */
  @Override
  protected Paint makePaint() {

    return getColor();
  }

  @Override
  protected boolean isSweetspot(int x, int y) {
    int sw_upper = getY();
    int sw_lower = sw_upper + getHeight();
    if (((sw_upper) <= y)
            && ((sw_lower) >= y)) {
      return true;
    }
    return false;
  }

  @Override
  protected void highLightSweetspot(Graphics2D g) {

    Color transparentHighColor = ColorManager.DerivedColor(0.0F, -0.1F, 0.3F, .7F);
    g.setPaint(transparentHighColor);
    g.fillRect(canvas.getDimensions().getViewportLeftPixel(), getY(),
            canvas.getDimensions().getViewportWidthPixel(), getHeight());

  }

  @Override
  protected void redrawSweetspot() {
    canvas.repaintRectangle(canvas.getDimensions().getViewportLeftPixel(), getY(),
            canvas.getDimensions().getViewportWidthPixel(), getHeight());

  }

  @Override
  public int getLeftBorder() {
    return canvas.getDimensions().getLoopStartPixel();
  }

  @Override
  public int getRightBorder() {
    return canvas.getDimensions().getLoopEndPixel();
  }

  /**
   * Filters those events that indicate that the LoopStart has changed and
   * adjusts the left border accordingly.
   *
   * @param eventTag
   * @param oldValue
   * @param newValue
   */
  @Override
  protected void processLeftBorderChanges(Object eventTag, Object oldValue, Object newValue) {
    if (eventTag == Prop.LOOPSTART_PIXEL) {
      adjustLeftBorder((Integer) oldValue, (Integer) newValue);
    }
  }

  /**
   * Filters those events that indicate that the LoopEnd has changed and adjusts
   * the right border accordingly.
   *
   * @param eventTag
   * @param eventTag
   * @param oldValue
   * @param newValue
   */
  @Override
  protected void processRightBorderChanges(Object eventTag, Object oldValue, Object newValue) {
    if (eventTag == Prop.LOOPEND_PIXEL) {
      adjustRightBorder((Integer) oldValue, (Integer) newValue);
    }
  }

  @Override
  public void setDraggedBorder(int newValue) {
    System.out.println("LoopZone.setDraggedBorder(" + newValue + ")");
    if (newValue > resizingStartX) {
      int midiLeft = canvas.getDimensions().floorQuarterMidiTick(resizingStartX);
      int midiRight = canvas.getDimensions().ceilQuarterMidiTick(newValue);
      canvas.getDimensions().setLoopStartMidi(midiLeft);
      canvas.getDimensions().setLoopEndMidi(midiRight);
    } else {
      int midiLeft = canvas.getDimensions().floorQuarterMidiTick(newValue);
      int midiRight = canvas.getDimensions().ceilQuarterMidiTick(resizingStartX);
      canvas.getDimensions().setLoopStartMidi(midiLeft);
      canvas.getDimensions().setLoopEndMidi(midiRight);
    }
  }
}
