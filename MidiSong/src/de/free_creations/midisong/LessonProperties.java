/*
 * Copyright 2013 Harald Postner.
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
package de.free_creations.midisong;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Properties;
import org.openide.util.Exceptions;

/**
 *
 * @author Harald Postner
 */
public class LessonProperties extends Properties {

  public static final String PROP_SONG = "song";
  public static final String PROP_DESCRIPTION = "description";
  public static final String PROP_TEMPOFACTOR = "tempoFactor";
  public static final String PROP_SELECTIONSTART = "selectionStart";
  public static final String PROP_SELECTIONEND = "selectionEnd";
  public static final String PROP_STARTPOINT = "startPoint";
  public static final String PROP_VOICEMUTE = "voiceMute";
  public static final String PROP_VOICESATTENUATION = "voicesAttenuation";
  public static final String PROP_ORCHESTRAATTENUATION = "orchestraAttenuation";

  public LessonProperties() {
    super();
  }

  public LessonProperties(File file) {
    this();
    loadFromFile(file);
  }

  public String getSong() {
    return getProperty(PROP_SONG, "unknown");
  }

  public void setSong(String value) {
    setProperty(PROP_SONG, value);
  }

  public String getDescription() {
    return getProperty(PROP_DESCRIPTION, "");
  }

  /**
   * Get the value of selectionStart
   *
   * @return the value of selectionStart
   */
  public long getSelectionStart() {
    try {
      return Long.valueOf(getProperty(PROP_SELECTIONSTART, "0"));
    } catch (NumberFormatException ignored) {
      return 0;
    }
  }

  /**
   * Set the value of selectionStart
   *
   * @param selectionStart new value of selectionStart
   */
  public void setSelectionStart(long selectionStart) {
    setProperty(PROP_SELECTIONSTART, Long.toString(selectionStart));
  }

  /**
   * Get the value of selectionEnd
   *
   * @return the value of selectionEnd
   */
  public long getSelectionEnd() {
    try {
      return Long.valueOf(getProperty(PROP_SELECTIONEND, "0"));
    } catch (NumberFormatException ignored) {
      return 0;
    }
  }

  /**
   * Set the value of selectionEnd
   *
   * @param selectionEnd new value of selectionEnd
   */
  public void setSelectionEnd(long selectionEnd) {
    setProperty(PROP_SELECTIONEND, Long.toString(selectionEnd));
  }

  /**
   * Get the value of startPoint
   *
   * @return the value of startPoint
   */
  public long getStartPoint() {
    try {
      return Long.valueOf(getProperty(PROP_STARTPOINT, "0"));
    } catch (NumberFormatException ignored) {
      return 0;
    }
  }

  /**
   * Set the value of startPoint
   *
   * @param startPoint new value of startPoint
   */
  public void setStartPoint(long startPoint) {
    setProperty(PROP_STARTPOINT, Long.toString(startPoint));
  }

  public double getTempoFactor() {
    try {
      return Double.valueOf(getProperty(PROP_TEMPOFACTOR, "1.0"));
    } catch (NumberFormatException ignored) {
      return 1.0D;
    }
  }

  public void setTempoFactor(double value) {
    setProperty(PROP_TEMPOFACTOR, Double.toString(value));
  }

  public final void loadFromFile(File file) {
    Reader reader = null;
    try {
      reader = new FileReader(file);
      load(reader);
    } catch (IOException e) {
      Exceptions.printStackTrace(e);
    } finally {
      try {
        reader.close();
      } catch (Exception e) {
      }
    }
  }

  public final void writeToFile(File file) {
    Writer writer = null;
    try {
      writer = new FileWriter(file);
      store(writer, "");
    } catch (IOException e) {
      Exceptions.printStackTrace(e);
    } finally {
      try {
        writer.close();
      } catch (Exception e) {
        Exceptions.printStackTrace(e);
      }
    }
  }

  public void setDescription(String value) {
    setProperty(PROP_DESCRIPTION, value);
  }

  public void setVoiceMute(int i, boolean mute) {
    String key = String.format(PROP_VOICEMUTE + ".%d", i);
    setProperty(key, Boolean.toString(mute));
  }

  public boolean getVoiceMute(int i) {
    String key = String.format(PROP_VOICEMUTE + ".%d", i);
    return Boolean.parseBoolean(getProperty(key, "false"));
  }

  public float getVoicesAttenuation() {
    try {
      return Float.valueOf(getProperty(PROP_VOICESATTENUATION, "0.0"));
    } catch (NumberFormatException ignored) {
      return 0.0F;
    }
  }

  public void setVoicesAttenuation(float value) {
    setProperty(PROP_VOICESATTENUATION, Float.toString(value));
  }

  public float getOrchestraAttenuation() {
    try {
      return Float.valueOf(getProperty(PROP_ORCHESTRAATTENUATION, "0.0"));
    } catch (NumberFormatException ignored) {
      return 0.0F;
    }
  }

  public void setOrchestraAttenuation(float value) {
    setProperty(PROP_ORCHESTRAATTENUATION, Float.toString(value));
  }
}
