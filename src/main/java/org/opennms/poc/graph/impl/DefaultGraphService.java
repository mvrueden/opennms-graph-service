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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.opennms.poc.graph.api.Edge;
import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.GraphProvider;
import org.opennms.poc.graph.api.GraphService;
import org.opennms.poc.graph.api.Query;
import org.opennms.poc.graph.api.Vertex;
import org.opennms.poc.graph.api.listener.GraphChangeListener;
import org.opennms.poc.graph.api.listener.GraphChangeSetListener;
import org.opennms.poc.graph.impl.change.ChangeSet;
import org.springframework.stereotype.Service;

// How would this listen to events in the first place?
@Service
public class DefaultGraphService implements GraphService {

    private class Entity<T> {
        private Namespace namespace;
        private T listener;
    }

    private List<GraphProvider> providers = new ArrayList<>();

    private List<Entity<GraphChangeSetListener<?, ?>>> graphChangeSetListenerEntities = new ArrayList<>();
    private List<Entity<GraphChangeListener<?, ?>>> graphChangeListenerEntities = new ArrayList<>();

    public void onBind(GraphChangeListener listener, Map properties) {
        final Entity<GraphChangeListener<?, ?>> entity = new Entity();
        entity.listener = listener;
        entity.namespace = new Namespace((String) properties.getOrDefault("namespace", "*"));
        graphChangeListenerEntities.add(entity);
    }

    public void onUnbind(GraphChangeListener listener, Map properties) {
        graphChangeListenerEntities.removeAll(
                graphChangeListenerEntities.stream().filter(e -> e.listener == listener).collect(Collectors.toList())
        );
    }

    public void onBind(GraphChangeSetListener listener, Map properties) {
        final Entity<GraphChangeSetListener<?, ?>> entity = new Entity();
        entity.listener = listener;
        entity.namespace = new Namespace((String) properties.getOrDefault("namespace", "*"));
        graphChangeSetListenerEntities.add(entity);
    }

    public void onUnbind(GraphChangeSetListener listener, Map properties) {
        graphChangeSetListenerEntities.removeAll(
                graphChangeSetListenerEntities.stream().filter(e -> e.listener == listener).collect(Collectors.toList())
        );
    }


    // OSGi-Hook
    public void onBind(GraphProvider provider, Map properties) {
        provider.setNotificationService(this);
        providers.add(provider);
        // TODO MVR this will probably not really work in osgi-context
        if (provider instanceof GraphChangeListener) {
            onBind((GraphChangeListener) provider, properties);
        }
        // TODO MVR this will probably not really work in osgi-context
        if (provider instanceof GraphChangeSetListener) {
            onBind((GraphChangeSetListener) provider, properties);
        }
    }

    // OSGi-Hook
    public void onUnbind(GraphProvider provider, Map properties) {
        providers.remove(provider);
        // TODO MVR this will probably not really work in osgi-context
        if (provider instanceof GraphChangeListener) {
            onUnbind((GraphChangeListener) provider, properties);
        }
        // TODO MVR this will probably not really work in osgi-context
        if (provider instanceof GraphChangeSetListener) {
            onUnbind((GraphChangeSetListener) provider, properties);
        }
    }

    @Override
    public List<Graph> getGraphs() {
        final List<Graph> collect = providers.stream().map(GraphProvider::getGraph).collect(Collectors.toList());
        return collect;
    }

    @Override
    public <V extends Vertex, E extends Edge<V>> Graph<V, E> getGraph(String namespace) {
        final Optional<GraphProvider> first = providers.stream()
                .filter(provider -> provider.getGraphInfo() != null && namespace.equals(provider.getGraphInfo().getNamespace()))
                .findFirst();
        if (first.isPresent()) {
            return first.get().getGraph();
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
        final ChangeSet<Vertex, Edge<Vertex>> changeSet = new ChangeSet(oldGraph, newGraph);
        graphChanged(changeSet);
    }

    @Override
    public void graphChanged(ChangeSet changeSet) {
        if (changeSet != null && changeSet.hasChanges()) {
            // Send them out
            for (GraphChangeListener listener : getChangeListeners(changeSet.getNamespace())) {
                changeSet.accept(listener);
            }
            for (GraphChangeSetListener listener : getChangeSetListeners(changeSet.getNamespace())) {
                changeSet.accept(listener);
            }
        }
    }

    private List<GraphChangeSetListener> getChangeSetListeners(final String namespace) {
        return graphChangeSetListenerEntities.stream()
                .filter(entity -> entity.namespace.matches(namespace))
                .map(entity -> entity.listener)
                .collect(Collectors.toList());
    }

    private List<GraphChangeListener> getChangeListeners(String namespace) {
        return graphChangeListenerEntities.stream()
                .filter(entity -> entity.namespace.matches(namespace))
                .map(entity -> entity.listener)
                .collect(Collectors.toList());
    }

}
