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
package de.free_creations.microsequencer;

import de.free_creations.midiutil.MidiUtil;
import java.io.File;
import java.io.IOException;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Track;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbModuleSuite.Configuration;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;
import rtaudio4java.AudioSystemFactory;
import rtaudio4java.RtError;

/**
 * This class tests the whole MicroSequncer package.
 * We assume that the RtAudio4Netbeans  by itself has been  thoroughly tested
 * in its own project. 
 * For more information on how to test NetBeans platform modules see also
 * {@link http://platform.netbeans.org/tutorials/nbm-test.html
 * http://platform.netbeans.org/tutorials/nbm-test.html}
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class OverallTest extends NbTestCase {

  /** Constructor required by JUnit */
  public OverallTest(String name) {
    super(name);
  }

  public static Test suite() {

    Configuration testConfig = NbModuleSuite.createConfiguration(OverallTest.class);
    testConfig.clusters(".*").enableModules(".*");
    testConfig.gui(true);

    return NbModuleSuite.create(testConfig);

  }

  /** Called before every test case. */
  @Override
  public void setUp() {
  }

  public void testAll() throws InvalidMidiDataException, IOException, MidiUnavailableException, RtError, InterruptedException {
    try {
      System.out.println("########  ProvisorischerTest3 #######");
      File orchestraSbkFile = new File("/home/harald/Music/SoundFonts/gm/Chorium.SF2");
      //File voicesSbkFile = new File("/home/harald/Music/SoundFonts/pianos/E_Piano/Roland_64VoicePiano.sf2");
      File voicesSbkFile = new File("/home/harald/Music/SoundFonts/pianos/Piano/steinbow_mg.sf2");

      File orchestraMid = new File("/win/D/myMusik/Midi/Chor/FaureRequiem/free-creations/"
              + "Introit.mid");
      File voicesMid = new File("/win/D/myMusik/Midi/Chor/FaureRequiem/free-creations/"
              + "IntroitVoices.MID");

      String osName = System.getProperty("os.name").toLowerCase();
      if (osName.indexOf("windows") > -1) {
        orchestraMid = new File("D:\\myMusik\\Midi\\Chor\\FaureRequiem\\free-creations\\"
                + "Introit.mid");
        voicesMid = new File("D:\\myMusik\\Midi\\Chor\\FaureRequiem\\free-creations\\"
                + "IntroitVoices.MID");
        orchestraSbkFile = new File("D:\\myMusik\\SoundFonts\\GM\\Chorium.SF2");
        voicesSbkFile = new File("D:\\myMusik\\SoundFonts\\pianos\\E_Piano\\Roland_64VoicePiano.sf2");
      }

      if (!orchestraMid.exists()) {
        throw new RuntimeException("Test-setup error: orchestraMid not found.");
      }
      if (!voicesSbkFile.exists()) {
        throw new RuntimeException("Test-setup error: voicesSbkFile not found.");
      }
      if (!orchestraSbkFile.exists()) {
        throw new RuntimeException("Test-setup error: orchestraSbkFile not found.");
      }
      if (!voicesMid.exists()) {
        throw new RuntimeException("Test-setup error: voicesMid not found.");
      }
      Sequence orchestraSeq = MidiSystem.getSequence(orchestraMid);
      Sequence voicesSeq = MidiSystem.getSequence(voicesMid);

      if (orchestraSeq.getDivisionType() != voicesSeq.getDivisionType()) {
        throw new RuntimeException("orchestraSeqand voicesSeq have not the same DivisionType");
      }
      if (orchestraSeq.getResolution() != voicesSeq.getResolution()) {
        throw new RuntimeException("orchestraSeqand voicesSeq have not the same Resolution");
      }

      Track[] trackSet_voices = new Track[]{
        //voicesSeq.getTracks()[1],
        // voicesSeq.getTracks()[2],
        voicesSeq.getTracks()[3], //voicesSeq.getTracks()[4]
      };


      Track[] trackSet_Orchestra = new Track[]{
        orchestraSeq.getTracks()[1],
        orchestraSeq.getTracks()[2],
        orchestraSeq.getTracks()[3],
        orchestraSeq.getTracks()[4],
        orchestraSeq.getTracks()[5],
        orchestraSeq.getTracks()[6],
        orchestraSeq.getTracks()[7],
        orchestraSeq.getTracks()[8],
        orchestraSeq.getTracks()[9],
        orchestraSeq.getTracks()[10],
        orchestraSeq.getTracks()[11],
        orchestraSeq.getTracks()[11],
        orchestraSeq.getTracks()[12],
        orchestraSeq.getTracks()[13],
        orchestraSeq.getTracks()[14],
        orchestraSeq.getTracks()[15],};


      System.out.println("Tracks in voices set.");
      for (Track trck : trackSet_voices) {
        System.out.println("... " + MidiUtil.readTrackname(trck));
      }

      System.out.println("Tracks in Orchestra set.");
      for (Track trck : trackSet_Orchestra) {
        System.out.println("... " + MidiUtil.readTrackname(trck));
      }
      


      //*****************************************************************

      MicroSequencer microSequencer = MicroSequencerManager.getInstance();
      microSequencer.setSequence(orchestraSeq);

      Soundbank voicesSbk = MidiSystem.getSoundbank(voicesSbkFile);
      Soundbank orchestraSbk = MidiSystem.getSoundbank(orchestraSbkFile);

      SequencerPort voicesPort = microSequencer.createDefaultSynthesizerPort("Voices", voicesSbk);
      SequencerPort orchestraPort = microSequencer.createDefaultSynthesizerPort("Orchestra", orchestraSbk);

      voicesPort.setTracks(trackSet_voices);
      orchestraPort.setTracks(trackSet_Orchestra);

      orchestraPort.getAudioPort().setAttenuation(0, 20F);
      orchestraPort.getAudioPort().setAttenuation(1, 20F);

      voicesPort.getAudioPort().setAttenuation(0, 0F);
      voicesPort.getAudioPort().setAttenuation(1, 0F);

      microSequencer.open();
      microSequencer.start();
      Thread.sleep(2000);


      for (int i = 0; i < 120; i++) {
        System.out.println(" getPeakVuAndClear:" + voicesPort.getAudioPort().getPeakVuAndClear(0));
        Thread.sleep(1000);
      }
      microSequencer.stop();
      Thread.sleep(500);

      microSequencer.close();



    } catch (Exception ex) {
      Exceptions.printStackTrace(ex);
    } finally {
      AudioSystemFactory.shutdown();
    }
  }
}
