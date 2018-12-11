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
import org.junit.runner.RunWith;
import org.opennms.poc.graph.impl.enrichment.ComputedVertexExample;
import org.opennms.poc.graph.impl.enrichment.EnrichmentProcessor;
import org.opennms.poc.graph.impl.enrichment.NodeSeverity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
// TODO MVR define test scope little smaller (only enrichment processor + compution is required)
public class ComputedVertexTest {

    @Autowired
    private EnrichmentProcessor processor;

    @Test
    public void verifyComputed() {
        final ComputedVertexExample vertex = new ComputedVertexExample("1");
        Assert.assertNull(vertex.getDescription());
        Assert.assertNull(vertex.getField1());

        processor.enrich(vertex);

        Assert.assertEquals("Enriched Description", vertex.getDescription());
        Assert.assertEquals(42L, (long) vertex.getField1());
        Assert.assertEquals(NodeSeverity.Major, vertex.getStatus());
    }
}
