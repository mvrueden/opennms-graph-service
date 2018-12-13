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

package org.opennms.poc.graph.impl.nodes;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.GraphContainer;
import org.opennms.poc.graph.api.GraphNotificationService;
import org.opennms.poc.graph.api.GraphProvider;
import org.opennms.poc.graph.api.info.DefaultGraphInfo;
import org.opennms.poc.graph.api.info.GraphContainerInfo;
import org.opennms.poc.graph.api.info.GraphInfo;
import org.opennms.poc.graph.api.meta.DefaultGraphContainer;
import org.opennms.poc.graph.api.simple.SimpleEdge;
import org.opennms.poc.graph.api.simple.SimpleGraph;
import org.opennms.poc.graph.impl.refs.NodeRefs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// Dummy GraphProvider, which shows all nodes.
// Required to simulate Node Enrichment
@Component
public class NodeGraphProvider implements GraphProvider {

    public static final String NAMESPACE = "nodes";

    @Autowired
    private NodeDao nodeDao;

    @Override
    public void setNotificationService(GraphNotificationService notificationService) {

    }

    @Override
    @Transactional
    public GraphContainer loadGraphContainer() {
        final GraphContainerInfo info = getContainerInfo();
        final DefaultGraphContainer graphContainer = new DefaultGraphContainer(info);
        graphContainer.addGraph(loadGraph());
        return graphContainer;
    }

    @Override
    public Graph loadGraph() {
        final SimpleGraph<NodeVertex, SimpleEdge<NodeVertex>> graph = new SimpleGraph<>(NAMESPACE);
        nodeDao.findAll().forEach(n -> {
            NodeVertex vertex =  new NodeVertex(n);
            vertex.setNodeInfo(NodeRefs.from(n.getId()).resolve(nodeDao)); // TODO MVR this is enriched, so should probably be automatically resolved
            graph.addVertex(vertex);
        });
        return graph;
    }

    @Override
    public GraphInfo getGraphInfo() {
        return new DefaultGraphInfo(NAMESPACE)
                .withLabel("Nodes")
                .withDescription("Visualizes all nodes (Later this will be enlind topology)");
    }

}
