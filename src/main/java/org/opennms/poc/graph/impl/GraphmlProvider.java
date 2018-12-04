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

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.features.graphml.model.GraphML;
import org.opennms.features.graphml.model.GraphMLGraph;
import org.opennms.features.graphml.model.GraphMLReader;
import org.opennms.features.graphml.model.InvalidGraphException;
import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.GraphProvider;
import org.opennms.poc.graph.api.generic.GenericEdge;
import org.opennms.poc.graph.api.generic.GenericGraph;
import org.opennms.poc.graph.api.generic.GenericVertex;

public class GraphmlProvider implements GraphProvider<GenericVertex, GenericEdge> {

    private final Graph graph;

    public GraphmlProvider(InputStream inputStream) throws InvalidGraphException {
        final GraphML graphML = GraphMLReader.read(inputStream);
        // TODO MVR handle multiple graphs properly

        // TODO MVR NPE...
        final GraphMLGraph graphMLGraph = graphML.getGraphs().get(1);
        final Graph<GenericVertex, GenericEdge> graph = new GenericGraph(graphMLGraph.getProperties());
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
        this.graph = graph;
    }

//    @Override
//    public String getNamespace() {
//        return graph.getNamespace();
//    }
//
//    @Override
//    public Graph getGraph() {
//        return graph;
//    }


    @Override
    public Graph getGraph() {
        return graph;
    }

}
