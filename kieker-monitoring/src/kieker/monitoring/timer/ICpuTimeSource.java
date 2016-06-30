/***************************************************************************
 * Copyright 2015 Kieker Project (http://kieker-monitoring.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/

package kieker.monitoring.timer;

import java.util.concurrent.TimeUnit;

/**
 * @author Dominic Parga Cacheiro
 * 
 * @since 1.12
 */
public interface ICpuTimeSource extends ITimeSource {
  /**
   * This method is used in the case that the time is dependant of a certain thread.
   *
   * @param id see {@link java.lang.management.ThreadMXBean#getThreadCpuTime(long)}
   * @return The timestamp for the current time converted to the set {@link TimeUnit}
   *
   * @see #getTimeUnit()
   * @since 1.12
   */
  long getTime(long id);

  /**
   * see {@link java.lang.management.ThreadMXBean#isThreadCpuTimeSupported()}
   */
  boolean isThreadCpuTimeSupported();
}
