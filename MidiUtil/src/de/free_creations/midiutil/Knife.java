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

import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

/**
 * The Knife is a helper class that permits to (non- destructibly) cut a Midi
 * sequence in two. The Knife is used to perform the left cut in
 * {@link MidiUtil#leftCut(javax.sound.midi.Sequence, long) MidiUtil.leftCut}.
 * It cuts a sequence at a given position and constructs a new sequence that
 * starts at the cut position. The essential task of this class is to make sure
 * that the new sequence is initialised correctly. This means, all Midi events
 * preceeding the cut position and which might influence the sound of the
 * synthesiser must be copied to the start of the new sequence.
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
class Knife {

  private Sequence newseq = null;
  private HashSet<Integer> encounteredItems = new HashSet<Integer>();

  /**
   * Constructs a new knife that cuts the given sequence at the given position.
   *
   * @param seq the sequence that shall be cut
   * @param pos the position where to cut.
   */
  Knife(Sequence seq, long pos) {
    if (pos == 0L) {
      newseq = seq;
      return;
    }
    Track[] tracks = seq.getTracks();
    try {
      // construct a new sequence with the same propertiews as the given sequence.
      newseq = new Sequence(seq.getDivisionType(), seq.getResolution(), tracks.length);
    } catch (InvalidMidiDataException ex) {
      Logger.getLogger(Knife.class.getName()).log(Level.SEVERE, null, ex);
      return;
    }

    // clone the given tracks into the new sequence
    Track[] newTracks = newseq.getTracks();
    for (int i = 0; i < tracks.length; i++) {
      copyTrack(tracks[i], newTracks[i], pos);
    }
  }

  /**
   * @return the result of cutting the sequence given in the constructor.
   */
  public Sequence getResult() {
    return newseq;
  }

  /**
   * Decide whether the given event should be shifted to the start of the new
   * sequence. Events that must be shifted are those which influence the state
   * of the sequencer like "program change" events. Out of all program changes
   * that precede the cut position only the last one must be shifted. <br> This
   * function caches the given events and therefore assumes that messages are
   * given in descending time order. If a particular kind of event appears
   * twice or more, only the first occurrence will be flagged as to be copied.
   *
   * @param event a midi event to be inspected
   * @return true if this particular event shall be copied
   */
  private boolean mustShift(MidiEvent event) {
    MidiMessage message = event.getMessage();

    //-- treat short messages
    if (message instanceof ShortMessage) {
      ShortMessage shortMessage = (ShortMessage) message;
      if (significantShortMessage(shortMessage)) {
        // construct a key
        int key = shortMessage.getStatus();
        if (shortMessage.getCommand() == ShortMessage.CONTROL_CHANGE) {
          key = 256 + shortMessage.getData1();
        }
        // say yes if this is the very first time we have seen such an event
        return !allreadyEncountered(key);
      } else {
        return false;
      }
    }

    //-- treat meta messages
    if (message instanceof MetaMessage) {

      MetaMessage metaMessage = (MetaMessage) message;
      if (significantMetaMessage(metaMessage)) {
        int key = metaMessage.getType() + 128 + 256;
        return !allreadyEncountered(key);
      } else {
        return false;
      }
    }
    return false;
  }

  private boolean allreadyEncountered(Integer key) {
    if (encounteredItems.contains(key)) {
      return true;
    }
    encounteredItems.add(key);
    return false;
  }

  /**
   * Indicate whether the given meta-message shall be shifted. These are those
   * meta-messages that are part of the initialisation of the track notably the
   * tempo, the sequence-name etc.
   *
   * @param message the message that shall be inspected.
   * @return true if the particular kind of message shall be shifted.
   */
  private boolean significantMetaMessage(MetaMessage message) {
    int type = message.getType();
    switch (type) {
      case (0x0):  // Sequence number
        return true;
      case (0x3):  // Sequence name
        return true;
      case (0x4):  // Instrument name
        return true;
      case (0x05):  //  Lyrics
        return false;
      case (0x9):  // Port Name
        return true;
      case (0x20):  // Channel prefix
        return true;
      case (0x21):  // Port prefix
        return true;
      case (0x51):  // set tempo
        return true;
      default:
        return true; //because I don't know...
    }
  }

  /**
   * Indicate whether the given short-message shall be shifted. These are those
   * messages that are part of the initialisation of the track, notably the
   * program-changes and all controllers etc.
   *
   * @param message the message that shall be inspected.
   * @return true if the particular kind of message shall be shifted.
   */
  private boolean significantShortMessage(ShortMessage message) {
    int command = message.getCommand();
    switch (command) {
      case ShortMessage.CONTROL_CHANGE:
        return true;
      case ShortMessage.PITCH_BEND:
        return true;
      case ShortMessage.PROGRAM_CHANGE:
        return true;
      default:
        return false;
    }
  }

  /**
   * Copy the given track into the new track. <br> The easy part of the problem
   * is to copy all events that are behind the cut position to the new track.
   * <br> A more difficult problem is the following: events that influence how
   * the synthesiser will sound (like volume changes etc.) and which occurred
   * before the cut-point, must be shifted to the start of the new sequence and
   * thus become the initialisation-set for the synthesiser. If several such
   * events of the same type precede the cut-point, only the last one shall be
   * copied. <br> In case the cut-point lies before the start of the original
   * sequence (cut-position is negative), the new sequence shall be prepended by
   * a suitable amount of silence. In this case the events for the
   * initialisation-set must be searched slightly after the start of the
   * original sequence, in order to make sure to catch all initialisations of
   * the original track.
   *
   * @param track the given track to be copied
   * @param newTrack the new track to be populated
   * @param pos position in the given track where the copying should start
   */
  private void copyTrack(Track track, Track newTrack, long cutpos) {
    encounteredItems.clear();
    int eventCount = track.size();
    long checkpos = cutpos;// the position from where we look for shift candidates
    if (cutpos < 0) {
      checkpos = 16; // in case
    }

    // go backwards; for every event preceeding the check position,
    // check if the event must be copied to the beginning of the new sequnce
    for (int i = eventCount - 1; i >= 0; i--) {
      MidiEvent event = track.get(i);
      if (event.getTick() < checkpos) {
        if (mustShift(event)) {
          insertEvent(newTrack, event, 0L);
        }
      }
    }
    // go forwards; for every event following the cut position,
    // copy the event to the new sequence
    encounteredItems.clear();
    for (int i = 0; i < eventCount; i++) {
      MidiEvent event = track.get(i);
      // all elements beond the check pos to be copied
      if (event.getTick() >= checkpos) {
        insertEvent(newTrack, event, event.getTick() - cutpos);
      } else {
        // if between checkpos and cutpos take only those that have not 
        // been shifted in above backwards loop (this is a hack)
        if (event.getTick() >= cutpos) {
          if (!mustShift(event)) {
            insertEvent(newTrack, event, event.getTick() - cutpos);
          }
        }
      }
    }
  }

  /**
   * Insert a new event into the given track.
   *
   * @param track the track that shall receive the event
   * @param eventTemplate a template for the new event
   * @param pos the position where to insert the new event
   */
  private void insertEvent(Track track, MidiEvent eventTemplate, long pos) {
    MidiEvent newEvent = new MidiEvent(
            (MidiMessage) eventTemplate.getMessage().clone(),
            pos);
    track.add(newEvent);
  }
}
