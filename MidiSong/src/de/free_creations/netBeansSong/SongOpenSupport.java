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
package de.free_creations.netBeansSong;

import java.io.IOException;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.OpenSupport;
import org.openide.windows.CloneableTopComponent;

/**
 * This class supports the {@link SongDataSupport SongDataSupport class}
 * in integrating the {@link Song Song class} into the
 * NetBeans actions framework. A SongOpenSupport object is
 * always attached to a {@link SongDataSupport SongDataSupport object}.
 * The SongOpenSupport provides the actions for opening the song file.
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class SongOpenSupport extends OpenSupport implements
        OpenCookie {

  private final SongDataSupport dataSupport;

  SongOpenSupport(SongDataSupport fileSupport) {
    super(fileSupport.getPrimaryEntry());
    this.dataSupport = fileSupport;
  }

  @Override
  protected CloneableTopComponent createCloneableTopComponent() {
    try {
      return dataSupport.getSessionView();
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }


}
