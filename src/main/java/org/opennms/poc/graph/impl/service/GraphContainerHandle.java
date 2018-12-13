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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.opennms.poc.graph.api.GraphContainer;
import org.opennms.poc.graph.api.GraphContainerProvider;
import org.opennms.poc.graph.api.GraphNotificationService;
import org.opennms.poc.graph.api.info.GraphContainerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphContainerHandle implements GraphContainerProvider {

    private static final Logger LOG = LoggerFactory.getLogger(GraphContainerHandle.class);

    private final ExecutorService executorService;
    private final GraphContainerProvider provider;
    private GraphContainerState state = GraphContainerState.Initializing;
    private GraphContainer cachedGraphContainer;
    private long lastReloadTime;

    public GraphContainerHandle(ExecutorService executorService, GraphContainerProvider provider) {
        this.provider = Objects.requireNonNull(provider);
        this.executorService = Objects.requireNonNull(executorService);
    }

    @Override
    public void setNotificationService(GraphNotificationService notificationService) {
        provider.setNotificationService(notificationService);
    }

    @Override
    public GraphContainer loadGraphContainer() {
        if (state == GraphContainerState.Initializing) {
            throw new IllegalStateException("Cannot read graph as not yet initialized. Please initialize first");
        }
        if (state == GraphContainerState.Error) {
            throw new IllegalStateException("Could not load graph.");
        }
        if (state != GraphContainerState.Reloading  && requireReload()) {
            LOG.warn("Graph {} needs reloading. Triggering reload", getContainerInfo().getNamespaces());
            state = GraphContainerState.Reloading;
            initialize();
        }
        return cachedGraphContainer;
    }

    @Override
    public GraphContainerInfo getContainerInfo() {
        return provider.getContainerInfo();
    }

    // TODO MVR here we reload hard every 5 seconds
    private boolean requireReload() {
        if (lastReloadTime == 0 || System.currentTimeMillis() - lastReloadTime >= 5000) {
            return true;
        }
        return false;
    }

    public void initialize() {
        final List<String> namespaces = provider.getContainerInfo().getNamespaces();
        LOG.info("Initialize loading of graph {}", namespaces);

        // Initialize provider
        final CompletableFuture<GraphContainer> completableFuture = new CompletableFuture<>();
        completableFuture.handle((graphContainer, throwable) -> {
            if (throwable != null) {
                LOG.error("Could not load graph: {}", throwable.getMessage(), throwable);
                state = GraphContainerState.Error;
            }
            if (graphContainer == null) {
                LOG.warn("Received null graph.");
                state = GraphContainerState.Error;
            }
            if (graphContainer != null && throwable == null) {
                LOG.info("GraphContainer loaded: {}({})", graphContainer.getInfo().getLabel(), namespaces);
                state = GraphContainerState.Ready;
            }
            cachedGraphContainer = graphContainer;
            lastReloadTime = System.currentTimeMillis();
            return graphContainer;
        });

        // Run
        executorService.submit(() -> {
            try {
                final GraphContainer graphContainer = provider.loadGraphContainer();
                completableFuture.complete(graphContainer);
            } catch (Exception ex) {
                completableFuture.completeExceptionally(ex);
            }
        });
    }

    public boolean isReady() {
        return state == GraphContainerState.Ready || state == GraphContainerState.Reloading;
    }
}
