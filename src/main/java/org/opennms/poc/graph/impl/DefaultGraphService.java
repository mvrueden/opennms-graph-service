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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.poc.graph.api.Edge;
import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.GraphProvider;
import org.opennms.poc.graph.api.GraphService;
import org.opennms.poc.graph.api.Query;
import org.opennms.poc.graph.api.Vertex;
import org.opennms.poc.graph.api.listener.GraphChangeStartedEvent;
import org.opennms.poc.graph.api.listener.GraphChangedFinishedEvent;
import org.opennms.poc.graph.api.listener.GraphListener;
import org.opennms.poc.graph.api.persistence.GraphRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

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
        private GraphListener listener;
    }

    private class GraphProviderEntity {
        private Namespace namespace;
        private GraphProvider provider;
    }

    private List<GraphProviderEntity> providers = new ArrayList<>();

    private List<GraphListenerEntity> listeners = new ArrayList<>();

    @Autowired
    private GraphRepository graphRepository;

    public void onBind(GraphListener listener, Map properties) {
        final GraphListenerEntity entity = new GraphListenerEntity();
        entity.listener = listener;
        entity.namespace = new Namespace((String) properties.getOrDefault("namespace", "*"));
        listeners.add(entity);
    }

    public void onUnbind(GraphListener listener, Map properties) {
        listeners.removeAll(
                listeners.stream().filter(e -> e.listener == listener).collect(Collectors.toList())
        );
    }

    // OSGi-Hook
    public void onBind(GraphProvider provider, Map properties) {
        final GraphProviderEntity entity = new GraphProviderEntity();
        entity.provider = provider;
        entity.namespace = new Namespace((String) properties.getOrDefault("namespace", "*"));
        entity.provider.provideGraph(graphRepository);
        providers.add(entity);
    }

    // OSGi-Hook
    public void onUnbind(GraphProvider provider, Map properties) {
        final List<GraphProviderEntity> removeMe = providers.stream().filter(e -> e.provider == provider).collect(Collectors.toList());
        removeMe.forEach(e -> e.provider.shutdownHook(graphRepository));
        removeMe.forEach(e -> providers.remove(e));
    }

    @Override
    public List<Graph<? extends Vertex, ? extends Edge<? extends Vertex>>> getGraphs() {
        return graphRepository.findAll()
                .stream()
                .map(p -> graphRepository.findByNamespace(p.getNamespace()))
                .collect(Collectors.toList());
    }

    @Override
    public <V extends Vertex, E extends Edge<V>> Graph<V, E> getGraph(String namespace) {
        return (Graph<V, E>) graphRepository.findByNamespace(namespace);
    }

    @Override
    public <V extends Vertex, E extends Edge<V>> Graph<V, E> getSnapshot(Query query) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void sendGraphChangeStartedEvent(GraphChangeStartedEvent event) {
        getListeners(event.getNamespace()).forEach(listener -> listener.handleGraphChangeStartEvent(event));
    }

    @Override
    public void sendGraphChangeFinishedEvent(GraphChangedFinishedEvent event) {
        getListeners(event.getNamespace()).forEach(listener -> listener.handleGraphChangeEndEvent(event));
    }

    @Override
    public void sendVertexAddedEvent(Vertex... vertices) {
        final Map<String, List<Vertex>> collect = Arrays.asList(vertices).stream().collect(Collectors.toMap(v -> v.getNamespace(), v -> Lists.newArrayList(v), (vertices1, vertices2) -> {
            final List<Vertex> list = new ArrayList<>(vertices1);
            list.addAll(vertices2.stream().filter(v -> !vertices1.contains(v)).collect(Collectors.toList()));
            return list;
        }));
        for (Map.Entry<String, List<Vertex>> eachEntry : collect.entrySet()) {
            getListeners(eachEntry.getKey()).forEach(l -> l.handleNewVertices(eachEntry.getValue().toArray(new Vertex[eachEntry.getValue().size()])));
        }
    }

    @Override
    public void sendEdgesAddedEvent(Edge... edges) {
        final Map<String, List<Edge>> collect = Arrays.asList(edges).stream().collect(Collectors.toMap(e -> e.getNamespace(), e -> Lists.newArrayList(e), (edges1, edges2) -> {
            final List<Edge> list = new ArrayList<>(edges1);
            list.addAll(edges2.stream().filter(v -> !edges1.contains(v)).collect(Collectors.toList()));
            return list;
        }));
        for (Map.Entry<String, List<Edge>> eachEntry : collect.entrySet()) {
            getListeners(eachEntry.getKey()).forEach(l -> l.handleNewEdges(eachEntry.getValue().toArray(new Edge[eachEntry.getValue().size()])));
        }
    }

    @Override
    public GraphRepository getGraphRepository() {
        return graphRepository;
    }

    private List<GraphListener> getListeners(String namespace) {
        return listeners.stream()
                .filter(entity -> entity.namespace.matches(namespace))
                .map(entity -> entity.listener)
                .collect(Collectors.toList());
    }

}
