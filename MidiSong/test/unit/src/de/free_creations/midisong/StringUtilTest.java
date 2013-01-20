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

import org.junit.*;
import static org.junit.Assert.*;

/**
 *
 * @author Harald Postner
 */
public class StringUtilTest {

  public StringUtilTest() {
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
   * Test of cleanXmlString method, of class StringUtil.
   */
  @Test
  public void testCleanXmlString() {
    System.out.println("cleanXmlString");
    String s = "abcdefghiklmnopqrstuvwxyzABCDEFGHIKLMNOPQRSTUVWXYZ+-:_";
    String expResult = "abcdefghiklmnopqrstuvwxyzABCDEFGHIKLMNOPQRSTUVWXYZ+-:_";
    String result = StringUtil.cleanXmlString(s);
    assertEquals(expResult, result);

    s = "ab" + '\u0002' + "cd";
    expResult = "abcd";
    result = StringUtil.cleanXmlString(s);
    assertEquals(expResult, result);

    s = "<>";
    expResult = "";
    result = StringUtil.cleanXmlString(s);
    assertEquals(expResult, result);


  }
}
