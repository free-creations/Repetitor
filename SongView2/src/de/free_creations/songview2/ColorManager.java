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

import java.awt.Color;
import javax.swing.UIManager;

/**
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class ColorManager {

  static public Color DerivedColor(float hueOffset, float saturationOffset, float brightnessOffset, float alpha) {

    Color color = getHighlightColor();
    float[] hsbvals = Color.RGBtoHSB(color.getRed(),
            color.getGreen(), color.getBlue(), null);


    color = Color.getHSBColor(hsbvals[0] + hueOffset,
            clampFloat(hsbvals[1] + saturationOffset),
            clampFloat(hsbvals[2] + brightnessOffset));

    return new Color(color.getRed(), color.getGreen(), color.getBlue(), clampInteger(alpha));
  }

  /**
   * @return the color that {@link LeftVoidZone } and
   * {@link RightVoidZone } shall use when active.
   */
  static public Color getVoidZoneActiveColor() {
    return Color.GRAY;
  }

  /**
   * @return the color that {@link LeftVoidZone } and
   * {@link RightVoidZone } shall use when inactive.
   */
  static public Color getVoidZoneInactiveColor() {
    return new Color(0.9F, //red
            0.9F,//green
            0.9F);//red
  }

  static private float clampFloat(float x) {
    if (x > 1.0F) {
      return 1.0F;
    }
    if (x < 0.0F) {
      return 0.0F;
    }
    return x;
  }

  static private int clampInteger(float x) {
    int result = (int) (x * 255.0F);
    if (result > 255) {
      return 255;
    }
    if (result < 0) {
      return 0;
    }
    return result;
  }

  static public Color getHighlightColor() {
    Color result;
    result = UIManager.getColor("nimbusBase");
    if (result != null) {
      return result;
    }

    result = UIManager.getColor("activeCaption");
    if (result != null) {
      return result;
    }
    result = UIManager.getColor("textHighlight");
    if (result != null) {
      return result;
    }
    return Color.blue;
  }
}
