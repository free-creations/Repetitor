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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import javax.swing.BoundedRangeModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class HorizontalScrollModel implements BoundedRangeModel,
        PropertyChangeListener {

  private ArrayList<ChangeListener> changeListeners = new ArrayList<ChangeListener>(1);
  private ChangeEvent changeEvent = new ChangeEvent(this);
  private boolean valueIsAdjusting = false;
  private Dimensions dimensions;

  public HorizontalScrollModel(Dimensions dims) {
    dimensions = dims;
    dimensions.addPropertyChangeListener(this);
  }

  private void fireStateChanged() {
    for (ChangeListener cl : changeListeners) {
      cl.stateChanged(changeEvent);
    }
  }

  @Override
  public void setValueIsAdjusting(boolean b) {
    if (valueIsAdjusting != b) {
      valueIsAdjusting = b;
      fireStateChanged();
    }
  }

  @Override
  public boolean getValueIsAdjusting() {
    return valueIsAdjusting;
  }

  @Override
  public void addChangeListener(ChangeListener x) {
    changeListeners.add(x);
  }

  @Override
  public void removeChangeListener(ChangeListener x) {
    changeListeners.remove(x);
  }

  @Override
  public int getMinimum() {
    return dimensions.getMinimumPixel();
  }

  @Override
  public void setMinimum(int newMinimum) {
    dimensions.setMinimumPixel(newMinimum);
  }

  @Override
  public int getMaximum() {
    return dimensions.getMaximumPixel();
  }

  @Override
  public void setMaximum(int newMaximum) {
    dimensions.setMaximumPixel(newMaximum);
  }

  @Override
  public int getValue() {
    return dimensions.getViewportLeftPixel();
  }

  @Override
  public void setValue(int newValue) {
    dimensions.setViewportLeftPixel(newValue);
  }

  @Override
  public int getExtent() {
    return dimensions.getViewportWidthPixel();
  }

  @Override
  public void setExtent(int newExtent) {
    dimensions.setViewportWidthPixel(newExtent);
  }

  @Override
  public void setRangeProperties(int value, int extent, int min, int max, boolean adjusting) {
    dimensions.setViewportLeftPixel(value);
    dimensions.setViewportWidthPixel(extent);
    dimensions.setMinimumPixel(min);
    dimensions.setMaximumPixel(max);
    setValueIsAdjusting(adjusting);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    Object propertyName = evt.getPropertyName();
    if ((propertyName == Prop.VIEWPORTLEFT_PIXEL)
            || (propertyName == Prop.VIEWPORTWIDTH_PIXEL)
            || (propertyName == Prop.MINIMUM_PIXEL)
            || (propertyName == Prop.MAXIMUM_PIXEL)) {
      fireStateChanged();
    }
  }
}
