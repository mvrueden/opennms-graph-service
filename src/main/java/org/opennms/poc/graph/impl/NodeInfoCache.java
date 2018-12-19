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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.poc.graph.api.info.NodeInfo;
import org.opennms.poc.graph.impl.refs.NodeIdRef;
import org.opennms.poc.graph.impl.refs.NodeRef;
import org.opennms.poc.graph.impl.refs.NodeRefs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

// TODO MVR is this really the best way of doing this? :(
@Service
public class NodeInfoCache {

    @Autowired
    private NodeDao nodeDao;

    @Autowired
    private TransactionOperations transactionTemplate;

    private Map<NodeRef, NodeInfo> nodeInfoMap = new HashMap<>();

    @PostConstruct
    public void init() {
        transactionTemplate.execute((TransactionCallback<Void>) status -> {
            long start = System.currentTimeMillis();
            System.out.println("Started initializing the NodeInfoCache");
            final List<OnmsNode> allNodes = nodeDao.findMatching(new CriteriaBuilder(OnmsNode.class)
                    .fetch("ipInterfaces")
                    .fetch("categories").toCriteria());
            allNodes.stream().forEach(node -> {
                final NodeInfo nodeInfo = NodeIdRef.createInfo(node);
                if (node.getForeignSource() != null && node.getForeignId() != null) {
                    nodeInfoMap.put(NodeRefs.from(node.getForeignSource() + ":" + node.getForeignId()), nodeInfo);
                } else {
                    nodeInfoMap.put(NodeRefs.from(node.getNodeId()), nodeInfo);
                }
            });
            System.out.println("DONE. Took " + (System.currentTimeMillis() - start) + " ms");
            return null;
        });
    }

    public NodeInfo getNodeInfo(NodeRef nodeRef) {
        return nodeInfoMap.get(nodeRef);
    }

    public NodeInfo getNodeInfo(int nodeId) {
        return getNodeInfo(NodeRefs.from(nodeId));
    }

    public NodeInfo getNodeInfo(String criteria) {
        return getNodeInfo(NodeRefs.from(criteria));
    }
}
