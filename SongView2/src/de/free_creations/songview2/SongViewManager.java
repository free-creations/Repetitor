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
package de.free_creations.songview2;

import de.free_creations.netBeansSong.SongDataSupport;
import de.free_creations.netBeansSong.SongSessionViewProvider;
import org.openide.windows.CloneableTopComponent;

/**
 * The factory class for the production of "songView" windows.
 * A "songView" window visually represents the content of a
 * song file. This class represents the interface to
 * the {@link SongDataSupport } class.
 * <p>Please note: this class must be registered as a service-provider.
 * To do this create a directory "META-INF.services" in the root
 * of the sources. Put there a file called
 * "de.free_creations.netBeansSong.SongSessionViewProvider" and in this
 * file put one line with the Java path of this class.
 * </p>
 * @author Harald Postner <Harald at H-Postner.de>
 */

@org.openide.util.lookup.ServiceProvider(
        service=SongSessionViewProvider.class,
        position=100)
public class SongViewManager implements SongSessionViewProvider {

  @Override
  public CloneableTopComponent getView(SongDataSupport dataSupport) {
    return new SongView2TopComponent(dataSupport);
  }
}
