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

package org.opennms.poc.graph.impl.partial;

import org.opennms.poc.graph.api.Edge;
import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.GraphService;
import org.opennms.poc.graph.api.Vertex;
import org.opennms.poc.graph.api.generic.GenericEdge;
import org.opennms.poc.graph.api.generic.GenericVertex;
import org.opennms.poc.graph.impl.change.ChangeSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PartialGraphUpdater {

    public static final String NAMESPACE = "simple";

    @Autowired
    private GraphService graphService;

    private boolean increasing = true;

    public PartialGraphUpdater() {

    }

    @Scheduled(initialDelay = 7500, fixedDelay = 5000)
    public void simulateGraphUpdate() {
        final int N = 50;
        final Graph graph = graphService.getGraph(NAMESPACE);
        // No edges, create 2 links
        if (graph.getVertices().isEmpty()) {
            increasing = true;
            final Vertex source = new GenericVertex(NAMESPACE, 1);
            final Vertex target = new GenericVertex(NAMESPACE, 2);
            final Edge edge = new GenericEdge(source, target);
            graphService.graphChanged(new ChangeSet(NAMESPACE).edgeAdded(edge));
        } else if(increasing && graph.getVertices().size() < N) {
            int vertexCount = graph.getVertices().size();
            final Vertex source = graph.getVertex("" + vertexCount); // get last vertex
            final Vertex target = new GenericVertex(NAMESPACE, vertexCount + 1); // Create new vertex
            final Edge edge = new GenericEdge(source, target); // Connect them
            graphService.graphChanged(new ChangeSet(NAMESPACE).edgeAdded(edge));
            increasing = vertexCount != N - 1;
        } else if (!increasing) {
            int edgeCount = graph.getEdges().size();
            final Vertex source = graph.getVertex("" + (edgeCount)); // get last vertex
            final Vertex target = graph.getVertex("" + (edgeCount + 1)); // get last last vertex
            final Edge edge = new GenericEdge(source, target); // Connect them
            final ChangeSet changeSet = new ChangeSet(NAMESPACE).vertexRemoved(target).edgeRemoved(edge);
            if (edgeCount == 1) {
                changeSet.vertexRemoved(source);
            }
            graphService.graphChanged(changeSet);
        }
    }
}
