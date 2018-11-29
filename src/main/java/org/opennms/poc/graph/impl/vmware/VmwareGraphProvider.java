/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.poc.graph.impl.vmware;

import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.GraphProvider;
import org.opennms.poc.graph.api.builder.GraphBuilder;
import org.opennms.poc.graph.api.persistence.GraphRepository;
import org.opennms.poc.graph.api.simple.SimpleGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VmwareGraphProvider implements GraphProvider<VmwareVertex, VmwareEdge> {

    private final static Logger LOG = LoggerFactory.getLogger(VmwareGraphProvider.class);

    private final GraphBuilder<VmwareVertex, VmwareEdge> graphBuilder;
    private Graph<VmwareVertex, VmwareEdge> graph = new SimpleGraph<>(VmwareImporter.NAMESPACE);

    public VmwareGraphProvider() {
        this.graphBuilder = new GraphBuilder<>(VmwareImporter.NAMESPACE, graphDiscoveryStartedEvent -> new SimpleGraph<>(VmwareImporter.NAMESPACE));
    }

//    @Override
    public String getNamespace() {
        return VmwareImporter.NAMESPACE;
    }

//    @Override
    public Graph<VmwareVertex, VmwareEdge> getGraph() {
        return graph;
    }

    @Override
    public void provideGraph(GraphRepository repository) {

    }

//    @Subscribe
//    public void handleGraphCreationStarted(GraphDiscoveryStartedEvent event) {
//        LOG.info("Graph Creation started");
//        graphBuilder.handleGraphCreationStarted(event);
//    }
//
//    @Subscribe
//    public void handleGraphCreationFinished(GraphDiscoveryFinishedEvent event) {
//        LOG.info("Graph Creation finished");
//        graphBuilder.handleGraphCreationFinished(event);
//        this.graph = graphBuilder.getGraph();
//    }
//
//    @Subscribe
//    public void handleVertexAdded(AddVertexEvent<VmwareVertex> event) {
//        LOG.info("Vertex added");
//        graphBuilder.handleVertexAdded(event);
//    }
//
//    @Subscribe
//    public void handleEdgeAdded(AddEdgeEvent<VmwareVertex, VmwareEdge> event) {
//        LOG.info("Edge added");
//        graphBuilder.handleEdgeAdded(event);
//    }
}
