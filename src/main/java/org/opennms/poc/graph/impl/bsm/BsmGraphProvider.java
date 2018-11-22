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

package org.opennms.poc.graph.impl.bsm;

import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.model.graph.BusinessServiceGraph;
import org.opennms.netmgt.bsm.service.model.graph.GraphEdge;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;
import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.GraphProvider;
import org.opennms.poc.graph.api.simple.SimpleGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Component
public class BsmGraphProvider implements GraphProvider {

    public static final String NAMESPACE = "bsm";

    @Autowired
    private BusinessServiceManager serviceManager;

    @Scheduled(initialDelay = 1000, fixedDelay = 10000)
    public void reload() {
        serviceManager.getStateMachine().setBusinessServices(serviceManager.getAllBusinessServices());
    }

    @Override
    public String getNamespace() {
        return NAMESPACE;
    }

    @Override
    public Graph getGraph() {
        final BusinessServiceGraph sourceGraph = serviceManager.getGraph();
        final Graph<AbstractVertex, BusinessServiceEdge<AbstractVertex>> targetGraph = new SimpleGraph<>(NAMESPACE);
        for (GraphVertex topLevelBusinessService : sourceGraph.getVerticesByLevel(0)) {
            addVertex(sourceGraph, targetGraph, topLevelBusinessService, null);
        }
        return targetGraph;
    }

    private void addVertex(BusinessServiceGraph sourceGraph, Graph<AbstractVertex, BusinessServiceEdge<AbstractVertex>>  targetGraph, GraphVertex sourceVertex, AbstractVertex targetVertex) {
        if (targetVertex == null) {
            // Create a topology vertex for the current vertex
            targetVertex = convertSourceVertex(sourceVertex);
            targetGraph.addVertices(targetVertex);
        }

        for (GraphEdge sourceEdge : sourceGraph.getOutEdges(sourceVertex)) {
            GraphVertex sourceChildVertex = sourceGraph.getOpposite(sourceVertex, sourceEdge);

            // Create a vertex for each child vertex
            final AbstractVertex targetChildVertex = convertSourceVertex(sourceChildVertex);
            sourceGraph.getInEdges(sourceChildVertex).stream()
                    .map(GraphEdge::getFriendlyName)
                    .filter(s -> !Strings.isNullOrEmpty(s))
                    .findFirst()
                    .ifPresent(targetChildVertex::setLabel);
            targetGraph.addVertices(targetChildVertex);

            // Connect the two
            final BusinessServiceEdge edge = new BusinessServiceEdge(sourceEdge, targetVertex, targetChildVertex);
            targetGraph.addEdges(edge);

            // Recurse
            addVertex(sourceGraph, targetGraph, sourceChildVertex, targetChildVertex);
        }
    }

    private AbstractVertex convertSourceVertex(GraphVertex graphVertex) {
        if (graphVertex.getBusinessService() != null) {
            return new BusinessServiceVertex(graphVertex);
        }
        if (graphVertex.getIpService() != null) {
            return new IpServiceVertex(graphVertex);
        }
        if (graphVertex.getReductionKey() != null) {
            return new ReductionKeyVertex(graphVertex);
        }
        throw new IllegalArgumentException("Cannot convert GraphVertex to BusinessServiceVertex: " + graphVertex);
    }
}
