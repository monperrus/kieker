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
import kieker.common.logging.Log;
import kieker.common.logging.LogFactory;

import java.util.concurrent.TimeUnit;

/**
 * This class serves methods for creating different timers for measuring time.
 *
 * @author Jan Waller, Dominic Parga Cacheiro
 * 
 * @since 1.3
 */
public abstract class AbstractTimeSource implements ITimeSource {

  // time unit - not used for now, supposed to be used in switch-case in the constructor (in future)
  public final byte NANOS = 0;
  public final byte MICROS = NANOS + 1;
  public final byte MILLIS = MICROS + 1;
  public final byte SECONDS = MILLIS + 1;

  // measuring
  protected final TimeUnit timeunit;
  protected long offset;

  /**
   * <p>
   * Default constructor. The default offset is set to 0.
   * </p>
   * @param configuration This config file can be used to store offset and time unit.
   * @param clazz This parameter is used for reading the configuration settings
   * (see {@link #CONFIG_KEY_OFFSET(Class)} and {@link #CONFIG_KEY_UNIT(Class)})
   * and for logging messages.
   */
  AbstractTimeSource(final Configuration configuration,
                     final Class<? extends AbstractTimeSource> clazz) {
    // setting default configuration (original comment: "somewhat dirty hack...")
    final Configuration defaultConfig = getDefaultConfiguration(); // NOPMD (overrideable)
    if (defaultConfig != null)
      configuration.setDefaultConfiguration(defaultConfig);

    String CONFIG_OFFSET = CONFIG_KEY_OFFSET(clazz);
    String CONFIG_UNIT = CONFIG_KEY_UNIT(clazz);
    Log log = LogFactory.getLog(clazz);

    // setting time unit
    final int timeunitval = configuration.getIntProperty(CONFIG_UNIT);
    switch (timeunitval) {
      case 0:
        timeunit = TimeUnit.NANOSECONDS;
        break;
      case 1:
        timeunit = TimeUnit.MICROSECONDS;
        break;
      case 2:
        timeunit = TimeUnit.MILLISECONDS;
        break;
      case 3:
        timeunit = TimeUnit.SECONDS;
        break;
      default:
        log.warn("Failed to determine value of " + CONFIG_UNIT + " (0, 1, 2, or 3 expected)." +
                "Setting to 0=nanoseconds");
        timeunit = TimeUnit.NANOSECONDS;
        break;
    }

    offset = 0;
  }

	/**
	 * This method should be overwritten, iff the timer is external to Kieker and
	 * thus its default configuration is not included in the default config file.
	 * 
	 * @return The configuration object containing the default configuration.
	 */
	protected Configuration getDefaultConfiguration() { // NOPMD (default implementation)
		return null;
	}

  /**
   * @return clazz.getName() + ".offset"
   * @since 1.12
   */
  public static String CONFIG_KEY_OFFSET(Class<? extends ITimeSource> clazz) {
    return clazz.getName() + ".offset";
  }

  /**
   * @return clazz.getName() + ".unit"
   * @since 1.12
   */
  public static String CONFIG_KEY_UNIT(Class<? extends ITimeSource> clazz) {
    return clazz.getName() + ".unit";
  }

  /*
  |=================|
  | (i) ITimeSource |
  |=================|
  */
  @Override
  public TimeUnit getTimeUnit() {
    return timeunit;
  }

	@Override
	public abstract String toString(); // findbugs: This has to be declared here to make this method abstract!
}
