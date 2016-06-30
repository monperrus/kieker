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

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import kieker.common.configuration.Configuration;
import kieker.common.logging.Log;
import kieker.common.logging.LogFactory;

/**
 * A timer implementation, counting in nanoseconds since a specified offset.
 * This timer uses {@link System#nanoTime()} to measure the current time stamp.
 *
 * @author Jan Waller, Dominic Parga Cacheiro
 *
 * @since 1.5
 */
public final class SystemNanoTimer extends AbstractTimeSource {
  private final long clockdifference;

  /**
   * <p>
   * Default constructor; Sets the time unit and offset as defined in the given configuration. Default offset is the
   * current time.
   * <p>
   * This timer uses {@link System#nanoTime()} to measure the current time stamp.
   * <p>
   * Accepted values for time unit (saved in {@link Configuration}):<br>
   * &bull 0 - nanoseconds<br>
   * &bull 1 - microseconds<br>
   * &bull 2 - milliseconds<br>
   * &bull 3 - seconds
   *
   * @param configuration This configuration sets:<br>
   * &bull The time unit of the returned times<br>
   * &bull The given offset to midnight, January 1, 1970 UTC interpreted in milliseconds.<br>NOTE: Since
   * {@link System#nanoTime()} has an undefined offset, a clock difference is calculated with millisecond accuracy
   * using {@link System#currentTimeMillis()}.
   */
  public SystemNanoTimer(Configuration configuration) {
    super(configuration, SystemNanoTimer.class);

    clockdifference = System.nanoTime() - (TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis()));

    // setting offset
    String CONFIG_OFFSET = CONFIG_KEY_OFFSET(SystemNanoTimer.class);
    if (configuration.getStringProperty(CONFIG_OFFSET).length() == 0)
      offset = System.nanoTime();
    else
      offset = clockdifference + TimeUnit.MILLISECONDS.toNanos(configuration.getLongProperty(CONFIG_OFFSET));
  }

  /*
  |=================|
  | (i) ITimeSource |
  |=================|
  */
	@Override
	public final long getTime() {
		return this.timeunit.convert(System.nanoTime() - this.offset, TimeUnit.NANOSECONDS);
	}

	@Override
  public long getOffset() {
    return this.timeunit.convert(this.offset, TimeUnit.NANOSECONDS);
  }

	@Override
	public final String toString() {
		final StringBuilder sb = new StringBuilder(64);
		sb.append("Time in " + this.timeunit.toString().toLowerCase(Locale.ENGLISH) + " (with nanoseconds precision) since ");
		sb.append(new Date(TimeUnit.NANOSECONDS.toMillis(this.offset - this.clockdifference)));
		return sb.toString();
	}

}
