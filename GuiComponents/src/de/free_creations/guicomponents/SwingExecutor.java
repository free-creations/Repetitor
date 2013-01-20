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

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.TimeUnit;
import javax.swing.SwingUtilities;

/**
 * An Executor that guarantees to execute the given command in the event
 * dispatcher thread.
 *
 * @author Harald Postner
 */
public class SwingExecutor extends AbstractExecutorService {

  private static final SwingExecutor instance = new SwingExecutor();

  public static SwingExecutor instance() {
    return instance;
  }

  /**
   * Always executes the given command on the AWT event dispatching thread.
   * @param command 
   */
  @Override
  public void execute(Runnable command) {
    if (SwingUtilities.isEventDispatchThread()) {
      command.run();
    } else {
      SwingUtilities.invokeLater(command);
    }
  }

  @Override
  public void shutdown() {
    throw new UnsupportedOperationException("The AWT event dispatching thread cannont be shut down.");
  }

  @Override
  public List<Runnable> shutdownNow() {
    throw new UnsupportedOperationException("The AWT event dispatching thread cannont be shut down.");
  }

  @Override
  public boolean isShutdown() {
    throw new UnsupportedOperationException("The AWT event dispatching thread cannont be shut down.");
  }

  @Override
  public boolean isTerminated() {
    throw new UnsupportedOperationException("The AWT event dispatching thread cannont be shut down.");
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    throw new UnsupportedOperationException("The AWT event dispatching thread cannont be shut down.");
  }
}
