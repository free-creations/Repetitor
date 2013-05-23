/*
 * Copyright 2011 Harald Postner.
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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This object administrates the dimensions of the canvas and the zones on the
 * canvas. The dimensions are converted between different references systems;
 * pixel-units and midi-ticks. If one item is changed, the other are
 * automatically adjusted.
 * 
 * Internally, all dimensions are stored in the midi coordinate system.
 * 
 * Changes: 21. May 2013, the max and min values are not anymore adjusted when
 * the pixelToMidiFactor changes. This was done because displayed region became
 * too huge after "setMidiToPixel". Problem: there might be a white space appearing on the left.
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
class Dimensions {

  static final private Logger logger = Logger.getLogger(Dimensions.class.getName());
  private long maximumMidi = 0;
  private long minimumMidi = 0;
  private long loopStartMidi = 0;
  private long leadInEndMidi = 0;
  private long leadOutStartMidi = 0;
  private long loopEndMidi = 0;
  private double pixelToMidiFactor = 10.0D;
  protected double midiToPixelFactor = 0.1D;
  private long cursorMidi = 0;
  private long startPointMidi = 0;
  private long viewportLeftMidi = 0;
  private int viewportWidthPixel = 0;
  private int viewportHeightPixel;
  private int resolution = 240;

  /**
   * Get the value of viewportHeightPixel
   *
   * @return the value of viewportHeightPixel
   */
  public int getViewportHeight() {
    return viewportHeightPixel;
  }

  /**
   * Set the value of viewportHeightPixel
   *
   * @param viewportHeightPixel new value of viewportHeightPixel
   */
  public void setViewportHeight(int viewportHeight) {
    int oldViewportHeight = this.viewportHeightPixel;
    this.viewportHeightPixel = viewportHeight;
    propertyChangeSupport.firePropertyChange(Prop.VIEWPORTHEIGHT, oldViewportHeight, viewportHeight);
  }

  /**
   * Get the value of viewportWidthMidi
   *
   * @return the value of viewportWidthMidi
   */
  public long getViewportWidthMidi() {
    return pixelToMidi(viewportWidthPixel);
  }

  /**
   * Set the value of viewportWidthMidi
   *
   * @param viewportWidthMidi new value of viewportWidthMidi
   */
  public void setViewportWidthMidi(long newValue) {
    if (newValue != getViewportWidthPixel()) {
      setViewportWidthPixel(midiToPixel(newValue));
    }
  }

  /**
   * Get the value of viewportLeftMidi
   *
   * @return the value of viewportLeftMidi
   */
  public long getViewportLeftMidi() {
    return viewportLeftMidi;
  }

  /**
   * Set the value of viewportLeftMidi
   *
   * @param viewportLeftMidi new value of viewportLeftMidi
   */
  public void setViewportLeftMidi(long newViewportLeftMidi) {
    long oldViewportLeftMidi = this.viewportLeftMidi;
    int oldViewportLeftPixel = getViewportLeftPixel();
    if (oldViewportLeftMidi == newViewportLeftMidi) {
      return;
    }
    if (newViewportLeftMidi < getMinimumMidi()) {
      setMinimumMidi(newViewportLeftMidi);
    }
    this.viewportLeftMidi = newViewportLeftMidi;
    propertyChangeSupport.firePropertyChange(Prop.VIEWPORTLEFT_MIDI, oldViewportLeftMidi, this.viewportLeftMidi);
    propertyChangeSupport.firePropertyChange(Prop.VIEWPORTLEFT_PIXEL, oldViewportLeftPixel, getViewportLeftPixel());
  }

  /**
   * Get the value of viewportWidthPixel
   *
   * @return the value of viewportWidthPixel
   */
  public int getViewportWidthPixel() {
    return viewportWidthPixel;
  }

  /**
   * Set the value of viewportWidthPixel
   *
   * @param viewportWidthPixel new value of viewportWidthPixel
   */
  public void setViewportWidthPixel(int newViewportWidthPixel) {
    if (newViewportWidthPixel < 0L) {
      throw new IllegalArgumentException("Viewport width cannot be negative.");
    }
    long oldViewportWidthMidi = getViewportWidthMidi();
    int oldViewportWidthPixel = getViewportWidthPixel();
    if (oldViewportWidthPixel == newViewportWidthPixel) {
      return;
    }
    int viewportRightPixel = newViewportWidthPixel + getViewportLeftPixel();
    if (viewportRightPixel > getMaximumPixel()) {
      setMaximumPixel(viewportRightPixel);
    }
    this.viewportWidthPixel = newViewportWidthPixel;
    propertyChangeSupport.firePropertyChange(Prop.VIEWPORTWIDTH_MIDI, oldViewportWidthMidi, getViewportWidthMidi());
    propertyChangeSupport.firePropertyChange(Prop.VIEWPORTWIDTH_PIXEL, oldViewportWidthPixel, getViewportWidthPixel());

  }

  /**
   * Get the value of viewportLeftPixel
   *
   * @return the value of viewportLeftPixel
   */
  public int getViewportLeftPixel() {
    return midiToPixel(getViewportLeftMidi());
  }

  /**
   * Set the value of viewportLeftPixel
   *
   * @param viewportLeftPixel new value of viewportLeftPixel
   */
  public void setViewportLeftPixel(int newValue) {
    if (newValue != getViewportLeftPixel()) {
      setViewportLeftMidi(pixelToMidi(newValue));
    }
  }

  /**
   * Get the value of cursorPixel
   *
   * @return the value of cursorPixel
   */
  public int getCursorPixel() {
    return midiToPixel(cursorMidi);
  }

  /**
   * Get the value of startPointPixel
   *
   * @return the value of cursorPixel
   */
  public int getStartPointPixel() {
    return midiToPixel(startPointMidi);
  }

  /**
   * Set the value of cursorPixel
   *
   * @param cursorPixel new value of cursorPixel
   */
  public void setCursorPixel(int newValue) {
    if (newValue != getCursorPixel()) {
      setCursorMidi(pixelToMidi(newValue));
    }
  }

  /**
   * Set the value of StartPointPixel
   *
   * @param newValue new value of StartPointPixel
   */
  public void setStartPointPixel(int newValue) {
    if (newValue != getStartPointPixel()) {
      setStartPointMidi(pixelToMidi(newValue));
    }
  }

  /**
   * Get the value of cursorMidi
   *
   * @return the value of cursorMidi
   */
  public long getCursorMidi() {
    return cursorMidi;
  }

  /**
   * Get the value of startPointMidi
   *
   * @return the value of startPointMidi
   */
  public long getStartPointMidi() {
    return startPointMidi;
  }

  /**
   * Set the value of cursorMidi
   *
   * @param cursorMidi new value of cursorMidi
   */
  public void setCursorMidi(long newCursorMidi) {
    long oldCursorMidi = this.cursorMidi;
    int oldCursorPixel = getCursorPixel();
    if (oldCursorMidi == newCursorMidi) {
      return;
    }
    if (newCursorMidi > getMaximumMidi()) {
      newCursorMidi = getMaximumMidi();
    }
    if (newCursorMidi < getMinimumMidi()) {
      newCursorMidi = getMinimumMidi();
    }
    this.cursorMidi = newCursorMidi;
    propertyChangeSupport.firePropertyChange(Prop.CURSOR_MIDI, oldCursorMidi, this.cursorMidi);
    propertyChangeSupport.firePropertyChange(Prop.CURSOR_PIXEL, oldCursorPixel, getCursorPixel());
  }

  /**
   * Set the value of StartPointMidi
   *
   * @param newStartPointMidi new value of StartPointMidi
   */
  public void setStartPointMidi(long newStartPointMidi) {
    long oldStartPointMidi = this.startPointMidi;
    int oldStartPointPixel = getStartPointPixel();
    if (newStartPointMidi > getMaximumMidi()) {
      newStartPointMidi = getMaximumMidi();
    }
    if (newStartPointMidi < getMinimumMidi()) {
      newStartPointMidi = getMinimumMidi();
    }
    if (oldStartPointMidi == newStartPointMidi) {
      return;
    }
    this.startPointMidi = newStartPointMidi;
    propertyChangeSupport.firePropertyChange(Prop.STARTPOINT_MIDI, oldStartPointMidi, this.cursorMidi);
    propertyChangeSupport.firePropertyChange(Prop.STARTPOINT_PIXEL, oldStartPointPixel, getCursorPixel());
  }

  /**
   * Get the value of rightVoidStartPixel
   *
   * @return the value of rightVoidStartPixel
   */
  public int getLoopEndPixel() {
    return midiToPixel(getLoopEndMidi());
  }

  /**
   * Set the value of rightVoidStartPixel
   *
   * @param rightVoidStartPixel new value of rightVoidStartPixel
   */
  public void setLoopEndPixel(int newValue) {
    if (newValue != getLoopEndPixel()) {
      setLoopEndMidi(pixelToMidi(newValue));
    }
  }

  /**
   * Get the value of newLeadOutStartPixel
   *
   * @return the value of newLeadOutStartPixel
   */
  public int getLeadOutStartPixel() {
    return midiToPixel(getLeadOutStartMidi());
  }

  /**
   * Set the value of newLeadOutStartPixel
   *
   * @param newLeadOutStartPixel new value of newLeadOutStartPixel
   */
  public void setLeadOutStartPixel(int newValue) {
    if (newValue != getLeadOutStartPixel()) {
      setLeadOutStartMidi(pixelToMidi(newValue));
    }
  }

  /**
   * Get the value of newLeadInEndPixel
   *
   * @return the value of newLeadInEndPixel
   */
  public int getLeadInEndPixel() {
    return (int) Math.round(getMidiToPixelFactor() * (double) getLeadInEndMidi());
  }

  /**
   * Set the value of newLeadInEndPixel
   *
   * @param newLeadInEndPixel new value of newLeadInEndPixel
   */
  public void setLeadInEndPixel(int newValue) {
    if (newValue != getLeadInEndPixel()) {
      setLeadInEndMidi(pixelToMidi(newValue));
    }
  }

  /**
   * Get the value of newLeftVoidEndPixel
   *
   * @return the value of newLeftVoidEndPixel
   */
  public int getLoopStartPixel() {
    return midiToPixel(getLoopStartMidi());
  }

  /**
   * Set the value of newLeftVoidEndPixel
   *
   * @param newLeftVoidEndPixel new value of newLeftVoidEndPixel
   */
  public void setLoopStartPixel(int newValue) {
    if (newValue != getLoopStartPixel()) {
      setLoopStartMidi(pixelToMidi(newValue));
    }
  }

  /**
   * Get the value of minimumPixel
   *
   * @return the value of minimumPixel
   */
  public int getMinimumPixel() {
    return midiToPixel(getMinimumMidi());
  }

  /**
   * Set the value of minimumPixel
   *
   * @param minimumPixel new value of minimumPixel
   */
  public void setMinimumPixel(int newValue) {
    if (newValue != getMinimumPixel()) {
      setMinimumMidi(pixelToMidi(newValue));
    }
  }

  /**
   * Get the value of newMaximumPixel
   *
   * @return the value of newMaximumPixel
   */
  public int getMaximumPixel() {
    return midiToPixel(getMaximumMidi());
  }

  /**
   * Set the value of newMaximumPixel
   *
   * @param newMaximumPixel new value of newMaximumPixel
   */
  public void setMaximumPixel(int newValue) {
    if (newValue != getMaximumPixel()) {
      setMaximumMidi(pixelToMidi(newValue));
    }
  }

  /**
   * Get the value of midiToPixelFactor
   *
   * @return the value of midiToPixelFactor
   */
  public double getMidiToPixelFactor() {
    return midiToPixelFactor;
  }

  /**
   * Set the value of the Midi-To-Pixel-Factor. The Midi-To-Pixel-Factor is the
   * value by which we must multiply Midi-ticks in order to get the
   * corresponding distance in screen-pixels. A larger value of the
   * Midi-To-Pixel-Factor will result in a more detailed (more stretched)
   * representation.
   *
   * @param midiToPixelFactor new value of midiToPixelFactor
   */
  public void setMidiToPixelFactor(double midiToPixelFactor) {
    setPixelToMidiFactor(1D / midiToPixelFactor);
  }

  /**
   * Get the value of pixelToMidiFactor
   *
   * @return the value of pixelToMidiFactor
   */
  public double getPixelToMidiFactor() {
    return pixelToMidiFactor;
  }

  /**
   * Set the value of the pixel-To-Midi-Factor. The pixel-To-Midi-Factor is the
   * value by which we must multiply screen-pixels in order to get the
   * corresponding number of midiTicks. A larger value of the
   * pixel-To-Midi-Factor will result in a more condensed representation.
   *
   * @param pixelToMidiFactor new value of pixelToMidiFactor
   */
  public void setPixelToMidiFactor(double newPixelToMidiFactor) {

    if (doubleEquals(newPixelToMidiFactor, getPixelToMidiFactor())) {
      return;
    }
    if (newPixelToMidiFactor < 10E-6) {
      throw new IllegalArgumentException("pixelToMidiFactor too small");
    }
    if (newPixelToMidiFactor > 10E6) {
      throw new IllegalArgumentException("pixelToMidiFactor too large");
    }

    double newMidiToPixeFactor = 1.0D / newPixelToMidiFactor;

    double oldPixelToMidiFactor = getPixelToMidiFactor();
    double oldMidiToPixelFactor = getMidiToPixelFactor();

    int oldMinimumPixel = getMinimumPixel();
    int oldMaximumPixel = getMaximumPixel();
    int oldLoopStartPixel = getLoopStartPixel();
    int oldLeadinEndPixel = getLeadInEndPixel();
    int oldLeadoutStartPixel = getLeadOutStartPixel();
    int oldLoopEndPixel = getLoopEndPixel();
    int oldCursorPixel = getCursorPixel();
    int oldViewportLeftPixel = getViewportLeftPixel();
    long oldViewportWidthMidi = getViewportWidthMidi();


//    long newViewportLeftMidi = pixelToMidi(getViewportLeftPixel(), newPixelToMidiFactor);
//    if (getMinimumMidi() > newViewportLeftMidi) {
//      long oldMinimumMidi = this.minimumMidi;
//      this.minimumMidi = newViewportLeftMidi;
//      propertyChangeSupport.firePropertyChange(Prop.MINIMUM_MIDI, oldMinimumMidi, getMinimumMidi());
//    }
////    // make sure that the current viewport is at least within the range covered by maxMidi
//    long newViewportRightMidi = pixelToMidi(getViewportLeftPixel() + getViewportWidthPixel(), newPixelToMidiFactor);
//    if (this.maximumMidi < newViewportRightMidi) {
//      long oldMaximumMidi = this.maximumMidi;
//      this.maximumMidi = newViewportRightMidi;
//      logger.log(Level.FINER, "setPixelToMidiFactor newMaximumMidi ={0}; oldMaximumMidi= {1}", new Object[]{this.maximumMidi, oldMaximumMidi});
//      propertyChangeSupport.firePropertyChange(Prop.MAXIMUM_MIDI, oldMaximumMidi, getMaximumMidi());
//    }

    this.pixelToMidiFactor = newPixelToMidiFactor;
    this.midiToPixelFactor = newMidiToPixeFactor;


    propertyChangeSupport.firePropertyChange(Prop.PIXELTOMIDIFACTOR, oldPixelToMidiFactor, newPixelToMidiFactor);
    propertyChangeSupport.firePropertyChange(Prop.MIDITOPIXELFACTOR, oldMidiToPixelFactor, getMidiToPixelFactor());

    propertyChangeSupport.firePropertyChange(Prop.MINIMUM_PIXEL, oldMinimumPixel, getMinimumPixel());
    propertyChangeSupport.firePropertyChange(Prop.MAXIMUM_PIXEL, oldMaximumPixel, getMaximumPixel());

    propertyChangeSupport.firePropertyChange(Prop.LOOPSTART_PIXEL, oldLoopStartPixel, getLoopStartPixel());
    propertyChangeSupport.firePropertyChange(Prop.LEADINEND_PIXEL, oldLeadinEndPixel, getLeadInEndPixel());
    propertyChangeSupport.firePropertyChange(Prop.LEADOUTSTART_PIXEL, oldLeadoutStartPixel, getLeadOutStartPixel());
    propertyChangeSupport.firePropertyChange(Prop.LOOPEND_PIXEL, oldLoopEndPixel, getLoopEndPixel());
    propertyChangeSupport.firePropertyChange(Prop.CURSOR_PIXEL, oldCursorPixel, getCursorPixel());
    propertyChangeSupport.firePropertyChange(Prop.VIEWPORTLEFT_PIXEL, oldViewportLeftPixel, getViewportLeftPixel());

    propertyChangeSupport.firePropertyChange(Prop.VIEWPORTWIDTH_MIDI, oldViewportWidthMidi, getViewportWidthMidi());
  }

  /**
   * Get the value of loopEndMidi
   *
   * @return the value of loopEndMidi
   */
  public long getLoopEndMidi() {
    return loopEndMidi;
  }

  /**
   * Set the value of loopEndMidi
   *
   * @param loopEndMidi new value of loopEndMidi
   */
  public void setLoopEndMidi(long newValue) {
    long oldValueMidi = this.loopEndMidi;
    int oldValuePixel = getLoopEndPixel();
    if (oldValueMidi == newValue) {
      return;
    }
    if (newValue > getMaximumMidi()) {
      newValue = getMaximumMidi();
    }
    if (newValue < getLeadOutStartMidi()) {
      setLeadOutStartMidi(newValue);
      newValue = getLeadOutStartMidi();
    }
    this.loopEndMidi = newValue;
    propertyChangeSupport.firePropertyChange(Prop.LOOPEND_MIDI, oldValueMidi, this.loopEndMidi);
    propertyChangeSupport.firePropertyChange(Prop.LOOPEND_PIXEL, oldValuePixel, getLoopEndPixel());
  }

  /**
   * Get the value of leadOutStartMidi
   *
   * @return the value of leadOutStartMidi
   */
  public long getLeadOutStartMidi() {
    return leadOutStartMidi;
  }

  /**
   * Set the value of leadOutStartMidi
   *
   * @param leadOutStartMidi new value of leadOutStartMidi
   */
  public void setLeadOutStartMidi(long newLeadOutStart) {
    long oldLeadOutStart = this.leadOutStartMidi;
    int oldLeadOutStartPixel = getLeadOutStartPixel();
    if (oldLeadOutStart == newLeadOutStart) {
      return;
    }

    if (newLeadOutStart > getLoopEndMidi()) {
      setLoopEndMidi(newLeadOutStart);
      newLeadOutStart = getLoopEndMidi();
    }
    if (newLeadOutStart < getLeadInEndMidi()) {
      setLeadInEndMidi(newLeadOutStart);
      newLeadOutStart = getLeadInEndMidi();
    }
    if (oldLeadOutStart == newLeadOutStart) {
      return;
    }
    this.leadOutStartMidi = newLeadOutStart;
    propertyChangeSupport.firePropertyChange(Prop.LEADOUTSTART_MIDI, oldLeadOutStart, this.leadOutStartMidi);
    propertyChangeSupport.firePropertyChange(Prop.LEADOUTSTART_PIXEL, oldLeadOutStartPixel, getLeadOutStartPixel());
  }

  /**
   * Get the value of leadInEndMidi
   *
   * @return the value of leadInEndMidi
   */
  public long getLeadInEndMidi() {
    return leadInEndMidi;
  }

  /**
   * Set the value of leadInEndMidi
   *
   * @param leadInEndMidi new value of leadInEndMidi
   */
  public void setLeadInEndMidi(long newLeadInEndMidi) {
    long oldLeadInEndMidi = this.leadInEndMidi;
    int oldLeadInEndPixel = getLeadInEndPixel();
    if (oldLeadInEndMidi == newLeadInEndMidi) {
      return;
    }

    if (newLeadInEndMidi > getLeadOutStartMidi()) {
      setLeadOutStartMidi(newLeadInEndMidi);
      newLeadInEndMidi = getLeadOutStartMidi();
    }
    if (newLeadInEndMidi < getLoopStartMidi()) {
      setLoopStartMidi(newLeadInEndMidi);
      newLeadInEndMidi = getLoopStartMidi();
    }
    this.leadInEndMidi = newLeadInEndMidi;
    propertyChangeSupport.firePropertyChange(Prop.LEADINEND_MIDI, oldLeadInEndMidi, this.leadInEndMidi);
    propertyChangeSupport.firePropertyChange(Prop.LEADINEND_PIXEL, oldLeadInEndPixel, getLeadInEndPixel());
  }

  /**
   * Get the value of loopStartMidi
   *
   * @return the value of loopStartMidi
   */
  public long getLoopStartMidi() {
    return loopStartMidi;
  }

  /**
   * Set the value of loopStartMidi
   *
   * @param loopStartMidi new value of loopStartMidi
   */
  public void setLoopStartMidi(long newValue) {
    long oldValueMidi = this.loopStartMidi;
    int oldValuePixel = getLoopStartPixel();
    if (oldValueMidi == newValue) {
      return;
    }
    if (newValue > getLeadInEndMidi()) {
      setLeadInEndMidi(newValue);
      newValue = getLeadInEndMidi();
    }
    if (newValue < getMinimumMidi()) {
      newValue = getMinimumMidi();
    }
    if (oldValueMidi == newValue) {
      return;
    }
    this.loopStartMidi = newValue;
    propertyChangeSupport.firePropertyChange(Prop.LOOPSTART_MIDI, oldValueMidi, this.loopStartMidi);
    propertyChangeSupport.firePropertyChange(Prop.LOOPSTART_PIXEL, oldValuePixel, getLoopStartPixel());
  }

  /**
   * Get the value of minimumMidi
   *
   * @return the value of minimumMidi
   */
  public long getMinimumMidi() {
    return minimumMidi;
  }

  /**
   * Set the value of minimumMidi
   *
   * @param minimumMidi new value of minimumMidi
   */
  public void setMinimumMidi(long newMinimumMidi) {
    long oldMinimumMidi = this.minimumMidi;
    int oldMinimumPixel = getMinimumPixel();
    if (getViewportLeftMidi() < newMinimumMidi) {
      newMinimumMidi = getViewportLeftMidi();
    }
    if (oldMinimumMidi == newMinimumMidi) {
      return;
    }
    if (this.maximumMidi < newMinimumMidi) {
      setMaximumMidi(newMinimumMidi);
    }
    if (newMinimumMidi > getLoopStartMidi()) {
      setLoopStartMidi(newMinimumMidi);
    }
    if (getCursorMidi() < newMinimumMidi) {
      setCursorMidi(newMinimumMidi);
    }
    this.minimumMidi = newMinimumMidi;
    propertyChangeSupport.firePropertyChange(Prop.MINIMUM_MIDI, oldMinimumMidi, getMinimumMidi());
    propertyChangeSupport.firePropertyChange(Prop.MINIMUM_PIXEL, oldMinimumPixel, getMinimumPixel());
  }

  /**
   * Get the value of maximumMidi
   *
   * @return the value of maximumMidi
   */
  public long getMaximumMidi() {
    return maximumMidi;
  }

  /**
   * Set the value of maximumMidi. This value records the largest value
   * encountered so far.
   * 
   * Note: the maximum never shrinks....
   *
   * @param newMaximumMidi new value of maximumMidi. If the given value is
   * smaller than the current value, the update will be ignored.
   */
  public void setMaximumMidi(long newMaximumMidi) {
    long oldMaximumMidi = this.maximumMidi;
    logger.log(Level.FINER, "setMaximumMidi new ={0}; old= {1}", new Object[]{newMaximumMidi, oldMaximumMidi});
    if (newMaximumMidi <= oldMaximumMidi) {
      logger.log(Level.FINER, "setMaximumMidi ignored");
      return;
    }

    int oldMaximumPixel = getMaximumPixel();
    long viewportRightMidi = getViewportLeftMidi() + getViewportWidthMidi();
    if (viewportRightMidi > newMaximumMidi) {
      newMaximumMidi = viewportRightMidi;
    }

    if (newMaximumMidi < getMinimumMidi()) {
      setMinimumMidi(newMaximumMidi);
    }
    if (newMaximumMidi < getLoopEndMidi()) {
      setLoopEndMidi(newMaximumMidi);
    }
    if (getCursorMidi() > newMaximumMidi) {
      setCursorMidi(newMaximumMidi);
    }
    this.maximumMidi = newMaximumMidi;
    logger.log(Level.FINER, "setMaximumMidi done({0})", newMaximumMidi);
    propertyChangeSupport.firePropertyChange(Prop.MAXIMUM_MIDI, oldMaximumMidi, newMaximumMidi);
    propertyChangeSupport.firePropertyChange(Prop.MAXIMUM_PIXEL, oldMaximumPixel, getMaximumPixel());
  }
  private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

  /**
   * Add PropertyChangeListener.
   *
   * @param listener
   */
  public void addPropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeSupport.addPropertyChangeListener(listener);
  }

  /**
   * Remove PropertyChangeListener.
   *
   * @param listener
   */
  public void removePropertyChangeListener(PropertyChangeListener listener) {
    propertyChangeSupport.removePropertyChangeListener(listener);
  }

  /**
   * Checks whether two doubles or floats are equal within a small positive
   * number given by epsilon.
   *
   * @param d1
   * @param d2
   * @param epsilon
   * @return
   */
  private boolean doubleEquals(double d1, double d2, double epsilon) {
    return Math.abs(d1 - d2) < epsilon;
  }

  private boolean doubleEquals(double d1, double d2) {
    return doubleEquals(d1, d2, 10.0E-7);
  }

  public int pixelToMidi(long pixVal) {
    return pixelToMidi(pixVal, getPixelToMidiFactor());
  }

  private int pixelToMidi(long pixVal, double pixelToMidiFactor) {
    return (int) Math.round(pixelToMidiFactor * (double) pixVal);
  }

  public int midiToPixel(long midiVal) {
    return midiToPixel(midiVal, getMidiToPixelFactor());
  }

  private int midiToPixel(long midiVal, double midiToPixelFactor) {
    return (int) Math.round(midiToPixelFactor * (double) midiVal);
  }

  public int calulateSnapMidiTick(int oldPixelPos, int newPixelPos) {
    int oldTickPos = pixelToMidi(oldPixelPos);
    int newTickPos = pixelToMidi(newPixelPos);
    int tickDistance = Math.abs(oldTickPos - newTickPos);
    int quarterDistance = getResolution();
    int eighthDistance = getResolution() / 2;
    int sixteenthDistance = getResolution() / 4;
    if (tickDistance > 4 * quarterDistance) {
      return ((int) Math.round((float) newTickPos / (float) quarterDistance)) * quarterDistance;
    } else if (tickDistance > 2 * eighthDistance) {
      return ((int) Math.round((float) newTickPos / (float) eighthDistance)) * eighthDistance;
    } else if (tickDistance > 2 * sixteenthDistance) {
      return ((int) Math.round((float) newTickPos / (float) sixteenthDistance)) * sixteenthDistance;
    }
    return newTickPos;
  }

  /**
   * Calculate the nearest midi tick that lies on a quarter given a pixel
   * position.
   *
   * @param pixelPos the given position in pixel units.
   * @return a position expressed in midi ticks
   */
  public int floorQuarterMidiTick(int pixelPos) {
    int tickPos = pixelToMidi(pixelPos);
    int quarterDistance = getResolution();
    return tickPos - (tickPos % quarterDistance);

  }

  int ceilQuarterMidiTick(int pixelPos) {
    int tickPos = pixelToMidi(pixelPos);
    int quarterDistance = getResolution();
    return (tickPos + quarterDistance) - (tickPos % quarterDistance);
  }

  /**
   * Set the number of Midi Ticks per quarter note. The use of this function is
   * reserved to {@link DirectorBand#setTrack(javax.sound.midi.Track, int) }
   *
   * @param resolution the new resolution to be used.
   */
  protected void setResolution(int resolution) {
    if (resolution != this.resolution) {
      this.resolution = resolution;
    }
  }

  /**
   * @return the number of midi ticks per quarter note.
   */
  public int getResolution() {
    return resolution;
  }
}
