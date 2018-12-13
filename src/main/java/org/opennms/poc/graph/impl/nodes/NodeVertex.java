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

package org.opennms.poc.graph.impl.nodes;

import org.opennms.netmgt.model.OnmsNode;
import org.opennms.poc.graph.api.enrichment.Enriched;
import org.opennms.poc.graph.api.enrichment.NodeRefAware;
import org.opennms.poc.graph.api.generic.GenericVertex;
import org.opennms.poc.graph.api.info.NodeInfo;
import org.opennms.poc.graph.api.simple.SimpleVertex;
import org.opennms.poc.graph.impl.enrichment.NodeResolutionEnrichment;
import org.opennms.poc.graph.impl.refs.NodeRef;
import org.opennms.poc.graph.impl.refs.NodeRefs;

public class NodeVertex extends SimpleVertex implements NodeRefAware {

    @Enriched(name="node", enrichment = NodeResolutionEnrichment.class)
    public NodeInfo nodeInfo;

    public NodeVertex(OnmsNode n) {
        super(NodeGraphProvider.NAMESPACE, n.getNodeId());
    }

    @Override
    public NodeRef getNodeRef() {
        return NodeRefs.from(getId());
    }

    public NodeInfo getNodeInfo() {
        return nodeInfo;
    }

    public void setNodeInfo(NodeInfo nodeInfo) {
        this.nodeInfo = nodeInfo;
    }

    @Override
    public GenericVertex asGenericVertex() {
        // TODO MVR enrichment :(
        final GenericVertex vertex = super.asGenericVertex();
        vertex.setProperty("node", getNodeInfo());
        return vertex;
    }
}