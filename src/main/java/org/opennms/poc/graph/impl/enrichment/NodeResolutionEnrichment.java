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

import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.poc.graph.api.Vertex;
import org.opennms.poc.graph.api.aware.NodeAware;
import org.opennms.poc.graph.api.enrichment.Enrichment;
import org.opennms.poc.graph.api.info.NodeInfo;
import org.opennms.poc.graph.impl.refs.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Scope(SCOPE_PROTOTYPE) // TODO MVR is this correct? We need probably multiple and need to ensure they are thread safe
public class NodeResolutionEnrichment implements Enrichment<NodeInfo> {

    @Autowired
    private NodeDao nodeDao;

    @Override
    @Transactional
    public NodeInfo compute(Vertex vertex) {
        if (!(vertex instanceof NodeAware)) {
            throw new IllegalArgumentException("Provided vertex must be of type NodeRefAware in order to resolve the node");
        }
        final NodeRef nodeRef = ((NodeAware) vertex).getNodeRef();
        final NodeInfo resolve = nodeRef.resolve(nodeDao);
        return resolve;
    }
}
