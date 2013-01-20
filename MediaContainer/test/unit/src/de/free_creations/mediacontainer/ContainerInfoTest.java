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
package de.free_creations.mediacontainer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Harald Postner
 */
public class ContainerInfoTest extends NbTestCase {

  public ContainerInfoTest(java.lang.String testName) {
    super(testName);
  }

  public void testCreation() {
    ArrayList<String> expected = new ArrayList<String>();
    expected.add("file:///dataFile1.xml");
    expected.add("file:///dataFile2.xml");

    ContainerInfo testItem = new ContainerInfo(expected);

    List<String> actual = testItem.getRootfiles();

    assertEquals(expected, actual);
  }

  public void testCreation2() throws IOException, JAXBException {

    ArrayList<String> expected = new ArrayList<String>();
    expected.add("file:///_1_Introit.xml");
    expected.add("file:///_2_Offertoire.xml");
    expected.add("file:///_3_Sanctus.xml");
    expected.add("file:///patrozinium/_1_Kyrie.xml");


    File inFile = getGoldenFile("container2.xml");
    InputStream in = new FileInputStream(inFile);

    ContainerInfo testItem = new ContainerInfo(in);
    List<String> actual = testItem.getRootfiles();
    assertEquals(expected, actual);

  }

  public void testWriteToStream() throws Exception {
    ArrayList<String> expected = new ArrayList<String>();
    expected.add("file:///dataFile1.xml");
    expected.add("file:///dataFile2.xml");

    ContainerInfo testItem = new ContainerInfo(expected);

    clearWorkDir();
    String filename = "container.xml";
    File testFile = new File(getWorkDir(), filename);
    OutputStream out = new FileOutputStream(testFile);

    testItem.writeToStream(out);
    out.close();
    System.out.println("File written:" + testFile.getAbsolutePath());
    compareReferenceFiles(filename, filename, "diff.txt");

  }
}
