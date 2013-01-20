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

/**
 * Like the {@link RPosition} the RPositionEx (Extended Rhythmic Position)
 * specifies a position in a song, additionally it also gives information about
 * the meter that is used at the given position. The position is given by the
 * measure and the beat within the measure. The beat can be indicated as decimal
 * number, giving the possibility to specify half or one third of a beat. The
 * duration of a measure and the number of full beats in a measure are dependant
 * of the meter.
 *
 * This class also provides some utility-functions to print and parse.
 *
 * @author Harald Postner
 */
public class RPositionEx extends RPosition implements BeatPosition {

  private final int numerator;
  private final int denominator;

  /**
   * Construct an RPosition object that represents the default position (at the
   * beginning of the song and the default meter (4/4).
   */
  public RPositionEx() {
    super(0, 0D);
    this.numerator = 4;
    this.denominator = 4;
  }

  /**
   * Construct an RPosition object that represents a position at the given
   * values.
   *
   * @param measure the measures from the beginning of the song.
   * @param beat the beat position within the measure (can be a fraction of a
   * beat).
   * @param numerator a non zero positive integer indicating the number of beats
   * making one measure.
   * @param denominator a non zero positive integer indicating the length of one
   * beat (2 stands for 1/2, 4 stands for 1/4 and so on)
   *
   */
  public RPositionEx(int numerator, int denominator, long measure, double beat) {
    super(measure, beat);
    this.numerator = numerator;
    this.denominator = denominator;
  }

  /**
   * @return The number of beats making one measure.
   */
  public int getNumerator() {
    return numerator;
  }

  /**
   * @return the length of one beat.
   */
  public int getDenominator() {
    return denominator;
  }
}
