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
package de.free_creations.guicomponents;

import java.awt.event.ActionEvent;
import javax.swing.JButton;

/**
 *
 * @author Harald Postner
 */
public class StartStopButton extends JButton {

  /**
   * Describes the state of the button.
   */
  public static enum State {

    /**
     * The button is in starting state. It shows the the label with the text
     * "Start". Clicking on a button in this state will initiate some process
     * and change the button 's state to the STOPPING state.
     */
    STARTING,
    /**
     * The button is in stopping state. It shows the the label with the text
     * "Stop". Clicking on a button in this state will stop the process and
     * change the button 's state to the STARTING state.
     */
    STOPPING
  }
  private State state;

  public StartStopButton() {
    super();
    // make sure all state dependant varables are set according to the Starting state
    setStartingState();
  }

  public final void setState(State state) {
    if (this.state != state) {

      switch (state) {
        case STARTING:
          setStartingState();
          break;
        case STOPPING:
          setStoppingState();
          break;
      }
    }
  }

  private void setStartingState() {
    state = State.STARTING;
    setText("Start");
    setIcon(new javax.swing.ImageIcon(getClass().getResource(
            "/de/free_creations/guicomponents/resources/playOrange24.png")));

  }

  private void setStoppingState() {
    state = State.STOPPING;
    setText("Stop");
    setIcon(new javax.swing.ImageIcon(getClass().getResource(
            "/de/free_creations/guicomponents/resources/stopOrange24.png")));
  }

  public State getState() {
    return this.state;
  }

  @Override
  protected void fireActionPerformed(ActionEvent event) {
    super.fireActionPerformed(event);


  }
}
