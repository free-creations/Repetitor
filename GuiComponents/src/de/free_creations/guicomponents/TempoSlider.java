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

import java.awt.event.MouseWheelEvent;
import javax.swing.JSlider;

/**
 * The logarithmic slider is a variant of the {@link JSlider} who's value varies
 * exponentially with the distance.
 *
 * @author Harald Postner
 */
public class TempoSlider extends JSlider {

  /**
   * The current exponential value.
   */
  private double exptValue = 1.0D;
  public static final String PROP_EXPTVALUE = "exptValue";
  /**
   * The Exponential value when the slider is at the minimum.
   */
  private double minExpt = 1.0D / 3.0D;
  public static final String PROP_MINEXPT = "minExpt";
  /**
   * The Exponential value when the slider is at the maximum.
   */
  private double maxExpt = 3.0D;
  public static final String PROP_MAXEXPT = "maxExpt";
  /**
   * The number of steps between min and max.
   */
  private int steps = 128;
  public static final String PROP_STEPS = "steps";
  private double factor = 1D;
  private boolean adjustExptVal = true;
  private int movePerMouseWheelTick;
  private int mouseWheelTicksFullRange = 16;

  /**
   * Creates a logarithmic slider with the range 1/3 to 3 and an initial value
   * of 1.
   */
  public TempoSlider() {
    super();
    adjustMinMax();
    setExptValue(1.0D);
    enableEvents(java.awt.AWTEvent.MOUSE_WHEEL_EVENT_MASK);
  }

  /**
   * Get the value of exptValue
   *
   * @return the value of exptValue
   */
  public double getExptValue() {
    return exptValue;
  }

  /**
   * Set the value of exptValue
   *
   * @param exptValue new value of exptValue
   */
  public final void setExptValue(double exptValue) {
    this.exptValue = exptValue;
    int newValue = (int) Math.round(Math.log(exptValue) / factor);
    if (getValue() != newValue) {
      adjustExptVal = false;
      setValue(newValue);
      adjustExptVal = true;

    }
  }

  /**
   * Get the Exponential value that the slider will have at its maximum.
   *
   * @return the value of minExpt
   */
  public double getMinExpt() {
    return minExpt;
  }

  /**
   * Set the value of minExpt
   *
   * @param minExpt new value of minExpt
   */
  public void setMinExpt(double minExpt) {
    double oldMinExpt = this.minExpt;
    if (oldMinExpt != minExpt) {
      this.minExpt = minExpt;
      adjustMinMax();
      firePropertyChange(PROP_MINEXPT, oldMinExpt, minExpt);
    }
  }

  /**
   * Get the Exponential value that the slider will have at its minimum.
   *
   * @return the value of maxExpt
   */
  public double getMaxExpt() {
    return maxExpt;
  }

  /**
   * Set the value of maxExpt
   *
   * @param maxExpt new value of maxExpt
   */
  public void setMaxExpt(double maxExpt) {
    double oldMaxExpt = this.maxExpt;
    if (oldMaxExpt != maxExpt) {
      this.maxExpt = maxExpt;
      adjustMinMax();
      firePropertyChange(PROP_MAXEXPT, oldMaxExpt, maxExpt);
    }
  }

  /**
   * Get the value of steps
   *
   * @return the value of steps
   */
  public int getSteps() {
    return steps;
  }

  /**
   * Set the value of steps
   *
   * @param steps new value of steps
   */
  public void setSteps(int steps) {
    int oldSteps = this.steps;
    if (oldSteps != steps) {
      this.steps = steps;
      adjustMinMax();
      firePropertyChange(PROP_STEPS, oldSteps, steps);
    }
  }

  private void adjustMinMax() {
    double minD = Math.log(getMinExpt());
    double maxD = Math.log(getMaxExpt());
    double range = maxD - minD;
    factor = range / (double) steps;
    int min = (int) Math.round(minD / factor);
    int max = (int) Math.round(maxD / factor);
    setMinimum(min);
    setMaximum(max);
    adjustMovePerMouseWheelTick();

  }

  @Override
  protected void fireStateChanged() {
    if (adjustExptVal) {
      this.exptValue = Math.exp(factor * getValue());
    }
    super.fireStateChanged();
  }

  @Override
  protected void processMouseWheelEvent(MouseWheelEvent evt) {
    if (isEnabled()) {
      super.processMouseWheelEvent(evt);
      int clicks = evt.getWheelRotation();
      setValue(getValue() - clicks * movePerMouseWheelTick);
    }

  }

  private void adjustMovePerMouseWheelTick() {
    int range = getMaximum() - getMinimum();
    movePerMouseWheelTick = range / mouseWheelTicksFullRange;
    if (movePerMouseWheelTick < 1) {
      movePerMouseWheelTick = 1;
    }
  }
}
