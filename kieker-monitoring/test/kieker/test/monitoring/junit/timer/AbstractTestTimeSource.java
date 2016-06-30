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

import java.util.Random;
import java.util.concurrent.TimeUnit;

import kieker.monitoring.timer.ICpuTimeSource;
import org.junit.Assert;

import kieker.monitoring.timer.ITimeSource;

import kieker.test.common.junit.AbstractKiekerTest;

/**
 * @author Jan Waller
 * 
 * @since 1.5
 */
public abstract class AbstractTestTimeSource extends AbstractKiekerTest { // NOPMD (no abstract methods)
  /*
  |============|
  | parameters |
  |============|
  */
  private final long timeSourceAbsToleranceMillis = 2; // choosing 2 because 1 occasionally fails on some machines (with nanos)
  private final long cpuTimeSourceAbsToleranceNanos = TimeUnit.MILLISECONDS.toNanos(1);

  /*
  |=======================|
  | cpu time measurements |
  |=======================|
  */
  /**
   * <p>
   * This method tests whether the given cpu time source acts like a timestamper. This means, it measures the time passed
   * to a defined origin. This method assumes this origin is the "birth" of the current thread
   * interpreted in nanoseconds, so it can be compared to {@link System#nanoTime()}.
   * <p>
   * The time stamp measured by the cpu time source should be the value measured by {@link System#nanoTime()}
   * +/- tolerance.
   * <p>
   * To make sure, the time source is warmed up, {@link ICpuTimeSource#getTime()} is checked twice, because an interval
   * timer could initialize the first return value with anything. Such cases has to be the detected.
   *
   * @param timesource This {@link ICpuTimeSource} should be tested.
   * @param timeunit The given time source should round its results to this timeunit.
   * @param offset The offset of the given cpu time source in milliseconds
   *               (although the time source converts it to nanoseconds!)
   */
  public final void testCpuTimestamping(final ICpuTimeSource timesource,
                                        final TimeUnit timeunit,
                                        final long offset) {
    testCpuTimestamping(new ICpuTimeSource[] { timesource }, timeunit, offset);
  }

  public final void testSummedCpuTimestamping(final ICpuTimeSource systemTimesource,
                                              final ICpuTimeSource userTimesource,
                                              final TimeUnit timeunit,
                                              final long offset) {
    testCpuTimestamping(new ICpuTimeSource[] { userTimesource, systemTimesource }, timeunit, offset);
  }

  private void testCpuTimestamping(final ICpuTimeSource[] timesources,
                                   final TimeUnit timeunit,
                                   final long offset) {

    /* parameter setting */
    final long waitingTimeMillis = 200;
    final long offsetNanos =TimeUnit.MILLISECONDS.toNanos(offset);
    final String tuPostfix = timeUnitAsPostfix(timeunit);

    /* testing */
    // Multiple runs, because an interval timer could initialize the first return value with anything. These cases
    // has to be detected.
    for (int i = 0; i < 2; i++) {
      final long[] measured = new long[1];
      final long before = -offsetNanos -cpuTimeSourceAbsToleranceNanos;
      final long[] after = new long[1];

      /* run thread */
      Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
          after[0] = System.nanoTime();
          AbstractTestTimeSource.this.activelyTimeConsumingMethod(waitingTimeMillis);
          for (ICpuTimeSource cpuTimeSource : timesources)
            measured[0] = measured[0] + cpuTimeSource.getTime();
          after[0] = (System.nanoTime() - after[0]) - offsetNanos + cpuTimeSourceAbsToleranceNanos;
        }
      });
      t.start();
      try {
        t.join();
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      /* validation */
      final long beforeTU = timeunit.convert(before, TimeUnit.NANOSECONDS);
      final long afterTU = timeunit.convert(after[0], TimeUnit.NANOSECONDS);

      Assert.assertTrue(
              "Measured time (" + measured[0] + tuPostfix + ") has to be >= " + beforeTU + tuPostfix,
              beforeTU <= measured[0]);
      Assert.assertTrue(
              "Measured time (" + measured[0] + tuPostfix + ") has to be <= " + afterTU + tuPostfix,
              measured[0] <= afterTU);
    }
  }

  /*
  |==========================|
  | normal time measurements |
  |==========================|
  */
  /**
   * <p>
   * This method tests whether the given time source acts like a timestamper. This means, it measures the time passed
   * to a defined origin. This method assumes this origin is midnight, 1970-1-1 UTC
   * interpreted in milliseconds, so it can be compared to {@link System#currentTimeMillis()}.
   * <p>
   * The time stamp measured by the time source should be the value measured by {@link System#currentTimeMillis()}
   * +/- tolerance.
   * <p>
   * To make sure, the time source is warmed up, {@link ITimeSource#getTime()} is checked twice, because an interval
   * timer could initialize the first return value with anything. Such cases has to be the detected.
   *
   * @param timesource This {@link ITimeSource} should be tested.
   * @param timeunit The given timesource should round its results to this timeunit.
   * @param offset The offset of the given time source in milliseconds
   */
  public final void testTimestamping(final ITimeSource timesource,
                                     final TimeUnit timeunit,
                                     final long offset,
                                     final TimeUnit offsetTimeUnit) { // NOPMD (only used by other tests)

    final String tuPostfix = timeUnitAsPostfix(timeunit);
    long offsetMillis = offsetTimeUnit.toMillis(offset);

    /* testing */
    // Multiple runs, because an interval timer could initialize the first return value with anything. These cases
    // has to be detected.
    for (int i = 0; i < 2; i++) {
      final long before = System.currentTimeMillis() - offsetMillis - timeSourceAbsToleranceMillis;
      final long measured = timesource.getTime();
      final long after = System.currentTimeMillis() - offsetMillis + timeSourceAbsToleranceMillis;

      final long beforeTU = timeunit.convert(before, TimeUnit.MILLISECONDS);
      final long afterTU = timeunit.convert(after, TimeUnit.MILLISECONDS);

      Assert.assertTrue(
              "Measured time (" + measured + tuPostfix + ") has to be >= " + beforeTU + tuPostfix,
              beforeTU <= measured);
      Assert.assertTrue(
              "Measured time (" + measured + tuPostfix + ") has to be <= " + afterTU + tuPostfix,
              measured <= afterTU);
    }
  }

  // TODO not tested yet; offset is ignored
  /**
   * <p>
   * This method tests whether the given time source acts like an interval stamper. This means, it measures the time
   * passed since the last measurement. Therefore, {@link Thread#sleep(long)} is called with random durations chosen
   * from [50ms, 100ms[ up to an entire duration of 2 seconds (a check with 1ms is made after 1s).
   * <p>
   * The time difference measured by the time source should be the value measured by
   * {@link System#currentTimeMillis()} +/- tolerance.
   *
   * @param timesource This {@link ITimeSource} should be tested.
   * @param timeunit The given timesource should round its results to this timeunit.
   */
  public final void testIntervalMeasuring(final ITimeSource timesource, final TimeUnit timeunit) {

    /* parameter setting */
    final long runtime = TimeUnit.SECONDS.toMillis(2);
    long runned = 0;

    /* testing */
    boolean oneMilliChecked = false;
    while (runned < runtime) {
      // prepare waiting time
      long waitingTime;
      if (!oneMilliChecked && runned >= runtime / 2) {
        waitingTime = 1;
        oneMilliChecked = true;
      } else
        waitingTime = (long) (new Random().nextFloat() * 50 + 50);

      // measurement
      long outer = System.currentTimeMillis();
      timesource.getTime();
      try {
        Thread.sleep(waitingTime);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      long inner = timesource.getTime();
      outer = System.currentTimeMillis() - outer;
      runned += waitingTime;

      // check
      outer = timeunit.convert(outer, TimeUnit.MILLISECONDS);
      long difference = Math.abs(outer - inner);
      String unit = timeUnitAsPostfix(timeunit);
      Assert.assertTrue(
              "Elapsed time measured by the tested time source (" + inner + unit + ") differs from the time" +
                      "measured by System.currentTimeMillis() (" + outer + unit + ") more than "
                      + timeSourceAbsToleranceMillis + "ms (" + difference + unit + ").",
              difference <= timeunit.convert(timeSourceAbsToleranceMillis, TimeUnit.MILLISECONDS));
    }
  }

  /*
  |=======|
  | utils |
  |=======|
  */
  private static String timeUnitAsPostfix(TimeUnit timeunit) {
    switch (timeunit) {
      case SECONDS:
        return " s";
      case MILLISECONDS:
        return " ms";
      case MICROSECONDS:
        return " us";
      case NANOSECONDS:
        return " ns";
      default:
        return "";
    }
  }

  private void activelyTimeConsumingMethod(long waitingTimeMillis) {
    long stamp = System.currentTimeMillis();
    while (System.currentTimeMillis() - stamp < waitingTimeMillis);
  }
}
