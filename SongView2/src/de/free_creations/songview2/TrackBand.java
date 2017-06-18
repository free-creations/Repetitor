/*
 * Copyright 2011 Harald Postner .
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

import de.free_creations.midiutil.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.sound.midi.Track;

/**
 * A band that shows a midi track.
 *
 * @author admin
 */
public class TrackBand extends Band {

  /**
   * the text color for the lyrics in an inactive track
   */
  static private final Color activeTextColor = Color.WHITE;
  /**
   * the text color for the lyrics in an active track
   */
  static private final Color inactiveTextColor = new Color(0, 0, 0, 200);
  /**
   * the text color that this track currently uses
   */
  private Color currentTextColor = inactiveTextColor;
  private LinearGradientPaint background = null;
  private LinearGradientPaint inactiveBackground = null;
  private NoteTrack noteTrack = null;
  private LyricTrack lyricsTrack = null;
  private ArrayList<LyricBox> lyricBoxes = null;
  private float pitchToPixelFactor;

  /**
   * the font to be used for the lyrics. Can only be determined once we have a
   * paint context.
   */
  private Font lyricsFont = null;
  /**
   * the highest pitch that will be represented in this band
   */
  private int maxPitch;
  // the update-lyricboxes function stores the last MidiToPixelFactor here
  private double previousMidiToPixelFactor = 0.0D;
  public static final int defaultLyricsHeightPixels = 15;
  private int lyricsHeightPixels = defaultLyricsHeightPixels; // 

  public int getLyricsHeightPixels() {
    return lyricsHeightPixels;
  }

  public void setLyricsHeightPixels(int lyricsHeightPixels) {
    if (lyricsHeightPixels < 3) {
      lyricsHeightPixels = 3;
    }
    int oldVal = this.lyricsHeightPixels;
    if (oldVal == lyricsHeightPixels) {
      return;
    }
    this.lyricsHeightPixels = lyricsHeightPixels;
    this.lyricsFont = null;
    forceLyricRepaint = true;
    invalidate();
  }

  private String trackName = "";
  private boolean forceLyricRepaint = false;
  
  public String getName(){
    return trackName;
  }

  /**
   * @deprecated use isActive instead
   * @return
   */
  @Deprecated
  public boolean isEnabled() {
    return isActive();
  }

  /**
   * @deprecated use setActive instead
   * @param enabled
   */
  @Deprecated
  public void setEnabled(boolean enabled) {
    setActive(enabled);
  }

  /**
   * Change the active status of this band.
   *
   * @param active
   */
  @Override
  public void setActive(boolean active) {
    if (active == isActive()) {
      return;
    }
    super.setActive(active);
    if (active) {
      currentTextColor = activeTextColor;
    } else {
      currentTextColor = inactiveTextColor;
    }
    forceLyricRepaint = true;
    invalidate();
  }

  @Override
  public int getTotalHeight() {
    return getBandHeight() + lyricsHeightPixels + 3;
  }

  /**
   * Calculate how much the lyrics must be squeezed to fit on screen.
   *
   * @param fontMetrics
   * @return a value between 0.0 and 1.0
   */
  private double calculateLyricsStretch(FontMetrics fontMetrics) {
    // initialise with minimum values
    int previousEnd = Integer.MIN_VALUE / 2;
    String previousText = null;
    int maxOverlap = 0; //Integer.MIN_VALUE;
    String maxOverlapText = null;
    int count = 0;
    // determine what is the biggest overlap of two adjacent strings
    for (Lyric lyric : lyricsTrack) {
      int thisStart = canvas.getDimensions().midiToPixel(lyric.getTickPos());
      int thisOverlap = previousEnd - thisStart;
      if (thisOverlap > 5) {
        count++;
      }
      if (thisOverlap > maxOverlap) {
        maxOverlap = thisOverlap;
        maxOverlapText = previousText;
      }
      previousText = lyric.getText();
      previousEnd = thisStart + fontMetrics.stringWidth(previousText);
    }
    // if only two  strings overlaps then do not squeeze the display
    if (count < 2) {
      return 1.0D;
    }
    // OK, there was an overlap; calculate the squeeze-factor
    int originalTextWidth = fontMetrics.stringWidth(maxOverlapText);
    int newTextWidth = originalTextWidth - ((maxOverlap * 90) / 100);
    double stretch = (double) newTextWidth / (double) originalTextWidth;
    return stretch;
  }

  private class LyricBox {

    private final Image image;
    private final int x;
    private final boolean connectToPrevious;
    private final int width;

    LyricBox(Image image, int x, int width, boolean connectToPrevious) {
      this.image = image;
      this.x = x;
      this.connectToPrevious = connectToPrevious;
      this.width = width;
    }

    public Image getImage() {
      return image;
    }

    public int getX() {
      return x;
    }

    public boolean isConnected() {
      return connectToPrevious;
    }

    private int getWidth() {
      return width;
    }
  }

  TrackBand(SongCanvas parent) {
    super(parent);
    initDefaults();
    processTrack(null);
    updatePitchToPixelFactor();
  }

  /**
   * Helper routine that calculates the required font size in points for a font
   * with a given height in pixels.
   *
   * @param g the graphic context that contains the desired font
   * @param pixelHeight the requested text height
   * @return the new font size in point
   */
  private double fontPixelToPoint(Graphics2D g, double pixelHeight) {
    Font currentFont = g.getFont();
    double currentPoints = currentFont.getSize2D();
    Rectangle2D maxCharBounds = currentFont.getMaxCharBounds(g.getFontRenderContext());
    double currentHeightPixel = maxCharBounds.getHeight();
    return (pixelHeight * currentPoints) / currentHeightPixel;
  }

  /**
   * Applies the lyrics font into the graphic context. If an appropriate font
   * has not yet been determined, such a font will be created.
   *
   * @param g
   */
  private void applyLyricsFont(Graphics2D g) {

    if (lyricsFont == null) {
      Font contextFont = g.getFont();
      double newSizePoint = fontPixelToPoint(g, lyricsHeightPixels);
      lyricsFont = contextFont.deriveFont((float) newSizePoint);
    }
    g.setFont(lyricsFont);
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

    // set the font to a larger font if required
    applyLyricsFont(g);

    // draw the background
    FontMetrics fontMetrics = g.getFontMetrics();
    lyricsHeightPixels = fontMetrics.getHeight();
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
    if (isActive()) {
      g.setPaint(getBackground());
    } else {
      g.setPaint(getInactiveBackground());
    }
    g.fillRect(canvasleft, getY(), canvasWidth, getTotalHeight());

    drawName(g);
    drawNotes(g);
    drawLyrics(g);

    // reset rendering characteristics to their original values
    g.setRenderingHints(originalRenderingHints);
    g.setFont(originalFont);
    g.setPaint(originalPaint);
    g.setStroke(originalStroke);
  }

  private void drawName(Graphics2D g) {
    FontMetrics fontMetrics = g.getFontMetrics();
    int canvasleft = canvas.getDimensions().getMinimumPixel();
    g.setColor(currentTextColor);
    g.drawString(trackName, canvasleft, getY() + fontMetrics.getHeight());
  }

  /**
   * Draw the notes.
   *
   * @param g the graphic context to draw on.
   */
  private void drawNotes(Graphics2D g) {
    if (noteTrack.isEmpty()) {
      return;
    }

    if (isActive()) {
      g.setPaint(Color.WHITE);
    } else {
      g.setPaint(Color.gray);
    }

    float noteHeight = pitchToPixelFactor;
    if (noteHeight < 1.0F) {
      noteHeight = 1.0F;
    }
    for (Note note : noteTrack) {
      float noteX = canvas.getDimensions().midiToPixel(note.getTickPos());
      float noteWidth = canvas.getDimensions().midiToPixel(note.getDuration()) - 2.0F;
      if (noteWidth < 1.0F) {
        noteWidth = 1.0F;
      }
      float noteY = pitchToPixel(note.getPitch());
      Shape noteLine = new Rectangle2D.Float(noteX, noteY, noteWidth, noteHeight);
      g.fill(noteLine);
    }
  }

  private void drawLyrics(Graphics2D g) {
    if (lyricsTrack.isEmpty()) {
      return;
    }
    FontMetrics fontMetrics = g.getFontMetrics();
    updateLyricBoxes(g);

    g.setColor(currentTextColor);
    int lyricY = getY() + getBandHeight();
    int connectionY = lyricY + fontMetrics.getAscent();
    int previousX = Integer.MIN_VALUE;
    for (LyricBox lyricBox : lyricBoxes) {
      Image image = lyricBox.getImage();
      int thisX = lyricBox.getX() + 1; // we shift the Lyrics by one pixel to the left
      g.drawImage(image, thisX, lyricY, canvas);
      if (lyricBox.isConnected()) {
        int lineStartX = previousX + 1;
        int lineEndX = thisX - 1;
        if (lineEndX - lineStartX > 2) {
          g.drawLine(previousX, connectionY, thisX, connectionY);
        }
      }
      previousX = thisX + lyricBox.getWidth();
    }
  }

  private void updateLyricBoxes(Graphics2D g) {
    FontMetrics fontMetrics = g.getFontMetrics();
    // avoid to re-calculate the lyrics boxes if not needed.
    if (previousMidiToPixelFactor == canvas.getDimensions().getMidiToPixelFactor()) {
      if (!forceLyricRepaint) {
        return;
      }
    }

    lyricBoxes.clear();
    double stretchFactor = calculateLyricsStretch(fontMetrics);
    boolean previousIsHypenized = false;
    for (Lyric lyric : lyricsTrack) {
      int lyricX = canvas.getDimensions().midiToPixel(lyric.getTickPos());
      Image image = makeStringRectangle(lyric.getText(), g, stretchFactor);
      LyricBox lyricBox = new LyricBox(image, lyricX, image.getWidth(canvas), previousIsHypenized);
      lyricBoxes.add(lyricBox);
      previousIsHypenized = lyric.isHyphenated();
    }
    previousMidiToPixelFactor = canvas.getDimensions().getMidiToPixelFactor();
    forceLyricRepaint = false;
  }

  private Image makeStringRectangle(String text, Graphics2D g, double stretch) {
    //stretch = 1.0D; //inhibit stretching
    FontMetrics metrics = g.getFontMetrics();
    Rectangle2D rect = metrics.getStringBounds(text, g);
    int height = (int) Math.floor(rect.getHeight());
    int width = (int) Math.floor(stretch * rect.getWidth()) + 3;
    //Image image = canvas.createImage(width, height);
    Image image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);

    Graphics2D imageG = (Graphics2D) image.getGraphics();
    imageG.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

    imageG.setColor(new Color(0, 0, 0, 0)); // transparent
    imageG.fillRect(0, 0, width, height);
    imageG.setColor(currentTextColor);
    imageG.setFont(g.getFont());
    AffineTransform at = new AffineTransform();

    at.scale(stretch, 1.0);
    imageG.transform(at);

    // Draw Text
    imageG.drawString(text, 0, metrics.getAscent());

    return image;
  }

  @Override
  protected final void processTrack(Track newTrack) {
    noteTrack = new NoteTrack(newTrack);
    lyricsTrack = new LyricTrack(newTrack);
    trackName = MidiUtil.readTrackname(newTrack);
    lyricBoxes = new ArrayList<LyricBox>();
    updatePitchToPixelFactor();
    previousMidiToPixelFactor = 0.0D;
  }

  private void initDefaults() {
  }

  private Paint getBackground() {
    int canvasleft = canvas.getDimensions().getMinimumPixel();
    int canvasright = canvas.getDimensions().getMaximumPixel();

    // if no background has been caculated 
    // or the dimensions have changed, calculate it now:
    if ((background == null)
            || (background.getStartPoint().getX() != canvasleft)
            || (background.getEndPoint().getX() != canvasright)) {

      Color backgroundLeft
              = ColorManager.DerivedColor(0.0f, 0.0f, -0.05f, 1f);
      Color backgroundRight
              = ColorManager.DerivedColor(0.0f, -0.1f, -0.05f, 1f);

      background = new LinearGradientPaint(
              canvasleft, 0,
              canvasright, 0,
              new float[]{0F, 1F},
              new Color[]{backgroundLeft, backgroundRight});
    }
    return background;
  }

  private Paint getInactiveBackground() {
    int canvasleft = canvas.getDimensions().getMinimumPixel();
    int canvasright = canvas.getDimensions().getMaximumPixel();

    // if no inactiveBackground has been caculated 
    // or the dimensions have changed, calculate it now:
    if ((inactiveBackground == null)
            || (inactiveBackground.getStartPoint().getX() != canvasleft)
            || (inactiveBackground.getEndPoint().getX() != canvasright)) {

      Color inactiveBackgroundLeft
              = ColorManager.DerivedColor(0.0f, -0.5f, 0.3f, 0.5f);
      //ColorManager.DerivedColor(0.0f, 0.0f, -0.2f, 0.5f);
      Color inactiveBackgroundRight
              = ColorManager.DerivedColor(0.0f, -0.5f, 0.1f, 0.5f);
      // ColorManager.DerivedColor(0.0f, -0.5f, -0.2f, 0.5f);

      inactiveBackground = new LinearGradientPaint(
              canvasleft, 0,
              canvasright, 0,
              new float[]{0F, 1F},
              new Color[]{inactiveBackgroundLeft, inactiveBackgroundRight});
    }
    return inactiveBackground;
  }

  @Override
  protected void processHeightChanged(int oldHeight, int height) {
    updatePitchToPixelFactor();
  }

  private void updatePitchToPixelFactor() {
    float ambitus = noteTrack.getMaxPitch() - noteTrack.getMinPitch();
    if (ambitus < 16.0F) {
      ambitus = 16.0F;
    }
    int newMaxPitch = (int) ((noteTrack.getMaxPitch() + noteTrack.getMinPitch() + ambitus) / 2.0F);
    float newPitchToPixelFactor = (float) getBandHeight() / ambitus;
    if ((newMaxPitch == maxPitch)
            && (floatEquals(newPitchToPixelFactor, pitchToPixelFactor, 10E-3f))) {
      return;
    }
    maxPitch = newMaxPitch;
    pitchToPixelFactor = newPitchToPixelFactor;
    invalidate();
  }

  private int pitchToPixel(int pitch) {
    int delta = maxPitch - pitch;
    return getY() + Math.round(pitchToPixelFactor * (float) delta);
  }

  private boolean floatEquals(float d1, float d2, float epsilon) {
    return Math.abs(d1 - d2) < epsilon;
  }
}
