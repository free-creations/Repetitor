/*
 * Copyright 2012 Harald Postner.
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
package de.free_creations.guicomponents;

import java.awt.Graphics;
import java.awt.event.MouseWheelEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JSlider;
import javax.swing.UIDefaults;

/**
 * The SliderVuMeter is a combination of a {@link JSlider} and a VU meter
 * (device to display a signal level in Volume Units).
 *
 * @author Harald Postner
 */
public class SliderVuMeter extends JSlider {

  public static final String PROP_MAXVUVALUE = "maxVuValue";
  public static final String PROP_MINVUVALUE = "minVuValue";
  private final SliderVuMeterTrackPainter sliderVuMeterTrackPainter;
  private int vuValue;
  private static final Logger logger = Logger.getLogger(SliderVuMeter.class.getName());
  private boolean isCustomized = false;
  private int movePerMouseWheelTick;
  private int mouseWheelTicksFullRange = 16;
  private int maxVuValue = 0;
  private int minVuValue = -80;

  public SliderVuMeter(int orientation, int min, int max, int value) {
    super(orientation, min, max, value);
    vuValue = min;
    sliderVuMeterTrackPainter = new SliderVuMeterTrackPainter();
    enableEvents(java.awt.AWTEvent.MOUSE_WHEEL_EVENT_MASK);
    adjustMovePerMouseWheelTick();
  }

  public SliderVuMeter(int min, int max, int value) {
    this(JSlider.HORIZONTAL, min, max, value);
  }

  public SliderVuMeter(int min, int max) {
    this(JSlider.HORIZONTAL, min, max, (min + max) / 2);
  }

  public SliderVuMeter(int orientation) {
    this(orientation, 0, 100, 50);
  }

  public SliderVuMeter() {
    this(JSlider.HORIZONTAL, 0, 100, 50);
  }

  /**
   * Get the value of maxVuValue
   *
   * @return the value of maxVuValue
   */
  public int getMaxVuValue() {
    return maxVuValue;
  }

  /**
   * Set the value of maxVuValue
   *
   * @param maxVuValue new value of maxVuValue
   */
  public void setMaxVuValue(int maxVuValue) {
    int oldMaxVuValue = this.maxVuValue;
    this.maxVuValue = maxVuValue;
    firePropertyChange(PROP_MAXVUVALUE, oldMaxVuValue, maxVuValue);
  }

  /**
   * Get the value of minVuValue
   *
   * @return the value of minVuValue
   */
  public int getMinVuValue() {
    return minVuValue;
  }

  /**
   * Set the value of minVuValue
   *
   * @param minVuValue new value of minVuValue
   */
  public void setMinVuValue(int minVuValue) {
    int oldMinVuValue = this.minVuValue;
    this.minVuValue = minVuValue;
    firePropertyChange(PROP_MINVUVALUE, oldMinVuValue, minVuValue);
  }

  @Override
  public void setMaximum(int maximum) {
    super.setMaximum(maximum);
    adjustMovePerMouseWheelTick();
  }

  @Override
  public void setMinimum(int minimum) {
    super.setMinimum(minimum);
    adjustMovePerMouseWheelTick();
  }

  private void adjustMovePerMouseWheelTick() {
    int range = getMaximum() - getMinimum();
    movePerMouseWheelTick = range / mouseWheelTicksFullRange;
    if (movePerMouseWheelTick < 1) {
      movePerMouseWheelTick = 1;
    }
  }

  public void setVuValue(int value) {
    if (vuValue == value) {
      return;
    }
    vuValue = value;

    repaint();
  }

  public int getVuValue() {
    return vuValue;
  }

  @Override
  public void paint(Graphics g) {
    if (!isCustomized) {
      customize();
      updateUI();
      isCustomized = true;
    }
    super.paint(g);
  }

  private void customize() {
    UIDefaults uiDefaults = new UIDefaults();
    try {
      uiDefaults.put("Slider:SliderTrack[Enabled].backgroundPainter", sliderVuMeterTrackPainter);

      uiDefaults.put("Slider:SliderThumb[Disabled].backgroundPainter", new SliderVuMeterThumpPainter(false, false, false));
      uiDefaults.put("Slider:SliderThumb[Enabled].backgroundPainter", new SliderVuMeterThumpPainter(false, true, false));
      uiDefaults.put("Slider:SliderThumb[Focused+MouseOver].backgroundPainter", new SliderVuMeterThumpPainter(true, true, false));
      uiDefaults.put("Slider:SliderThumb[Focused+Pressed].backgroundPainter", new SliderVuMeterThumpPainter(true, true, true));
      uiDefaults.put("Slider:SliderThumb[Focused].backgroundPainter", new SliderVuMeterThumpPainter(false, true, false));
      uiDefaults.put("Slider:SliderThumb[MouseOver].backgroundPainter", new SliderVuMeterThumpPainter(true, true, false));
      uiDefaults.put("Slider:SliderThumb[Pressed].backgroundPainter", new SliderVuMeterThumpPainter(false, true, false));
    } catch (IOException ex) {
      logger.log(Level.SEVERE, null, ex);
    }
    if (getOrientation() == JSlider.HORIZONTAL) {
      putClientProperty("Slider.paintThumbArrowShape", Boolean.TRUE);
    }
    putClientProperty("JComponent.sizeVariant", "large");
    putClientProperty("Nimbus.Overrides", uiDefaults);
    putClientProperty("Nimbus.Overrides.InheritDefaults", true);

  }

  @Override
  protected void processMouseWheelEvent(MouseWheelEvent evt) {
    if (isEnabled()) {
      super.processMouseWheelEvent(evt);
      int clicks = evt.getWheelRotation();
      setValue(getValue() + clicks * movePerMouseWheelTick);
    }

  }
}
