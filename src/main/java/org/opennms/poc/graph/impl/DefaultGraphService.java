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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.poc.graph.api.Edge;
import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.GraphProvider;
import org.opennms.poc.graph.api.GraphService;
import org.opennms.poc.graph.api.Query;
import org.opennms.poc.graph.api.Vertex;
import org.opennms.poc.graph.api.listener.GraphListener;
import org.opennms.poc.graph.api.listener.LinkEvent;
import org.springframework.stereotype.Service;

// How would this listen to events in the first place?
@Service
public class DefaultGraphService implements GraphService {

    private Map<String, GraphProvider> providers = new HashMap<>();
    private List<GraphListener> listeners = new ArrayList<>();

    public void onBind(GraphListener listener, Map properties) {
        listeners.add(listener);
    }

    public void onUnbind(GraphListener listener, Map properties) {
        listeners.remove(listener);
    }

    // OSGi-Hook
    public void onBind(GraphProvider provider, Map properties) {
        Objects.requireNonNull(provider);

        final String namespace = provider.getNamespace();
        if (providers.containsKey(namespace)) {
            throw new IllegalStateException("Provider for namespace [" + namespace + "] already registered");
        }
        providers.put(namespace, provider);

        // Register if listening to events
        if (provider instanceof GraphListener) {
            registerListener((GraphListener) provider);
        }
    }

    // OSGi-Hook
    public void onUnbind(GraphProvider provider, Map properties) {
        final String namespace = provider.getNamespace();
        providers.remove(namespace);
        if (provider instanceof GraphListener) {
            unregisterListener((GraphListener) provider);
        }
    }

    @Override
    public void linkEvent(final LinkEvent event) {
        listeners.forEach(listener -> listener.linkEvent(event));
    }

    @Override
    public List<Graph<? extends Vertex, ? extends Edge<? extends Vertex>>> getGraphs() {
        return providers.values()
                .stream()
                .map(p -> (Graph<Vertex, Edge<Vertex>>) p.getGraph())
                .collect(Collectors.toList());
    }

    @Override
    public <V extends Vertex, E extends Edge<V>> Graph<V, E> getGraph(String namespace) {
        final GraphProvider graphProvider = providers.get(namespace);
        if (graphProvider == null) {
            throw new NoSuchElementException("There is no graph provider registered for namespace [" + namespace + "]");
        }
        return graphProvider.getGraph();
    }

    @Override
    public <V extends Vertex, E extends Edge<V>> Graph<V, E> getGraph(Query query) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void registerListener(GraphListener graphProvider) {
        if (!listeners.contains(graphProvider)) {
            listeners.add(graphProvider);
        }
    }

    private void unregisterListener(GraphListener provider) {
        listeners.remove(provider);
    }
}
