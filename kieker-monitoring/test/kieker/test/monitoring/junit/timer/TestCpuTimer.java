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

package kieker.test.monitoring.junit.timer;

import kieker.common.configuration.Configuration;
import kieker.monitoring.core.configuration.ConfigurationFactory;
import kieker.monitoring.timer.CpuTimer;
import kieker.monitoring.timer.CpuTimer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * A test for the class {@link CpuTimer}.
 * 
 * @author Dominic Parga Cacheiro
 * 
 * @since 1.12
 */
public final class TestCpuTimer extends AbstractTestTimeSource {

  private Configuration configuration;
  private TimeUnit timeunit;
  private long offset;

  /**
	 * Default constructor.
	 */
	public TestCpuTimer() {
		// empty default constructor
	}

  @Before
  public final void beforeParameterSetting() {
    configuration = ConfigurationFactory.createDefaultConfiguration();
  }

  @After
  public final void executeTesting() { // NOPMD (assert in superclass)
    String timeunitIdx = "-1";
    switch (timeunit) {
      case NANOSECONDS:
        timeunitIdx = "0";
        break;
      case MICROSECONDS:
        timeunitIdx = "1";
        break;
      case MILLISECONDS:
        timeunitIdx = "2";
        break;
      case SECONDS:
        timeunitIdx = "3";
        break;
    }
    configuration.setProperty(
            CpuTimer.CONFIG_KEY_UNIT(CpuTimer.class),
            timeunitIdx);
    configuration.setProperty(
            CpuTimer.CONFIG_KEY_OFFSET(CpuTimer.class),
            "" + offset);
    super.testCpuTimestamping(
            new CpuTimer(configuration),
            timeunit,
            offset);
  }

  /*
  |====================|
  | parameter settings |
  |====================|
  */
	/**
	 * This method tests the timer with default configuration.
	 */
	@Test
	public final void testDefault() {
    timeunit = TimeUnit.NANOSECONDS;
    offset = configuration.getLongProperty(CpuTimer.CONFIG_KEY_OFFSET(CpuTimer.class));
	}

	/**
	 * This method tests the timer with nanoseconds as used time unit.
	 */
	@Test
	public final void testNanoseconds0() {
    timeunit = TimeUnit.NANOSECONDS;
    offset = 0;
	}

	/**
	 * This method tests the timer with microseconds as used time unit.
	 */
	@Test
	public final void testMicroseconds0() {
    timeunit = TimeUnit.MICROSECONDS;
    offset = 0;
	}

	/**
	 * This method tests the timer with milliseconds as used time unit.
	 */
	@Test
  public final void testMilliseconds0() {
    timeunit = TimeUnit.MILLISECONDS;
    offset = 0;
	}

	/**
	 * This method tests the timer with seconds as used time unit.
	 */
	@Test
	public final void testSeconds0() {
    timeunit = TimeUnit.SECONDS;
    offset = 0;
	}

  /**
   * This method tests the timer with nanoseconds as used time unit.
   */
  @Test
  public final void testNanoseconds149() {
    timeunit = TimeUnit.NANOSECONDS;
    offset = 149;
  }

  /**
   * This method tests the timer with microseconds as used time unit.
   */
  @Test
  public final void testMicroseconds149() {
    timeunit = TimeUnit.MICROSECONDS;
    offset = 149;
  }

  /**
   * This method tests the timer with milliseconds as used time unit.
   */
  @Test
  public final void testMilliseconds149() {
    timeunit = TimeUnit.MILLISECONDS;
    offset = 149;
  }

  /**
   * This method tests the timer with seconds as used time unit.
   */
  @Test
  public final void testSeconds149() {
    timeunit = TimeUnit.SECONDS;
    offset = 149;
  }
}