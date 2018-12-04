package org.opennms.poc.graph;

import org.opennms.poc.graph.api.persistence.GraphRepository;
import org.opennms.poc.graph.impl.DefaultGraphService;
import org.opennms.poc.graph.impl.bsm.BsmGraphProvider;
import org.opennms.poc.graph.impl.nodes.NodeGraphProvider;
import org.opennms.poc.graph.impl.vmware.VmwareGraphProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
public class GraphApplication {

	private static Logger LOG = LoggerFactory.getLogger(GraphApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(GraphApplication.class, args);
	}

	@Autowired
	private DefaultGraphService graphService;

	@Autowired
	private BsmGraphProvider bsmGraphProvider;

	@Autowired
	private NodeGraphProvider nodeGraphProvider;

	@Autowired
	private VmwareGraphProvider vmwareGraphProvider;

	@Autowired
	private GraphRepository graphRepository;

//	@Scheduled(initialDelay = 5000, fixedDelay = 60 * 1000 * 60 * 24)
//	public void initializeGraphProvider() throws InvalidGraphException {
//		graphService.onBind(new GraphmlProvider(getClass().getResourceAsStream("/graphml-graph.xml")), new HashMap());
//		graphService.onBind(new VmwareGraphListener(graphRepository),  ImmutableMap.of("namespace", VmwareImporter.NAMESPACE));
//		graphService.onBind(vmwareGraphProvider, new HashMap<>());
//		graphService.onBind(bsmGraphProvider, new HashMap());
//		graphService.onBind(nodeGraphProvider, new HashMap<>());
////		graphService.onBind(event -> {
////			LOG.info("New event of type {} received.", event.getType());
//////			final Graph g = graphService.getGraph(NAMESPACE);
//////			g.getVertices().stream().forEach(v -> LOG.info("V {}", v.getId()));
//////			g.getEdges().stream().forEach(e -> LOG.info("E {} -> {}", e.getSource().getId(), e.getTarget().getId()));
////		}, new HashMap());
//	}
//
//	@Scheduled(initialDelay = 5000, fixedDelay = 60 * 1000 * 10)
//	public void startDiscovery() throws MalformedURLException, RemoteException {
////		final EventBus eventBus = graphService.getEventBus();
//		new VmwareImporter(graphService, VmwareConfig.Host, VmwareConfig.Username, VmwareConfig.Password).startImport();
//	}
}
