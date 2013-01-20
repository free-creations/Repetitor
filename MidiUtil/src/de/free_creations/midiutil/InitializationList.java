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
package de.free_creations.midiutil;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashSet;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

/**
 * This class helps when we want to start the playback of a Midi track
 * on an arbitrary position. Before the start-position there might be 
 * Midi-messages that influence the sound at the start-position
 * (for example program changes). For a given track 
 * and a given position such messages are collected into this set and
 * send to the synthesiser before starting the playback.
 * 
 * <code>
 *   // build the Initialization List
 *   InitializationList i = new InitializationList(track, position);
 *   // reset all controlleres on all channels
 *   for (int channel=0;channel<16;channel++){
 *      midiPort.send(makeResetAllControllersMessage(channel), now);
 *   }
 *   // send the messages collected in the Initialization List
 *   for(MidiMessage m: i){
 *     midiPort.send(m, now);
 *    }
 *   sequencer.setPosition(position);
 *   sequencer.start();
 *   </code>
 *
 * @author Harald Postner 
 */
public class InitializationList extends AbstractList<MidiMessage> {

  /** These are the messages that this list holds **/
  private final ArrayList<MidiMessage> messages = new ArrayList<MidiMessage>();
  private HashSet<Integer> encounteredItems = new HashSet<Integer>();

  /**
   * Go through the track and collect all initialisation- relevant events.
   * @param track the track to be inspected (note we assume that the
   * events are sorted in time ascending order (always true for
   * java generated tracks).
   * @param tickPos the position in Midi ticks up to which
   * we are looking for events.
   */
  public InitializationList(Track track, long tickPos) {
    // we go backwards through the track.
    // For every event we check wether
    // it lies before the given position and
    // if it is relevent for initialisation. If it is
    // so, we'll add it to our list.

    int eventCount = track.size();
    //go backwards through the track
    for (int i = eventCount - 1; i >= 0; i--) {
      MidiEvent event = track.get(i);
      if (event.getTick() < tickPos) {
        MidiMessage message = event.getMessage();
        if (isInitializationRelevant(message)) {
          messages.add(message);
        }
      }
    }
    encounteredItems = null; //free for garbadge collection

  }

  @Override
  public MidiMessage get(int i) {
    // we  have inverted the order when adding the messages. Now 
    // correct this.
    return messages.get(size()-i-1);
  }

  @Override
  public int size() {
    return messages.size();
  }

  /**
   * A message it is a significant message type (see below)
   * and if it is the first time we see such an event (because
   * we assume that the messages are inspected from end to
   * the beginning)
   * @param message a midi message to be inspected
   * @return true if it must be used to initialise the sequencer.
   */
  private boolean isInitializationRelevant(MidiMessage message) {
    //-- treat short messages
    if (message instanceof ShortMessage) {
      ShortMessage shortMessage = (ShortMessage) message;
      if (significantShortMessage(shortMessage)) {
        // construct a key to identify the type of event.
        // For all kinds of messages except contoll-changes, the
        // status is a goood key.
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

      return false;
    }
    return false;

  }

  private boolean significantShortMessage(ShortMessage message) {
    int command = message.getCommand();
    switch (command) {
      case ShortMessage.CONTROL_CHANGE:
        return true;
      case ShortMessage.PITCH_BEND:
        return true;
      case ShortMessage.PROGRAM_CHANGE:
        return true;
      case ShortMessage.POLY_PRESSURE:
        return true;
      case ShortMessage.CHANNEL_PRESSURE:
        return true;
      default:
        return false;
    }
  }

  /**
   * Check if for a given event it is the first time we see it.
   * @param key a number that identifies the type of event.
   * @return true if such an event has already been 
   * seen. Return false if it is the first time that such 
   * an event is encountered.
   */
  private boolean allreadyEncountered(int key) {
    if (encounteredItems.contains(key)) {
      return true;
    }
    encounteredItems.add(key);
    return false;
  }
}
