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

import de.free_creations.midiutil.RPosition;
import java.awt.event.MouseWheelEvent;
import javax.swing.AbstractSpinnerModel;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;

/**
 * A single line input field that lets the user select a 
 * song position. The RPositionSpinner provides a pair of tiny arrow buttons 
 * for stepping sequentially through the measures of the song.
 * @author Harald Postner
 */
public class RPositionSpinner extends JSpinner {

  static public class RPositionSpinnerModel extends AbstractSpinnerModel {

    private RPosition position = new RPosition();

    @Override
    public RPosition getValue() {
      return position;
    }

    @Override
    public void setValue(Object value) {
      if (value == null) {
        position = new RPosition();
      }
      if (!(value instanceof RPosition)) {
        throw new IllegalArgumentException("illegal value");
      }
      if (!value.equals(this.position)) {
        this.position = (RPosition) value;
        fireStateChanged();
      }
    }

    @Override
    public RPosition getNextValue() {
      return new RPosition(position.getMeasure() + 1, position.getBeat());
    }

    @Override
    public RPosition getPreviousValue() {
      return new RPosition(position.getMeasure() - 1, position.getBeat());
    }
  }

  class RPositionEditor extends JSpinner.DefaultEditor {

    public RPositionEditor(final RPositionSpinner spinner) {
      super(spinner);
      final JFormattedTextField textField = getTextField();
      textField.setFormatterFactory(RPositionFormatter.getFactory());
      textField.setEditable(true);
    }
  }

  public RPositionSpinner() {
    super(new RPositionSpinnerModel());
    RPositionEditor editor = new RPositionEditor(this);
    setEditor(editor);

    enableEvents(java.awt.AWTEvent.MOUSE_WHEEL_EVENT_MASK);

  }

  @Override
  protected void processMouseWheelEvent(MouseWheelEvent evt) {
    if (isEnabled()) {
      super.processMouseWheelEvent(evt);
      int clicks = evt.getWheelRotation();
      setValue(new RPosition(getValue().getMeasure() - clicks, getValue().getBeat()));
    }

  }

  @Override
  public RPosition getValue() {
    return (RPosition) super.getValue();

  }
}
