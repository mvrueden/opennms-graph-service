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

package org.opennms.poc.graph.impl.graphml;

import java.io.InputStream;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.opennms.features.graphml.model.GraphML;
import org.opennms.features.graphml.model.GraphMLGraph;
import org.opennms.features.graphml.model.GraphMLReader;
import org.opennms.features.graphml.model.InvalidGraphException;
import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.GraphContainer;
import org.opennms.poc.graph.api.GraphContainerProvider;
import org.opennms.poc.graph.api.GraphNotificationService;
import org.opennms.poc.graph.api.generic.GenericEdge;
import org.opennms.poc.graph.api.generic.GenericGraph;
import org.opennms.poc.graph.api.generic.GenericProperties;
import org.opennms.poc.graph.api.generic.GenericVertex;
import org.opennms.poc.graph.api.info.DefaultGraphContainerInfo;
import org.opennms.poc.graph.api.info.GraphContainerInfo;
import org.opennms.poc.graph.api.meta.DefaultGraphContainer;

public class GraphmlGraphContainerProvider implements GraphContainerProvider {

    private final GraphML graphML;
    private DefaultGraphContainer graphContainer;

    public GraphmlGraphContainerProvider(InputStream inputStream) throws InvalidGraphException {
        this(GraphMLReader.read(inputStream));
    }

    public GraphmlGraphContainerProvider(GraphML graphML) {
        this.graphML = Objects.requireNonNull(graphML);
        // This should not be invoked at this point, however it is static anyways and in order
        // to know the graph infos we must read the data.
        // Maybe we can just read it partially at some point, however this is how it is implemented for now
        loadGraphContainer();
    }

    @Override
    public void setNotificationService(GraphNotificationService notificationService) {

    }

    @Override
    public GraphContainer loadGraphContainer() {
        if (graphContainer == null) {
            final DefaultGraphContainerInfo info = new DefaultGraphContainerInfo(UUID.randomUUID().toString());
            info.setLabel(graphML.getProperty(GenericProperties.LABEL));
            info.setDescription( graphML.getProperty(GenericProperties.DESCRIPTION));
            final DefaultGraphContainer graphContainer = new DefaultGraphContainer(info);
            for (GraphMLGraph eachGraph : graphML.getGraphs()) {
                final Graph<GenericVertex, GenericEdge> convertedGraph = convert(eachGraph);
                graphContainer.addGraph(convertedGraph);
            }
            this.graphContainer = graphContainer;
        }
        return this.graphContainer;
    }

    @Override
    public GraphContainerInfo getContainerInfo() {
        // AS this is static content, the container info is already part of the graph, no extra setup required
        // TODO MVR maybe we should partially read this while instantiating and then implement the full loading,
        // But as this is already al lin memory anyways we can just do the conversion when instantiating. At least for now
        return graphContainer.getInfo();
    }

    private static final Graph<GenericVertex, GenericEdge> convert(GraphMLGraph graphMLGraph) {
        final GenericGraph graph = new GenericGraph(graphMLGraph.getProperties());
        final List<GenericVertex> vertices = graphMLGraph.getNodes()
                .stream().map(n -> {
                    // In case of GraphML each vertex does not have a namespace, but it is inherited from the graph
                    // Therefore here we have to manually set it
                    final GenericVertex v = new GenericVertex(n.getProperties());
                    v.setNamespace(graph.getNamespace());
                    return v;
                })
                .collect(Collectors.toList());
        graph.addVertices(vertices);

        final List<GenericEdge> edges = graphMLGraph.getEdges().stream().map(e -> {
            final GenericVertex source = graph.getVertex(e.getSource().getId());
            final GenericVertex target = graph.getVertex(e.getTarget().getId());
            final GenericEdge edge = new GenericEdge(source, target);
            edge.setProperties(e.getProperties());

            // In case of GraphML each edge does not have a namespace, but it is inherited from the graph
            // Therefore here we have to manually set it
            edge.setNamespace(graph.getNamespace());
            return edge;
        }).collect(Collectors.toList());
        graph.addEdges(edges);
        return graph;
    }
}
