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
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

/**
 * A single line input field that lets the user select a value for the tempo
 * factor.
 */
public class TempoSpinner extends JSpinner {

  private final static double incFactor = 1.05d;

  /**
   * The model is overwritten so that it increases exponentially.
   */
  private class LogSpinnerNumberModel extends SpinnerNumberModel {

    LogSpinnerNumberModel(double value,
            double minimum,
            double maximum,
            double stepSize) {
      super(value, minimum, maximum, stepSize);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getNextValue() {
      double currentValue = getNumber().doubleValue();
      double nextValue = currentValue * incFactor;
      if (getMaximum().compareTo(nextValue) < 0) {
        return currentValue;
      } else {
        return nextValue;
      }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getPreviousValue() {
      double currentValue = getNumber().doubleValue();
      double previousValue = currentValue / incFactor;
      if (getMinimum().compareTo(previousValue) > 0) {
        return currentValue;
      } else {
        return previousValue;
      }
    }
  }

  public TempoSpinner() {
    setModel(new LogSpinnerNumberModel(1.0d, 0.1d, 10.0d, 0.01));
    setEditor(new NumberEditor(this, "##0.00%"));
    setValue(1.0);
    enableEvents(java.awt.AWTEvent.MOUSE_WHEEL_EVENT_MASK);
  }

  @Override
  protected void processMouseWheelEvent(MouseWheelEvent evt) {
    if (isEnabled()) {
      super.processMouseWheelEvent(evt);
      int clicks = evt.getWheelRotation();
      SpinnerModel model = getModel();
      if (clicks < 0) {
        for (int i = 0; i < -clicks; i++) {
          model.setValue(model.getNextValue());
        }
      }
      if (clicks > 0) {
        for (int i = 0; i < clicks; i++) {
          model.setValue(model.getPreviousValue());
        }
      }
    }
  }
}
