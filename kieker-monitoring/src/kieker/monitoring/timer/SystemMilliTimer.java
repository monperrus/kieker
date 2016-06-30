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
 * A timer implementation, counting in milliseconds since a specified offset.
 * This timer uses {@link System#currentTimeMillis()} to measure the current time stamp.
 * 
 * @author Jan Waller, Dominic Parga Cacheiro
 * 
 * @since 1.5
 */
public final class SystemMilliTimer extends AbstractTimeSource {
  /**
   * <p>
   * Default constructor; Sets the time unit and offset as defined in the given configuration. Default offset is the
   * current time.
   * <p>
   * This timer uses {@link System#currentTimeMillis()} to measure the current time stamp.
   * <p>
   * Accepted values for time unit (saved in {@link Configuration}):<br>
   * &bull 0 - nanoseconds<br>
   * &bull 1 - microseconds<br>
   * &bull 2 - milliseconds<br>
   * &bull 3 - seconds
   *
   * @param configuration This configuration sets:<br>
   * &bull The time unit of the returned times<br>
   * &bull The given offset to midnight, January 1, 1970 UTC interpreted in milliseconds.
   */
  public SystemMilliTimer(Configuration configuration) {
    super(configuration, SystemMilliTimer.class);

    // setting offset
    String CONFIG_OFFSET = CONFIG_KEY_OFFSET(SystemMilliTimer.class);
    if (configuration.getStringProperty(CONFIG_OFFSET).length() == 0)
      offset = System.currentTimeMillis();
    else
      offset = configuration.getLongProperty(CONFIG_OFFSET);
  }

  /*
  |=================|
  | (i) ITimeSource |
  |=================|
  */
	@Override
	public final long getTime() {
		return this.timeunit.convert(System.currentTimeMillis() - this.offset, TimeUnit.MILLISECONDS);
	}

	@Override
	public long getOffset() {
		return this.timeunit.convert(this.offset, TimeUnit.MILLISECONDS);
	}

	@Override
	public final String toString() {
		final StringBuilder sb = new StringBuilder(64);
		sb.append("Time in " + this.timeunit.toString().toLowerCase(Locale.ENGLISH) + " (with milliseconds precision) since ");
		sb.append(new Date(this.offset));
		return sb.toString();
	}
}
