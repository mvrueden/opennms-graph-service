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

package org.opennms.poc.graph.impl;

import java.util.Objects;

import org.opennms.poc.graph.api.Edge;
import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.GraphProvider;
import org.opennms.poc.graph.api.GraphProviderDescriptor;
import org.opennms.poc.graph.api.Vertex;
import org.opennms.poc.graph.api.listener.EventType;
import org.opennms.poc.graph.api.listener.GraphListener;
import org.opennms.poc.graph.api.listener.LinkEvent;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class SimpleGraphProvider implements GraphProvider, GraphListener {

    private final edu.uci.ics.jung.graph.Graph graph = new DirectedSparseMultigraph<Vertex, Edge>();
    private final String namespace;

    public SimpleGraphProvider(String namespace) {
        this.namespace = Objects.requireNonNull(namespace);
    }

    @Override
    public GraphProviderDescriptor getGraphProviderDescriptor() {
        return new GraphProviderDescriptor(namespace);
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public Graph getGraph() {
        final DefaultGraph graph = new DefaultGraph(getNamespace());
        graph.addVertices(this.graph.getVertices());
        graph.addEdges(this.graph.getEdges());
        return graph;
    }

    @Override
    public void linkEvent(LinkEvent event) {
        final Edge link = event.getLink();
        switch(event.getType()) {
            case LinkDiscovered:
                final Vertex source = link.getSource();
                final Vertex target = link.getTarget();
                if (!graph.containsVertex(source)) {
                    graph.addVertex(source);
                }
                if (!graph.containsVertex(target)) {
                    graph.addVertex(target);
                }
                graph.addEdge(link, source, target, EdgeType.DIRECTED);
                break;
            case LinkRemoved:
                graph.removeEdge(link);
                if (graph.getInEdges(link.getSource()).isEmpty() && graph.getOutEdges(link.getSource()).isEmpty()) {
                    graph.removeVertex(link.getSource());
                }
                if (graph.getInEdges(link.getTarget()).isEmpty() && graph.getOutEdges(link.getTarget()).isEmpty()) {
                    graph.removeVertex(link.getTarget());
                }
                break;
            case LinkUpdated:
                // TODO MVR maybe not the best way of implementing this
                linkEvent(new LinkEvent(EventType.LinkRemoved, link));
                linkEvent(new LinkEvent(EventType.LinkDiscovered, link));
                break;
            default: throw new IllegalStateException("No handler for LinkEvent of type " + event.getType() + " found.");
        }
    }

}
