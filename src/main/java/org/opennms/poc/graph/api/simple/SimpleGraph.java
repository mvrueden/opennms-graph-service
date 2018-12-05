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

package org.opennms.poc.graph.api.simple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.poc.graph.api.Edge;
import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.Vertex;
import org.opennms.poc.graph.api.generic.GenericGraph;
import org.opennms.poc.graph.api.generic.GenericProperties;

// TODO MVR enforce namespace
// TODO MVR this is basically a copy of GenericGraph :'(
// TODO MVR implement duplication detection (e.g. adding same vertex twice
// and as well as adding different edges with different source/target vertices, should add each vertex only once,
// maybe not here, but at some point that check should be implemented)
public class SimpleGraph<V extends SimpleVertex, E extends SimpleEdge<V>> implements Graph<V, E> {
    private final List<V> vertices = new ArrayList<>();
    private final List<E> edges = new ArrayList<>();
    private final String namespace;

    private String label;
    private String description;

    private final Map<String, V> vertexToIdMap = new HashMap<>();
    private final Map<String, E> edgeToIdMap = new HashMap<>();

    public SimpleGraph(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public List<V> getVertices() {
        return Collections.unmodifiableList(vertices);
    }

    @Override
    public List<E> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void addEdges(List<E> edges) {
        for (E eachEdge : edges) {
            addEdge(eachEdge);
        }
    }

    @Override
    public void addVertices(List<V> vertices) {
        for (V eachVertex : vertices) {
            addVertex(eachVertex);
        }
    }


    @Override
    public void addVertex(V vertex) {
        Objects.requireNonNull(vertex);
        Objects.requireNonNull(vertex.getId());
        vertexToIdMap.put(vertex.getId(), vertex);
        vertices.add(vertex);
    }

    @Override
    public void addEdge(E edge) {
        Objects.requireNonNull(edge);
        Objects.requireNonNull(edge.getId());
        if (edge.getSource().getNamespace().equalsIgnoreCase(getNamespace()) && getVertex(edge.getSource().getId()) == null) {
            addVertex(edge.getSource());
        }
        if (edge.getTarget().getNamespace().equalsIgnoreCase(getNamespace()) && getVertex(edge.getTarget().getId()) == null) {
            addVertex(edge.getTarget());
        }
        edgeToIdMap.put(edge.getId(), edge);
        edges.add(edge);
    }

    @Override
    public void removeEdge(E edge) {
        Objects.requireNonNull(edge);
        edgeToIdMap.remove(edge.getId());
        edges.remove(edge);
    }

    @Override
    public void removeVertex(V vertex) {
        Objects.requireNonNull(vertex);
        vertexToIdMap.remove(vertex.getId());
        vertices.remove(vertex);
    }

    @Override
    public V getVertex(String id) {
        return getVertices().stream().filter(v -> v.getId().equals(id)).findAny().orElse(null);
    }

    @Override
    public E getEdge(String id) {
        return edgeToIdMap.get(id);
    }

    @Override
    public List<String> getVertexIds() {
        return vertexToIdMap.keySet().stream().sorted().collect(Collectors.toList());
    }

    @Override
    public List<String> getEdgeIds() {
        return edgeToIdMap.keySet().stream().sorted().collect(Collectors.toList());
    }


    @Override
    public GenericGraph asGenericGraph() {
        final GenericGraph graph = new GenericGraph();
        graph.setNamespace(getNamespace());
        graph.setProperty(GenericProperties.LABEL, getLabel());
        graph.setProperty(GenericProperties.DESCRIPTION, getDescription());
        getVertices().stream().map(Vertex::asGenericVertex).forEach(graph::addVertex);
        getEdges().stream().map(Edge::asGenericEdge).forEach(graph::addEdge);
        return graph;
    }

}
