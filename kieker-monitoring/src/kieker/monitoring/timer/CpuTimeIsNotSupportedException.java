package kieker.monitoring.timer;

/**
 * @author Dominic Parga Cacheiro
 *
 * @since 1.12
 */
public class CpuTimeIsNotSupportedException extends RuntimeException {
  public CpuTimeIsNotSupportedException() {
    super("Measuring thread cpu time is not supported by the JVM. " +
            "For more details, see java.lang.management.ThreadMXBean.isThreadCpuTimeSupported()");
  }
}
