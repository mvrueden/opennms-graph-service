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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.Vertex;
import org.opennms.poc.graph.api.aware.LocationAware;
import org.opennms.poc.graph.api.aware.NodeAware;
import org.opennms.poc.graph.api.context.DefaultGraphContext;
import org.opennms.poc.graph.api.focus.Focus;
import org.opennms.poc.graph.api.info.NodeInfo;
import org.opennms.poc.graph.impl.SemanticZoomLevelTransformer;
import org.opennms.poc.graph.impl.refs.NodeRef;
import org.opennms.poc.graph.impl.refs.NodeRefs;

import com.google.common.collect.Lists;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

// TODO MVR enforce namespace
public class GenericGraph extends AbstractElement implements Graph<GenericVertex, GenericEdge>, NodeAware, LocationAware {

    private DirectedSparseGraph<GenericVertex, GenericEdge> jungGraph = new DirectedSparseGraph<>();
    private final Map<String, GenericVertex> vertexToIdMap = new HashMap<>();
    private final Map<String, GenericEdge> edgeToIdMap = new HashMap<>();
    private Focus focusStrategy;

    public GenericGraph() {}

    public GenericGraph(String namespace) {
        setNamespace(namespace);
    }

    public GenericGraph(Map<String, Object> properties) {
        setProperties(properties);
    }

    @Override
    public List<GenericVertex> getVertices() {
        // TODO MVR use junggraph.getVetices instead. However addEdge is adding the edges if not in same namespace
        // We have to figure out a workaround for that somehow
        return new ArrayList<>(vertexToIdMap.values());
    }

    @Override
    public List<GenericEdge> getEdges() {
        // TODO MVR use junggraph.getEdges instead. However addEdge is adding the edges if not in same namespace
        // We have to figure out a workaround for that somehow
        return new ArrayList<>(edgeToIdMap.values());
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
    public Class<? extends Vertex> getVertexType() {
        return GenericVertex.class;
    }

    @Override
    public List<Vertex> getDefaultFocus() {
        if (focusStrategy != null) {
            return focusStrategy.getFocus(new DefaultGraphContext(this)).stream().map(vr -> vertexToIdMap.get(vr.getId())).collect(Collectors.toList());
        }
        return Lists.newArrayList();
    }

    @Override
    public String getLocation() {
        return getProperty(GenericProperties.LOCATION);
    }

    @Override
    public NodeRef getNodeRef() {
        String nodeId = getProperty(GenericProperties.NODE_ID);
        String foreignSource = getProperty(GenericProperties.FOREIGN_SOURCE);
        String foreignId = getProperty(GenericProperties.FOREIGN_ID);
        if (nodeId != null) {
            return NodeRefs.from(nodeId);
        } else if (foreignSource != null && foreignId != null) {
            return NodeRefs.from(foreignSource + ":" + foreignId);
        }
        return null;
    }

    @Override
    public NodeInfo getNodeInfo() {
        final Optional<Object> first = getProperties().values().stream().filter(v -> v instanceof NodeInfo).findFirst();
        if (first.isPresent()) {
            return (NodeInfo) first.get();
        }
        return (NodeInfo) getComputedProperties().values().stream().filter(v -> v instanceof NodeInfo).findFirst().orElse(null);
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
    public Graph<GenericVertex, GenericEdge> getSnapshot(Collection<GenericVertex> verticesInFocus, int szl) {
        return new SemanticZoomLevelTransformer<GenericVertex, GenericEdge, GenericGraph>(verticesInFocus, szl).transform(this, () -> new GenericGraph(getProperties()));
    }

    @Override
    public List<GenericVertex> resolveVertices(Collection<String> vertexIds) {
        return vertexIds.stream().map(vid -> vertexToIdMap.get(vid)).filter(v -> v != null).collect(Collectors.toList());
    }

    @Override
    public List<GenericEdge> resolveEdges(Collection<String> edgeIds) {
        return edgeIds.stream().map(eid -> edgeToIdMap.get(eid)).collect(Collectors.toList());
    }

    @Override
    public Collection<GenericVertex> getNeighbors(GenericVertex eachVertex) {
        return jungGraph.getNeighbors(eachVertex);
    }

    @Override
    public Collection<GenericEdge> getConnectingEdges(GenericVertex eachVertex) {
        final Set<GenericEdge> edges = new HashSet<>();
        edges.addAll(jungGraph.getInEdges(eachVertex));
        edges.addAll(jungGraph.getOutEdges(eachVertex));
        return edges;
    }

    @Override
    public void addEdges(Collection<GenericEdge> edges) {
        for (GenericEdge eachEdge : edges) {
            addEdge(eachEdge);
        }
    }

    @Override
    public void addVertices(Collection<GenericVertex> vertices) {
        for (GenericVertex eachVertex : vertices) {
            addVertex(eachVertex);
        }
    }

    @Override
    public void addVertex(GenericVertex vertex) {
        Objects.requireNonNull(vertex);
        Objects.requireNonNull(vertex.getId());
        if (jungGraph.containsVertex(vertex)) return; // already added
        if (vertexToIdMap.containsKey(vertex.getId())) return; // already added
        vertexToIdMap.put(vertex.getId(), vertex);
        jungGraph.addVertex(vertex);
    }

    @Override
    public void addEdge(GenericEdge edge) {
        Objects.requireNonNull(edge);
        Objects.requireNonNull(edge.getId());
        if (jungGraph.containsEdge(edge)) return; // already added
        if (edge.getSource().getNamespace().equalsIgnoreCase(getNamespace()) && getVertex(edge.getSource().getId()) == null) {
            addVertex(edge.getSource());
        }
        if (edge.getTarget().getNamespace().equalsIgnoreCase(getNamespace()) && getVertex(edge.getTarget().getId()) == null) {
            addVertex(edge.getTarget());
        }
        edgeToIdMap.put(edge.getId(), edge);
        jungGraph.addEdge(edge, edge.getSource(), edge.getTarget());
    }

    @Override
    public void removeEdge(GenericEdge edge) {
        Objects.requireNonNull(edge);
        edgeToIdMap.remove(edge.getId());
        jungGraph.removeEdge(edge);
        // TODO MVR remove vertices as well?
    }

    @Override
    public void removeVertex(GenericVertex vertex) {
        Objects.requireNonNull(vertex);
        vertexToIdMap.remove(vertex.getId());
        jungGraph.removeVertex(vertex);
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

    public void setDefaultFocus(Focus focusStrategy) {
        this.focusStrategy = Objects.requireNonNull(focusStrategy);
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
        return Objects.equals(getVertices(), that.getVertices())
                && Objects.equals(getEdges(), that.getEdges())
                && Objects.equals(focusStrategy, that.focusStrategy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getVertices(), getEdges(), focusStrategy);
    }
}
