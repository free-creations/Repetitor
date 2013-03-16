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

import de.free_creations.audioconfig.AudioSystemInfo.ArchitectureInfo;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import rtaudio4java.AudioSystem;
import rtaudio4java.AudioSystem.StreamOptions;
import rtaudio4java.AudioSystem.StreamParameters;
import rtaudio4java.AudioSystemFactory;
import rtaudio4java.DeviceInfo;
//import org.netbeans.api.options.OptionsDisplayer;

/**
 * This class is responsible to persistently store and retrieve the Audio
 * settings. <br/> Audio settings are stored using the Java preference system.
 * See {@link java.util.prefs } for more information.
 *
 * Note: in windows the the Java preference system uses the registry to
 * permanently store information. In linux it uses a flat file named something
 * like "home/.java/.userPrefs/de/_!'}!cg"l!'`!|w"j!()!~
 *
 * @"h!(
 * @!a@"v!'4!cw==/audioconfig/prefs.xml"
 *
 *
 * @author Harald Postner <Harald at H-Postner.de>
 */
public class StoredConfig {

  public class ConfigRecord {

    private final int architectureNumber;
    private final StreamParameters outputParameters;
    private final StreamParameters inputParameters;
    private final int sampleRate;
    private final int bufferSize;
    private final StreamOptions options;

    protected ConfigRecord(int architectureNumber, StreamParameters outputParameters, StreamParameters inputParameters, int sampleRate, int bufferSize, StreamOptions options) {
      this.architectureNumber = architectureNumber;
      this.outputParameters = outputParameters;
      this.inputParameters = inputParameters;
      this.sampleRate = sampleRate;
      this.bufferSize = bufferSize;
      this.options = options;
    }

    public int getArchitectureNumber() {
      return architectureNumber;
    }

    public StreamParameters getOutputParameters() {
      return outputParameters;
    }

    public StreamParameters getInputParameters() {
      return inputParameters;
    }

    public int getSampleRate() {
      return sampleRate;
    }

    public int getBufferSize() {
      return bufferSize;
    }

    public AudioSystem.StreamOptions getOptions() {
      return options;
    }
  }
  private static final int undefined = Integer.MIN_VALUE;
  private static final int impossible = Integer.MIN_VALUE;
  private static final int none = -1;
  private static final String undefinedStr = "undefined";
  private int locNumberOfOutputChannels = undefined;
  private int locFirstOutputChannel = undefined;
  private String locInputDeviceDescription = undefinedStr;
  private int locArchitectureNumber = undefined;
  private String locOutputDeviceDescription = undefinedStr;
  private int locFirstInputChannel = undefined;
  private int locNumberOfInputChannels = undefined;
  private int locSampleRate = undefined;
  private int locBufferSize = undefined;
  private int locBufferCount = undefined;

  /**
   * Listener that waits for the user to press the OK button on the audio-
   * configuration dialog.
   */
  public interface ConfigDialogEndListener {

    public void dialogClosed();
  }
  //static private Preferences prefs = NbPreferences.forModule(Audioconfig.class);
  private static Preferences prefs = Preferences.userNodeForPackage(StoredConfig.class);
  //
  private static final String architectureNumberFlag = "architectureNumber";
  //
  private static final String outDeviceDescriptionFlag = "outDeviceDescription";
  private static final String outFirstChannelFlag = "outFirstChannel";
  private static final String outNumberOfChannelsFlag = "outNumberOfChannels";
  //
  private static final String inDeviceDescriptionFlag = "inDeviceDescription";
  private static final String inFirstChannelFlag = "inFirstChannel";
  private static final String inNumberOfChannelsFlag = "inNumberOfChannels";
  //
  private static final String sampleRateFlag = "sampleRate";
  private static final String bufferSizeFlag = "bufferSize";
  private static final String bufferCountFlag = "bufferCount";
  //
  public final static String noInput = "input disabled";

  /**
   * Matches the stored configuration with the locally available Hard- and
   * Software.
   *
   * @param availableSystem an AudioSystemInfo object describing the locally
   * available Hard and Software.
   * @return a record who's elements can be used to open an rtaudio4java audio
   * stream.
   */
  public synchronized ConfigRecord match(AudioSystemInfo availableSystem) {

    ArchitectureInfo availableDevices = matchArchitecture(availableSystem);
    if (availableDevices == null) {
      return null;
    }
    DeviceInfo outputDevice = matchOutputDevice(availableDevices);
    DeviceInfo inputDevice = matchInputDevice(availableDevices);
    int sampleRate = matchSampleRate(outputDevice);
    if (sampleRate == impossible) {
      outputDevice = null;
    }
    if (sampleRate != matchSampleRate(inputDevice)) {
      inputDevice = null;
    }
    // check channels on output device
    if (outputDevice != null) {
      if (getFirstOutputChannel() >= outputDevice.getOutputChannels()) {
        outputDevice = null;
      } else {
        if ((getFirstOutputChannel() + getNumberOfOutputChannels()) > outputDevice.getOutputChannels()) {
          outputDevice = null;
        }
      }
    }
    // check channels on input device
    if (inputDevice != null) {
      if (getFirstInputChannel() >= inputDevice.getInputChannels()) {
        inputDevice = null;
      } else {
        if ((getFirstInputChannel() + getNumberOfInputChannels()) > inputDevice.getInputChannels()) {
          inputDevice = null;
        }
      }
    }

    StreamParameters outputParameters = null;
    if (outputDevice != null) {
      outputParameters = new StreamParameters(
              deviceName2Number(availableDevices, getOutputDeviceDescription()),
              getFirstOutputChannel(),
              getNumberOfOutputChannels());
    }

    StreamParameters inputParameters = null;
    if (inputDevice != null) {
      inputParameters = new StreamParameters(
              deviceName2Number(availableDevices, getInputDeviceDescription()),
              getFirstInputChannel(),
              getNumberOfInputChannels());
    }

    AudioSystem.StreamOptions options = new AudioSystem.StreamOptions(
            false, //noninterleaved
            false, // minimizeLatency
            false,// hogDevice
            true,// scheduleRealtime
            getBufferCount(),//numberOfBuffers
            0,// priority
            "Repetitor"//String streamName
            );

    ConfigRecord configRecord = new ConfigRecord(
            getArchitectureNumber(),
            outputParameters,
            inputParameters,
            getSampleRate(),
            getBufferSize(), 
            options);
    
    return configRecord;

  }

  private int deviceName2Number(ArchitectureInfo availableDevices, String deviceName) {
    if (availableDevices == null) {
      return none;
    }
    if (deviceName == null) {
      return none;
    }
    DeviceInfo[] deviceInfos = availableDevices.getDeviceInfos();
    for (int i = 0; i < deviceInfos.length; i++) {
      if (deviceName.equals(deviceInfos[i].getName())) {
        return i;
      }
    }
    return none;
  }

  /**
   * Verify whether the stored sound architecture is currently available.
   * @param availableSystem a record describing all sound architectures.
   * @return an ArchitectureInfo describing the stored sound architecture or
   * null if the stored sound architecture is currently not available.
   */
  private ArchitectureInfo matchArchitecture(AudioSystemInfo availableSystem) {
    int storedArch = getArchitectureNumber();
    for (ArchitectureInfo arch : availableSystem) {
      if (storedArch == arch.getApiNumber()) {
        return arch;
      }
    }
    return null;
  }
  /**
   * Verify whether the stored device description corresponds to
   * an available device.
   * @param availableDevices a list of all currently available devices
   * @return the device-info record for the stored device or null
   * if the stored device is currently not available.
   */
  private DeviceInfo matchOutputDevice(ArchitectureInfo availableDevices) {
    String storedOutputDevice = getOutputDeviceDescription();
    if (storedOutputDevice == null) {
      return null;
    }
    for (DeviceInfo device : availableDevices.getDeviceInfos()) {
      if (storedOutputDevice.equals(device.getName())) {
        if (device.isProbed()) {
          return device;
        }
      }
    }
    return null;
  }

  /**
   * Verify whether the stored device description corresponds to
   * an available device.
   * @param availableDevices a list of all currently available devices
   * @return the device-info record for the stored device or null
   * if the stored device is currently not available.
   */
  private DeviceInfo matchInputDevice(ArchitectureInfo availableDevices) {
    String storedInputDevice = getInputDeviceDescription();
    if (storedInputDevice == null) {
      return null;
    }
    for (DeviceInfo device : availableDevices.getDeviceInfos()) {
      if (storedInputDevice.equals(device.getName())) {
        if (device.isProbed()) {
          return device;
        }
      }
    }
    return null;
  }

  /**
   * Check whether the stored sample rate is supported by the given device.
   * @param device the device to by checked.
   * @return the stored sample rate if it is supported or "impossible" if
   * the stored sample rate is not supported.
   */
  private int matchSampleRate(DeviceInfo device) {
    if (device == null) {
      return impossible;
    }
    int storedSampleRate = getSampleRate();
    if (device.getSampleRates().contains(storedSampleRate)) {
      return storedSampleRate;
    }
    return impossible;
  }

  /**
   * Makes the settings permanent.
   */
  public synchronized void flush() throws BackingStoreException {
    prefs.putInt(architectureNumberFlag, getArchitectureNumber());

    prefs.put(outDeviceDescriptionFlag, getOutputDeviceDescription());
    prefs.putInt(outFirstChannelFlag, getFirstOutputChannel());
    prefs.putInt(outNumberOfChannelsFlag, getNumberOfOutputChannels());
    //
    prefs.put(inDeviceDescriptionFlag, getInputDeviceDescription());
    prefs.putInt(inFirstChannelFlag, getFirstInputChannel());
    prefs.putInt(inNumberOfChannelsFlag, getNumberOfInputChannels());
    //
    prefs.putInt(sampleRateFlag, getSampleRate());
    prefs.putInt(bufferSizeFlag, getBufferSize());
    prefs.putInt(bufferCountFlag, getBufferCount());
    prefs.flush();
  }

  /**
   * Retrieve the availableDevices type number (also called API Type) from the
   * stored Audio settings.
   *
   * @return the system type number.
   */
  public synchronized int getArchitectureNumber() {
    if (locArchitectureNumber == undefined) {
      locArchitectureNumber = prefs.getInt(architectureNumberFlag, AudioSystemFactory.APITYPE_WINDOWS_DS);
    }
    return locArchitectureNumber;
  }

  /**
   * Store the system availableDevices (also called API Type) in the Audio
   * settings.
   *
   * @param value the system type number.
   */
  public synchronized void putArchitectureNumber(int value) {
    locArchitectureNumber = value;
  }

  /**
   * Retrieve a description of the output device from the stored Audio settings.
   *
   * @return the description of the output device
   */
  public synchronized String getOutputDeviceDescription() {
    if (undefinedStr.equals(locOutputDeviceDescription)) {
      locOutputDeviceDescription = prefs.get(outDeviceDescriptionFlag, "Default");
    }
    return locOutputDeviceDescription;
  }

  /**
   * Store a description of the output device in the Audio settings.
   *
   * @param value a description of the output device
   */
  public synchronized void putOutputDeviceDescription(String value) {
    locOutputDeviceDescription = value;
  }

  /**
   * Retrieve a description of the input device from the stored Audio settings.
   *
   * @return the description of the output device
   */
  public synchronized String getInputDeviceDescription() {
    if (undefinedStr.equals(locInputDeviceDescription)) {
      locInputDeviceDescription = prefs.get(inDeviceDescriptionFlag, "noInput");
    }
    return locInputDeviceDescription;
  }

  /**
   * Store a description of the input device in the Audio settings.
   *
   * @param value a description of the output device
   */
  public synchronized void putInputDeviceDescription(String value) {
    locInputDeviceDescription = value;
  }

  /**
   * Retrieve the identification of the first output channel from the Audio
   * settings.
   *
   * @return the identification of the first channel
   */
  public synchronized int getFirstOutputChannel() {
    if (locFirstOutputChannel == undefined) {
      locFirstOutputChannel = prefs.getInt(outFirstChannelFlag, 0);
    }
    return locFirstOutputChannel;
  }

  /**
   * Store the identification of the first output channel in the Audio settings.
   *
   * @param value the identification of the first channel
   */
  public synchronized void putFirstOutputChannel(int value) {
    locFirstOutputChannel = value;
  }

  /**
   * Retrieve the identification of the first input channel from the Audio
   * settings.
   *
   * @return the identification of the first channel
   */
  public synchronized int getFirstInputChannel() {
    if (locFirstInputChannel == undefined) {
      locFirstInputChannel = prefs.getInt(inFirstChannelFlag, 0);
    }
    return locFirstInputChannel;
  }

  /**
   * Store the identification of the first input channel in the Audio settings.
   *
   * @param value the identification of the first channel
   */
  public synchronized void putFirstInputChannel(int value) {
    locFirstInputChannel = value;
  }

  /**
   * Retrieve the number of output channels from the Audio settings.
   *
   * @return the number channels
   */
  public synchronized int getNumberOfOutputChannels() {
    if (locNumberOfOutputChannels == undefined) {
      locNumberOfOutputChannels = prefs.getInt(outNumberOfChannelsFlag, 2);
    }
    return locNumberOfOutputChannels;

  }

  /**
   * Store the number of output channels in the Audio settings.
   *
   * @param value the number channels
   */
  public synchronized void putNumberOfOutputChannels(int value) {
    locNumberOfOutputChannels = value;
  }

  /**
   * Retrieve the number of input channels from the Audio settings.
   *
   * @return the number channels
   */
  public synchronized int getNumberOfInputChannels() {
    if (locNumberOfInputChannels == undefined) {
      locNumberOfInputChannels = prefs.getInt(inNumberOfChannelsFlag, 2);
    }
    return locNumberOfInputChannels;
  }

  /**
   * Store the number of input channels in the Audio settings.
   *
   * @param value the number channels
   */
  public synchronized void putNumberOfInputChannels(int value) {
    locNumberOfInputChannels = value;
  }

  /**
   * Retrieve the sample rate from the Audio settings.
   *
   * @return the sample rate
   */
  public synchronized int getSampleRate() {
    if (locSampleRate == undefined) {
      locSampleRate = prefs.getInt(sampleRateFlag, 44100);
    }
    return locSampleRate;
  }

  /**
   * Store the sample rate in the Audio settings.
   *
   * @param value the sample rate
   */
  public synchronized void putSampleRate(int value) {
    locSampleRate = value;
  }

  /**
   * Retrieve the buffer size from the Audio settings.
   *
   * @return the buffer size
   */
  public synchronized int getBufferSize() {
    if (locBufferSize == undefined) {
      locBufferSize = prefs.getInt(bufferSizeFlag, 1024);
    }
    return locBufferSize;
  }

  /**
   * Store the buffer size in the Audio settings.
   *
   * @return the buffer size
   */
  public synchronized void putBufferSize(int value) {
    locBufferSize = value;
  }

  /**
   * Retrieve the buffer count from the Audio settings.
   *
   * @return the buffer count
   */
  public synchronized int getBufferCount() {
    if (locBufferCount == undefined) {
      locBufferCount = prefs.getInt(bufferCountFlag, 8);
    }
    return locBufferCount;
  }

  /**
   * Store the buffer count in the Audio settings.
   *
   * @param value the buffer count
   */
  public synchronized void putBufferCount(int value) {
    locBufferCount = value;
  }
}
