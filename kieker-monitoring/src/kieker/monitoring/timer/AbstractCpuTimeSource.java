package kieker.monitoring.timer;

import kieker.common.configuration.Configuration;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * This class serves methods for creating different timers for measuring cputime.
 *
 * @author Dominic Parga Cacheiro
 *
 * @since 1.12
 */
public abstract class AbstractCpuTimeSource extends AbstractTimeSource implements ICpuTimeSource {
  protected final ThreadMXBean bean;
  protected final boolean isThreadCpuTimeSupported;

  /**
   * <p>
   * This class uses {@link ThreadMXBean} to measure cputime of threads.<br>
   * NOTE: cpu time = user time + system time
   * <p>
   * You should check for {@link #isThreadCpuTimeSupported()} once before you use this class.
   * <p>
   * Regarding accuracy: {@link ThreadMXBean#getCurrentThreadCpuTime()} says: "The returned value is of
   * nanoseconds precision but not necessarily nanoseconds accuracy").
   *
   * <p>
   * This constructor initializes the class instance of {@link ThreadMXBean}
   * and {@link ThreadMXBean#isThreadCpuTimeSupported()}.
   *
   * @param configuration This configuration sets:<br>
   * &bull The time unit of the returned times<br>
   * &bull The given offset to "birth" of measured thread in milliseconds, converted in the constructor to nanoseconds; default: 0
   * @param clazz This parameter is used for reading the configuration settings
   * (see {@link #CONFIG_KEY_OFFSET(Class)} and {@link #CONFIG_KEY_UNIT(Class)})
   * and for logging messages.
   */
  AbstractCpuTimeSource(final Configuration configuration,
                        final Class<? extends AbstractTimeSource> clazz) {
    super(configuration, clazz);

    bean = ManagementFactory.getThreadMXBean();
    isThreadCpuTimeSupported = bean.isThreadCpuTimeSupported();

    // setting offset
    String CONFIG_OFFSET = CONFIG_KEY_OFFSET(clazz);
    if (configuration.getStringProperty(CONFIG_OFFSET).length() == 0)
      offset = 0;
    else
      offset = TimeUnit.MILLISECONDS.toNanos(configuration.getLongProperty(CONFIG_OFFSET));
  }

  /*
  |====================|
  | (i) ICpuTimeSource |
  |====================|
  */
  @Override
  public boolean isThreadCpuTimeSupported() {
    return isThreadCpuTimeSupported;
  }

  /**
   * @return The offset of the timesource in {@link TimeUnit}.
   */
  @Override
  public long getOffset() {
    return timeunit.convert(offset, TimeUnit.NANOSECONDS);
  }

  @Override
  public final String toString() {
    final StringBuilder sb = new StringBuilder(64);
    sb.append("Cpu time in " + timeunit.toString().toLowerCase(Locale.ENGLISH) + " (with nanoseconds precision) since ");
    sb.append(new Date(TimeUnit.NANOSECONDS.toMillis(offset)) + " ms");
    return sb.toString();
  }
}
