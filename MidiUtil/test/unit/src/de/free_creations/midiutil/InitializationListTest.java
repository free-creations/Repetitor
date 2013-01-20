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

import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.sound.midi.Sequence;
import javax.sound.midi.InvalidMidiDataException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Harald Postner 
 */
public class InitializationListTest {

    /**
     * Test the handling of "main volume" messages
     */
    @Test
    public void testMainVolume() throws InvalidMidiDataException {
        System.out.println("testMainVolume");
        Sequence sequence = new Sequence(Sequence.PPQ, 240, 2);
        Track track = sequence.getTracks()[1];


        final int pickPos = 240; // the position of an event that must be picked
        final int startPos = pickPos + 480; //here we'll start playback of the sequence

        // an event to be ignored (before the event that we expect to be taken over)
        insertVolume(track, pickPos - 10, 01, 00);

        // the event that we expect to be taken over
        ShortMessage candidateMessage = insertVolume(track, pickPos, 01, 01);

        // an event to be ignored (after the start position)
        insertVolume(track, startPos + 100, 01, 02);




        InitializationList candidate = new InitializationList(track, startPos);

        // verify ...
        assertEquals(1, candidate.size());
        assertEquals(candidateMessage, candidate.get(0));


    }

    /**
     * Test the handling of "pitch bend" messages
     */
    @Test
    public void testPitchBend() throws InvalidMidiDataException {
        System.out.println("testPitchBend");
        Sequence sequence = new Sequence(Sequence.PPQ, 240, 2);
        Track track = sequence.getTracks()[1];


        final int pickPos = 240; // the position of an event that must be picked
        final int startPos = pickPos + 480; //here we'll start playback of the sequence

        // an event to be ignored (before the event that we expect to be taken over)
        insertPitchBend(track, pickPos - 10, 01, 00);

        // the event that we expect to be taken over
        ShortMessage candidateMessage = insertPitchBend(track, pickPos, 01, 01);

        // an event to be ignored (after the start position)
        insertPitchBend(track, startPos + 100, 01, 02);




        InitializationList candidate = new InitializationList(track, startPos);

        // verify ...
        assertEquals(1, candidate.size());
        assertEquals(candidateMessage, candidate.get(0));

    }

    private ShortMessage insertVolume(Track track, long pos, int channel, int data) throws InvalidMidiDataException {
        ShortMessage message = new ShortMessage();
        message.setMessage(
                ShortMessage.CONTROL_CHANGE, //command,
                channel, //channel,
                07, //data1,
                data);//data2)

        MidiEvent event = new MidiEvent(message, pos);
        track.add(event);
        return message;
    }

    private ShortMessage insertPitchBend(Track track, long pos, int channel, int data) throws InvalidMidiDataException {
        ShortMessage message = new ShortMessage();
        message.setMessage(
                ShortMessage.PITCH_BEND, //command,
                channel, //channel,
                data, //data1,
                0);//data2)

        MidiEvent event = new MidiEvent(message, pos);
        track.add(event);
        return message;
    }
}
