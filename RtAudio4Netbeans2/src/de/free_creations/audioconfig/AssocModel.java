/*
 *  Copyright 2011 Harald Postner <Harald at H-Postner.de>.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  under the License.
 */
package de.free_creations.audioconfig;

import java.util.ArrayList;
import javax.swing.DefaultComboBoxModel;

/**
 * The AssocModel is a special ComboBoxModel where every item is represented by
 * a tuple of a number associated with a string.
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
class AssocModel extends DefaultComboBoxModel<String> {

  private ArrayList<Integer> numbers = new ArrayList<>();

  /**
   * Add a new item to the Combo box.
   *
   * @param number a number specifying the item.
   * @param description a plain text specifying the item.
   */
  public void addPair(int number, String description) {
    addElement(description);
    numbers.add(number);
  }

  /**
   * Find the description corresponding to a given number.
   *
   * @param number the given number
   * @return the description corresponding to the given number. Returns null if
   * the number is unknown.
   */
  public String numberToDescription(int number) {
    int idx = numbers.indexOf(number);
    if (idx != -1) {
      return (String) getElementAt(idx);
    } else {
      return null;
    }
  }

  /**
   * Find the number corresponding to a given description.
   *
   * @param description the given description
   * @return the number corresponding to the given description. Returns -1 if
   * the description is unknown.
   */
  public int descriptionToNumber(String description) {
    int idx = getIndexOf(description);
    if (idx != -1) {
      return numbers.get(idx);
    } else {
      return -1;
    }
  }

  /**
   * Check if the given description is known.
   *
   * @param s the given description
   * @return true if the given description is known.
   */
  public boolean containsDescription(String s) {
    return getIndexOf(s) != -1;
  }

  /**
   * Check if the number is known.
   *
   * @param n the given number
   * @return true if the given number is known.
   */
  public boolean containsNumber(int n) {
    return numbers.contains(n);
  }

  /**
   * Get the description of the currently selected item.
   *
   * @return the description of the currently selected item, or null if no item
   * is selected.
   */
  public String getSelectedDescription() {
    return (String) getSelectedItem();
  }

  /**
   * Get the numeric value of the currently selected item.
   *
   * @return the number of the currently selected item, or -1 if no item is
   * selected.
   */
  public int getSelectedNumber() {
    String desc = getSelectedDescription();
    if (desc != null) {
      return descriptionToNumber(desc);
    } else {
      return -1;
    }
  }

  /**
   * Set the currently selected item to the one with the given number.
   *
   * @param number the given number
   * @return true if the setting succeeded.
   */
  public boolean setSelectedNumber(int number) {
    if (!containsNumber(number)) {
      return false;
    }
    setSelectedItem(numberToDescription(number));
    return true;

  }

  /**
   * Set the currently selected item to the one with the given description.
   *
   * @param s the given description
   * @return true if the setting succeeded.
   */
  public boolean setSelectedDescription(String s) {
    if (!containsDescription(s)) {
      return false;
    }
    setSelectedItem(s);
    return true;

  }

  /**
   * Returns the number associated with a given item.
   *
   * @param index
   * @return
   */
  public int getNumberOfItem(int index) {
    if (index < 0) {
      throw new RuntimeException("Invalid Parameter " + index);
    }
    if (index > numbers.size()) {
      throw new RuntimeException("Invalid Parameter " + index);
    }
    return numbers.get(index);
  }
}
