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

import kieker.common.configuration.Configuration;

import java.lang.management.ThreadMXBean;
import java.util.concurrent.TimeUnit;

/**
 * This class uses {@link ThreadMXBean} to measure the cpu time of threads, rounded to a specified time unit.<br>
 * NOTE: cpu time = user time + system time
 *
 * @author Dominic Parga Cacheiro
 *
 * @since 1.12
 */
public class CpuTimer extends AbstractCpuTimeSource {

  /**
   * see {@link AbstractCpuTimeSource#AbstractCpuTimeSource(Configuration, Class)}
   */
  public CpuTimer(Configuration configuration) {
    super(configuration, CpuTimer.class);
  }

  /*
  |====================|
  | (i) ICpuTimeSource |
  |====================|
  */
  /**
   * This method does the same as {@link #getTime(long)} using {@link Thread#getId()} of {@link Thread#currentThread()}
   * <br>
   * (but it is NOT calling this but {@link ThreadMXBean#getCurrentThreadCpuTime()}).
   */
  @Override
  public long getTime() {
    if (!isThreadCpuTimeSupported)
      throw new CpuTimeIsNotSupportedException();

    return timeunit.convert(
            bean.getCurrentThreadCpuTime() - offset,
            TimeUnit.NANOSECONDS);
  }

  /**
   * <p>
   * This method is used for measuring cpu time of the currently running thread using
   * {@link ThreadMXBean#getThreadCpuTime(long)}.
   * <p>
   * Regarding accuracy: {@link ThreadMXBean#getThreadCpuTime(long)} says: "The returned value is of
   * nanoseconds precision but not necessarily nanoseconds accuracy").
   * <p>
   * NOTE: cpu time = user time + system time
   *
   * @param id thread id for system time
   * @return cpu time (in nanoseconds) of the thread with given ID, rounded to the specified time unit
   * @throws CpuTimeIsNotSupportedException if {@link #isThreadCpuTimeSupported()} returns false
   */
  @Override
  public long getTime(long id) {
    if (!isThreadCpuTimeSupported)
      throw new CpuTimeIsNotSupportedException();

    return timeunit.convert(
            bean.getThreadCpuTime(id) - offset,
            TimeUnit.NANOSECONDS);
  }
}
