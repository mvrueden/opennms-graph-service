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

package org.opennms.poc.graph.impl.vmware;

import java.util.Objects;

import org.opennms.poc.graph.api.Edge;
import org.opennms.poc.graph.api.Vertex;
import org.opennms.poc.graph.api.generic.GenericEdge;
import org.opennms.poc.graph.api.generic.GenericGraph;
import org.opennms.poc.graph.api.generic.GenericVertex;
import org.opennms.poc.graph.api.listener.GraphChangeStartedEvent;
import org.opennms.poc.graph.api.listener.GraphChangedFinishedEvent;
import org.opennms.poc.graph.api.listener.GraphListener;
import org.opennms.poc.graph.api.persistence.GraphRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VmwareGraphListener implements GraphListener {

    private final static Logger LOG = LoggerFactory.getLogger(VmwareGraphListener.class);
    private final GraphRepository graphRepository;
    private GenericGraph graph;

    public VmwareGraphListener(GraphRepository graphRepository) {
        this.graphRepository = Objects.requireNonNull(graphRepository);
    }

    @Override
    public void handleGraphChangeStartEvent(GraphChangeStartedEvent startedEvent) {
        if (startedEvent.getNamespace().equalsIgnoreCase("vmware")) {
            LOG.info("Graph Creation started");
            graph = new GenericGraph();
            graph.setNamespace(startedEvent.getNamespace() + ".listener");
        }
    }

    @Override
    public void handleGraphChangeEndEvent(GraphChangedFinishedEvent finishedEvent) {
        if (finishedEvent.getNamespace().equalsIgnoreCase("vmware")) {
            LOG.info("Graph Creation finished: {}", finishedEvent.isSuccess());
            if (finishedEvent.isSuccess()) {
                graphRepository.save(graph);
            } else {
                // it was not successful, so we don't do anything
            }
        }
    }

    @Override
    public void handleNewVertices(Vertex... vertices) {
        LOG.info("Vertex added: {}", vertices);
        for (Vertex eachVertex : vertices) {
            if (eachVertex.getNamespace().equalsIgnoreCase("vmware")) {
                final GenericVertex v = eachVertex.asGenericVertex();
                v.setNamespace(graph.getNamespace());
                graph.addVertex(v);
            }
        }
    }

    @Override
    public void handleNewEdges(Edge... edges) {
        LOG.info("Edge added {}", edges);
        for (Edge eachEdge : edges) {
            if (eachEdge.getNamespace().equalsIgnoreCase("vmware")) {
                final GenericEdge genericEdge = eachEdge.asGenericEdge();
                genericEdge.setNamespace(graph.getNamespace());
                graph.addEdge(genericEdge);
            }
        }
    }
}
