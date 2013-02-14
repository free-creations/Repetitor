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
package de.free_creations.microsequencer.filestreaming;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

/**
 *
 * @author Harald Postner
 */
public class AudioWriterTest {

  public AudioWriterTest() {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of close method, of class AudioWriter.
   */
  @Test
  @Ignore
  public void testClose() throws Exception {
  }

  /**
   * Test of putNext method, of class AudioWriter.
   */
  @Test
  public void testPutNext() throws FileNotFoundException, IOException, ExecutionException {
    File testDir = new File("tmp");
    assertTrue("Please create a directory " + testDir.getAbsolutePath(), testDir.exists());
    
    File outFile = new File(testDir, "AudioWriter.test");
    if(outFile.exists()){
      outFile.delete();
    }

    int floatCount = 0;
    float[] audioArray = new float[997];

    //un matched cache
    AudioWriter audioWriter = new AudioWriter(new File(testDir, "AudioWriter.test"), 5003);
    for(int i=0;i<33;i++){
      for(float sample:audioArray){
        sample = floatCount;
        floatCount++;
      }
      audioWriter.putNext(audioArray);      
    }
    audioWriter.close();

  }
}
