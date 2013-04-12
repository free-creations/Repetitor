/*
 * Copyright 2013 Harald Postner.
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

import java.io.File;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Harald Postner
 */
public class LessonPropertiesTest {

  /**
   * Test of getSong method, of class LessonProperties.
   */
  @Test
  @Ignore
  public void testGetSong() {
  }

  /**
   * Test of getDescription method, of class LessonProperties.
   */
  @Test
  @Ignore
  public void testGetDescription() {
  }

  /**
   * Test of getTempoFactor method, of class LessonProperties.
   */
  @Test
  @Ignore
  public void testGetTempoFactor() {
  }

  /**
   * Test of setTempoFactor method, of class LessonProperties.
   */
  @Test
  @Ignore
  public void testSetTempoFactor() {
  }

  /**
   * Test of loadFromFile method, of class LessonProperties.
   */
  @Test
  public void testReadWrite() {
    File output = new File("test.lesson");
    float tempoFactor = 0.12345F;

    LessonProperties instance1 = new LessonProperties();

    instance1.setTempoFactor(tempoFactor);

    instance1.writeToFile(output);
    System.out.println("### Ouput written to " + output.getAbsolutePath());

    LessonProperties instance2 = new LessonProperties(output);

    assertEquals(tempoFactor, instance2.getTempoFactor(), 1E-10);


  }
}
