package kieker.analysis.plugin.filter.forward;

import kieker.analysis.IProjectContext;
import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.annotation.OutputPort;
import kieker.analysis.plugin.annotation.Plugin;
import kieker.analysis.plugin.filter.AbstractFilterPlugin;
import kieker.common.configuration.Configuration;

@Plugin(outputPorts = @OutputPort(name = CountingPrinter.OUTPUT_PORT_NAME_RELAY_EVENT))
public class CountingPrinter extends AbstractFilterPlugin {

	/**
	 * The name of the input port receiving the incoming events.
	 */
	public static final String INPUT_PORT_NAME_EVENTS = "inputEvents";

	public static final String OUTPUT_PORT_NAME_RELAY_EVENT = "CountingPrinter.OUTPUT_PORT_NAME_RELAY_EVENT";

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
	public CountingPrinter(final Configuration configuration, final IProjectContext projectContext) {
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
		deliver(OUTPUT_PORT_NAME_RELAY_EVENT, event);
	}
}
