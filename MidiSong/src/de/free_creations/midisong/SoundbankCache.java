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

import com.sun.media.sound.SF2SoundbankReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Soundbank;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;

/**
 * This class avoids to have the same soundbank loaded several times.
 * @author Harald Postner
 */
class SoundbankCache {

  static private class CacheEntry {

    private final String relativePath;
    private final FileObject baseDir;
    private final Soundbank soundbank;

    public CacheEntry(Soundbank soundbank, FileObject baseDir, String relativePath) {
      this.soundbank = soundbank;
      this.baseDir = baseDir;
      this.relativePath = relativePath;
    }
    public Soundbank getSoundbank(){
      return soundbank;
    }

    public boolean equals(FileObject baseDir, String relativePath) {
      if (this.baseDir != null) {
        if (baseDir != null) {
          try {
            if (!this.baseDir.getURL().sameFile(baseDir.getURL())) {
              return false;
            }
          } catch (FileStateInvalidException ex) {
            return false;
          }
        } else {
          return false;
        }
      } else {
        if (baseDir != null) {
          return false;
        }
      }
      if (this.relativePath != null) {
        if (!this.relativePath.equals(relativePath)) {
          return false;
        }
      } else {
        if (relativePath != null) {
          return false;
        }
      }
      return true;
    }
  }
  static private ArrayList<CacheEntry> cache = new ArrayList<CacheEntry>();

  static public Soundbank getSoundbank(FileObject baseDir, String relativePath) throws EInvalidSongFile {
    CacheEntry entry = getCachedSoundbank(baseDir, relativePath);
    if(entry != null){
      return entry.getSoundbank();
    }
    Soundbank soundbank = getSoundbankFromFile(baseDir, relativePath);
    cacheSoundbank(soundbank, baseDir, relativePath);
    return soundbank;   
    
  }

  static private CacheEntry getCachedSoundbank(FileObject baseDir, String relativePath) {
    for (CacheEntry entry : cache) {
      if (entry.equals(baseDir, relativePath)) {
        return entry;
      }
    }
    return null;
  }

  static private Soundbank getSoundbankFromFile(FileObject baseDir, String relativePath) throws EInvalidSongFile {
    Soundbank soundbank = null;
    FileObject sbFileObject = baseDir.getFileObject(relativePath);
    if (sbFileObject == null) {
      throw new EInvalidSongFile("Could not load file " + relativePath);
    }
    InputStream stream;
    try {
      stream = sbFileObject.getInputStream();
    } catch (FileNotFoundException ex) {
      throw new EInvalidSongFile(ex);
    }

    // extract the soundbank
    try {
      SF2SoundbankReader reader = new SF2SoundbankReader();
      soundbank = reader.getSoundbank(stream); //                     
      stream.close();
    } catch (InvalidMidiDataException ex) {
      throw new EInvalidSongFile(ex);
    } catch (IOException ex) {
      throw new EInvalidSongFile(ex);
    }
    return soundbank;

  }

  static private void cacheSoundbank(Soundbank soundbank, FileObject baseDir, String relativePath) {
    CacheEntry cacheEntry = new CacheEntry(soundbank, baseDir, relativePath);
    cache.add(cacheEntry);
  }
}
