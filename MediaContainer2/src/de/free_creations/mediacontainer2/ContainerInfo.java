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
package de.free_creations.mediacontainer2;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;

/**
 * A class that helps to process the file "META-INF/container.xml". The
 * forementioned file provides an index of all orchestra files available in the
 * mediacontainer. The W3C XML Schema for this document can be found in the doc
 * folder.
 *
 * @author Harald Postner
 */
public class ContainerInfo {

  private static final Logger logger = Logger.getLogger(ContainerInfo.class.getName());
  public static final String mimeType = "text/repetitormedia+xml";
  private final ContainerInfoData containerInfoData;

  /**
   * Create create a ContainerInfo object that references the given rootfiles.
   *
   * @param rootfileURIs
   */
  public ContainerInfo(List<String> rootfileURIs) {
    ContainerInfoData.Rootfiles rootfiles = new ContainerInfoData.Rootfiles();
    for (String fileUrI : rootfileURIs) {
      ContainerInfoData.Rootfiles.Rootfile fileData = new ContainerInfoData.Rootfiles.Rootfile();
      fileData.setFullPath(fileUrI);
      fileData.setMediaType(mimeType);
      rootfiles.getRootfile().add(fileData);
    }
    containerInfoData = new ContainerInfoData();
    containerInfoData.setRootfiles(rootfiles);
  }

  /**
   * Create create a ContainerInfo object from the given xml stream.
   *
   * @param rootfileURIs
   */
  public ContainerInfo(InputStream is) throws JAXBException {
    javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance(ContainerInfoData.class.getPackage().getName());
    javax.xml.bind.Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
    containerInfoData = (ContainerInfoData) unmarshaller.unmarshal(is);
  }

  /**
   *
   * @return the list of rootfiles that this ContainerInfo object references.
   */
  public List<String> getRootfiles() {
    ArrayList<String> result = new ArrayList<String>();
    List<ContainerInfoData.Rootfiles.Rootfile> fileDescs = containerInfoData.getRootfiles().getRootfile();
    for (ContainerInfoData.Rootfiles.Rootfile fileDesc : fileDescs) {
      if (mimeType.equals(fileDesc.getMediaType())) {
        result.add(fileDesc.getFullPath());
      }
    }
    return result;
  }

  /**
   * Write an xml stream.
   *
   * @param os
   * @throws Exception
   */
  public void writeToStream(OutputStream os) throws Exception {
    try {
      javax.xml.bind.JAXBContext jaxbCtx = javax.xml.bind.JAXBContext.newInstance(containerInfoData.getClass().getPackage().getName());
      javax.xml.bind.Marshaller marshaller = jaxbCtx.createMarshaller();
      marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-8"); //NOI18N
      marshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      marshaller.marshal(containerInfoData, os);
    } catch (javax.xml.bind.JAXBException ex) {
      throw new Exception("writeToStream failed", ex);
    }
  }
}
