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
package de.free_creations.midiutil;

import java.io.UnsupportedEncodingException;
import java.security.InvalidParameterException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;

/**
 * Textual meta messages are defined meta messages who's content is a text.
 *
 * @author Harald Postner
 */
public class TextualMeta {

  protected String text = "";
  protected long tickPos;

  /**
   * Construct a new Textual object for a given position a with a given text.
   *
   * @param tickPos the position in MIDI ticks.
   * @param text the text of the textual event.
   */
  public TextualMeta(long tickPos, String text) {
    setText(text);
    this.tickPos = tickPos;
  }

  /**
   * Construct a new Textual object by extracting the text and the position from
   * given Midi message.
   *
   * @param textualMetaEvent a MIDI meta message that must be of type "textuals"
   */
  public TextualMeta(MidiEvent textualMetaEvent) {

    if (!isTextualMeta(textualMetaEvent)) {
      throw new InvalidParameterException(textualMetaEvent + " is not  a \"textual\" event.");
    }
    MetaMessage textMessage = (MetaMessage) textualMetaEvent.getMessage();
    byte[] stringData = textMessage.getData();

    this.tickPos = textualMetaEvent.getTick();
    try {
      setText(new String(stringData, "UTF-8"));
    } catch (UnsupportedEncodingException ex) {
      Logger.getLogger(Lyric.class.getName()).log(Level.WARNING, null, ex);
    }
  }

  /**
   * @return the text of this textual event.
   */
  public String getText() {
    return text;
  }

  /*
   * @return the position in MIDI ticks.
   */
  public long getTickPos() {
    return tickPos;
  }

  /**
   * A function useful to discover textual events in a sequence of MIDI events.
   *
   * @param event any kind of MIDI event
   * @return true if the event is a textual event
   */
  public static boolean isTextualMeta(MidiEvent event) {
    if (null == event) {
      return false;
    }
    return isTextualMetaMessage(event.getMessage());
  }

  /**
   * A function useful to discover textual events in a sequence of MIDI
   * messages.
   *
   * @param message any kind of MIDI message (or null)
   * @return true if the event is a textual event
   */
  public static boolean isTextualMetaMessage(MidiMessage message) {
    if (null == message) {
      return false;
    }
    if (!(message instanceof MetaMessage)) {
      return false;
    }
    MetaMessage metaMessage = (MetaMessage) message;
    int messageType = metaMessage.getType();
    switch (messageType) {
      case MidiUtil.textMeta:
        return true;
      case MidiUtil.copyrightMeta:
        return true;
      case MidiUtil.tracknameMeta:
        return true;
      case MidiUtil.instrumentNameMeta:
        return true;
      case MidiUtil.lyricMeta:
        return true;
      case MidiUtil.markerMeta:
        return true;
      case MidiUtil.cuePointMeta:
        return true;
      case MidiUtil.programNameMeta:
        return true;
      case MidiUtil.portNameMeta:
        return true;
      default:
        return false;
    }
  }

  /**
   * Set the text.
   *
   * @param text
   */
  protected void setText(String text) {
    this.text = text;
    if (text == null) {
      this.text = "";
    }
  }
}
