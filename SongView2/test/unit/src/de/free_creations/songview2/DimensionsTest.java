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
package de.free_creations.songview2;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Harald Postner
 */
public class DimensionsTest {

  Dimensions instance;
  DimensionsPropertyListener listener;

  /**
   * Test-Helper class that permits to verify whether the Dimensions class has
   * send the expected events.
   */
  private class DimensionsPropertyListener implements PropertyChangeListener {

    HashMap<String, PropertyChangeEvent> recordedEvents =
            new HashMap<String, PropertyChangeEvent>();

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
      recordedEvents.put(evt.getPropertyName(), evt);
    }

    public void reset() {
      recordedEvents.clear();
    }

    public void assertRecorded(String key) {
      //assertTrue("Expected Event >" + key + "< did not happen.", recordedEvents.containsKey(key));
      if (!recordedEvents.containsKey(key)) {
        System.err.println("Expected Event >" + key + "< did not happen. Recorded so far:");
        for (String akey : recordedEvents.keySet()) {
          System.err.println("   " + akey);
        }
        fail("Expected Event >" + key + "< did not happen.");
      }
    }

    public void assertNotRecorded(String key) {
      if (recordedEvents.containsKey(key)) {

        fail("Unexpected Event >" + key + "<.");
      }
    }

    public void printRecordedKeys(String message) {
      System.out.println(message + "Events Recorded so far:");
      for (String akey : recordedEvents.keySet()) {
        System.out.println("   " + akey);
      }
      System.out.println("----------------");
    }

    public Object getNewValue(String key) {
      assertRecorded(key);
      PropertyChangeEvent event = recordedEvents.get(key);
      return event.getNewValue();
    }

    public Object getOldValue(String key) {
      assertRecorded(key);
      PropertyChangeEvent event = recordedEvents.get(key);
      return event.getOldValue();
    }

    public double getNewValueDouble(String key) {
      assertRecorded(key);
      PropertyChangeEvent event = recordedEvents.get(key);
      return (Double) event.getNewValue();
    }

    public double getOldValueDouble(String key) {
      assertRecorded(key);
      PropertyChangeEvent event = recordedEvents.get(key);
      return (Double) event.getOldValue();
    }

    public long getNewValueLong(String key) {
      assertRecorded(key);
      PropertyChangeEvent event = recordedEvents.get(key);
      return (Long) event.getNewValue();
    }

    public long getOldValueLong(String key) {
      assertRecorded(key);
      PropertyChangeEvent event = recordedEvents.get(key);
      return (Long) event.getOldValue();
    }

    public int getNewValueInt(String key) {
      assertRecorded(key);
      PropertyChangeEvent event = recordedEvents.get(key);
      return (Integer) event.getNewValue();
    }

    public int getOldValueInt(String key) {
      assertRecorded(key);
      PropertyChangeEvent event = recordedEvents.get(key);
      return (Integer) event.getOldValue();
    }
  }

  @Before
  public void setUp() {
    instance = new Dimensions();
    listener = new DimensionsPropertyListener();
    instance.setMinimumMidi(-10L);
    instance.setMaximumMidi(100L);
    instance.addPropertyChangeListener(listener);
  }

  @Test
  public void testCreation() {
    System.out.println("testCreation");
    Dimensions nullinstance = new Dimensions();
    verifySequence(nullinstance);
  }

  @Test
  public void testChangingMidiToPixelFactor() {
    System.out.println("testChangingMidiToPixelFactor");
    instance.setViewportWidthPixel(100);
    verifySequence(instance);

    instance.setPixelToMidiFactor(10);
    verifySequence(instance);

    //verify property change messages
    instance.setMaximumPixel(111);
    instance.setMinimumPixel(-11);
    instance.setViewportLeftPixel(-7);
    instance.setLoopStartPixel(13);
    instance.setCursorPixel(15);
    verifySequence(instance);

    double oldMidiToPixelFactor = instance.getMidiToPixelFactor();
    int oldMaximumPixel = instance.getMaximumPixel();
    int oldMinimumPixel = instance.getMinimumPixel();
    long oldMaximumMidi = instance.getMaximumMidi();
    long oldMinimumMidi = instance.getMinimumMidi();

    int oldLeftVoidEnd = instance.getLoopStartPixel();
    int oldLeadinEnd = instance.getLeadInEndPixel();
    int oldLeadoutStart = instance.getLeadOutStartPixel();
    int oldRightVoidStart = instance.getLoopEndPixel();
    int oldCursor = instance.getCursorPixel();
    int oldViewportLeftPixel = instance.getViewportLeftPixel();
    long oldViewportWidthMidi = instance.getViewportWidthMidi();


    listener.reset();


    instance.setPixelToMidiFactor(123.1234567);

    assertEquals(oldMidiToPixelFactor,
            listener.getOldValueDouble(Prop.MIDITOPIXELFACTOR), 10E-7);
    assertEquals(instance.getMidiToPixelFactor(),
            listener.getNewValueDouble(Prop.MIDITOPIXELFACTOR), 10E-7);

    assertEquals(oldMaximumPixel,
            listener.getOldValueInt(Prop.MAXIMUM_PIXEL));
    assertEquals(instance.getMaximumPixel(),
            listener.getNewValueInt(Prop.MAXIMUM_PIXEL));

    //  Changes: 21. May 2013, max and min Midi will not change anymore
//    assertEquals(oldMaximumMidi,
//            listener.getOldValueLong(Prop.MAXIMUM_MIDI));
//    assertEquals(instance.getMaximumMidi(),
//            listener.getNewValueLong(Prop.MAXIMUM_MIDI));
    listener.assertNotRecorded(Prop.MAXIMUM_MIDI);

    assertEquals(oldMinimumPixel,
            listener.getOldValueInt(Prop.MINIMUM_PIXEL));
    assertEquals(instance.getMinimumPixel(),
            listener.getNewValueInt(Prop.MINIMUM_PIXEL));

//    assertEquals(oldMinimumMidi,
//            listener.getOldValueLong(Prop.MINIMUM_MIDI));
//    assertEquals(instance.getMinimumMidi(),
//            listener.getNewValueLong(Prop.MINIMUM_MIDI));
    listener.assertNotRecorded(Prop.MINIMUM_MIDI);

    assertEquals(oldLeftVoidEnd,
            listener.getOldValueInt(Prop.LOOPSTART_PIXEL));
    assertEquals(instance.getLoopStartPixel(),
            listener.getNewValueInt(Prop.LOOPSTART_PIXEL));

    assertEquals(oldLeadinEnd,
            listener.getOldValueInt(Prop.LEADINEND_PIXEL));
    assertEquals(instance.getLeadInEndPixel(),
            listener.getNewValueInt(Prop.LEADINEND_PIXEL));

    assertEquals(oldLeadoutStart,
            listener.getOldValueInt(Prop.LEADOUTSTART_PIXEL));
    assertEquals(instance.getLeadOutStartPixel(),
            listener.getNewValueInt(Prop.LEADOUTSTART_PIXEL));

    assertEquals(oldRightVoidStart,
            listener.getOldValueInt(Prop.LOOPEND_PIXEL));
    assertEquals(instance.getLoopEndPixel(),
            listener.getNewValueInt(Prop.LOOPEND_PIXEL));

    assertEquals(oldCursor,
            listener.getOldValueInt(Prop.CURSOR_PIXEL));
    assertEquals(instance.getCursorPixel(),
            listener.getNewValueInt(Prop.CURSOR_PIXEL));

    assertEquals(oldViewportLeftPixel,
            listener.getOldValueInt(Prop.VIEWPORTLEFT_PIXEL));
    assertEquals(instance.getViewportLeftPixel(),
            listener.getNewValueInt(Prop.VIEWPORTLEFT_PIXEL));

    assertEquals(oldViewportWidthMidi,
            listener.getOldValueLong(Prop.VIEWPORTWIDTH_MIDI));
    assertEquals(instance.getViewportWidthMidi(),
            listener.getNewValueLong(Prop.VIEWPORTWIDTH_MIDI));
  }

  @Test
  public void testSetViewportwidth() {
    System.out.println("testSetViewportwidth");

    //attempt to set beyond the maximum
    instance.setViewportLeftMidi(120L);
    instance.setViewportWidthMidi(80L);
    verifySequence(instance);
    //attempt to set beyond the minimum
    instance.setViewportLeftMidi(-20L);
    verifySequence(instance);

    //verify property change messages
    instance.setMidiToPixelFactor(2.0);

    long oldVal = 10;
    long newVal = 20;
    instance.setViewportLeftMidi(oldVal);
    assertEquals(oldVal,
            instance.getViewportLeftMidi());

    listener.reset();
    instance.setViewportLeftMidi(newVal);
    assertEquals(newVal,
            instance.getViewportLeftMidi());
    assertEquals(oldVal,
            listener.getOldValueLong(Prop.VIEWPORTLEFT_MIDI));
    assertEquals(newVal,
            listener.getNewValueLong(Prop.VIEWPORTLEFT_MIDI));
    assertEquals(2 * (int) oldVal,
            listener.getOldValueInt(Prop.VIEWPORTLEFT_PIXEL));
    assertEquals(2 * (int) newVal,
            listener.getNewValueInt(Prop.VIEWPORTLEFT_PIXEL));
  }

  @Test
  public void testSetRightVoidStart() {
    System.out.println("setRightVoidStart");

    //attempt to set beyond the maximum
    instance.setLoopEndMidi(120L);
    verifySequence(instance);
    assertTrue(instance.getLoopEndMidi() > 90L);
    //attempt to set beyond the minimum
    instance.setLoopEndMidi(-20L);
    verifySequence(instance);
    assertTrue(instance.getLoopEndMidi() < 30L);

    //verify property change messages
    instance.setMidiToPixelFactor(2.0);

    long oldVal = 10;
    long newVal = 20;
    instance.setLoopEndMidi(oldVal);
    assertEquals(oldVal,
            instance.getLoopEndMidi());
    listener.reset();
    instance.setLoopEndMidi(newVal);
    assertEquals(newVal,
            instance.getLoopEndMidi());
    assertEquals(oldVal,
            listener.getOldValueLong(Prop.LOOPEND_MIDI));
    assertEquals(newVal,
            listener.getNewValueLong(Prop.LOOPEND_MIDI));
    assertEquals(2 * (int) oldVal,
            listener.getOldValueInt(Prop.LOOPEND_PIXEL));
    assertEquals(2 * (int) newVal,
            listener.getNewValueInt(Prop.LOOPEND_PIXEL));
  }

  @Test
  public void testSetLeadOutStart() {
    System.out.println("testSetLeadOutStart");

    //attempt to set beyond the maximum
    instance.setLeadOutStartMidi(120L);
    verifySequence(instance);
    assertTrue(instance.getLeadOutStartMidi() > 80L);
    //attempt to set beyond the minimum
    instance.setLeadOutStartMidi(-20L);
    verifySequence(instance);
    assertTrue(instance.getLeadOutStartMidi() < 20L);

    //set somewhere between 20 and 80
    int middle = (instance.getLoopEndPixel() + instance.getLeadInEndPixel()) / 2;
    instance.setLeadOutStartPixel(middle);
    verifySequence(instance);
    assertEquals(middle, instance.getLeadOutStartPixel());

    //verify property change messages
    instance.setMidiToPixelFactor(2.0);

    long oldVal = 10;
    long newVal = 20;
    instance.setLeadOutStartMidi(oldVal);
    assertEquals(oldVal,
            instance.getLeadOutStartMidi());

    listener.reset();
    instance.setLeadOutStartMidi(newVal);
    assertEquals(newVal,
            instance.getLeadOutStartMidi());
    assertEquals(oldVal,
            listener.getOldValueLong(Prop.LEADOUTSTART_MIDI));
    assertEquals(newVal,
            listener.getNewValueLong(Prop.LEADOUTSTART_MIDI));
    assertEquals(2 * (int) oldVal,
            listener.getOldValueInt(Prop.LEADOUTSTART_PIXEL));
    assertEquals(2 * (int) newVal,
            listener.getNewValueInt(Prop.LEADOUTSTART_PIXEL));
  }

  @Test
  public void testSetLeadInEnd() {
    System.out.println("testSetLeadInEnd");

    //attempt to set beyond the maximum
    instance.setLeadInEndMidi(120L);
    verifySequence(instance);
    assertTrue(instance.getLeadInEndMidi() > 70L);
    //attempt to set beyond the minimum
    instance.setLeadInEndMidi(-20L);
    verifySequence(instance);
    assertTrue(instance.getLeadInEndMidi() < 10L);


    //verify property change messages
    instance.setMidiToPixelFactor(2.0);

    long oldVal = 10;
    long newVal = 20;
    instance.setLeadInEndMidi(oldVal);
    assertEquals(oldVal,
            instance.getLeadInEndMidi());

    listener.reset();
    instance.setLeadInEndMidi(newVal);
    assertEquals(newVal,
            instance.getLeadInEndMidi());
    assertEquals(oldVal,
            listener.getOldValueLong(Prop.LEADINEND_MIDI));
    assertEquals(newVal,
            listener.getNewValueLong(Prop.LEADINEND_MIDI));
    assertEquals(2 * (int) oldVal,
            listener.getOldValueInt(Prop.LEADINEND_PIXEL));
    assertEquals(2 * (int) newVal,
            listener.getNewValueInt(Prop.LEADINEND_PIXEL));

  }

  @Test
  public void testSetLeftVoidEnd() {
    System.out.println("testSetLeftVoidEnd");

    //attempt to set beyond the maximum
    instance.setLoopStartMidi(120L);
    verifySequence(instance);
    assertTrue(instance.getLoopStartMidi() > 60L);
    //attempt to set beyond the minimum
    instance.setLoopStartMidi(-20L);
    verifySequence(instance);
    assertTrue(instance.getLoopStartMidi() < -0L);

    //verify property change messages
    instance.setMidiToPixelFactor(2.0);

    long oldVal = 10;
    long newVal = 20;
    instance.setLoopStartMidi(oldVal);
    assertEquals(oldVal,
            instance.getLoopStartMidi());

    listener.reset();
    instance.setLoopStartMidi(newVal);
    assertEquals(newVal,
            instance.getLoopStartMidi());
    assertEquals(oldVal,
            listener.getOldValueLong(Prop.LOOPSTART_MIDI));
    assertEquals(newVal,
            listener.getNewValueLong(Prop.LOOPSTART_MIDI));
    assertEquals(2 * (int) oldVal,
            listener.getOldValueInt(Prop.LOOPSTART_PIXEL));
    assertEquals(2 * (int) newVal,
            listener.getNewValueInt(Prop.LOOPSTART_PIXEL));
  }

  @Test
  public void testSetMinimum() {
    System.out.println("setMinimum");

    instance.setLoopStartMidi(10L);
    verifySequence(instance);
    //attempt to set the minimum beyond the LeftVoidEnd
    instance.setViewportLeftMidi(60L);
    instance.setMinimumMidi(50L);
    verifySequence(instance);
    assertEquals(50L, instance.getMinimumMidi());
    //attempt to set the minimum beyond the Maximum
    instance.setViewportLeftMidi(200L);
    instance.setMinimumMidi(200L);
    verifySequence(instance);
    assertTrue(200L <= instance.getMaximumMidi());

    //verify property change messages
    instance.setMidiToPixelFactor(2.0);

    long oldVal = -10;
    long newVal = -20;
    instance.setMinimumMidi(oldVal);
    assertEquals(oldVal,
            instance.getMinimumMidi());

    listener.reset();
    instance.setMinimumMidi(newVal);
    assertEquals(newVal,
            instance.getMinimumMidi());
    assertEquals(oldVal,
            listener.getOldValueLong(Prop.MINIMUM_MIDI));
    assertEquals(newVal,
            listener.getNewValueLong(Prop.MINIMUM_MIDI));
    assertEquals(2 * (int) oldVal,
            listener.getOldValueInt(Prop.MINIMUM_PIXEL));
    assertEquals(2 * (int) newVal,
            listener.getNewValueInt(Prop.MINIMUM_PIXEL));
  }

  @Test
  public void testSetMaximum() {
    System.out.println("setMaximum");

    instance.setLoopEndMidi(91L);
    verifySequence(instance);
    //attempt to set the maximum beyond the RightVoidStart
    instance.setMaximumMidi(80L);
    verifySequence(instance);
    // changed: the maximum never shrinks...?
    //assertEquals(80L, instance.getMaximumMidi());
    assertEquals(100L, instance.getMaximumMidi());
    //attempt to set the maximum beyond the minimum
    instance.setViewportLeftMidi(-80);
    instance.setMaximumMidi(-80);
    verifySequence(instance);
    // changed: the maximum never shrinks...?
    //assertEquals(-80L, instance.getMaximumMidi());
    assertEquals(100L, instance.getMaximumMidi());

    //verify property change messages
    instance.setMidiToPixelFactor(2.0);

    long oldVal = 100;
    long newVal = 120;
    //instance.setMaximumMidi(oldVal);
    assertEquals(oldVal,
            instance.getMaximumMidi());

    listener.reset();
    instance.setMaximumMidi(newVal);
    assertEquals(newVal,
            instance.getMaximumMidi());
    assertEquals(oldVal,
            listener.getOldValueLong(Prop.MAXIMUM_MIDI));
    assertEquals(newVal,
            listener.getNewValueLong(Prop.MAXIMUM_MIDI));
    assertEquals(2 * (int) oldVal,
            listener.getOldValueInt(Prop.MAXIMUM_PIXEL));
    assertEquals(2 * (int) newVal,
            listener.getNewValueInt(Prop.MAXIMUM_PIXEL));

  }

  @Test
  public void testSetCursor() {
    System.out.println("testSetCursor");
    instance.setMidiToPixelFactor(2.0);
    instance.setCursorPixel(50);
    verifySequence(instance);
    assertEquals(50,
            instance.getCursorPixel());
    assertEquals(25,
            instance.getCursorMidi());

    //attempt to set beyond the maximum
    instance.setCursorMidi(120L);
    verifySequence(instance);
    assertTrue(instance.getCursorMidi() <= 100);
    //attempt to set beyond the minimum
    instance.setCursorMidi(-20L);
    verifySequence(instance);
    assertTrue(instance.getCursorMidi() >= -10);

    //verify property change messages

    long oldVal = 10;
    long newVal = 20;
    instance.setCursorMidi(oldVal);
    assertEquals(oldVal,
            instance.getCursorMidi());

    listener.reset();
    instance.setCursorMidi(newVal);
    assertEquals(newVal,
            instance.getCursorMidi());
    assertEquals(oldVal,
            listener.getOldValueLong(Prop.CURSOR_MIDI));
    assertEquals(newVal,
            listener.getNewValueLong(Prop.CURSOR_MIDI));
    assertEquals(2 * (int) oldVal,
            listener.getOldValueInt(Prop.CURSOR_PIXEL));
    assertEquals(2 * (int) newVal,
            listener.getNewValueInt(Prop.CURSOR_PIXEL));

    // if minimum or maximum change, the cursor should follow,
    // so it is allways between min and max
    // changed: max never shrinks
//    instance.setCursorMidi(90); // set the cursor at 90
//    assertEquals(90, instance.getCursorMidi()); // check that it is now at 90
//    verifySequence(instance); // routine check
//    instance.setMaximumMidi(80); // now move minimum left to cursor
//    assertEquals(80, instance.getMaximumMidi()); // check that move has happend
//    verifySequence(instance); // this will check that cursor is still between min and max

    instance.setCursorMidi(10); // try the same for the min
    assertEquals(10, instance.getCursorMidi()); // just to make sure
    verifySequence(instance); //just to make sure that all is OK
    instance.setViewportLeftMidi(30); // so we can move the min in the next step
    instance.setMinimumMidi(20); // so here we do it
    assertEquals(20, instance.getMinimumMidi()); // just to make the minb was moved
    verifySequence(instance); // again, this will check that cursor is still between min and max
  }

  @Test
  public void testSetMidiToPixelFactor() {
    System.out.println("setMidiToPixelFactor");

    double expected = 123.1234D;
    instance.setMidiToPixelFactor(expected);
    assertEquals(expected, instance.getMidiToPixelFactor(), 1.0E-5);
    assertEquals(1.0 / expected, instance.getPixelToMidiFactor(), 1.0E-5);
  }

  @Test
  public void testSetPixelToMidiFactor() {
    System.out.println("setPixelToMidiFactor");

    double expected = 123.1234D;
    instance.setPixelToMidiFactor(expected);
    assertEquals(expected, instance.getPixelToMidiFactor(), 1.0E-5);
    assertEquals(1.0 / expected, instance.getMidiToPixelFactor(), 1.0E-5);
  }

  @Test
  public void testSetMaximumPixel() {
    System.out.println("setMaximumPixel");

    double factor = 123.1234D;
    instance.setPixelToMidiFactor(factor);
    instance.setMaximumPixel(200);
    assertEquals(200, instance.getMaximumPixel());

  }

  @Test
  public void testSetMinimumPixel() {
    System.out.println("setMinimumPixel");

    double factor = 123.1234D;
    instance.setPixelToMidiFactor(factor);
    instance.setMinimumPixel(-200);
    assertEquals(-200, instance.getMinimumPixel());

  }

  @Test
  public void testSetRightVoidStartPixel() {
    System.out.println("setRightVoidStartPixel");

    double factor = 123.1234D;
    instance.setPixelToMidiFactor(factor);
    instance.setMaximumPixel(200);
    instance.setLoopEndPixel(50);
    assertEquals(50, instance.getLoopEndPixel());

  }

  /**
   * Test of getViewportHeight method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testGetViewportHeight() {
  }

  /**
   * Test of setViewportHeight method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testSetViewportHeight() {
  }

  /**
   * Test of getViewportWidthMidi method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testGetViewportWidthMidi() {
  }

  /**
   * Test of setViewportWidthMidi method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testSetViewportWidthMidi() {
  }

  /**
   * Test of getViewportLeftMidi method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testGetViewportLeftMidi() {
  }

  /**
   * Test of setViewportLeftMidi method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testSetViewportLeftMidi() {
  }

  /**
   * Test of getViewportWidthPixel method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testGetViewportWidthPixel() {
  }

  /**
   * Test of setViewportWidthPixel method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testSetViewportWidthPixel() {
  }

  /**
   * Test of getViewportLeftPixel method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testGetViewportLeftPixel() {
  }

  /**
   * Test of setViewportLeftPixel method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testSetViewportLeftPixel() {
  }

  /**
   * Test of getCursorPixel method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testGetCursorPixel() {
  }

  /**
   * Test of getStartPointPixel method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testGetStartPointPixel() {
  }

  /**
   * Test of setCursorPixel method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testSetCursorPixel() {
  }

  /**
   * Test of setStartPointPixel method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testSetStartPointPixel() {
  }

  /**
   * Test of getCursorMidi method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testGetCursorMidi() {
  }

  /**
   * Test of getStartPointMidi method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testGetStartPointMidi() {
  }

  /**
   * Test of setCursorMidi method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testSetCursorMidi() {
  }

  /**
   * Test of setStartPointMidi method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testSetStartPointMidi() {
  }

  /**
   * Test of getLoopEndPixel method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testGetLoopEndPixel() {
  }

  /**
   * Test of setLoopEndPixel method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testSetLoopEndPixel() {
  }

  /**
   * Test of getLeadOutStartPixel method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testGetLeadOutStartPixel() {
  }

  /**
   * Test of setLeadOutStartPixel method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testSetLeadOutStartPixel() {
  }

  /**
   * Test of getLeadInEndPixel method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testGetLeadInEndPixel() {
  }

  /**
   * Test of setLeadInEndPixel method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testSetLeadInEndPixel() {
  }

  /**
   * Test of getLoopStartPixel method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testGetLoopStartPixel() {
  }

  /**
   * Test of setLoopStartPixel method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testSetLoopStartPixel() {
  }

  /**
   * Test of getMinimumPixel method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testGetMinimumPixel() {
  }

  /**
   * Test of getMaximumPixel method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testGetMaximumPixel() {
  }

  /**
   * Test of getMidiToPixelFactor method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testGetMidiToPixelFactor() {
  }

  /**
   * Test of getPixelToMidiFactor method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testGetPixelToMidiFactor() {
  }

  /**
   * Test of getLoopEndMidi method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testGetLoopEndMidi() {
  }

  /**
   * Test of setLoopEndMidi method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testSetLoopEndMidi() {
  }

  /**
   * Test of getLeadOutStartMidi method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testGetLeadOutStartMidi() {
  }

  /**
   * Test of setLeadOutStartMidi method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testSetLeadOutStartMidi() {
  }

  /**
   * Test of getLeadInEndMidi method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testGetLeadInEndMidi() {
  }

  /**
   * Test of setLeadInEndMidi method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testSetLeadInEndMidi() {
  }

  /**
   * Test of getLoopStartMidi method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testGetLoopStartMidi() {
  }

  /**
   * Test of setLoopStartMidi method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testSetLoopStartMidi() {
  }

  /**
   * Test of getMinimumMidi method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testGetMinimumMidi() {
  }

  /**
   * Test of setMinimumMidi method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testSetMinimumMidi() {
  }

  /**
   * Test of getMaximumMidi method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testGetMaximumMidi() {
  }

  /**
   * Test of setMaximumMidi method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testSetMaximumMidi() {
  }

  /**
   * Test of addPropertyChangeListener method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testAddPropertyChangeListener() {
  }

  /**
   * Test of removePropertyChangeListener method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testRemovePropertyChangeListener() {
  }

  /**
   * Test of pixelToMidi method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testPixelToMidi() {
  }

  /**
   * Test of midiToPixel method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testMidiToPixel() {
  }

  /**
   * Test of calulateSnapMidiTick method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testCalulateSnapMidiTick() {
  }

  /**
   * Test of floorQuarterMidiTick method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testFloorQuarterMidiTick() {
  }

  /**
   * Test of ceilQuarterMidiTick method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testCeilQuarterMidiTick() {
  }

  /**
   * Test of setResolution method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testSetResolution() {
  }

  /**
   * Test of getResolution method, of class Dimensions.
   */
  @Test
  @Ignore("Test is automatically generated")
  public void testGetResolution() {
  }

  /**
   * verify that the invariants on the dimensions hold.
   *
   * @param dim the dimension to test
   */
  private void verifySequence(Dimensions dim) {
    // verify correct sequence of zones
    assertTrue(dim.getMinimumMidi() <= dim.getLoopStartMidi());
    assertTrue(dim.getLoopStartMidi() <= dim.getLeadInEndMidi());
    assertTrue(dim.getLeadInEndMidi() <= dim.getLeadOutStartMidi());
    assertTrue(dim.getLeadOutStartMidi() <= dim.getLoopEndMidi());
    assertTrue(dim.getLoopEndMidi() <= dim.getMaximumMidi());
    assertTrue(dim.getMinimumPixel() <= dim.getLoopStartPixel());
    assertTrue(dim.getLoopStartPixel() <= dim.getLeadInEndPixel());
    assertTrue(dim.getLeadInEndPixel() <= dim.getLeadOutStartPixel());
    assertTrue(dim.getLeadOutStartPixel() <= dim.getLoopEndPixel());
    assertTrue(dim.getLoopEndPixel() <= dim.getMaximumPixel());


    // verify viewport
    // Changes: 21. May 2013, these assertions do not hold anymore
//    assertTrue(dim.getMinimumPixel() <= dim.getViewportLeftPixel());
//    assertTrue(0 <= dim.getViewportWidthPixel());
//    assertTrue(dim.getViewportLeftPixel() + dim.getViewportWidthPixel()
//            <= dim.getMaximumPixel());

    // verify cursor
    assertTrue(dim.getMinimumMidi() <= dim.getCursorMidi());
    assertTrue(dim.getCursorMidi() <= dim.getMaximumMidi());
    assertTrue(dim.getMinimumPixel() <= dim.getCursorPixel());
    assertTrue(dim.getCursorPixel() <= dim.getMaximumPixel());
  }
}
