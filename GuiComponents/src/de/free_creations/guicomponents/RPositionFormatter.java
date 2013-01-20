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
import java.text.ParseException;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.text.DefaultFormatterFactory;

/**
 *
 * @author Harald Postner
 */
public class RPositionFormatter extends AbstractFormatter {

  public static JFormattedTextField.AbstractFormatterFactory getFactory() {
    return new DefaultFormatterFactory(new RPositionFormatter());
  }

  public RPositionFormatter() {
    super();
  }

  @Override
  public RPosition stringToValue(String text) throws ParseException {
    if (text == null) {
      return new RPosition();
    }
    try {
      return new RPosition(text);
    } catch (Exception ex) {
      throw new ParseException(ex.getMessage(), 0);
    }
  }

  @Override
  public String valueToString(Object value) throws ParseException {
    if (value == null) {
      return "";
    }
    if (!(value instanceof RPosition)) {
      throw new ParseException("Cannot convert " + value.getClass() + "into RhythmicPosition.", 0);
    }
    RPosition pos = (RPosition) value;
    return pos.format();
  }
}
