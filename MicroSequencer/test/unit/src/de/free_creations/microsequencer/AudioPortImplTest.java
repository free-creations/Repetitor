/*
 * Copyright 2011 harald.
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
package de.free_creations.microsequencer;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author harald
 */
public class AudioPortImplTest {

    public AudioPortImplTest() {
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
     * Test of getPeakVuAndClear method, of class AudioPortImpl.
     */
    @Test
    public void testGetPeakVuAndClear() {
    }

    /**
     * Test of getAttenuation method, of class AudioPortImpl.
     */
    @Test
    public void testGetAttenuation() {
    }

    /**
     * Test of getAttenuations method, of class AudioPortImpl.
     */
    @Test
    public void testGetAttenuations() {
    }

    /**
     * Test of setAttenuation method, of class AudioPortImpl.
     */
    @Test
    public void testSetAttenuation() {
    }

    /**
     * Test of open method, of class AudioPortImpl.
     */
    @Test
    public void testOpen() throws Exception {
    }

    /**
     * Test of start method, of class AudioPortImpl.
     */
    @Test
    public void testStart() {
    }

    /**
     * Test of close method, of class AudioPortImpl.
     */
    @Test
    public void testClose() {
    }

    /**
     * Test of process method, of class AudioPortImpl.
     */
    @Test
    public void testProcess() throws Exception {
    }

    /**
     * Test of stop method, of class AudioPortImpl.
     */
    @Test
    public void testStop() {
    }

    /**
     * Test of getTargetAttenuationVolt method, of class AudioPortImpl.
     */
    @Test
    public void testGetTargetAttenuationVolt() {
        System.out.println("testGetTargetAttenuationVolt");
        AudioPortImpl instance = new AudioPortImpl(new TestAudioProducer(), null);

        int channel = 0;

        instance.setAttenuation(channel, 0);
        assertEquals(1.0F, instance.getTargetAttenuationVolt(channel), 1E-9F);
        
        instance.setAttenuation(channel, 80);
        assertEquals(1E-4F, instance.getTargetAttenuationVolt(channel), 1E-10F);

    }

    private class TestAudioProducer implements AudioProcessor {

        @Override
        public void open(int samplingRate, int nFrames, int inputChannelCount, int outputChannelCount, boolean noninterleaved)  {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void close() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void start() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void stop() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public float[] process(double streamTime, float[] input) throws Exception {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
