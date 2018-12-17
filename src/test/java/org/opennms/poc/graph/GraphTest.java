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

package org.opennms.poc.graph;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;
import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.info.DefaultGraphInfo;
import org.opennms.poc.graph.api.info.GraphInfo;
import org.opennms.poc.graph.api.simple.SimpleEdge;
import org.opennms.poc.graph.api.simple.SimpleGraph;
import org.opennms.poc.graph.api.simple.SimpleVertex;

import com.google.common.collect.Lists;

public class GraphTest {

    private static final String NAMESPACE = "DUMMY";

    @Test
    public void verifyGetSnapshotSimple() {
        final GraphInfo graphInfo = new DefaultGraphInfo(NAMESPACE, SimpleVertex.class);
        final SimpleGraph<SimpleVertex, SimpleEdge<SimpleVertex>> graph = new SimpleGraph<>(NAMESPACE);
        graph.applyInfo(graphInfo);
        graph.addVertex(new SimpleVertex(NAMESPACE, "1"));
        graph.addVertex(new SimpleVertex(NAMESPACE, "2"));
        graph.addEdge(new SimpleEdge<>(graph.getVertex("1"), graph.getVertex("2")));

        // Verify that empty works
        for (int szl=0; szl<10; szl++) {
            final Graph<SimpleVertex, SimpleEdge<SimpleVertex>> snapshot = graph.getSnapshot(Lists.newArrayList(), szl);
            assertEquals(Boolean.TRUE, snapshot.getVertices().isEmpty());
            assertEquals(Boolean.TRUE, snapshot.getEdges().isEmpty());
        }

        // Now verify with actual data
        final List<SimpleVertex> focussedVertices = Lists.newArrayList(graph.getVertex("2"));
        for (int szl=1; szl<10; szl++) {
            final Graph<SimpleVertex, SimpleEdge<SimpleVertex>> snapshot = graph.getSnapshot(focussedVertices, szl);

            // TODO MVR we cannot implement equals as we would access computed/enriched fields when doing so
            // And that kinda contradicts the whole purpose of the lazy loadiness. However we could implement
            // equals by ignoring the computed fields. For now we just manually check for what we want
            assertThat(snapshot.getNamespace(), is(NAMESPACE));
            assertThat(snapshot.getVertexIds(), hasItems("1", "2"));
            assertThat(snapshot.getEdges(), hasSize(1));
        }
    }

    @Test
    public void verifyGetSnapshotComplex() {
        final GraphInfo graphInfo = new DefaultGraphInfo(NAMESPACE, SimpleVertex.class);
        final SimpleGraph<SimpleVertex, SimpleEdge<SimpleVertex>> graph = new SimpleGraph<>(NAMESPACE);
        graph.applyInfo(graphInfo);
        for (int i=1; i<=9; i++) {
            graph.addVertex(new SimpleVertex(NAMESPACE, Integer.toString(i)));
        }
        graph.addEdge(new SimpleEdge<>(graph, "1", "2"));
        graph.addEdge(new SimpleEdge<>(graph, "2", "3"));
        graph.addEdge(new SimpleEdge<>(graph, "2", "4"));
        graph.addEdge(new SimpleEdge<>(graph, "3", "6"));
        graph.addEdge(new SimpleEdge<>(graph, "5", "6"));
        graph.addEdge(new SimpleEdge<>(graph, "5", "7"));
        graph.addEdge(new SimpleEdge<>(graph, "7", "8"));
        graph.addEdge(new SimpleEdge<>(graph, "8", "9"));

        // retrieve snapshot
        Graph<SimpleVertex, SimpleEdge<SimpleVertex>> snapshot = graph.getSnapshot(Lists.newArrayList(graph.getVertex("1"), graph.getVertex("5")), 0);
        assertThat(snapshot.getVertices(), hasItems(graph.getVertex("1"), graph.getVertex("5")));
        assertEquals(Boolean.TRUE, snapshot.getEdges().isEmpty());

        // Increase szl to 1
        snapshot = graph.getSnapshot(Lists.newArrayList(graph.getVertex("1"), graph.getVertex("5")), 1);
        assertThat(snapshot.getVertexIds(), hasItems("1", "2", "5", "6", "7"));
        assertThat(snapshot.getEdgeIds(), hasItems("1->2", "5->6", "5->7"));

        // Increase szl to 2
        snapshot = graph.getSnapshot(Lists.newArrayList(graph.getVertex("1"), graph.getVertex("5")), 2);
        assertThat(snapshot.getVertexIds(), hasItems("1", "2", "3", "4", "5", "6", "7", "8"));

        // Increase szl to 3
        snapshot = graph.getSnapshot(Lists.newArrayList(graph.getVertex("1"), graph.getVertex("5")), 3);
        assertThat(snapshot.getVertexIds(), hasItems("1", "2", "3", "4", "5", "6", "7", "8", "9"));
        assertThat(snapshot.getEdgeIds(), hasItems("1->2", "2->3", "2->4", "5->6", "5->7", "7->8", "8->9"));

    }


}
