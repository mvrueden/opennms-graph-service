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

package org.opennms.poc.graph.api.generic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.poc.graph.api.Graph;

// TODO MVR enforce namespace
public class GenericGraph extends AbstractElement implements Graph<GenericVertex, GenericEdge> {

    private final List<GenericVertex> vertices = new ArrayList<>();
    private final List<GenericEdge> edges = new ArrayList<>();
    private final Map<String, GenericVertex> vertexToIdMap = new HashMap<>();
    private final Map<String, GenericEdge> edgeToIdMap = new HashMap<>();

    public GenericGraph() {}

    public GenericGraph(Map<String, Object> properties) {
        setProperties(properties);
    }

    @Override
    public List<GenericVertex> getVertices() {
        return Collections.unmodifiableList(vertices);
    }

    @Override
    public List<GenericEdge> getEdges() {
        return Collections.unmodifiableList(edges);
    }

    @Override
    public GenericVertex getVertex(String id) {
        return vertexToIdMap.get(id);
    }

    @Override
    public String getLabel() {
        return getProperty(GenericProperties.LABEL);
    }

    @Override
    public String getDescription() {
        return getProperty(GenericProperties.DESCRIPTION);
    }

    @Override
    public GenericEdge getEdge(String id) {
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
    public void addEdges(List<GenericEdge> edges) {
        for (GenericEdge eachEdge : edges) {
            addEdge(eachEdge);
        }
    }

    @Override
    public void addVertices(List<GenericVertex> vertices) {
        for (GenericVertex eachVertex : vertices) {
            addVertex(eachVertex);
        }
    }

    @Override
    public void addVertex(GenericVertex vertex) {
        Objects.requireNonNull(vertex);
        Objects.requireNonNull(vertex.getId());
        vertexToIdMap.put(vertex.getId(), vertex);
        vertices.add(vertex);
    }

    @Override
    public void addEdge(GenericEdge edge) {
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
    public void removeEdge(GenericEdge edge) {
        Objects.requireNonNull(edge);
        edgeToIdMap.remove(edge.getId());
        edges.remove(edge);
    }

    @Override
    public void removeVertex(GenericVertex vertex) {
        Objects.requireNonNull(vertex);
        vertexToIdMap.remove(vertex.getId());
        vertices.remove(vertex);
    }

    @Override
    public void setId(String id) {
        setNamespace(id);
    }

    @Override
    public void setNamespace(String namespace) {
        super.setNamespace(namespace);
        super.setId(namespace);
    }

    @Override
    public GenericGraph asGenericGraph() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GenericGraph that = (GenericGraph) o;
        return Objects.equals(vertices, that.vertices) &&
                Objects.equals(edges, that.edges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), vertices, edges);
    }
}
