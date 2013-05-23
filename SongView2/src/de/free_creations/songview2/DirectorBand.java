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

import de.free_creations.midiutil.TimeSignature;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Track;
import javax.swing.Timer;

/**
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
final class DirectorBand extends Band {

  private LinearGradientPaint background = null;
  //private long trackSize;
  private ArrayList<TimeSignature> timeSignatures =
          new ArrayList<TimeSignature>();
  private boolean isDragging = true;
  private int draggingStartX;

  private class Drifter {

    private double speed; // pixel / per nano-second
    private final double minSpeed = 5E-8; // pixel / per nano-second
    private final double damping = 1E-1; // 
    private final int updatePeriode = 100;
    private final Timer timer = new Timer(updatePeriode,
            new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if(Math.abs(speed)<minSpeed){
          stop();
          return;
        }
        speed = speed - (speed * damping);
        int deltaX = (int) Math.round(updatePeriode * speed * 1E6);
        int newLeft = canvas.getDimensions().getViewportLeftPixel() - deltaX;
        canvas.getDimensions().setViewportLeftPixel(newLeft);
      }
    });

    public void start(double speed) {
      this.speed = speed;
      timer.start();
    }

    public void stop() {
      timer.start();
    }
    public boolean isRunning(){
      return timer.isRunning();
    }
  }
  private Drifter drifter = new Drifter();

  private class SpeedCalculator {

    private double speed = 0D; // pixel / per nano-second
    private double totalShift = 0D;
    private double startTime = 0D;

    public double getSpeed() {

      return speed;

    }

    public void reset() {
      startNewMean(0D, System.nanoTime());

      speed = 0D;
    }

    private void startNewMean(double previousSpeed, double time) {
      startTime = time;
      totalShift = 0D;
      speed = previousSpeed;

    }

    public void newShift(double value) {
      double currentTime = System.nanoTime();
      double elapsedTime = currentTime - startTime;
      totalShift = totalShift + value;
      if (elapsedTime > 200E6) {
        startNewMean(totalShift / elapsedTime, currentTime);
      }
    }
  }
  private SpeedCalculator speedCalculator = new SpeedCalculator();

  DirectorBand(SongCanvas parent) {
    super(parent);
    processTrack(null);
  }

  @Override
  public void draw(Graphics2D g) {
    // cache current rendering characteristics
    Paint originalPaint = g.getPaint();
    Stroke originalStroke = g.getStroke();
    Font originalFont = g.getFont();
    RenderingHints originalRenderingHints = g.getRenderingHints();

    int canvasleft = canvas.getDimensions().getMinimumPixel();
    int canvasright = canvas.getDimensions().getMaximumPixel();
    int canvasWidth = canvasright - canvasleft;

    // now do the rendering
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);



    g.setPaint(getBackground());
    g.fillRect(canvasleft, 0, canvasWidth, PREF_BANDHEIGHT);

    g.setStroke(new BasicStroke(1.0f));
    g.setPaint(Color.WHITE);
    g.draw(new Line2D.Float(canvasleft, PREF_BANDHEIGHT, canvasWidth, PREF_BANDHEIGHT));
    g.setPaint(Color.GRAY);
    g.draw(new Line2D.Float(canvasleft, PREF_BANDHEIGHT + 1, canvasWidth, PREF_BANDHEIGHT + 1));



    long counter = 1;
    for (int i = 0; i < timeSignatures.size() - 1; i++) {
      counter = drawMeasures(g,
              counter,
              timeSignatures.get(i),
              timeSignatures.get(i + 1).getTickPos());
    }
    if (timeSignatures.size() > 0) {
      drawMeasures(g,
              counter,
              timeSignatures.get(timeSignatures.size() - 1),
              canvas.getDimensions().getMaximumMidi());
      drawMeasures(g,
              0,
              timeSignatures.get(0),
              canvas.getDimensions().getMinimumMidi());
    }
    Font font = new Font(Font.DIALOG, Font.PLAIN, 12);
    g.setFont(font);





    // reset rendering characteristics to their original values
    g.setRenderingHints(originalRenderingHints);
    g.setFont(originalFont);
    g.setPaint(originalPaint);
    g.setStroke(originalStroke);
  }

  /**
   * Draw measures
   *
   * @param g the graphics context to draw onto.
   * @param number the index of the first measure
   * @param timeSig the time signature to start with
   * @param lastTick the last tick of the lastmeasure to draw
   */
  private long drawMeasures(Graphics2D g, long number, TimeSignature timeSig, long lastTick) {
    long beatLength = timeSig.getBeatLength();
    long barLength = timeSig.getBarLength();
    long startTick = timeSig.getTickPos();
    long range = lastTick - startTick;
    long count = range / barLength;
    long remainingTicks = range - count * barLength;
    int remainingBeats = (int) (remainingTicks / beatLength);

    if (count > 0) {
      for (long i = 0; i < count; i++) {
        drawMeasure(g, i + number, startTick, timeSig.getNumerator(), beatLength);
        startTick = startTick + barLength;
      }
      if (remainingTicks > 1) {
        drawMeasure(g, count + number, startTick, remainingBeats + 1, beatLength);
        count++;
      }

    } else {
      for (long i = 0; i < -count; i++) {
        startTick = startTick - barLength;
        drawMeasure(g, number - i, startTick, timeSig.getNumerator(), beatLength);
      }
    }
    return number + count;

  }

  private void drawMeasure(Graphics2D g, long number, long startTick, int beatNum, long beatLength) {
    int startPixel = canvas.getDimensions().midiToPixel(startTick);
    g.setPaint(Color.white);
    g.setStroke(new BasicStroke(1.0f));
    g.draw(new Line2D.Float(startPixel, 0, startPixel, PREF_BANDHEIGHT));
    g.setXORMode(Color.gray);
    g.draw(new Line2D.Float(startPixel, PREF_BANDHEIGHT, startPixel, canvas.getDimensions().getViewportHeight()));
    g.setPaintMode();
    for (int i = 1; i < beatNum; i++) {
      startTick = startTick + beatLength;
      drawBeat(g, startTick);
    }
    if (number > 0) {
      String s = "" + number;
      g.setPaint(Color.WHITE);
      g.drawString(s, startPixel + 5, PREF_BANDHEIGHT / 2);
    }
  }

  private void drawBeat(Graphics2D g, long startTick) {
    int startPixel = canvas.getDimensions().midiToPixel(startTick);
    g.setPaint(Color.white);
    float[] dashPattern = {3, 3};
    g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER, 10,
            dashPattern, 0));


    g.setXORMode(Color.gray);
    g.draw(new Line2D.Float(startPixel, PREF_BANDHEIGHT, startPixel, canvas.getDimensions().getViewportHeight()));
    g.setPaintMode();
  }

  private Paint getBackground() {
    int canvasleft = canvas.getDimensions().getMinimumPixel();
    int canvasright = canvas.getDimensions().getMaximumPixel();
    int canvasWidth = canvasright - canvasleft;

    if ((background == null)
            || (background.getStartPoint().getX() != canvasleft)
            || (background.getEndPoint().getX() != canvasright)) {


      // extract the hue of the HighlightColor
      Color color = ColorManager.getHighlightColor();
      float[] hsbvals = Color.RGBtoHSB(color.getRed(),
              color.getGreen(), color.getBlue(), null);

      float hue = hsbvals[0];

      // with this hue construct three colors with different saturation and darkness
      Color backgroundLeft =
              Color.getHSBColor(hue, 0.93F, 0.44F);
      Color backgroundMiddle =
              Color.getHSBColor(hue, 0.4F, 0.7F);
      Color backgroundRight =
              Color.getHSBColor(hue, 0.0F, 0.3F);

      // construct a linear gradient centered in the middle of Director track
      float centerY = (float) PREF_BANDHEIGHT / 2.0F;
      float centerX = (float) (canvasWidth) / 2F;
      float d = centerY * centerY / centerX;

      background = new LinearGradientPaint(
              centerX + d, 0F,
              centerX - d, PREF_BANDHEIGHT,
              new float[]{0F, .5F, 1F},
              new Color[]{backgroundRight, backgroundMiddle, backgroundLeft});
    }
    return background;
  }

  /**
   * Scan the given track for time-signatures and record them in the
   * {@link #timeSignatures} list.
   *
   * @param newTrack
   */
  @Override
  protected void processTrack(Track newTrack) {
    long trackSize = 4 * resolution; // the minimum
    if (newTrack == null) {
      initDefaults();
    } else {
      trackSize = Math.max(trackSize, newTrack.size());
      timeSignatures.clear();
      for (int i = 0; i < newTrack.size(); i++) {
        MidiEvent midiEvent = newTrack.get(i);
        if (TimeSignature.isTimeSignatureEvent(midiEvent)) {
          timeSignatures.add(new TimeSignature(midiEvent, resolution));
        }
      }
    }
    //there must at least be a 4/4 timesignature at the beginning of the track...
    if (timeSignatures.isEmpty()) {
      timeSignatures.add(new TimeSignature(4, 4, resolution, 0L));
    }
    // if the new track is longer than the canvas, enlarge the canvas.
    if (trackSize > canvas.getDimensions().getMaximumMidi()) {
      canvas.getDimensions().setMaximumMidi(trackSize);
    }
  }

  /**
   * Make sure that the variables {@link #trackSize} and {@link #timeSignatures}
   * are initialized with reasonable values.
   */
  private void initDefaults() {

    timeSignatures.clear();
    timeSignatures.add(new TimeSignature(4, 4, resolution, 0L));
  }

  @Override
  protected void invalidate() {
    canvas.repaint();
  }

  @Override
  public int getTotalHeight() {
    return canvas.getDimensions().getViewportHeight();
  }

  @Override
  public void mouseClicked(int x_canvas, int y_canvas) {
    if(drifter.isRunning()){
      drifter.stop();
      return;
    }
    int oldPixelPos = canvas.getDimensions().getCursorPixel();
    int newMidiPos = canvas.getDimensions().calulateSnapMidiTick(oldPixelPos, x_canvas);
    canvas.getDimensions().setCursorMidi(newMidiPos);
    canvas.getDimensions().setStartPointMidi(newMidiPos);
  }

  @Override
  public void setTrack(Track newTrack, int resolution) {
    super.setTrack(newTrack, resolution);
    canvas.getDimensions().setResolution(resolution);
  }

  /**
   * Indicate whether the layer participates in a dragging action.
   *
   * @param startDragging true if we start a dragging action. False if we end
   * the dragging action.
   * @param mouseX the start X-coordinate of the dragging action in the canvas
   * coordinate system.
   * @param mouseY the start Y-coordinate of the dragging action in the canvas
   * coordinate system.
   */
  @Override
  public void setDraggingActivated(boolean startDragging, int mouseX, int mouseY) {
    isDragging = startDragging;
    if (startDragging) {
      drifter.stop();
    } else {
      drifter.start(speedCalculator.getSpeed());
    }
    draggingStartX = mouseX;
    speedCalculator.reset();
  }

  /**
   * This function is called by the canvas whenever the mouse is being dragged
   * to a new position.
   *
   * @param x the new X position of the mouse in the canvas coordinate system
   * (note that the offset to the viewport coordinate system is taken into
   * account)
   */
  @Override
  public void mouseDragged(int mouseX) {
    int deltaX = mouseX - draggingStartX;
    speedCalculator.newShift(deltaX);
    int newLeft = canvas.getDimensions().getViewportLeftPixel() - deltaX;
    canvas.getDimensions().setViewportLeftPixel(newLeft);



  }
}
