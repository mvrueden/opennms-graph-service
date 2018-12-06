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

package org.opennms.poc.graph.impl.slow;

import org.opennms.poc.graph.api.Graph;
import org.opennms.poc.graph.api.GraphNotificationService;
import org.opennms.poc.graph.api.GraphProvider;
import org.opennms.poc.graph.api.info.DefaultGraphInfo;
import org.opennms.poc.graph.api.info.GraphInfo;
import org.opennms.poc.graph.api.simple.SimpleEdge;
import org.opennms.poc.graph.api.simple.SimpleGraph;
import org.opennms.poc.graph.api.simple.SimpleVertex;
import org.springframework.stereotype.Service;

import com.google.common.base.Throwables;

@Service
public class SlowGraphProvider implements GraphProvider<SimpleVertex, SimpleEdge<SimpleVertex>> {
    @Override
    public void setNotificationService(GraphNotificationService notificationService) {

    }

    @Override
    public Graph<SimpleVertex, SimpleEdge<SimpleVertex>> loadGraph() {
        try {
            Thread.sleep(30 * 1000);

            final SimpleGraph<SimpleVertex, SimpleEdge<SimpleVertex>> graph = new SimpleGraph<>("slow");
            graph.applyInfo(getGraphInfo());
            graph.addVertex(new SimpleVertex("slow", "v1"));
            graph.addVertex(new SimpleVertex("slow", "v2"));
            return graph;
        } catch (InterruptedException ex) {
            throw Throwables.propagate(ex);
        }
    }

    @Override
    public GraphInfo getGraphInfo() {
        return new DefaultGraphInfo("slow").withLabel("Slow").withDescription("Graph simulating a very slow loading time of 30 seconds");
    }
}
