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

import java.util.List;
import java.util.Objects;

import org.opennms.poc.graph.api.Edge;
import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.GraphNotificationService;
import org.opennms.poc.graph.api.GraphProvider;
import org.opennms.poc.graph.api.Vertex;
import org.opennms.poc.graph.api.generic.GenericEdge;
import org.opennms.poc.graph.api.generic.GenericGraph;
import org.opennms.poc.graph.api.generic.GenericVertex;
import org.opennms.poc.graph.api.info.GraphInfo;
import org.opennms.poc.graph.api.listener.GraphChangeListener;
import org.opennms.poc.graph.api.listener.GraphChangeSetListener;
import org.opennms.poc.graph.impl.change.ChangeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Partially builds a topology from GenericVertices and Edges
public class PartialGraphListener implements GraphChangeSetListener<Vertex, Edge<Vertex>>, GraphProvider {
    
    private static final Logger LOG = LoggerFactory.getLogger(PartialGraphListener.class);
    private final String namespace;
    private GenericGraph graph = new GenericGraph();

    public PartialGraphListener(String namespace) {
        this.namespace = Objects.requireNonNull(namespace);
        graph.setNamespace(namespace);
    }

    @Override
    public void setNotificationService(GraphNotificationService notificationService) {

    }

    @Override
    public Graph getGraph() {
        return graph;
    }

    @Override
    public GraphInfo getGraphInfo() {
        return graph;
    }

    @Override
    public void graphChanged(ChangeSet<Vertex, Edge<Vertex>> changeSet) {
        changeSet.accept(new GraphChangeListener<Vertex, Edge<Vertex>>() {

            @Override
            public void handleVerticesAdded(List<Vertex> verticesAdded) {
                LOG.info("Vertex added: {}", verticesAdded);
                for (Vertex eachVertex : verticesAdded) {
                    if (eachVertex.getNamespace().equalsIgnoreCase(namespace)) {
                        final GenericVertex v = eachVertex.asGenericVertex();
                        v.setNamespace(graph.getNamespace());
                        graph.addVertex(v);
                    }
                }
            }

            @Override
            public void handleVerticesRemoved(List<Vertex> verticesRemoved) {
                LOG.info("Vertex removed: {}", verticesRemoved);
                for (Vertex eachVertex : verticesRemoved) {
                    if (eachVertex.getNamespace().equalsIgnoreCase(namespace)) {
                        final GenericVertex vertex = graph.getVertex(eachVertex.getId());
                        graph.removeVertex(vertex);
                    }
                }
            }

            @Override
            public void handleVerticesUpdated(List<Vertex> verticesUpdated) {
                LOG.info("Vertex updated: {}", verticesUpdated);
                for (Vertex eachVertex : verticesUpdated) {
                    if (eachVertex.getNamespace().equalsIgnoreCase(namespace)) {
                        final GenericVertex vertex = graph.getVertex(eachVertex.getId());
                        vertex.setProperties(eachVertex.asGenericVertex().getProperties());
                    }
                }
            }

            @Override
            public void handleEdgesAdded(List<Edge<Vertex>> edgesAdded) {
                LOG.info("Edge added {}", edgesAdded);
                for (Edge eachEdge : edgesAdded) {
                    if (eachEdge.getNamespace().equalsIgnoreCase(namespace)) {
                        final GenericEdge genericEdge = eachEdge.asGenericEdge();
                        genericEdge.setNamespace(graph.getNamespace());
                        graph.addEdge(genericEdge);
                    }
                }
            }

            @Override
            public void handleEdgesUpdated(List<Edge<Vertex>> edgesUpdated) {
                LOG.info("Edge updated: {}", edgesUpdated);
                for (Edge eachEdge : edgesUpdated) {
                    if (eachEdge.getNamespace().equalsIgnoreCase(namespace)) {
                        final GenericEdge edge = graph.getEdge(eachEdge.getId());
                        edge.setProperties(eachEdge.asGenericEdge().getProperties());
                    }
                }
            }

            @Override
            public void handleEdgesRemoved(List<Edge<Vertex>> edgesRemoved) {
                LOG.info("Edge removed: {}", edgesRemoved);
                for (Edge eachEdge : edgesRemoved) {
                    if (eachEdge.getNamespace().equalsIgnoreCase(namespace)) {
                        final GenericEdge edge = graph.getEdge(eachEdge.getId());
                        graph.removeEdge(edge);
                    }
                }
            }
        });
    }
}
