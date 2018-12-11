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

package org.opennms.poc.graph.impl.enrichment;

import org.opennms.poc.graph.api.enrichment.Computed;
import org.opennms.poc.graph.api.enrichment.Constant;
import org.opennms.poc.graph.api.enrichment.NodeRef;
import org.opennms.poc.graph.api.info.NodeInfo;
import org.opennms.poc.graph.api.simple.SimpleVertex;

public class ComputedVertexExample extends SimpleVertex {

    public static final String NAMESPACE = "example";

    @NodeRef
    private Integer nodeId = 18;

    @Constant(name = "field1", value = "42")
    private Long field1;

    @Constant(name = "field2", value = "Enriched Description")
    private String description;

    @Computed(name = "node", compution = NodeResolutionCompution.class)
    private NodeInfo nodeInfo;

    @Computed(name = "severity", compution = NodeSeverityComputation.class)
    private NodeSeverity status;

    public ComputedVertexExample(String id) {
        super(NAMESPACE, id);
    }

    public Long getField1() {
        return field1;
    }

    public String getDescription() {
        return description;
    }

    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    public NodeSeverity getStatus() {
        return status;
    }
}
