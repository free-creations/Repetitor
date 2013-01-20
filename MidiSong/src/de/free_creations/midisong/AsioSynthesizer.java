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
package de.free_creations.midisong;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * This class represents an ASIO plug-in attached to a 
 * {@link MidiSynthesizerTrack  }.
 * @TODO this class must be implemented...
 * @author Harald Postner <Harald at H-Postner.de>
 */
@XmlRootElement
@XmlType
@XmlAccessorType(value = XmlAccessType.NONE)
public class AsioSynthesizer extends SynthesizerData {


  /**
   * A placeholder for the parameters that shall be defined when implemeting
   * this class.
   */
  @XmlElement( required = false)
  private String asioParamters;

  @Override
  public void detach() {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public float getVuLevel(int audioChannel) {
    throw new UnsupportedOperationException("Not supported yet.");
  }


 
}
