package kieker.analysis.plugin.filter.sink;

import kieker.analysis.IProjectContext;
import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.filter.AbstractFilterPlugin;
import kieker.common.configuration.Configuration;

public class CountingPrintSink extends AbstractFilterPlugin {

	/**
	 * The name of the input port receiving the incoming events.
	 */
	public static final String INPUT_PORT_NAME_EVENTS = "inputEvents";

	public static final String CONF_THRESHOLD = "threshold";

	private final int threshold;

	private int counter;

	/**
	 * Creates a new instance of this class using the given parameters.
	 * 
	 * @param configuration
	 *            The configuration for this component.
	 * @param projectContext
	 *            The project context for this component.
	 */
	public CountingPrintSink(final Configuration configuration, final IProjectContext projectContext) {
		super(configuration, projectContext);
		this.threshold = configuration.getIntProperty(CONF_THRESHOLD);
		log.debug("Using threshold:" + threshold);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final Configuration getCurrentConfiguration() {
		return configuration;
	}

	@InputPort(name = INPUT_PORT_NAME_EVENTS, eventTypes = { Object.class }, description = "Receives incoming objects to be counted and printed")
	public final void inputEvent(final Object event) {
		counter++;
		if (counter % threshold == 0) {
			log.info("count: " + counter);
		}
	}
}
