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
import javax.swing.JFormattedTextField;

/**
 * The RPositionField is a {@link JFormattedTextField} that displays 
 * a position in a song. The position is given by the measure and
 * the beat within the measure. The beat can be indicated as decimal number, 
 * giving the possibility to specify half or one third of a beat.
 * The duration of a measure and the number of full beats in a measure
 * are dependant of the meter.
 * @author Harald Postner
 */
public class RPositionField extends JFormattedTextField {

  /**
   * Construct a new Text Field that accepts and displays 
   * values of type {@link RPosition}.
   */
  public RPositionField() {
    super(RPositionFormatter.getFactory(), new RPosition());
  }

  /**
   * {@inheritDoc} 
   */
  @Override
  public RPosition getValue() {
    RPosition value = (RPosition) super.getValue();
    if (value != null) {
      return value;
    } else {
      return new RPosition();
    }
  }

  /**
   * {@inheritDoc} 
   */
  @Override
  public void setValue(Object value) {
    if (!(value instanceof RPosition)) {
      throw new IllegalArgumentException("" + value + "is not of type RPosition.");
    }
    super.setValue(value);

  }
}
