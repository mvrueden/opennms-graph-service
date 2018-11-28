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

package org.opennms.poc.graph.impl;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.poc.graph.api.enrichment.VertexEnrichmentProcessor;
import org.opennms.poc.graph.api.generic.GenericProperties;
import org.opennms.poc.graph.api.generic.GenericVertex;
import org.opennms.poc.graph.api.info.NodeInfo;
import org.opennms.poc.graph.impl.refs.NodeRefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// TODO MVR how to deal with ip addresses and so on... this may be more complicated than I thought :(
@Component
public class NodeVertexEnrichmentProcessor implements VertexEnrichmentProcessor {

    private static Logger LOG = LoggerFactory.getLogger(NodeVertexEnrichmentProcessor.class);

    @Autowired
    private NodeDao nodeDao;

    @Override
    public boolean canEnrich(String namespace) {
        return true;
    }

    @Override
    @Transactional
    public void enrich(GenericVertex vertex) {
        // TODO MVR handle case when node_id AND node_ref are defined
        if (vertex.getProperty(GenericProperties.NODE_ID) != null && vertex.getProperty(GenericProperties.NODE_REF) != null) {
            LOG.warn("{} and {} are defined on vertex with id {}. {} will be preferred",
                    GenericProperties.NODE_ID, GenericProperties.NODE_REF,
                    vertex.getId(), GenericProperties.NODE_REF);
        }
        if (vertex.getProperty(GenericProperties.NODE_REF) != null
                || vertex.getProperty(GenericProperties.NODE_ID) != null) {
            final String nodeRef = vertex.getProperty(GenericProperties.NODE_REF) != null
                    ? vertex.getProperty(GenericProperties.NODE_REF)
                    : "" + ((int) vertex.getProperty(GenericProperties.NODE_ID));
            final NodeInfo node = NodeRefs.from(nodeRef).resolve(nodeDao);
            vertex.setComputedProperty("node", node);
        }
    }
}
