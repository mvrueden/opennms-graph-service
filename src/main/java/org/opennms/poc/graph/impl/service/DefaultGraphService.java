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

package org.opennms.poc.graph.impl.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;

import org.opennms.poc.graph.api.Edge;
import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.GraphContainer;
import org.opennms.poc.graph.api.GraphContainerProvider;
import org.opennms.poc.graph.api.GraphProvider;
import org.opennms.poc.graph.api.GraphService;
import org.opennms.poc.graph.api.Query;
import org.opennms.poc.graph.api.Vertex;
import org.opennms.poc.graph.api.info.GraphContainerInfo;
import org.opennms.poc.graph.api.listener.GraphChangeListener;
import org.opennms.poc.graph.api.listener.GraphChangeSetListener;
import org.opennms.poc.graph.api.search.GraphSearchService;
import org.opennms.poc.graph.api.search.SearchCriteria;
import org.opennms.poc.graph.api.search.SearchProvider;
import org.opennms.poc.graph.api.search.SearchSuggestion;
import org.opennms.poc.graph.impl.change.ChangeSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// How would this listen to events in the first place?
@Service
public class DefaultGraphService implements GraphService, GraphSearchService {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultGraphService.class);

    private class Entity<T> {
        private Namespace namespace;
        private T listener;
    }

    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private List<GraphContainerHandle> providers = new ArrayList<>();
    private List<Entity<GraphChangeSetListener<?, ?>>> graphChangeSetListenerEntities = new ArrayList<>();
    private List<Entity<GraphChangeListener<?, ?>>> graphChangeListenerEntities = new ArrayList<>();

    @Autowired
    private List<SearchProvider> searchProviders;

    @PreDestroy
    public void preDestroy() {
        executorService.shutdown(); // TODO MVR do we really want to wait for completion of jobs before shutting down?!
    }

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
    public void onBind(GraphContainerProvider provider, Map properties) {
        Objects.requireNonNull(provider);
        LOG.info("New GraphContainerProvider registered {}", provider.getContainerInfo());

        final GraphContainerHandle graphContainerHandle = new GraphContainerHandle(executorService, provider);
        graphContainerHandle.setNotificationService(this);
        providers.add(graphContainerHandle);
        graphContainerHandle.initialize();

        // TODO MVR this will probably not really work in osgi-context
        // TODO MVR this only listens to Graph changes, but now we have the concept of a GraphContainerChange :(
        if (provider instanceof GraphChangeListener) {
            onBind((GraphChangeListener) provider, properties);
        }
        // TODO MVR this will probably not really work in osgi-context
        // TODO MVR this only listens to Graph changes, but now we have the concept of a GraphContainerChange :(
        if (provider instanceof GraphChangeSetListener) {
            onBind((GraphChangeSetListener) provider, properties);
        }
    }

    public void onUnbind(GraphContainerProvider provider, Map properties) {
        // TODO MVR here we should probably remove the handle instead of the provider...
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

    // TODO MVR keep or remove?
    public void onBind(GraphProvider provider, Map properties) {
        if (!(provider instanceof GraphContainerProvider)) {
            throw new IllegalStateException("Provider must also implement GraphContainerProvider interface");
        }
        onBind((GraphContainerProvider) provider, properties);
    }

    // OSGi-Hook
    // TODO MVR keep or remove?
    public void onUnbind(GraphProvider provider, Map properties) {
       onUnbind((GraphContainerProvider) provider, properties);
    }

    @Override
    public List<GraphContainerInfo> getGraphContainerDetails() {
        final List<GraphContainerInfo> collect = providers.stream()
                .filter(GraphContainerHandle::isReady)
                .map(gc -> gc.getContainerInfo())
                .collect(Collectors.toList());
        return collect;
    }

    @Override
    public GraphContainer getGraphContainer(String id) {
        final Optional<GraphContainerHandle> first = providers.stream()
                .filter(GraphContainerHandle::isReady)
                .filter(provider -> provider.getContainerInfo() != null && id.equals(provider.getContainerInfo().getId()))
                .findFirst();
        if (first.isPresent()) {
            return first.get().loadGraphContainer();
        }
        return null;
    }

    @Override
    public <V extends Vertex, E extends Edge<V>> Graph<V, E> getGraph(String namespace) {
        final Optional<GraphContainerHandle> first = providers.stream().filter(p -> p.getContainerInfo().getGraphInfo(namespace) != null).findFirst();
        if (first.isPresent()) {
            final GraphContainer graphContainer = first.get().loadGraphContainer();
            return (Graph<V, E>) graphContainer.getGraph(namespace);
        }
        return null;
    }

    @Override
    public <V extends Vertex, E extends Edge<V>> Graph<V, E> getGraph(String containerId, String graphNamespace) {
        final Optional<GraphContainer> containerOptional = Optional.ofNullable(getGraphContainer(containerId));
        if (containerOptional.isPresent()) {
            return (Graph<V, E>) containerOptional.get().getGraph(graphNamespace);
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

    @Override
    public List<SearchSuggestion> getSuggestions(String namespace, String input) {
        return searchProviders.stream().filter(provider -> provider.canSuggest(namespace))
                .flatMap(provider -> provider.getSuggestions(this, namespace, input).stream())
                .collect(Collectors.toList());
    }

    @Override
    public List<Vertex> search(SearchCriteria searchCriteria) {
        return searchProviders.stream().filter(provider -> provider.canResolve(searchCriteria.getProviderId()))
                .flatMap(provider -> provider.resolve(this, searchCriteria).stream())
                .collect(Collectors.toList());
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
