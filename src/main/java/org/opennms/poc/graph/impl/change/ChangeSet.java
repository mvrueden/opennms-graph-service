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

package org.opennms.poc.graph.impl.change;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.opennms.poc.graph.api.Edge;
import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.Vertex;

public class ChangeSet<V extends Vertex, E extends Edge<V>> {
    private final String namespace;
    private final Date changeDate;
    private List<V> verticesAdded = new ArrayList<>();
    private List<V> verticesRemoved = new ArrayList<>();
    private List<V> verticesUpdated = new ArrayList<>();
    private List<E> edgesAdded = new ArrayList<>();
    private List<E> edgesRemoved = new ArrayList<>();
    private List<E> edgesUpdated = new ArrayList<>();

    private boolean graphInfoChanged;

    public ChangeSet(String namespace, Date changeDate) {
        this.namespace = Objects.requireNonNull(namespace);
        this.changeDate = Objects.requireNonNull(changeDate);
    }

    public void vertexAdded(V vertex) {
        verticesAdded.add(vertex);
    }

    public void vertexRemoved(V vertex) {
        verticesRemoved.add(vertex);
    }

    public void vertexUpdated(V vertex) {
        verticesUpdated.add(vertex);
    }

    public void edgeAdded(E edge) {
        edgesAdded.add(edge);
    }

    public void edgeRemoved(E edge) {
        edgesRemoved.add(edge);
    }

    public void edgeUpdated(E edge) {
        edgesUpdated.add(edge);
    }

    public void graphInfoChanged(boolean infoChanged) {
        graphInfoChanged = infoChanged;
    }

    public String getNamespace() {
        return namespace;
    }

    public Date getChangeDate() {
        return changeDate;
    }

    public List<V> getVerticesAdded() {
        return verticesAdded;
    }

    public List<V> getVerticesRemoved() {
        return verticesRemoved;
    }

    public List<V> getVerticesUpdated() {
        return verticesUpdated;
    }

    public List<E> getEdgesAdded() {
        return edgesAdded;
    }

    public List<E> getEdgesRemoved() {
        return edgesRemoved;
    }

    public List<E> getEdgesUpdated() {
        return edgesUpdated;
    }

    public boolean hasGraphInfoChanged() {
        return graphInfoChanged;
    }

    public void detectChanges(Graph<V, E> oldGraph, Graph<V, E> newGraph) {
        if (oldGraph == null && newGraph == null) return;
        if (oldGraph == null && newGraph != null) {
            newGraph.getVertices().forEach(v -> vertexAdded(v));
            newGraph.getEdges().forEach(e -> edgeAdded(e));
        }
        if (oldGraph != null && newGraph == null) {
            oldGraph.getVertices().forEach(v -> vertexRemoved(v));
            oldGraph.getEdges().forEach(e -> edgeRemoved(e));
        }
        if (oldGraph != null && newGraph != null) {
            if (!oldGraph.getNamespace().equalsIgnoreCase(newGraph.getNamespace())) {
                throw new IllegalStateException("Cannot detect changes between different namespaces");
            }
            detectVertexChanges(oldGraph, newGraph);
            detectEdgeChanges(oldGraph, newGraph);
        }
    }

    public boolean hasChanges() {
        return hasGraphInfoChanged()
                || !edgesAdded.isEmpty()
                || !edgesRemoved.isEmpty()
                || !edgesUpdated.isEmpty()
                || !verticesAdded.isEmpty()
                || !verticesRemoved.isEmpty()
                || !verticesUpdated.isEmpty();
    }

    protected void detectVertexChanges(Graph<V, E> oldGraph, Graph<V, E> newGraph) {
        // Find all vertices/edges which are in the old and new graph
        final List<String> oldVertexIds = new ArrayList<>(oldGraph.getVertexIds());
        final List<String> newVertexIds = new ArrayList<>(newGraph.getVertexIds());

        // Detect removed vertices
        final List<String> removedVertices = new ArrayList<>(oldVertexIds);
        removedVertices.removeAll(newVertexIds);
        removedVertices.forEach(id -> vertexRemoved(oldGraph.getVertex(id)));

        // Detect added vertices
        final List<String> addedVertices = new ArrayList<>(newVertexIds);
        addedVertices.removeAll(oldVertexIds);
        addedVertices.forEach(id -> vertexAdded(newGraph.getVertex(id)));

        // Detect updated vertices
        final List<String> sharedVertcies = new ArrayList<>(newVertexIds);
        sharedVertcies.removeAll(removedVertices);
        sharedVertcies.removeAll(addedVertices);
        sharedVertcies.stream().forEach(id -> {
            V oldVertex = oldGraph.getVertex(id);
            V newVertex = newGraph.getVertex(id);
            if (!oldVertex.equals(newVertex)) {
                vertexUpdated(newVertex);
            }
        });
    }

    protected void detectEdgeChanges(Graph<V, E> oldGraph, Graph<V, E> newGraph) {
        // Find all vertices/edges which are in the old and new graph
        final List<String> oldEdgeIds = new ArrayList<>(oldGraph.getEdgeIds());
        final List<String> newEdgeIds = new ArrayList<>(newGraph.getEdgeIds());

        // Detect removed vertices
        final List<String> removedEdges = new ArrayList<>(oldEdgeIds);
        removedEdges.removeAll(newEdgeIds);
        removedEdges.forEach(id -> edgeRemoved(oldGraph.getEdge(id)));

        // Detect added vertices
        final List<String> addedEdges = new ArrayList<>(newEdgeIds);
        addedEdges.removeAll(oldEdgeIds);
        addedEdges.forEach(id -> edgeAdded(newGraph.getEdge(id)));

        // Detect updated vertices
        final List<String> sharedEdges = new ArrayList<>(newEdgeIds);
        sharedEdges.removeAll(removedEdges);
        sharedEdges.removeAll(addedEdges);
        sharedEdges.stream().forEach(id -> {
            E oldEdge = oldGraph.getEdge(id);
            E newEdge = newGraph.getEdge(id);
            if (!oldEdge.equals(newEdge)) {
                edgeUpdated(newEdge);
            }
        });
    }
}
