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
package de.free_creations.midisong;

import java.io.File;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Harald Postner
 */
public class BugDemo {

  public JAXBContext jAXBContext;

  @XmlRootElement(namespace = "repetitormedia")
  @XmlAccessorType(XmlAccessType.NONE)
  @XmlType
  static public final class XSong extends MidiTrack {
  };

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws JAXBException {
    new BugDemo().doit2();
  }

  private void doit() throws JAXBException {
    System.out.println("### NullPointerBug started");
    XSong aSong = new XSong();

    File output = new File("TEST.XML");

    Marshaller marshaller = JAXBContext.newInstance(GenericTrack.class).createMarshaller();
    marshaller.marshal(aSong, output);

    System.out.println("### Ouput written to " + output.getAbsolutePath());

  }

  private void doit2() throws JAXBException {
    jAXBContext = JAXBContext.newInstance(BuiltinSynthesizer.class,
            SynthesizerData.class,
            MidiSynthesizerTrack.class,
            MasterTrack.class,
            MidiTrack.class,
            GenericTrack.class,
            Song.class);

  }
}
