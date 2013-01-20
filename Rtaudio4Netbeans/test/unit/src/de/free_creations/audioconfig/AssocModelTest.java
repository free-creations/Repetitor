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

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class AssocModelTest {

  AssocModel instance;
  AssocModel emptyInstance;

  @Before
  public void initialize() throws Exception {
    emptyInstance = new AssocModel();
    instance = new AssocModel();
    instance.addPair(2, "two");
    instance.addPair(4, "four");
    instance.addPair(6, "six");
    instance.addPair(100, "xxx");
    instance.addPair(101, "xxx");
  }

  /**
   * Test of addPair method, of class AssocModel.
   */
  @Test
  public void testAddPair() {
    System.out.println("addPair");
    //pairs have been added in initialize
    assertTrue(instance.containsNumber(101));
    assertTrue(instance.containsNumber(100));
    assertTrue(instance.containsNumber(2));

    assertTrue(instance.containsDescription("xxx"));
    assertTrue(instance.containsDescription("two"));
  }

  /**
   * Test of numberToDescription method, of class AssocModel.
   */
  @Test
  public void testNumberToDescription() {
    System.out.println("numberToDescription");
    assertEquals("two", instance.numberToDescription(2));
    assertEquals("xxx", instance.numberToDescription(100));
    assertNull(instance.numberToDescription(1234567));
    assertNull(emptyInstance.numberToDescription(1234567));
  }

  /**
   * Test of descriptionToNumber method, of class AssocModel.
   */
  @Test
  public void testDescriptionToNumber() {
    System.out.println("descriptionToNumber");
    assertEquals(2, instance.descriptionToNumber("two"));
    assertEquals(100, instance.descriptionToNumber("xxx"));
    assertEquals(-1, instance.descriptionToNumber("not in list"));
    assertEquals(-1, emptyInstance.descriptionToNumber("any"));
  }

  /**
   * Test of containsDescription method, of class AssocModel.
   */
  @Test
  public void testContainsDescription() {
    System.out.println("containsDescription");
    //mostly tested in "testAddPair()"
    assertFalse(instance.containsDescription("not in list"));
    assertFalse(emptyInstance.containsDescription("any"));

  }

  /**
   * Test of containsNumber method, of class AssocModel.
   */
  @Test
  public void testContainsNumber() {
    System.out.println("containsNumber");
    //mostly tested in "testAddPair()"
    assertFalse(instance.containsNumber(123434567));
    assertFalse(emptyInstance.containsNumber(123434567));

  }

  /**
   * Test of getSelectedDescription method, of class AssocModel.
   */
  @Test
  public void testSetGetSelectedDescription() {
    System.out.println("setGetSelectedDescription");

    assertFalse(emptyInstance.setSelectedDescription("any"));
    assertEquals(null, emptyInstance.getSelectedDescription());
    assertEquals(-1, emptyInstance.getSelectedNumber());

    assertFalse(instance.setSelectedDescription("not in list"));

    assertTrue(instance.setSelectedDescription("two"));
    assertEquals("two", instance.getSelectedDescription());
    assertEquals(2, instance.getSelectedNumber());
    assertFalse(instance.setSelectedDescription("also not in list"));
    assertEquals("two", instance.getSelectedDescription());
    assertEquals(2, instance.getSelectedNumber());

    assertTrue(instance.setSelectedDescription("xxx"));
    assertEquals("xxx", instance.getSelectedDescription());
    assertEquals(100, instance.getSelectedNumber());
  }

  /**
   * Test of getSelectedNumber method, of class AssocModel.
   */
  @Test
  public void testSetGetSelectedNumber() {
    System.out.println("setGetSelectedNumber");
    assertFalse(emptyInstance.setSelectedNumber(123));
    assertEquals(-1, emptyInstance.getSelectedNumber());
    assertEquals(null, emptyInstance.getSelectedDescription());


    assertFalse(instance.setSelectedNumber(123456));

    assertTrue(instance.setSelectedNumber(2));
    assertEquals(2, instance.getSelectedNumber());
    assertEquals("two", instance.getSelectedDescription());
    assertFalse(instance.setSelectedNumber(987654321));
    assertEquals(2, instance.getSelectedNumber());
    assertEquals("two", instance.getSelectedDescription());

    assertTrue(instance.setSelectedNumber(101));
    assertEquals("xxx", instance.getSelectedDescription());
    assertEquals(100, instance.getSelectedNumber()); // this can have strange effects
  }
}
