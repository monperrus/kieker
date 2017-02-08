package kieker.analysisteetime.util.graph.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import kieker.analysisteetime.util.graph.Direction;
import kieker.analysisteetime.util.graph.Edge;
import kieker.analysisteetime.util.graph.Graph;
import kieker.analysisteetime.util.graph.Vertex;

public class GraphImpl extends ElementImpl implements Graph {

	protected String name = "G";

	protected Map<String, Vertex> vertices = new HashMap<String, Vertex>();
	protected Map<String, Edge> edges = new HashMap<String, Edge>();

	protected Long currentDefaultId = 0l;

	protected VertexImpl parentVertex = null;

	public GraphImpl() {}

	protected GraphImpl(final VertexImpl parentVertex) {
		this.parentVertex = parentVertex;
	}

	@Override
	public Vertex addVertex(final Object id) {
		String idString = null;
		if (id == null) {
			do {
				idString = getDefaultId();
			} while (vertices.containsKey(idString));
		} else {
			idString = id.toString();
			if (vertices.containsKey(idString)) {
				throw ExceptionFactory.vertexWithIdAlreadyExists(id);
			}
		}

		Vertex vertex = new VertexImpl(idString, this);
		vertices.put(vertex.getId().toString(), vertex);
		return vertex;
	}

	@Override
	public Vertex getVertex(final Object id) {
		if (id == null) {
			throw ExceptionFactory.vertexIdCanNotBeNull();
		}
		String idString = id.toString();
		return this.vertices.get(idString);
	}

	@Override
	public Iterable<Vertex> getVertices() {
		return new ArrayList<Vertex>(this.vertices.values());
	}

	@Override
	public void removeVertex(final Vertex vertex) {
		if (!this.vertices.containsKey(vertex.getId().toString())) {
			throw ExceptionFactory.vertexWithIdDoesNotExist(vertex.getId());
		}

		for (Edge edge : vertex.getEdges(Direction.IN)) {
			this.removeEdge(edge);
		}
		for (Edge edge : vertex.getEdges(Direction.OUT)) {
			this.removeEdge(edge);
		}

		this.vertices.remove(vertex.getId().toString());
	}

	@Override
	public Edge addEdge(final Object id, final Vertex outVertex, final Vertex inVertex) {

		// BETTER Throw Exception if Vertices are null

		Stack<VertexImpl> outVertexParents = new Stack<>();
		for (VertexImpl parent = (VertexImpl) outVertex; parent != null; parent = parent.graph.parentVertex) {
			outVertexParents.push(parent);
		}
		Stack<VertexImpl> inVertexParents = new Stack<>();
		for (VertexImpl parent = (VertexImpl) inVertex; parent != null; parent = parent.graph.parentVertex) {
			inVertexParents.push(parent);
		}

		if (outVertexParents.peek().graph != inVertexParents.peek().graph) {
			throw ExceptionFactory.verticesAreNotInSameGraph(outVertex.getId(), inVertex.getId());
		}

		GraphImpl firstEqualParent = null;
		while (!outVertexParents.isEmpty() && !inVertexParents.isEmpty() && outVertexParents.peek().graph == inVertexParents.pop().graph) {
			firstEqualParent = outVertexParents.pop().graph;
		}
		return firstEqualParent.addEdgeChecked(id, outVertex, inVertex);
	}

	protected Edge addEdgeChecked(final Object id, final Vertex outVertex, final Vertex inVertex) {
		String idString;
		if (id == null) {
			do {
				idString = getDefaultId();
			} while (edges.containsKey(idString));
		} else {
			idString = id.toString();
			if (edges.containsKey(idString)) {
				throw ExceptionFactory.edgeWithIdAlreadyExists(id);
			}
		}

		final Edge edge = new EdgeImpl(idString, outVertex, inVertex, this);
		this.edges.put(edge.getId().toString(), edge);
		((VertexImpl) outVertex).addOutEdge(edge);
		((VertexImpl) inVertex).addInEdge(edge);

		return edge;
	}

	@Override
	public Edge getEdge(final Object id) {
		if (id == null) {
			throw ExceptionFactory.edgeIdCanNotBeNull();
		}
		String idString = id.toString();
		return this.edges.get(idString);
	}

	@Override
	public Iterable<Edge> getEdges() {
		return new ArrayList<Edge>(this.edges.values());
	}

	@Override
	public void removeEdge(final Edge edge) {
		if (!this.edges.containsKey(edge.getId().toString())) {
			throw ExceptionFactory.edgeWithIdDoesNotExist(edge.getId());
		}

		((VertexImpl) edge.getVertex(Direction.IN)).removeInEdge(edge);
		((VertexImpl) edge.getVertex(Direction.OUT)).removeOutEdge(edge);

		this.edges.remove(edge.getId().toString());
	}

	private String getDefaultId() {
		String idString;
		do {
			idString = currentDefaultId.toString();
			currentDefaultId++;
		} while (vertices.containsKey(idString) || edges.containsKey(idString));
		return idString;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(final String name) {
		if (name != null) {
			this.name = name;
		}
	}

}