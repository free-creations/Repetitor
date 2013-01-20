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

/**
 * Provides utility functions to support the <em>General-Midi</em> standard.
 * <em>General-MIDI</em> or <em>GM</em> is a standardised specification
 * for music synthesisers
 * that respond to MIDI messages. Especially the
 * the instrument sound or "program" that shall be selected with the <em>Program
 * Change message</em> is defined in GM. This utility permits to translate
 * the program number into a human readable string.
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class GmUtil {

  private static final String[] instrumentNames = new String[]{
    /*------ Piano */
    /*  0 */"Acoustic Grand Piano",
    /*  1 */ "Bright Acoustic Piano",
    /*  2 */ "Electric grand Piano",
    /*  3 */ "Honky Tonk Piano",
    /*  4 */ "Electric Piano 1",
    /*  5 */ "Electric Piano 2",
    /*  6 */ "Harpsichord",
    /*  7 */ "Clavinet",
    /*------ Chromatic Percussion */
    /*  8 */ "Celesta",
    /*  9 */ "Glockenspiel",
    /* 10 */ "Music Box",
    /* 11 */ "Vibraphone",
    /* 12 */ "Marimba",
    /* 13 */ "Xylophone",
    /* 14 */ "Tubular bells",
    /* 15 */ "Dulcimer",
    /*------ Organ */
    /* 16 */ "Drawbar Organ",
    /* 17 */ "Percussive Organ",
    /* 18 */ "Rock Organ",
    /* 19 */ "Church Organ",
    /* 20 */ "Reed Organ",
    /* 21 */ "Accordion",
    /* 22 */ "Harmonica",
    /* 23 */ "Tango Accordion",
    /*------ Guitar */
    /* 24 */ "Acoustic Guitar (nylon)",
    /* 25 */ "Acoustic Guitar (steel)",
    /* 26 */ "Electric Guitar (jazz)",
    /* 27 */ "Electric Guitar (clean)",
    /* 28 */ "Electric Guitar (muted)",
    /* 29 */ "Overdriven Guitar",
    /* 30 */ "Distortion Guitar",
    /* 31 */ "Guitar harmonics",
    /*------ Bass */
    /* 32 */ "Acoustic Bass",
    /* 33 */ "Electric Fingered Bass",
    /* 34 */ "Electric Picked Bass",
    /* 35 */ "Fretless Bass",
    /* 36 */ "Slap Bass 1",
    /* 37 */ "Slap Bass 2",
    /* 38 */ "Syn Bass 1",
    /* 39 */ "Syn Bass 2",
    /*------ Strings/Orchestra */
    /* 40 */ "Violin",
    /* 41 */ "Viola",
    /* 42 */ "Cello",
    /* 43 */ "Contrabass",
    /* 44 */ "Tremolo Strings",
    /* 45 */ "Pizzicato Strings",
    /* 46 */ "Orchestral Harp",
    /* 47 */ "Timpani",
    /*------ Ensemble */
    /* 48 */ "String Ensemble 1",
    /* 49 */ "String Ensemble 2 (Slow)",
    /* 50 */ "Syn Strings 1",
    /* 51 */ "Syn Strings 2",
    /* 52 */ "Choir Aahs",
    /* 53 */ "Voice Oohs",
    /* 54 */ "Syn Choir",
    /* 55 */ "Orchestral Hit",
    /*------ Brass */
    /* 56 */ "Trumpet",
    /* 57 */ "Trombone",
    /* 58 */ "Tuba",
    /* 59 */ "Muted Trumpet",
    /* 60 */ "French Horn",
    /* 61 */ "Brass Section",
    /* 62 */ "Syn Brass 1",
    /* 62 */ "Syn Brass 2",
    /*------ Reed */
    /* 64 */ "Soprano Sax",
    /* 65 */ "Alto Sax",
    /* 66 */ "Tenor Sax",
    /* 67 */ "Baritone Sax",
    /* 68 */ "Oboe",
    /* 69 */ "English Horn",
    /* 7O */ "Bassoon",
    /* 71 */ "Clarinet",
    /*------ Pipe */
    /* 72 */ "Piccolo",
    /* 73 */ "Flute",
    /* 74 */ "Recorder",
    /* 75 */ "Pan Flute",
    /* 76 */ "Bottle Blow",
    /* 77 */ "Shakuhachi",
    /* 78 */ "Whistle",
    /* 79 */ "Ocarina",
    /*------ Synth Lead */
    /* 80 */ "Syn Square Wave",
    /* 81 */ "Syn Sawtooth Wave",
    /* 82 */ "Syn Calliope",
    /* 83 */ "Syn Chiff",
    /* 84 */ "Syn Charang",
    /* 85 */ "Syn Voice",
    /* 86 */ "Syn Fifths Sawtooth Wave",
    /* 87 */ "Syn Brass & Lead",
    /*------ Synth Pad */
    /* 88 */ "New Age Syn Pad",
    /* 89 */ "Warm Syn Pad",
    /* 90 */ "Polysynth Syn Pad",
    /* 91 */ "Choir Syn Pad",
    /* 92 */ "Bowed Syn Pad",
    /* 93 */ "Metal Syn Pad",
    /* 94 */ "Halo Syn Pad",
    /* 95 */ "Sweep Syn Pad",
    /*------ Synth Effects */
    /* 96 */ "SFX Rain",
    /* 97 */ "SFX Soundtrack",
    /* 98 */ "SFX Crystal",
    /* 99 */ "SFX Atmosphere",
    /*100 */ "SFX Brightness",
    /*101 */ "SFX Goblins",
    /*102 */ "SFX Echoes",
    /*103 */ "SFX Sci-fi",
    /*------ Ethnic */
    /*104 */ "Sitar",
    /*105 */ "Banjo",
    /*106 */ "Shamisen",
    /*107 */ "Koto",
    /*108 */ "Kalimba",
    /*109 */ "Bag Pipe",
    /*110 */ "Fiddle",
    /*111 */ "Shanai",
    /*------ Percussive */
    /*112 */ "Tinkle Bell",
    /*113 */ "Agogo",
    /*114 */ "Steel Drums",
    /*115 */ "Woodblock",
    /*116 */ "Taiko Drum",
    /*117 */ "Melodic Tom",
    /*118 */ "Syn Drum",
    /*119 */ "Reverse Cymbal",
    /*------ Sound Effects */
    /*120 */ "Guitar Fret Noise",
    /*121 */ "Breath Noise",
    /*122 */ "Seashore",
    /*123 */ "Bird Tweet",
    /*124 */ "Telephone Ring",
    /*125 */ "Helicopter",
    /*126 */ "Applause",
    /*127 */ "Gun Shot",};

  /**
   * Translates the General-Midi-Instrument-Number into a human readable string.
   * @param gmNumber the General-Midi-Instrument-Number (zero based)
   * @return the melodic sound as a human readable string.
   */
  public static String numberToString(int gmNumber) {
    if ((gmNumber >= 0)
            && (gmNumber < instrumentNames.length)) {
      return instrumentNames[gmNumber];
    }
    return "" + gmNumber;
  }
}
