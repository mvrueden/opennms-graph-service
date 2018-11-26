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

package org.opennms.poc.graph.api.builder;

import java.util.Objects;
import java.util.function.Function;

import org.opennms.poc.graph.api.Edge;
import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.Vertex;
import org.opennms.poc.graph.api.events.AddEdgeEvent;
import org.opennms.poc.graph.api.events.AddVertexEvent;
import org.opennms.poc.graph.api.events.Event;
import org.opennms.poc.graph.api.events.GraphDiscoveryFinishedEvent;
import org.opennms.poc.graph.api.events.GraphDiscoveryStartedEvent;

import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;

// Helper class to build a graph from various events
// The Builder is designed to follow the defined Lifecycle.
public class GraphBuilder<V extends Vertex, E extends Edge<V>> {

    // states : Undefined, Building, Defined
    private State state = State.Undefined;
    private final String namespace;
    private Graph<V, E> graph;
    private final Function<GraphDiscoveryStartedEvent, Graph<V, E>> graphFactory;

    public GraphBuilder(String namespace, Function<GraphDiscoveryStartedEvent, Graph<V, E>> graphFactory) {
        this.namespace = namespace;
        this.graphFactory = Objects.requireNonNull(graphFactory);
    }


    public State getState() {
        return state;
    }

    public boolean isReady() {
        return getState() == State.Finished;
    }

    public Graph<V, E> getGraph() {
        if (getState() == State.Undefined) {
            throw new IllegalStateException("Graph is not yet instantiated");
        }
        if (getState() == State.Building) {
            throw new IllegalStateException("Graph is still beeing build");
        }
        return graph;
    }

    public Graph<V, E> getSnapshot() {
        return graph;
    }

    @Subscribe
    public void handleGraphCreationStarted(GraphDiscoveryStartedEvent event) {
        if (shouldHandle(event)) {
            state = State.Building;
            this.graph = graphFactory.apply(event);
        }
    }

    @Subscribe
    public void handleGraphCreationFinished(GraphDiscoveryFinishedEvent event) {
        if (shouldHandle(event)) {
            state = State.Finished;
        }
    }

    @Subscribe
    public void handleVertexAdded(AddVertexEvent<V> event) {
        if (shouldHandle(event) && state == State.Building) {
            graph.addVertices(Lists.newArrayList(event.getVertex()));
        }
    }

    @Subscribe
    public void handleEdgeAdded(AddEdgeEvent<V, E> event) {
        if (shouldHandle(event) && state == State.Building) {
            final E edge = event.getEdge();
            if (!graph.getVertices().contains(edge.getSource())) {
                graph.addVertex(edge.getSource());
            }
            if (!graph.getVertices().contains(edge.getTarget())) {
                graph.addVertex(edge.getTarget());
            }
            graph.addEdge(event.getEdge());
        }
    }

    private boolean shouldHandle(Event event) {
        return namespace.equalsIgnoreCase(event.getNamespace());
    }

}
