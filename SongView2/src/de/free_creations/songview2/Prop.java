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

/**
 * This class defines the identifiers for the properties that can be
 * monitored by a change listener.  
 * @author Harald Postner
 * @see {@link SongPanel#addViewChangeListener(java.beans.PropertyChangeListener) }
 */
public final class Prop {

  /**
   * The largest Midi Tick that that can be scrolled to. This value includes
   * at least the last MIDI event, but can be larger than the whole song.
   */
  public static final String MAXIMUM_MIDI = "maximumMidi";
  /**
   * The smallest Midi Tick that that can be scrolled to. This value includes
   * the first MIDI event, but can be smaller than the beginning of the song.
   */
  public static final String MINIMUM_MIDI = "minimumMidi";
  /**
   * Start of the loop area, expressed in Midi ticks.
   */
  public static final String LOOPSTART_MIDI = "loopStartMidi";
  /**
   * The end of the lead-in in the loop area (start of full volume),
   * expressed in Midi ticks.
   */
  public static final String LEADINEND_MIDI = "leadInEndMidi";
  /**
   * The start of the lead-out in the loop area, expressed in Midi ticks.
   */
  public static final String LEADOUTSTART_MIDI = "leadOutStartMidi";
  /**
   * The end of the loop area.
   */
  public static final String LOOPEND_MIDI = "loopEndMidi";
  public static final String PIXELTOMIDIFACTOR = "pixelToMidiFactor";
  public static final String MIDITOPIXELFACTOR = "midiToPixelFactor";
  public static final String MAXIMUM_PIXEL = "maximumPixel";
  public static final String MINIMUM_PIXEL = "minimumPixel";
  public static final String LOOPSTART_PIXEL = "loopStartPixel";
  public static final String LEADINEND_PIXEL = "leadInEndPixel";
  public static final String LEADOUTSTART_PIXEL = "leadOutStartPixel";
  public static final String LOOPEND_PIXEL = "loopEndPixel";
  /**
   * The current playing position in Midi ticks.
   */
  public static final String CURSOR_MIDI = "cursorMidi";
  /**
   * The current playing position in pixel.
   */
  public static final String CURSOR_PIXEL = "cursorPixel";
  /**
   * The position where playing starts (in Midi ticks).
   */
  public static final String STARTPOINT_MIDI = "startPointMidi";
  /**
   * The position where playing starts (in pixels).
   */
  public static final String STARTPOINT_PIXEL = "startPointPixel";
  public static final String VIEWPORTLEFT_PIXEL = "viewportLeftPixel";
  public static final String VIEWPORTWIDTH_PIXEL = "viewportWidthPixel";
  public static final String VIEWPORTLEFT_MIDI = "viewportLeftMidi";
  public static final String VIEWPORTWIDTH_MIDI = "viewportWidthMidi";
  public static final String VIEWPORTHEIGHT = "viewportHeight";
}
