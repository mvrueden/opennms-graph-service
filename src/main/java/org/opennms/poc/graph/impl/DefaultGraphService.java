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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.opennms.poc.graph.api.Edge;
import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.GraphProvider;
import org.opennms.poc.graph.api.GraphService;
import org.opennms.poc.graph.api.Query;
import org.opennms.poc.graph.api.Vertex;
import org.opennms.poc.graph.api.listener.GraphChangeListener;
import org.opennms.poc.graph.impl.change.ChangeSet;
import org.springframework.stereotype.Service;

// How would this listen to events in the first place?
@Service
public class DefaultGraphService implements GraphService {

    private class Namespace {

        private final String namespace;

        public Namespace(String namespace) {
            this.namespace = Objects.requireNonNull(namespace);
        }

        public boolean matches(String input) {
            return namespace.equals("*") || namespace.equalsIgnoreCase(input);
        }
    }

    private class GraphListenerEntity {
        private Namespace namespace;
        private GraphChangeListener listener;
    }

    private class GraphProviderEntity {
        private Namespace namespace;
        private GraphProvider provider;
    }

    private List<GraphProviderEntity> providers = new ArrayList<>();

    private List<GraphListenerEntity> listeners = new ArrayList<>();

    public void onBind(GraphChangeListener listener, Map properties) {
        final GraphListenerEntity entity = new GraphListenerEntity();
        entity.listener = listener;
        entity.namespace = new Namespace((String) properties.getOrDefault("namespace", "*"));
        listeners.add(entity);
    }

    public void onUnbind(GraphChangeListener listener, Map properties) {
        listeners.removeAll(
                listeners.stream().filter(e -> e.listener == listener).collect(Collectors.toList())
        );
    }

    // OSGi-Hook
    public void onBind(GraphProvider provider, Map properties) {
        final GraphProviderEntity entity = new GraphProviderEntity();
        entity.provider = provider;
        entity.namespace = new Namespace((String) properties.getOrDefault("namespace", "*"));
        provider.setNotificationService(this);
        providers.add(entity);
        if (provider instanceof GraphChangeListener) {
            onBind((GraphChangeListener) provider, properties);
        }
    }

    // OSGi-Hook
    public void onUnbind(GraphProvider provider, Map properties) {
        final List<GraphProviderEntity> removeMe = providers.stream().filter(e -> e.provider == provider).collect(Collectors.toList());
        removeMe.forEach(e -> providers.remove(e));
    }

    @Override
    public List<Graph> getGraphs() {
        final List<Graph> collect = providers.stream().map(e -> e.provider.getGraph())
                .collect(Collectors.toList());
        return collect;
    }

    @Override
    public <V extends Vertex, E extends Edge<V>> Graph<V, E> getGraph(String namespace) {
        final Optional<GraphProviderEntity> first = providers.stream()
                .filter(p -> p.provider.getGraphInfo() != null
                                && namespace.equals(p.provider.getGraphInfo().getNamespace()))
                .findFirst();
        if (first.isPresent()) {
            return first.get().provider.getGraph();
        }
        return null;
    }

    @Override
    public <V extends Vertex, E extends Edge<V>> Graph<V, E> getSnapshot(Query query) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void graphChanged(Graph oldGraph, Graph newGraph) {
        if (oldGraph == null && newGraph == null) {
            return; // If both graphs are null, there is nothing we can do
        }

        // Detect changes
        final String namespace = oldGraph != null ? oldGraph.getNamespace() : newGraph.getNamespace();
        final ChangeSet<Vertex, Edge<Vertex>> changeSet = new ChangeSet(namespace, new Date()); // TODO MVR in my opinion this should be automatically detected from the graphs
        changeSet.detectChanges(oldGraph, newGraph);

        // Send them out
        final List<GraphChangeListener> listeners = getListeners(changeSet.getNamespace());
        for (GraphChangeListener listener : listeners) {
            // TODO MVR maybe we can just call listener.graphChanged(changeSet) instead?
            if (changeSet.getVerticesAdded().isEmpty()) {
                listener.handleVerticesAdded(changeSet.getVerticesAdded());
            }
            if (changeSet.getVerticesRemoved().isEmpty()) {
                listener.handleVerticesRemoved(changeSet.getVerticesRemoved());
            }
            if (changeSet.getVerticesUpdated().isEmpty()) {
                listener.handleVerticesUpdated(changeSet.getVerticesUpdated());
            }
            if (changeSet.getEdgesAdded().isEmpty()) {
                listener.handleEdgesAdded(changeSet.getEdgesAdded());
            }
            if (changeSet.getEdgesUpdated().isEmpty()) {
                listener.handleEdgesUpdated(changeSet.getEdgesUpdated());
            }
            if (changeSet.getEdgesRemoved().isEmpty()) {
                listener.handleEdgesRemoved(changeSet.getEdgesRemoved());
            }
        }
    }

    private List<GraphChangeListener> getListeners(String namespace) {
        return listeners.stream()
                .filter(entity -> entity.namespace.matches(namespace))
                .map(entity -> entity.listener)
                .collect(Collectors.toList());
    }

}
