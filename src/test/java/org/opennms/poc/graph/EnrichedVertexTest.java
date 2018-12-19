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

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.poc.graph.impl.enrichment.EnrichedVertexExample;
import org.opennms.poc.graph.impl.enrichment.EnrichmentProcessor;
import org.opennms.poc.graph.impl.enrichment.NodeSeverity;
import org.opennms.poc.graph.impl.enrichment.VertexFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
// TODO MVR define test scope little smaller (only enrichment processor + compution is required)
public class EnrichedVertexTest {

    private int nodeId = 100002; // TODO MVR create in test instead of expecting it to be there

    @Autowired
    private EnrichmentProcessor processor;

    @Autowired
    private VertexFactory vertexFactory;

    @Test
    public void verifyEnrichment() {
        final EnrichedVertexExample vertex = new EnrichedVertexExample("1", nodeId);
        Assert.assertNull(vertex.getNodeInfo());
        Assert.assertNull(vertex.getNodeSeverity());

        processor.enrich(vertex);

        Assert.assertEquals(NodeSeverity.Major, vertex.getNodeSeverity());
        Assert.assertNotNull(vertex.getNodeInfo());
    }

    // The idea is, that an annotated field is automatically populated on access
    @Test
    public void verifyAutoEnrichmentOnAccess() throws NoSuchMethodException, IntrospectionException, InstantiationException, IllegalAccessException, InvocationTargetException {
        final EnrichedVertexExample vertex = vertexFactory.createVertex(EnrichedVertexExample.class, "1", nodeId);
        Assert.assertEquals(NodeSeverity.Major, vertex.getNodeSeverity());
        Assert.assertNotNull(vertex.getNodeInfo());
    }
}
