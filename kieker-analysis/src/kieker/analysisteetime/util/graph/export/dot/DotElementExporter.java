package kieker.analysisteetime.util.graph.export.dot;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import kieker.analysisteetime.util.graph.Direction;
import kieker.analysisteetime.util.graph.Edge;
import kieker.analysisteetime.util.graph.Graph;
import kieker.analysisteetime.util.graph.Vertex;
import kieker.analysisteetime.util.graph.export.AbstractTransformer;
import kieker.analysisteetime.util.graph.util.dot.DotGraphWriter;
import kieker.analysisteetime.util.graph.util.dot.attributes.DotClusterAttribute;
import kieker.analysisteetime.util.graph.util.dot.attributes.DotEdgeAttribute;
import kieker.analysisteetime.util.graph.util.dot.attributes.DotNodeAttribute;

class DotElementExporter extends AbstractTransformer<Void> {

	protected final DotGraphWriter dotGraphWriter;
	protected final DotExportConfiguration configuration;

	protected DotElementExporter(final Graph graph, final DotGraphWriter dotGraphWriter, final DotExportConfiguration configuration) {
		super(graph);
		this.dotGraphWriter = dotGraphWriter;
		this.configuration = configuration;
	}

	@Override
	protected void transformVertex(final Vertex vertex) {
		try {
			if (vertex.hasChildGraph()) {
				Graph childGraph = vertex.getChildGraph();

				dotGraphWriter.addClusterStart(vertex.getId().toString());

				for (Entry<DotClusterAttribute, Function<Vertex, String>> attribute : configuration.getClusterAttributes().entrySet()) {
					dotGraphWriter.addGraphAttribute(attribute.getKey().toString(), attribute.getValue().apply(vertex));
				}

				DotElementExporter childGraphWriter = new DotElementExporter(childGraph, dotGraphWriter, configuration);
				childGraphWriter.transform();

				dotGraphWriter.addClusterStop();
			} else {
				dotGraphWriter.addNode(vertex.getId().toString(), getAttributes(vertex));
			}
		} catch (IOException e) {
			handleIOException(e);
		}
	}

	@Override
	protected void transformEdge(final Edge edge) {
		try {
			final String sourceId = edge.getVertex(Direction.OUT).getId().toString();
			final String targetId = edge.getVertex(Direction.IN).getId().toString();

			dotGraphWriter.addEdge(sourceId, targetId, getAttributes(edge));
		} catch (IOException e) {
			handleIOException(e);
		}

	}

	protected void handleIOException(final IOException ioException) {
		throw new IllegalStateException(ioException);
	}

	protected Map<String, String> getAttributes(final Edge edge) {
		final Map<String, String> attributes = new HashMap<>();
		for (Entry<DotEdgeAttribute, Function<Edge, String>> entry : configuration.getEdgeAttributes().entrySet()) {
			final String value = entry.getValue().apply(edge);
			if (value != null) {
				attributes.put(entry.getKey().toString(), value);
			}
		}
		return attributes;
	}

	protected Map<String, String> getAttributes(final Vertex vertex) {
		final Map<String, String> attributes = new HashMap<>();
		for (Entry<DotNodeAttribute, Function<Vertex, String>> entry : configuration.getNodeAttributes().entrySet()) {
			final String value = entry.getValue().apply(vertex);
			if (value != null) {
				attributes.put(entry.getKey().toString(), value);
			}
		}
		return attributes;
	}

	@Override
	protected void beforeTransformation() {
		// Do nothing

	}

	@Override
	protected void afterTransformation() {
		// Do nothing
	}

	@Override
	protected Void getTransformation() {
		return null;
	}

}
