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
package de.free_creations.midiutil;

import java.text.NumberFormat;
import java.text.ParseException;

/**
 * The RPosition (Rhythmic Position) designates a position in a song. The
 * position is given by the measure and the beat within the measure. The beat
 * can be indicated as decimal number, giving the possibility to specify half or
 * one third of a beat. The duration of a measure and the number of full beats
 * in a measure are dependant of the meter.
 *
 * This class also provides some utility-functions to print and parse.
 *
 * @author Harald Postner
 */
public class RPosition {

  private final long measure;
  private final double beat;

  /**
   * Construct an RPosition object that represents the default position (at the
   * beginning of the song).
   */
  public RPosition() {
    this(0, 0D);
  }

  /**
   * Construct an RPosition object that represents a position at the given
   * values.
   *
   * @param measure the measures from the beginning of the song.
   * @param beat the beats within the measure.
   */
  public RPosition(long measure, double beat) {
    this.measure = measure;
    this.beat = beat;
  }

  /**
   * Construct an RPosition object that represents a position at the given
   * string. The string must have the format _measures {| { _beats
   * {._decimalFraction}}}. For example "3" or "3 | 2.99" are valid. (note the
   * dot "." might be a "," sign depending on the decimal separator in the
   * current locale.)
   *
   * @param text a text representing a song position.
   */
  public RPosition(String text) {
    int newMeasure = 0;
    double newBeat = 0D;
    if (text == null) {
      throw new IllegalArgumentException();
    }
    // search for the charater that separates the measures from the beats.
    // We'll accept the characters "|", "!", ";" and ":" as separators.
    int separator = text.indexOf('|');
    if (separator == -1) {
      separator = text.indexOf('!');
    }
    if (separator == -1) {
      separator = text.indexOf(';');
    }
    if (separator == -1) {
      separator = text.indexOf(':');
    }

    if (separator == -1) {
      separator = text.length();
    }
    String measuresText = text.substring(0, separator).trim();
    if (!measuresText.isEmpty()) {
      try {
        newMeasure = NumberFormat.getNumberInstance().parse(measuresText).intValue() - 1;
      } catch (ParseException ex) {
        throw new IllegalArgumentException("Invalid rhythmic position. " + ex.getLocalizedMessage());
      }
    }
    int beatsStart = separator + 1;
    if (beatsStart < text.length()) {
      String beatsText = text.substring(beatsStart).trim();
      if (!beatsText.isEmpty()) {
        try {
          newBeat = NumberFormat.getNumberInstance().parse(beatsText).doubleValue() - 1d;
        } catch (ParseException ex) {
          throw new IllegalArgumentException("Invalid rhythmic position. " + ex.getLocalizedMessage());
        }
      }
    }

    this.measure = newMeasure;
    this.beat = newBeat;
  }

  /**
   * @return the beats within the measure (zero based).
   */
  public double getBeat() {
    return beat;
  }

  /**
   * @return the measures from the beginning of the song.
   */
  public long getMeasure() {
    return measure;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 11 * hash + (int) (this.measure ^ (this.measure >>> 32));
    hash = 11 * hash + (int) (Double.doubleToLongBits(this.beat) ^ (Double.doubleToLongBits(this.beat) >>> 32));
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final RPosition other = (RPosition) obj;
    if (this.measure != other.measure) {
      return false;
    }
    if (Double.doubleToLongBits(this.beat) != Double.doubleToLongBits(other.beat)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "RhythmicPosition{" + "measure=" + measure + ", beat=" + beat + '}';
  }

  static public RPosition valueOf(String text) throws IllegalArgumentException {
    return new RPosition(text);
  }

  static public String format(RPosition pos) {
    if (pos == null) {
      return "";
    }
    return String.format("%2d |%5.2f", pos.getMeasure() + 1, pos.getBeat() + 1);
  }

  public String format() {
    return format(this);
  }
}
