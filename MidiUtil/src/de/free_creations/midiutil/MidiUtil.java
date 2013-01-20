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
import javax.sound.midi.*;

/**
 * A collection of useful functions to manipulate MIDI messages and MIDI
 * sequences.
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class MidiUtil {

  // The biggest int that can be represented by 14 bit.
  // Many MIDI controller have this number as maximum.
  public static final int MAX14BIT = 0x3FFF;
  //Defined controller numbers
  public static final int /*
           * 0
           */ contBankSelect_MSB = 0;
  public static final int /*
           * 1
           */ contModulationWheel_MSB = 1;
  public static final int /*
           * 2
           */ contBreathControl_MSB = 2;
  public static final int /*
           * 3
           */ contContinuousController_3_MSB = 3;
  public static final int /*
           * 4
           */ contFootController_MSB = 4;
  public static final int /*
           * 5
           */ contPortamentoTime_MSB = 5;
  public static final int /*
           * 6
           */ contDataEntry_MSB = 6;
  public static final int /*
           * 7
           */ contMainVolume_MSB = 7;
  public static final int /*
           * 8
           */ contContinuousController_8_MSB = 8;
  public static final int /*
           * 9
           */ contContinuousController_9_MSB = 9;
  public static final int /*
           * 10
           */ contPan_MSB = 10;
  public static final int /*
           * 11
           */ contExpression_MSB = 11;
  public static final int /*
           * 12
           */ contContinuousController_12_MSB = 12;
  public static final int /*
           * 13
           */ contContinuousController_13_MSB = 13;
  public static final int /*
           * 14
           */ contContinuousController_14_MSB = 14;
  public static final int /*
           * 15
           */ contContinuousController_15_MSB = 15;
  public static final int /*
           * 16
           */ contGeneralPurposeSlider_1 = 16;
  public static final int /*
           * 17
           */ contGeneralPurposeSlider_2 = 17;
  public static final int /*
           * 18
           */ contGeneralPurposeSlider_3 = 18;
  public static final int /*
           * 19
           */ contGeneralPurposeSlider_4 = 19;
  public static final int /*
           * 20
           */ contContinuousController_20_MSB = 20;
  public static final int /*
           * 21
           */ contContinuousController_21_MSB = 21;
  public static final int /*
           * 22
           */ contContinuousController_22_MSB = 22;
  public static final int /*
           * 23
           */ contContinuousController_23_MSB = 23;
  public static final int /*
           * 24
           */ contContinuousController_24_MSB = 24;
  public static final int /*
           * 25
           */ contContinuousController_25_MSB = 25;
  public static final int /*
           * 26
           */ contContinuousController_26_MSB = 26;
  public static final int /*
           * 27
           */ contContinuousController_27_MSB = 27;
  public static final int /*
           * 28
           */ contContinuousController_28_MSB = 28;
  public static final int /*
           * 29
           */ contContinuousController_29_MSB = 29;
  public static final int /*
           * 30
           */ contContinuousController_30_MSB = 30;
  public static final int /*
           * 31
           */ contContinuousController_31_MSB = 31;
  public static final int /*
           * 32
           */ contBankSelect_LSB = 32;
  public static final int /*
           * 33
           */ contModulationWheel_LSB = 33;
  public static final int /*
           * 34
           */ contBreathControl_LSB = 34;
  public static final int /*
           * 35
           */ contContinuousController_3_LSB = 35;
  public static final int /*
           * 36
           */ contFootController_LSB = 36;
  public static final int /*
           * 37
           */ contPortamentoTime_LSB = 37;
  public static final int /*
           * 38
           */ contDataEntry_LSB = 38;
  public static final int /*
           * 39
           */ contMainVolume_LSB = 39;
  public static final int /*
           * 40
           */ contContinuousController_8_LSB = 40;
  public static final int /*
           * 41
           */ contContinuousController_9_LSB = 41;
  public static final int /*
           * 42
           */ contPan_LSB = 42;
  public static final int /*
           * 43
           */ contExpression_LSB = 43;
  public static final int /*
           * 44
           */ contContinuousController_12_LSB = 44;
  public static final int /*
           * 45
           */ contContinuousController_13_LSB = 45;
  public static final int /*
           * 46
           */ contContinuousController_14_LSB = 46;
  public static final int /*
           * 47
           */ contContinuousController_15_LSB = 47;
  public static final int /*
           * 48
           */ contContinuousController_16_LSB = 48;
  public static final int /*
           * 49
           */ contContinuousController_17_LSB = 49;
  public static final int /*
           * 50
           */ contContinuousController_18_LSB = 50;
  public static final int /*
           * 51
           */ contContinuousController_19_LSB = 51;
  public static final int /*
           * 52
           */ contContinuousController_20_LSB = 52;
  public static final int /*
           * 53
           */ contContinuousController_21_LSB = 53;
  public static final int /*
           * 54
           */ contContinuousController_22_LSB = 54;
  public static final int /*
           * 55
           */ contContinuousController_23_LSB = 55;
  public static final int /*
           * 56
           */ contContinuousController_24_LSB = 56;
  public static final int /*
           * 57
           */ contContinuousController_25_LSB = 57;
  public static final int /*
           * 58
           */ contContinuousController_26_LSB = 58;
  public static final int /*
           * 59
           */ contContinuousController_27_LSB = 59;
  public static final int /*
           * 60
           */ contContinuousController_28_LSB = 60;
  public static final int /*
           * 61
           */ contContinuousController_29_LSB = 61;
  public static final int /*
           * 62
           */ contContinuousController_30_LSB = 62;
  public static final int /*
           * 63
           */ contContinuousController_31_LSB = 63;
  public static final int /*
           * 64
           */ contSustain = 64;
  public static final int /*
           * 65
           */ contPortamento = 65;
  public static final int /*
           * 66
           */ contSustenuto = 66;
  public static final int /*
           * 67
           */ contSoftPedal = 67;
  public static final int /*
           * 68
           */ contLegatoPedal = 68;
  public static final int /*
           * 69
           */ contHold_2_Pedal = 69;
  public static final int /*
           * 70
           */ contUndefined_03 = 70;
  public static final int /*
           * 71
           */ contUndefined_04 = 71;
  public static final int /*
           * 72
           */ contUndefined_05 = 72;
  public static final int /*
           * 73
           */ contUndefined_06 = 73;
  public static final int /*
           * 74
           */ contUndefined_07 = 74;
  public static final int /*
           * 75
           */ contUndefined_08 = 75;
  public static final int /*
           * 76
           */ contUndefined_09 = 76;
  public static final int /*
           * 77
           */ contUndefined_10 = 77;
  public static final int /*
           * 78
           */ contUndefined_11 = 78;
  public static final int /*
           * 79
           */ contUndefined_12 = 79;
  public static final int /*
           * 80
           */ contUndefined_13 = 80;
  public static final int /*
           * 81
           */ contUndefined_14 = 81;
  public static final int /*
           * 82
           */ contUndefined_15 = 82;
  public static final int /*
           * 83
           */ contUndefined_16 = 83;
  public static final int /*
           * 84
           */ contUndefined_17 = 84;
  public static final int /*
           * 85
           */ contUndefined_18 = 85;
  public static final int /*
           * 86
           */ contUndefined_19 = 86;
  public static final int /*
           * 87
           */ contUndefined_20 = 87;
  public static final int /*
           * 88
           */ contUndefined_21 = 88;
  public static final int /*
           * 89
           */ contUndefined_22 = 89;
  public static final int /*
           * 90
           */ contUndefined_23 = 90;
  public static final int /*
           * 91
           */ contEffectsLevel = 91;
  public static final int /*
           * 92
           */ contUndefined_25 = 92;
  public static final int /*
           * 93
           */ contChorusLevel = 93;
  public static final int /*
           * 94
           */ contUndefined_27 = 94;
  public static final int /*
           * 95
           */ contUndefined_28 = 95;
  public static final int /*
           * 96
           */ contDataEntryPlus = 96;
  public static final int /*
           * 97
           */ contDataEntryMinus = 97;
  public static final int /*
           * 98
           */ contNRPN_LSB = 98;
  public static final int /*
           * 99
           */ contNRPN_MSB = 99;
  public static final int /*
           * 100
           */ contRPN_LSB = 100;
  public static final int /*
           * 101
           */ contRPN_MSB = 101;
  public static final int /*
           * 102
           */ contUndefined_3 = 102;
  public static final int /*
           * 103
           */ contUndefined_34 = 103;
  public static final int /*
           * 104
           */ contUndefined_35 = 104;
  public static final int /*
           * 105
           */ contUndefined_46 = 105;
  public static final int /*
           * 106
           */ contUndefined_47 = 106;
  public static final int /*
           * 107
           */ contUndefined_48 = 107;
  public static final int /*
           * 108
           */ contUndefined_49 = 108;
  public static final int /*
           * 109
           */ contUndefined_50 = 109;
  public static final int /*
           * 110
           */ contUndefined_51 = 110;
  public static final int /*
           * 111
           */ contUndefined_52 = 111;
  public static final int /*
           * 112
           */ contUndefined_53 = 112;
  public static final int /*
           * 113
           */ contUndefined_54 = 113;
  public static final int /*
           * 114
           */ contUndefined_55 = 114;
  public static final int /*
           * 115
           */ contUndefined_56 = 115;
  public static final int /*
           * 116
           */ contUndefined_57 = 116;
  public static final int /*
           * 117
           */ contUndefined_58 = 117;
  public static final int /*
           * 118
           */ contUndefined_59 = 118;
  public static final int /*
           * 119
           */ contUndefined_60 = 119;
  public static final int /*
           * 120
           */ contAllSoundOff = 120;
  public static final int /*
           * 121
           */ contAllControllersOff = 121;
  public static final int /*
           * 122
           */ contLocalControl = 122;
  public static final int /*
           * 123
           */ contAllNotesOff = 123;
  public static final int /*
           * 124
           */ contOmniModeOff = 124;
  public static final int /*
           * 125
           */ contOmniModeOn = 125;
  public static final int /*
           * 126
           */ contMonophonicOperation = 126;
  public static final int /*
           * 127
           */ contPolyphonicOperation = 127;
  
  /** Meta Events **/
  public static final int textMeta = 0x01;
  public static final int copyrightMeta = 0x02;
  public static final int tracknameMeta = 0x03;
  public static final int instrumentNameMeta = 0x04;
  public static final int lyricMeta = 0x05;
  public static final int markerMeta = 0x06;
  public static final int cuePointMeta = 0x07;
  public static final int programNameMeta = 0x08;
  public static final int portNameMeta = 0x09;
  public static final int tempoMeta = 0x51;
  public static final int timeSignatureMeta = 0x58;
  public static final int keySignatureMeta = 0x59;
  public static final int endOfTrackMeta = 0x2F;

  /**
   * Cut a sequence in two parts and throw away head, returning the tail. The
   * returned sequence is a new sequence, the given sequence is not touched. All
   * events are shifted to the start of the new sequence. Events in the given
   * sequence that influenced the sound at the cut position are shifted to the
   * start of the new sequence.
   *
   * @param seq the given sequence
   * @param pos the position where to cut.
   * @return a sequence that holds all the events on the right of the cut
   * position plus all the events that are needed to initialise the sequencer.
   */
  public static Sequence leftCut(Sequence seq, long pos) {
    Knife knife = new Knife(seq, pos);
    return knife.getResult();
  }

  /**
   * Insert silence at the beginning of a sequence.
   *
   * @param seq the input sequence
   * @param ticks the number of Midi ticks that should be inserted.
   * @return a new copy of the input sequence with the given number of ticks
   * inserted at the beginning. The time signature event and the tempo event are
   * shifted to the new beginning of the sequence.
   */
  public static Sequence insertSilence(Sequence seq, long ticks) {
    Knife knife = new Knife(seq, -ticks);
    return knife.getResult();
  }

  /**
   * Removes a region from a sequence.
   *
   * @param seq the input sequence.
   * @param fromTick the first midi tick of the region to be deleted.
   * @param toTick the first midi tick that will not be removed but shifted to
   * the left.
   * @return a new midi sequence
   * @Todo move controller messages to the "toTick" position... move note-off
   * messages to the fromTick position this function may replace "leftcut" and
   * "rightcut".
   * @Todo move relevant events to the borders before cutting 1) note offs
   * (those whose note on is not in the region) 2) the last controller...
   */
  public static Sequence cut(Sequence seq, long fromTick, long toTick) {
    if (toTick < fromTick) {
      throw new IllegalArgumentException("leftBorder must be larger than rightBorder.");
    }
    Sequence newseq;
    Track[] fromTracks = seq.getTracks();
    try {
      // construct a new sequence with the same propertiews as the given sequence.
      newseq = new Sequence(seq.getDivisionType(), seq.getResolution(), fromTracks.length);
    } catch (InvalidMidiDataException ex) {
      Logger.getLogger(MidiUtil.class.getName()).log(Level.SEVERE, null, ex);
      return seq;
    }
    long shift = toTick - fromTick;

    Track[] newTracks = newseq.getTracks();
    for (int t = 0; t < fromTracks.length; t++) {
      Track fromTrack = fromTracks[t];
      Track newTrack = newTracks[t];
      for (int i = 0; i < fromTrack.size(); i++) {
        MidiEvent event = fromTrack.get(i);
        if (event.getTick() < fromTick) {
          MidiEvent newEvent =
                  new MidiEvent((MidiMessage) event.getMessage().clone(),
                  event.getTick());
          newTrack.add(newEvent);
        } else if (event.getTick() < toTick) {
          if (Note.isNoteOffEvent(event)) {
            MidiEvent newEvent =
                    new MidiEvent((MidiMessage) event.getMessage().clone(),
                    fromTick);
            newTrack.add(newEvent);
          }
          //ignore this message for now
          /**
           * @ToDo move relevant events to the right border
           */
        } else {
          long newPos = event.getTick() - shift;
          MidiEvent newEvent =
                  new MidiEvent((MidiMessage) event.getMessage().clone(),
                  newPos);
          newTrack.add(newEvent);
        }
      }
    }
    return newseq;
  }

  /**
   * Stretch or squeeze a region in a sequence. All events laying in between
   * left and right border will be proportionally. All events laying on the
   * right of the right-border will be shifted by an amount of (newRightBorder -
   * rightBorder) to the right.
   *
   * @param seq the input sequence
   * @param leftBorder the lower delimitation of the input region to be shifted.
   * @param rightBorder the higher delimitation of the input region to be
   * shifted.
   * @param newRightBorder the new higher delimitation same region in the
   * output.
   * @return a new sequence which is a copy of the input sequence, but all
   * events following the leftBorder are shifted.
   */
  public static Sequence stretch(Sequence seq, long leftBorder, long rightBorder, long newRightBorder) {
    if (rightBorder <= leftBorder) {
      throw new IllegalArgumentException("leftBorder must be larger than rightBorder.");
    }
    if (rightBorder <= newRightBorder) {
      throw new IllegalArgumentException("leftBorder must be larger than newRightBorder.");
    }
    Sequence newseq;
    Track[] fromTracks = seq.getTracks();
    try {
      // construct a new sequence with the same propertiews as the given sequence.
      newseq = new Sequence(seq.getDivisionType(), seq.getResolution(), fromTracks.length);
    } catch (InvalidMidiDataException ex) {
      Logger.getLogger(MidiUtil.class.getName()).log(Level.SEVERE, null, ex);
      return seq;
    }

    Track[] newTracks = newseq.getTracks();
    for (int t = 0; t < fromTracks.length; t++) {
      Track fromTrack = fromTracks[t];
      Track newTrack = newTracks[t];
      stretchTrack(fromTrack, newTrack, leftBorder, rightBorder, newRightBorder);
    }
    return newseq;

  }

  private static void stretchTrack(Track fromTrack, Track newTrack, long leftBorder, long rightBorder, long newRightBorder) throws ArrayIndexOutOfBoundsException {

    long shift = newRightBorder - rightBorder;

    for (int i = 0; i < fromTrack.size(); i++) {
      MidiEvent event = fromTrack.get(i);
      if (event.getTick() < leftBorder) {
        MidiEvent newEvent =
                new MidiEvent((MidiMessage) event.getMessage().clone(),
                event.getTick());
        newTrack.add(newEvent);
      } else if (event.getTick() < rightBorder) {
        long newPos = event.getTick() + ((event.getTick() - leftBorder) * shift) / (rightBorder - leftBorder);
        MidiEvent newEvent =
                new MidiEvent((MidiMessage) event.getMessage().clone(),
                newPos);
        newTrack.add(newEvent);
      } else {
        long newPos = event.getTick() + shift;
        MidiEvent newEvent =
                new MidiEvent((MidiMessage) event.getMessage().clone(),
                newPos);
        newTrack.add(newEvent);
      }
    }
  }

  /**
   * Sets the master volume using a system exclusive message. Although the
   * Gervill synthesiser honours this message, it is not recommended to be used
   * as it produces ugly crackling sounds..
   *
   * @param volume must be a 14 bit value (between 0 and 16383);
   * @return the system exclusive message the changes the master volume.
   * @throws InvalidMidiDataException
   * @deprecated do not use, because of ugly crackling sounds..
   */
  @Deprecated
  static public SysexMessage createMasterVolumeMessage(int volume) throws InvalidMidiDataException {
    if (volume > MAX14BIT) {
      throw new InvalidMidiDataException("Volume " + volume + " too large.");
    }

    if (volume < 0) {
      throw new InvalidMidiDataException("volume " + volume + " too small.");
    }

    byte lowByte = (byte) (volume & 0x7F);
    byte hiByte = (byte) ((volume & 0x3F80) >> 7);

    SysexMessage message = new SysexMessage();
    byte[] data = new byte[]{
      (byte) 0x7F, // Realtime
      (byte) 0x7F, // "disregard channel".
      (byte) 0x04, // Sub-ID -- Device Control
      (byte) 0x01, // Sub-ID2 -- Master Volume
      lowByte, // Bits 0 to 6 of a 14-bit volume
      hiByte, // Bits 7 to 13 of a 14-bit volume
      (byte) 0xF7 // End of SysEx
    };
    message.setMessage(
            SysexMessage.SYSTEM_EXCLUSIVE,
            data,
            data.length);
    return message;

  }

  /**
   * Determine which channels are used within a given track.
   *
   * @param track
   * @return the number channels used in the given track.
   */
  public static int[] usedChannels(Track track) {
    HashSet<Integer> encounteredChannels = new HashSet<Integer>();
    for (int i = 0; i < track.size(); i++) {
      MidiMessage message = track.get(i).getMessage();
      if (isNoteOnMessage(message)) {
        ShortMessage shortMessage = (ShortMessage) message;
        encounteredChannels.add(shortMessage.getChannel());
      }
    }
    int[] result = new int[encounteredChannels.size()];

    int i = 0;
    for (Integer channel : encounteredChannels) {
      result[i] = channel;
      i++;
    }
    return result;

  }

  /**
   * Discover a "track name event" in a given track.
   *
   * @param track
   * @return the track name or an empty string.
   */
  public static String readTrackname(Track track) {
    if (track == null) {
      return "";
    }
    for (int i = 0; i < track.size(); i++) {
      MidiMessage message = track.get(i).getMessage();
      if (isTrackNameMessage(message)) {
        MetaMessage trackNameMessage = (MetaMessage) message;
        byte[] stringData = trackNameMessage.getData();
        return new String(stringData);
      }
    }
    return "";
  }

  /**
   * Determine whether the given event is a track name event.
   *
   * @param event
   * @return true if the given event holds the track name.
   */
  public static boolean isTrackNameEvent(MidiEvent event) {
    if (null == event) {
      return false;
    }
    return isTrackNameMessage(event.getMessage());
  }

  /**
   * Determine whether the given event is a track name message.
   *
   * @param message
   * @return true if the given message holds the track name.
   */
  public static boolean isTrackNameMessage(MidiMessage message) {
    if (null == message) {
      return false;
    }
    if (!(message instanceof MetaMessage)) {
      return false;
    }
    MetaMessage metaMessage = (MetaMessage) message;
    int messageType = metaMessage.getType();
    return (messageType == tracknameMeta);
  }

  /**
   * Determine whether the given event is a Lyric (a syllable of the song text).
   *
   * @param event
   * @return true if the given event is a lyric event
   */
  public static boolean isLyricsEvent(MidiEvent event) {
    if (null == event) {
      return false;
    }
    return isLyricsMessage(event.getMessage());
  }

  /**
   * Determine whether the given event is a Program change event
   *
   * @param event
   * @return true if the event is a program change
   */
  public static boolean isProgramEvent(MidiEvent event) {
    if (null == event) {
      return false;
    }
    return isProgramMessage(event.getMessage());
  }

  /**
   * Determine whether the given message is a Program change message (change of
   * the instrument sound).
   *
   * @param message any MIDI message or null.
   * @return true if the given message is a program change.
   */
  public static boolean isProgramMessage(MidiMessage message) {
    if (null == message) {
      return false;
    }
    if (!(message instanceof ShortMessage)) {
      return false;
    }
    ShortMessage shortMessage = (ShortMessage) message;
    int command = shortMessage.getCommand();
    return (command == ShortMessage.PROGRAM_CHANGE);
  }

  /**
   * Determine whether the given message is a Lyric.
   *
   * @param message any MIDI message or null.
   * @return true if the given message is a lyrics message
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
    return (messageType == lyricMeta);
  }

  /**
   * Check if the given message is a real "Note On" message. Note: messages with
   * a command of "NOTE_ON" but velocity of 0 are often used in place of
   * note-off messages, such messages are not considered as real note-on
   * message.
   *
   * @param message any MIDI message or null.
   * @return true if the given message denotes the start of a note.
   */
  public static boolean isNoteOnMessage(MidiMessage message) {
    if (null == message) {
      return false;
    }
    if (message instanceof ShortMessage) {
      ShortMessage shortMessage = (ShortMessage) message;
      if (shortMessage.getCommand() == ShortMessage.NOTE_ON) {
        if (shortMessage.getData2() != 0) {
          return true; // Note on and velocity is not 0
        }
      }

    }
    return false; //all other cases
  }

  /**
   * Cut a sequence in two and throw away the tail, returning the head. An
   * "AllControllersOff" event will be inserted at the end of the sequence to
   * avoid hanging notes.
   *
   * @param seq the given sequence
   * @param end the position where the sequence should be cut
   * @return the left part
   */
  public static Sequence rightCut(Sequence seq, long end) {

    Sequence newseq;
    Track[] tracks = seq.getTracks();
    try {
      // construct a new sequence with the same propertiews as the given sequence.
      newseq = new Sequence(seq.getDivisionType(), seq.getResolution(), tracks.length);
    } catch (InvalidMidiDataException ex) {
      Logger.getLogger(MidiUtil.class.getName()).log(Level.SEVERE, null, ex);
      return seq;
    }
    if (end < 1) {
      return newseq; // an empty sequence
    }


    Track[] newTracks = newseq.getTracks();
    for (int i = 0; i < tracks.length; i++) {
      copyTrack(tracks[i], newTracks[i], end);
    }
    return newseq;
  }

  private static void copyTrack(Track fromTrack, Track toTrack, long size) {

    // copy all event before "size" to the new track
    for (int i = 0; i < fromTrack.size(); i++) {
      MidiEvent event = fromTrack.get(i);
      if (event.getTick() < size) {
        MidiEvent newEvent =
                new MidiEvent((MidiMessage) event.getMessage().clone(),
                event.getTick());
        toTrack.add(newEvent);
      }
    }
    // insert appropriate "AllControllersOff" at the end of the track
    int[] usedChannels = usedChannels(toTrack);
    for (int i = 0; i < usedChannels.length; i++) {
      int channel = usedChannels[i];
      ShortMessage allContrOffMessage = new ShortMessage();
      try {
        allContrOffMessage.setMessage(ShortMessage.CONTROL_CHANGE,
                channel, contAllNotesOff, 0);
      } catch (InvalidMidiDataException ex) {
        Logger.getLogger(MidiUtil.class.getName()).log(Level.SEVERE, null, ex);
      }
      MidiEvent event = new MidiEvent(allContrOffMessage, size);
      toTrack.add(event);
    }



  }

  public static Sequence transpose(Sequence seq, int semiTones) {

    Sequence newseq;
    Track[] fromTracks = seq.getTracks();
    try {
      // construct a new sequence with the same propertiews as the given sequence.
      newseq = new Sequence(seq.getDivisionType(), seq.getResolution(), fromTracks.length);
    } catch (InvalidMidiDataException ex) {
      Logger.getLogger(MidiUtil.class.getName()).log(Level.SEVERE, null, ex);
      return seq;
    }

    Track[] newTracks = newseq.getTracks();
    for (int t = 0; t < fromTracks.length; t++) {
      Track fromTrack = fromTracks[t];
      Track newTrack = newTracks[t];
      // copy all event before "size" to the new track
      for (int i = 0; i < fromTrack.size(); i++) {
        MidiEvent event = fromTrack.get(i);
        MidiEvent newEvent = transposeEvent(event, semiTones);
        newTrack.add(newEvent);
      }
    }
    return newseq;
  }

  public static Sequence stretch(Sequence seq, int factor) {

    Sequence newseq;
    Track[] fromTracks = seq.getTracks();
    try {
      // construct a new sequence with the same propertiews as the given sequence.
      newseq = new Sequence(seq.getDivisionType(), seq.getResolution(), fromTracks.length);
    } catch (InvalidMidiDataException ex) {
      Logger.getLogger(MidiUtil.class.getName()).log(Level.SEVERE, null, ex);
      return seq;
    }

    Track[] newTracks = newseq.getTracks();
    for (int t = 0; t < fromTracks.length; t++) {
      Track fromTrack = fromTracks[t];
      Track newTrack = newTracks[t];
      // copy all event before "size" to the new track
      for (int i = 0; i < fromTrack.size(); i++) {
        MidiEvent event = fromTrack.get(i);
        MidiEvent newEvent =
                new MidiEvent((MidiMessage) event.getMessage().clone(),
                event.getTick() * factor);
        newTrack.add(newEvent);
      }
    }
    return newseq;
  }

  private static MidiEvent transposeEvent(MidiEvent event, int semiTones) {
    MidiEvent newEvent =
            new MidiEvent((MidiMessage) event.getMessage().clone(),
            event.getTick());
    if (Note.isNoteOnEvent(event) || Note.isNoteOffEvent(event)) {
      ShortMessage message = (ShortMessage) newEvent.getMessage();
      int pitch = message.getData1() + semiTones;
      try {
        message.setMessage(message.getStatus(), pitch, message.getData2());
      } catch (InvalidMidiDataException ignored) {
      }
    }
    return newEvent;
  }
}
