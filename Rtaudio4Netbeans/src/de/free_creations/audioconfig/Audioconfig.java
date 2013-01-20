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
package de.free_creations.audioconfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;
import org.netbeans.api.options.OptionsDisplayer;
import rtaudio4java.AudioSystem;
import rtaudio4java.AudioSystemFactory;
import rtaudio4java.DeviceInfo;

/**
 * This class is responsible to persistently store and retrieve the
 * Audio settings.
 * <br/>
 * Audio settings are stored using the Java preference system.
 * See {@link java.util.prefs } for more information.
 * 
 * Note: in windows the the Java preference system uses the registry
 * to permanently store information. In linux
 * it uses a flat file named something like
 * "home/.java/.userPrefs/de/_!'}!cg"l!'`!|w"j!()!~@"h!(@!a@"v!'4!cw==/audioconfig/prefs.xml"
 * 
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class Audioconfig {

  /**
   * Listener that waits for the user to press the OK button
   * on the audio- configuration dialog.
   */
  public interface ConfigDialogEndListener {

    public void dialogClosed();
  }
  //static private Preferences prefs = NbPreferences.forModule(Audioconfig.class);
  private static Preferences prefs = Preferences.userNodeForPackage(Audioconfig.class);
  //
  private static final String systemDescriptionFlag = "systemDescription";
  private static final String systemNumberFlag = "systemNumber";
  private static final String outDeviceDescriptionFlag = "outDeviceDescription";
  private static final String outDeviceNumberFlag = "outDeviceNumber";
  private static final String firstChannelFlag = "firstChannel";
  private static final String numberOfChannelsFlag = "numberOfChannels";
  private static final String sampleRateFlag = "sampleRate";
  private static final String bufferSizeFlag = "bufferSize";
  private static String message = "";

  /**
   * Retrieve the system type number (also called API Type) from the stored
   * Audio settings.
   * @return the system type number.
   */
  public static int getSystemNumber() {
    return prefs.getInt(systemNumberFlag, AudioSystemFactory.APITYPE_WINDOWS_DS);
  }

  /**
   * Store the system type (also called API Type) in the Audio settings.
   * @param value the system type number.
   */
  public static void putSystemNumber(int value) {
    if (value != -1) {
      prefs.putInt(systemNumberFlag, value);
    }
  }

  /**
   * Retrieve the description of the system type (also called API Type) from the stored
   * Audio settings.
   * @return the description of the system type
   */
  public static String getSystemDescription() {
    return prefs.get(systemDescriptionFlag, "UNSPECIFIED");
  }

  /**
   * Store the description of the system type (also called API Type) in the Audio settings.
   * @param value the description of the system type
   */
  public static void putSystemDescription(String value) {
    if (value != null) {
      prefs.put(systemDescriptionFlag, value);
    }
  }

  /**
   * Retrieve the identification number of the output device from the stored
   * Audio settings.
   * @return the identification number of the output device
   */
  public static int getOutputDeviceNumber() {
    return prefs.getInt(outDeviceNumberFlag, 0);
  }

  /**
   * Store the identification number of the output device in the
   * Audio settings.
   * @param value the identification number of the output device
   */
  public static void putOutputDeviceNumber(int value) {
    if (value != -1) {
      prefs.putInt(outDeviceNumberFlag, value);
    }
  }

  /**
   * Retrieve a description of the output device from the stored
   * Audio settings.
   * @return the description of the output device 
   */
  public static String getOutputDeviceDescription() {
    return prefs.get(outDeviceDescriptionFlag, "Default");
  }

  /**
   * Store a description of the output device in the
   * Audio settings.
   * @param value a description of the output device
   */
  public static void putOutputDeviceDescription(String value) {
    if (value != null) {
      prefs.put(outDeviceDescriptionFlag, value);
    }
  }

  /**
   * Retrieve the identification of the first channel from the
   * Audio settings.
   * @return the identification of the first channel
   */
  public static int getFirstChannel() {
    return prefs.getInt(firstChannelFlag, 0);
  }

  /**
   * Store the identification of the first channel in the
   * Audio settings.
   * @return the identification of the first channel
   */
  public static void putFirstChannel(int value) {
    if (value != -1) {
      prefs.putInt(firstChannelFlag, value);
    }
  }

  /**
   * Retrieve the number channels from the
   * Audio settings.
   * @return the number channels
   */
  public static int getNumberOfChannels() {
    return prefs.getInt(numberOfChannelsFlag, 2);
  }

  /**
   * Store the number channels in the
   * Audio settings.
   * @return the number channels
   */
  public static void putNumberOfChannels(int value) {
    if (value != -1) {
      prefs.putInt(numberOfChannelsFlag, value);
    }
  }

  /**
   * Retrieve the sample rate from the
   * Audio settings.
   * @return the sample rate
   */
  public static int getSampleRate() {
    return prefs.getInt(sampleRateFlag, 44100);
  }

  /**
   * Store the sample rate in the
   * Audio settings.
   * @return the sample rate
   */
  public static void putSampleRate(int value) {
    if (value != -1) {
      prefs.putInt(sampleRateFlag, value);
    }
  }

  /**
   * Retrieve the buffer size from the
   * Audio settings.
   * @return the buffer size
   */
  public static int getBufferSize() {
    return prefs.getInt(bufferSizeFlag, 1024);
  }

  /**
   * Store the buffer size in the
   * Audio settings.
   * @return the buffer size
   */
  public static void putBufferSize(int value) {
    if (value != -1) {
      prefs.putInt(bufferSizeFlag, value);
    }
  }

  /**
   * Export the settings to an XML stream.
   * @param os an output stream
   * @throws IOException
   * @throws BackingStoreException
   */
  public static void exportToXML(OutputStream os)
          throws IOException,
          BackingStoreException {
    prefs.exportSubtree(os);
  }

  /**
   * Import the settings from an XML stream.
   * @param is
   * @throws IOException
   * @throws InvalidPreferencesFormatException
   */
  public static void importFromXML(InputStream is)
          throws IOException,
          InvalidPreferencesFormatException {
    Preferences.importPreferences(is);
  }

  /**
   * Verify that the stored Audio settings are working on the current
   * audio hardware.
   * @return true if the stored audio settings can be used.
   */
  public static boolean isProbed() {
    AudioSystem rtAudioInstance = null;
    try {
      rtAudioInstance = AudioSystemFactory.getRtAudioInstance(getSystemNumber());
    } catch (Throwable th) {
      message = th.getMessage();
      return false;
    }

    if (rtAudioInstance.getCurrentApi() != getSystemNumber()) {
      message = "Invalid System";
      return false;
    }
    DeviceInfo deviceInfo = null;
    try {
      int deviceNumber = getOutputDeviceNumber();
      deviceInfo = rtAudioInstance.getDeviceInfo(deviceNumber).get();
    } catch (Exception ex) {
      message = ex.getMessage();
      return false;
    }

    if (!deviceInfo.isProbed()) {
      message = "Output device not recognised.";
      return false;
    }

    String deviceName = deviceInfo.getName();
    if (!getOutputDeviceDescription().equals(deviceName)) {
      message = "Output device not recognised.";
      return false;
    }

    int deviceChannels = deviceInfo.getOutputChannels();
    if (getFirstChannel() < 0) {
      message = "Invalid first channel.";
      return false;
    }
    if (getFirstChannel() >= deviceChannels) {
      message = "Invalid first channel.";
      return false;
    }

    if (getNumberOfChannels() < 0) {
      message = "Invalid number of channels.";
      return false;
    }

    int lastChannel = getFirstChannel() + getNumberOfChannels() - 1;
    if (lastChannel >= deviceChannels) {
      message = "Invalid number of channels.";
      return false;
    }

    Set<Integer> sampleRates = deviceInfo.getSampleRates();

    if (!sampleRates.contains(getSampleRate())) {
      message = "Invalid sample rate.";
      return false;
    }

    return true;
  }

  /**
   * Returns the message from the last probe attempt.
   * @return the error message from the last probe attempt.
   * When no message is available an empty string is returned.
   */
  public static String getProbeMessage() {
    return message;
  }

  /**
   * Show the Configuration dialog to the user. This function returns
   * immediately and does not
   * block until the user has closed the dialog. To catch the closing
   * of the dialog, the programmer must install a ConfigDialogEndListener.
   * This ConfigDialogEndListener will be invoked if the user
   * ends the dialog with OK. If the dialog is cancelled or even Netbeans
   * is shutdown, there will be no callback.
   * @param listener listener for the end of dialog.
   * @param message a message displayed on the configuration dialog.
   */
  public static void showConfigDialog(ConfigDialogEndListener listener, String message) {

    AudioSettingsOptionsPanelController.setConfigDialogEndListener(listener);
    AudioSettingsOptionsPanelController.setMessage(message);
    OptionsDisplayer displayer = OptionsDisplayer.getDefault();
    displayer.open("AudioSettings");

  }
}
