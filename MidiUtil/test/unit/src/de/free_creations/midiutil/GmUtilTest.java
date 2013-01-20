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

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author admin
 */
public class GmUtilTest {

  /**
   * Test of numberToString method, of class GmUtil.
   */
  @Test
  public void testNumberToString() {
    System.out.println("numberToString");
    int gmNumber = 0;
    String expResult = "Acoustic Grand Piano";
    String result = GmUtil.numberToString(gmNumber);
    assertEquals(expResult, result);

        gmNumber = 112;
    expResult = "Tinkle Bell";
    result = GmUtil.numberToString(gmNumber);
    assertEquals(expResult, result);


    gmNumber = 127;
    expResult = "Gun Shot";
    result = GmUtil.numberToString(gmNumber);
    assertEquals(expResult, result);

  }
}
