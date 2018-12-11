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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.poc.graph.api.Vertex;
import org.opennms.poc.graph.api.enrichment.Compution;
import org.opennms.poc.graph.api.enrichment.NodeRef;
import org.opennms.poc.graph.api.info.NodeInfo;
import org.opennms.poc.graph.impl.refs.NodeRefs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Scope(SCOPE_PROTOTYPE) // TODO MVR is this correct? We need probably multiple and need to ensure they are thread safe
public class NodeResolutionCompution implements Compution<NodeInfo> {

    @Autowired
    private NodeDao nodeDao;

    @Override
    @Transactional
    public NodeInfo compute(Vertex vertex) {
        for (Field f : vertex.getClass().getDeclaredFields()) {
            // Skip static fields
            if (!Modifier.isStatic(f.getModifiers())
                    || f.getAnnotation(NodeRef.class) != null) {
                // Verify value is != null
                try {
                    f.setAccessible(true);
                    if (f.get(vertex) == null) {
                        throw new IllegalStateException("Cannot load node info because noderef is null");
                    }
                    if (f.getType().isPrimitive() && ((Number)f.get(vertex)).longValue() == 0) {
                        throw new IllegalStateException("Cannot load node info because noderef is 0");
                    }
                    Object value = f.get(vertex);
                    final org.opennms.poc.graph.impl.refs.NodeRef nodeRef = NodeRefs.from(value.toString());
                    final NodeInfo resolve = nodeRef.resolve(nodeDao);
                    return resolve;


//                    // TODO MVR handle case when node_id AND node_ref are defined
//                    if (vertex.getProperty(GenericProperties.NODE_ID) != null && vertex.getProperty(GenericProperties.NODE_REF) != null) {
//                        LOG.warn("{} and {} are defined on vertex with id {}. {} will be preferred",
//                                GenericProperties.NODE_ID, GenericProperties.NODE_REF,
//                                vertex.getId(), GenericProperties.NODE_REF);
//                    }
//                    if (vertex.getProperty(GenericProperties.NODE_REF) != null
//                            || vertex.getProperty(GenericProperties.NODE_ID) != null) {
//                        final String nodeRef = vertex.getProperty(GenericProperties.NODE_REF) != null
//                                ? vertex.getProperty(GenericProperties.NODE_REF)
//                                : "" + ((int) vertex.getProperty(GenericProperties.NODE_ID));
//                        final NodeInfo node = NodeRefs.from(nodeRef).resolve(nodeDao);
//                        vertex.setComputedProperty("node", node);
//                    }

                } catch (IllegalAccessException ex) {
                    // TODO MVR
                }
            }
        }
        throw new IllegalStateException("Given vertex was not annotated with @NodeRef. Cannot resolve NodeInfo");
    }
}
