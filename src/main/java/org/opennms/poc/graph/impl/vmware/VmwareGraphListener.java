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

import java.util.List;

import org.opennms.poc.graph.api.Edge;
import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.GraphNotificationService;
import org.opennms.poc.graph.api.GraphProvider;
import org.opennms.poc.graph.api.Vertex;
import org.opennms.poc.graph.api.generic.GenericEdge;
import org.opennms.poc.graph.api.generic.GenericGraph;
import org.opennms.poc.graph.api.generic.GenericVertex;
import org.opennms.poc.graph.api.info.DefaultGraphInfo;
import org.opennms.poc.graph.api.info.GraphInfo;
import org.opennms.poc.graph.api.listener.GraphChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VmwareGraphListener implements GraphChangeListener<Vertex, Edge<Vertex>>, GraphProvider {

    private final static Logger LOG = LoggerFactory.getLogger(VmwareGraphListener.class);
    private GenericGraph graph;

    public VmwareGraphListener() {
        graph = new GenericGraph();
        graph.setNamespace("vmware.listener");
    }

    @Override
    public void handleVerticesAdded(List<Vertex> verticesAdded) {
        LOG.info("Vertex added: {}", verticesAdded);
        for (Vertex eachVertex : verticesAdded) {
            if (eachVertex.getNamespace().equalsIgnoreCase("vmware")) {
                final GenericVertex v = eachVertex.asGenericVertex();
                v.setNamespace(graph.getNamespace());
                graph.addVertex(v);
            }
        }
    }

    @Override
    public void handleVerticesRemoved(List<Vertex> verticesRemoved) {

    }

    @Override
    public void handleVerticesUpdated(List<Vertex> verticesUpdated) {

    }

    @Override
    public void handleEdgesAdded(List<Edge<Vertex>> edgesAdded) {
        LOG.info("Edge added {}", edgesAdded);
        for (Edge eachEdge : edgesAdded) {
            if (eachEdge.getNamespace().equalsIgnoreCase("vmware")) {
                final GenericEdge genericEdge = eachEdge.asGenericEdge();
                genericEdge.setNamespace(graph.getNamespace());
                graph.addEdge(genericEdge);
            }
        }
    }

    @Override
    public void handleEdgesUpdated(List<Edge<Vertex>> edgesUpdated) {

    }

    @Override
    public void handleEdgesRemoved(List<Edge<Vertex>> edgesRemoved) {

    }

    @Override
    public void setNotificationService(GraphNotificationService notificationService) {

    }

    @Override
    public Graph loadGraph() {
        return graph;
    }

    @Override
    public GraphInfo getGraphInfo() {
        return new DefaultGraphInfo(graph.getNamespace(), VmwareVertex.class)
                .withLabel("VMware Listener Provider")
                .withDescription("Listens for updates of the VMWare Importer to dynamically build the graph");
    }
}
