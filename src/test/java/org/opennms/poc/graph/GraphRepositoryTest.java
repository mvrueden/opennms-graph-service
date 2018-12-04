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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.poc.graph.api.generic.GenericEdge;
import org.opennms.poc.graph.api.generic.GenericGraph;
import org.opennms.poc.graph.api.generic.GenericVertex;
import org.opennms.poc.graph.api.persistence.GraphRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GraphRepositoryTest {

    private static final String NAMESPACE = "dummy";

    @Autowired
    private GraphRepository graphRepository;

    // TODO MVR delete by namespace
    // TODO MVR define foreign keys

    @Test
    public void verifyGraphPersistence() {
        assertNull(graphRepository.findByNamespace(NAMESPACE)); // nothing persisted yet

        final GenericGraph graph = new GenericGraph();
        graph.setNamespace(NAMESPACE);
        graph.setProperty("label", "Test Graph");
        graph.setProperty("description", "Test Graph. Yay.");

        final GenericVertex v1 = new GenericVertex(NAMESPACE, "v1");
        v1.setProperty("label", "Vertex 1"); // verify string
        v1.setProperty("status", Response.Status.OK); // Verify enum
        v1.setProperty("number", 10); // Verify int
        v1.setProperty("enabled", true); // Verify boolean

        final GenericVertex v2 = new GenericVertex(NAMESPACE, "v2");
        v2.setProperty("label", "Vertex 2");
        v2.setProperty("status", Response.Status.BAD_REQUEST);
        v2.setProperty("number", 20);
        v2.setProperty("enabled", false);

        graph.addVertex(v1);
        graph.addVertex(v2);
        graph.addEdge(new GenericEdge(v1, v2));

        // TODO MVR persist properties
        graphRepository.save(graph);

        final GenericGraph persistedGraph = graphRepository.findByNamespace(NAMESPACE);
        assertEquals(graph, persistedGraph);
//        System.out.println(graphEntity2);
    }

    // TODO MVR write same test with simpleGraph
}
