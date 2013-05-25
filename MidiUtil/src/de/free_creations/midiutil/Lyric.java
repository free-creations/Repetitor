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
package de.free_creations.midiutil;

import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;

/**
 * A Lyric-object represents a syllable or a word in a song. Instances of this
 * class represent specific MIDI events in a MIDI track. Syllables that end in a
 * hyphen are especially marked, because when showing such syllables on screen
 * the hyphen shall be stretched to join the next syllable. <br/> This class
 * also provides some utility functions to discover lyric-events in a sequence
 * of MIDI events. <h1>Errata</h1> The {@link <a href="http://www.midi.org/techspecs/rp17.php">Recommended Practice (RP-017)
 * SMF Lyric Meta Event Definition</a>} recommends to not use hyphens, but
 * instead let each syllable end in a blank if the syllable is at the end of a
 * word. Syllable that do not end in a blank must be joined to their successor.
 *
 * @Todo The hyphen stuff must be re-implemented.
 * @author Harald Postner
 */
public class Lyric extends TextualMeta implements Comparable<Lyric> {

  /**
   * The Midi Meta Message type for song Lyrics.
   */
  static public final int LYRICSTYPE = 5;
  private boolean hyphenized;

  /**
   * Construct a new Lyrics object for a given position a with a given text.
   *
   * @param tickPos the position in MIDI ticks.
   * @param text the text of the lyrics
   */
  public Lyric(long tickPos, String text) {
    super(tickPos, text);
  }

  /**
   * Construct a new Lyrics object by extracting the text and the position from
   * given Midi message.
   *
   * @param LyricsEvent a MIDI meta message that must be of type "lyrics"
   */
  public Lyric(MidiEvent LyricsEvent) {

    /**
     * &TODO find a better way to remove hyphens in the constructor in subclass
     * of TextualMeta
     */
    super(0, ""); // Oh, this is a hack !!!!!

    MetaMessage textMessage = (MetaMessage) LyricsEvent.getMessage();
    if (textMessage.getType() != LYRICSTYPE) {
      throw new InvalidParameterException(LyricsEvent + " is not  a \"lyrics\" event.");
    }
    byte[] stringData = textMessage.getData();

    this.tickPos = LyricsEvent.getTick();
    try {
      setText(new String(stringData, "ISO-8859-1")); //<< should not call overwritten procedure here
    } catch (UnsupportedEncodingException ex) {
      Logger.getLogger(Lyric.class.getName()).log(Level.WARNING, null, ex);
    }
  }

  /**
   * A "hyphenated" lyric is a syllable whose text representation in the MIDI
   * file originally ended in a hyphen.
   *
   * @return true if this lyric shall be shown with a hyphen.
   */
  public boolean isHyphenated() {
    return hyphenized;
  }

  /**
   * A function useful to discover lyric events in a sequence of MIDI events.
   *
   * @param event any kind of MIDI event
   * @return true if the event is a lyrics event
   */
  public static boolean isLyricsEvent(MidiEvent event) {
    if (null == event) {
      return false;
    }
    return isLyricsMessage(event.getMessage());
  }

  /**
   * A function useful to discover lyric events in a sequence of MIDI messages.
   *
   * @param message any kind of MIDI message (or null)
   * @return true if the event is a lyrics event
   */
  public static boolean isLyricsMessage(MidiMessage message) {
    if (null == message) {
      return false;
    }
    if (!(message instanceof MetaMessage)) {
      return false;
    }
    MetaMessage metaMessage = (MetaMessage) message;
    int messageType = metaMessage.getType();
    return (messageType == LYRICSTYPE);
  }

  /**
   * Compare two Lyric Objects on their position.
   *
   * @param o an other lyrics.
   * @return 1 if the other lyric is earlier than this one.
   */
  public int compareTo(Lyric o) {
    if (this.tickPos < o.getTickPos()) {
      return -1;
    }
    if (this.tickPos > o.getTickPos()) {
      return 1;
    }
    return this.text.compareTo(o.getText());
  }

  /**
   * Extract the text of a lyrics object and do the hyphenation magic.
   *
   * @param text
   */
  @Override
  protected void setText(String text) {
    if (text == null) {
      this.text = "";
      hyphenized = false;
      return;
    }
    if (text.endsWith(" ")) {
      hyphenized = false;
      this.text = text.substring(0, text.length() - 1);
    } else {
      hyphenized = true;
      this.text = text;
    }
  }
}
