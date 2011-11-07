package kieker.analysis.plugin;

import kieker.analysis.plugin.configuration.AbstractInputPort;
import kieker.analysis.plugin.configuration.OutputPort;

public abstract class AbstractAnalysisPlugin extends AbstractPlugin
		implements IAnalysisPlugin {

	@Override
	final protected void registerInputPort(final String name, final AbstractInputPort port) {
		super.registerInputPort(name, port);
	}

	@Override
	final protected void registerOutputPort(final String name, final OutputPort port) {
		super.registerOutputPort(name, port);
	}
}
