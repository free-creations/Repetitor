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
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;
import org.openide.util.Exceptions;

/**
 *
 * @author Harald Postner
 */
public class LessonProperties extends Properties {

  public static final String PROP_SONG = "song";
  public static final String PROP_DESCRIPTION = "description";

  public LessonProperties() {
    super();
  }

  public LessonProperties(File file) {
    this();
    loadFromFile(file);
  }

  public String getSong() {
    return getProperty(PROP_SONG);
  }

  public String getDescription() {
    return getProperty(PROP_DESCRIPTION);
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
}
