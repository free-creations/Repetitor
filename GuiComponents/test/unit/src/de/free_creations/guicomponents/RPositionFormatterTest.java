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
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.Locale;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Harald Postner
 */
public class RPositionFormatterTest {
  
  char decSep = DecimalFormatSymbols.getInstance().getDecimalSeparator();

  public RPositionFormatterTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of stringToValue method, of class RhythmicPositionFormatter.
   */
  @Test
  public void testStringToValue() throws Exception {
    System.out.println("stringToValue");
    RPositionFormatter instance = new RPositionFormatter();
    //
    RPosition expResult = new RPosition();
    String text = null;
    RPosition result = instance.stringToValue(text);
    assertEquals(expResult, result);
    //
    expResult = new RPosition();
    text = "";
    result = instance.stringToValue(text);
    assertEquals(expResult, result);
    //
    //
    text = "5";
    expResult = new RPosition(4, 0);
    result = instance.stringToValue(text);
    assertEquals(expResult, result);
    //
    text = "  5  ";
    expResult = new RPosition(4, 0);
    result = instance.stringToValue(text);
    assertEquals(expResult, result);
    //
    text = "  5|";
    expResult = new RPosition(4, 0);
    result = instance.stringToValue(text);
    assertEquals(expResult, result);
    //
    text = "  5|2";
    expResult = new RPosition(4, 1D);
    result = instance.stringToValue(text);
    assertEquals(expResult, result);
    //
    text = "  5|2"+decSep+"5";
    expResult = new RPosition(4, 1.5D);
    result = instance.stringToValue(text);
    assertEquals(expResult, result);
    //
    text = "  5 | 3"+decSep+"333333 ";
    expResult = new RPosition(4, 2.333333);
    result = instance.stringToValue(text);
    assertEquals(expResult, result);

  }

  @Test
  public void testStringToValueExceptions() throws Exception {
    System.out.println("testStringToValueExceptions");
    boolean caught = false;
    try {
      new RPositionFormatter().stringToValue(" xxx |");
    } catch (ParseException ex) {
      caught = true;
      System.out.println("... " + ex.getMessage());
    }
    assertTrue(caught);
    caught = false;
    try {
      new RPositionFormatter().stringToValue(" 2 | yyyy ");
    } catch (ParseException ex) {
      caught = true;
      System.out.println("... " + ex.getMessage());
    }
    assertTrue(caught);
  }

  /**
   * Test of valueToString method, of class RhythmicPositionFormatter.
   */
  @Test
  public void testValueToString() throws Exception {
    System.out.println("valueToString");

    RPositionFormatter instance = new RPositionFormatter();
    //
    RPosition pos = new RPosition(0, 0);
    String expResult = " 1 | 1"+decSep+"00";

    String result = instance.valueToString(pos);
    assertEquals(expResult, result);
    RPosition pos2 = instance.stringToValue(result);
    assertEquals(pos, pos2);
    //
    pos = new RPosition(98, 2.25);
    expResult = "99 | 3"+decSep+"25";

    result = instance.valueToString(pos);
    assertEquals(expResult, result);
    pos2 = instance.stringToValue(result);
    assertEquals(pos, pos2);
  }
}
