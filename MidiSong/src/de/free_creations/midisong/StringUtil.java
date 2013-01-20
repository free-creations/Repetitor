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
package de.free_creations.midisong;

/**
 * A helper class that provides a number of utilities to manipulate strings.
 *
 * @author Harald Postner
 */
public class StringUtil {

  /**
   * This function removes all characters that are not allowed in XML 1.0
   *
   * @see
   * {@link http://en.wikipedia.org/wiki/Valid_characters_in_XML}
   * @param s the input string
   * @return a string where all invalid character have been removed.
   */
  static public String cleanXmlString(String s) {
    StringBuilder result = new StringBuilder(s.length());
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (isValidXmlChar(c)) {
        result.append(c);
      }
    }
    return result.toString();
  }

  private static boolean isValidXmlChar(char c) {
    if ('\u007F' <= c) {
      if (c <= '\u0084') {
        return false;//highly discouraged
      }
    }
    if ('\u0086' <= c) {
      if (c <= '\u009F') {
        return false;//highly discouraged
      }
    }

    if ('<' == c) {
      return false; // not in an xml string
    }
    if ('>' == c) {
      return false; // not in an xml string
    }
    if ('\u0009' == c) {
      return true;
    }
    if ('\n' == c) {
      // U+000A
      return true;
    }
    if ('\r' == c) {
      // U+000D
      return true;
    }
    if ('\u0020' <= c) {
      if (c <= '\u007E') {
        return true;
      }
    }
    if (c == '\u0085') {

      return true;

    }
    return false;
  }
}
