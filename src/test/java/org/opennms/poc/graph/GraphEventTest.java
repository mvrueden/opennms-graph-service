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

import org.junit.Assert;
import org.junit.Test;
import org.opennms.poc.graph.api.builder.GraphBuilder;
import org.opennms.poc.graph.api.builder.State;
import org.opennms.poc.graph.api.events.AddEdgeEvent;
import org.opennms.poc.graph.api.events.AddVertexEvent;
import org.opennms.poc.graph.api.events.GraphDiscoveryFinishedEvent;
import org.opennms.poc.graph.api.events.GraphDiscoveryStartedEvent;
import org.opennms.poc.graph.api.simple.SimpleEdge;
import org.opennms.poc.graph.api.simple.SimpleGraph;
import org.opennms.poc.graph.api.simple.SimpleVertex;

import com.google.common.eventbus.EventBus;

public class GraphEventTest {

    private EventBus eventBus = new EventBus();

    private static final String NAMESPACE = "dummy";

    @Test
    public void verifySimpleCreation() {
        final GraphBuilder<SimpleVertex, SimpleEdge<SimpleVertex>> graphBuilder = new GraphBuilder<>(NAMESPACE, (event) -> new SimpleGraph<>(NAMESPACE));

        Assert.assertEquals(State.Undefined, graphBuilder.getState());
        eventBus.register(graphBuilder);
        eventBus.post(new GraphDiscoveryStartedEvent(NAMESPACE));
        Assert.assertEquals(State.Building, graphBuilder.getState());

        eventBus.post(new AddVertexEvent<>(new SimpleVertex(NAMESPACE, "1")));
        Assert.assertEquals(State.Building, graphBuilder.getState());

        eventBus.post(new AddVertexEvent<>(new SimpleVertex(NAMESPACE, "2")));
        Assert.assertEquals(State.Building, graphBuilder.getState());

        eventBus.post(new AddEdgeEvent<>(
                new SimpleEdge<>(
                    new SimpleVertex(NAMESPACE, "3"), new SimpleVertex(NAMESPACE, "4"))));
        Assert.assertEquals(State.Building, graphBuilder.getState());

        eventBus.post(new GraphDiscoveryFinishedEvent(NAMESPACE));
        Assert.assertEquals(State.Finished, graphBuilder.getState());

        Assert.assertEquals(4L, graphBuilder.getGraph().getVertices().size());
        Assert.assertEquals(1L, graphBuilder.getGraph().getEdges().size());
    }

    @Test
    public void verifyTransactionCreation() {

    }
}
