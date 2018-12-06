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

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.GraphNotificationService;
import org.opennms.poc.graph.api.GraphProvider;
import org.opennms.poc.graph.api.info.GraphInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphHandle implements GraphProvider {

    private static final Logger LOG = LoggerFactory.getLogger(GraphHandle.class);

    private final ExecutorService executorService;
    private final GraphProvider provider;
    private GraphState state = GraphState.Initializing;
    private Graph cachedGraph;
    private long lastReloadTime;

    public GraphHandle(ExecutorService executorService, GraphProvider provider) {
        this.provider = Objects.requireNonNull(provider);
        this.executorService = Objects.requireNonNull(executorService);
    }

    @Override
    public void setNotificationService(GraphNotificationService notificationService) {
        provider.setNotificationService(notificationService);
    }

    @Override
    public Graph loadGraph() {
        if (state == GraphState.Initializing) {
            throw new IllegalStateException("Cannot read graph as not yet initialized. Please initialize first");
        }
        if (state == GraphState.Error) {
            throw new IllegalStateException("Could not load graph.");
        }
        if (state != GraphState.Reloading  && requireReload()) {
            LOG.warn("Graph {} needs reloading. Triggering reload", getGraphInfo().getNamespace());
            state = GraphState.Reloading;
            initialize();
        }
        return cachedGraph;
    }

    // TODO MVR here we reload hard every 5 seconds
    private boolean requireReload() {
        if (lastReloadTime == 0 || System.currentTimeMillis() - lastReloadTime >= 5000) {
            return true;
        }
        return false;
    }

    @Override
    public GraphInfo getGraphInfo() {
        return provider.getGraphInfo();
    }

    public void initialize() {
        LOG.info("Initialize loading of graph {}", provider.getGraphInfo().getNamespace());

        // Initialize provider
        final CompletableFuture<Graph> completableFuture = new CompletableFuture<>();
        completableFuture.handle((graph, throwable) -> {
            if (throwable != null) {
                LOG.error("Could not load graph: {}", throwable.getMessage(), throwable);
                state = GraphState.Error;
            }
            if (graph == null) {
                LOG.warn("Received null graph.");
                state = GraphState.Error;
            }
            if (graph != null && throwable == null) {
                LOG.info("Graph loaded: {}", graph.getNamespace());
                state = GraphState.Ready;
            }
            cachedGraph = graph;
            lastReloadTime = System.currentTimeMillis();
            return graph;
        });

        // Run
        executorService.submit(() -> {
            try {
                final Graph graph = provider.loadGraph();
                completableFuture.complete(graph);
            } catch (Exception ex) {
                completableFuture.completeExceptionally(ex);
            }
        });
    }

    public boolean isReady() {
        return state == GraphState.Ready || state == GraphState.Reloading;
    }
}
