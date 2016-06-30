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
 * @author Jan Waller, Dominic Parga Cacheiro
 * 
 * @since 1.3
 */
public interface ITimeSource {

  /**
   * @return The time difference between the current time and the previously set offset
   * converted to the set {@link TimeUnit}.
   *
   * @see #getTimeUnit()
   * @since 1.3
   */
	long getTime();

	/**
	 * @return The offset of the timesource to midnight, 1970-1-1 UTC in {@link TimeUnit}.
	 * 
	 * @see #getTimeUnit()
	 * @since 1.7
	 */
	long getOffset();

	/**
	 * @return The {@link TimeUnit} of the timesource.
	 * 
	 * @since 1.7
	 */
	TimeUnit getTimeUnit();

	/**
	 * @return A String representation of the timesource. E.g., the meaning of a timestamp from this source.
	 * 
	 * @since 1.5
	 */
	@Override
	String toString();
}
