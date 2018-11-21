package org.opennms.poc.graph;

import java.util.HashMap;

import org.opennms.features.graphml.model.InvalidGraphException;
import org.opennms.poc.graph.api.Edge;
import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.GraphProvider;
import org.opennms.poc.graph.api.Vertex;
import org.opennms.poc.graph.api.generic.GenericEdge;
import org.opennms.poc.graph.api.generic.GenericVertex;
import org.opennms.poc.graph.api.listener.EventType;
import org.opennms.poc.graph.api.listener.LinkEvent;
import org.opennms.poc.graph.impl.DefaultGraphService;
import org.opennms.poc.graph.impl.GraphmlProvider;
import org.opennms.poc.graph.impl.PartialGraphProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@SpringBootApplication
@EnableScheduling
public class GraphApplication {

	private static Logger LOG = LoggerFactory.getLogger(GraphApplication.class);

	private static final String NAMESPACE = "simple";

	public static void main(String[] args) {
		SpringApplication.run(GraphApplication.class, args);
	}

	@Autowired
	private DefaultGraphService graphService;

	@Scheduled(initialDelay = 5000, fixedDelay = 60 * 1000 * 60 * 24)
	public void initializeGraphProvider() throws InvalidGraphException {
		graphService.onBind(new GraphmlProvider(getClass().getResourceAsStream("/graphml-graph.xml")), new HashMap());
		graphService.onBind((GraphProvider) new PartialGraphProvider(NAMESPACE), new HashMap<>());
		graphService.onBind(event -> {
			LOG.info("New event of type {} received.", event.getType());
//			final Graph g = graphService.getGraph(NAMESPACE);
//			g.getVertices().stream().forEach(v -> LOG.info("V {}", v.getId()));
//			g.getEdges().stream().forEach(e -> LOG.info("E {} -> {}", e.getSource().getId(), e.getTarget().getId()));
		}, new HashMap());
	}

	@Scheduled(initialDelay = 7500, fixedDelay = 5000)
	public void newLink() {
		final Graph graph = graphService.getGraph(NAMESPACE);

		// No edges, create 2 links
		if (graph.getVertices().isEmpty()) {
			final Vertex source = createVertex(1);
			final Vertex target = createVertex(2);
			final Edge edge = new GenericEdge(source, target);
			final LinkEvent event = new LinkEvent(EventType.LinkDiscovered, edge);
			graphService.linkEvent(event);
		} else if(graph.getVertices().size() <= 50) {
			int vertexCount = graph.getVertices().size();
			final Vertex source = graph.getVertex("" + (vertexCount - 1)); // get last vertex
			final Vertex target = createVertex(vertexCount); // Create new vertex
			final Edge edge = new GenericEdge(source, target); // Connect them
			final LinkEvent event = new LinkEvent(EventType.LinkDiscovered, edge);
			graphService.linkEvent(event);
		} else {
			int vertexCount = graph.getVertices().size();
			final Vertex target = graph.getVertex("" + (vertexCount - 1)); // get last vertex
			final Vertex source = graph.getVertex("" + (vertexCount - 2)); // get last vertex
			final Edge edge = new GenericEdge(source, target); // Connect them
			final LinkEvent event = new LinkEvent(EventType.LinkRemoved, edge);
			graphService.linkEvent(event);
		}
	}

	private Vertex createVertex(int id) {
		return new GenericVertex(NAMESPACE, id);
	}
}
