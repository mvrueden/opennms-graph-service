/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.poc.graph.impl.bsm;

import java.util.Set;

import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;
import org.opennms.poc.graph.api.generic.GenericVertex;

public class IpServiceVertex extends AbstractVertex {

    private final Integer ipServiceId;
    private final Set<String> reductionKeys;
    private final String ipAddress;
    private final int nodeId;

    public IpServiceVertex(IpService ipService, int level) {
        this(ipService.getId(),
            ipService.getServiceName(),
            ipService.getIpAddress(),
            ipService.getReductionKeys(),
            ipService.getNodeId(),
            level);
    }

    public IpServiceVertex(GraphVertex graphVertex) {
        this(graphVertex.getIpService(), graphVertex.getLevel());
    }

    private IpServiceVertex(int ipServiceId, String ipServiceName, String ipAddress, Set<String> reductionKeys, int nodeId, int level) {
        super(Type.IpService + ":" + ipServiceId, ipServiceName, level);
        this.ipServiceId = ipServiceId;
        this.reductionKeys = reductionKeys;
        this.ipAddress = ipAddress;
        this.nodeId = nodeId;
    }

    public Integer getIpServiceId() {
        return ipServiceId;
    }

    @Override
    public Type getType() {
        return Type.IpService;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public Set<String> getReductionKeys() {
        return reductionKeys;
    }

    @Override
    public GenericVertex asGenericVertex() {
        final GenericVertex vertex = super.asGenericVertex();
        vertex.setProperty("ipAddress", ipAddress);
        vertex.setProperty("serviceId", getIpServiceId());
        vertex.setProperty("tooltip", String.format("IP Service '%s' on %s", label, ipAddress));
        vertex.setProperty("icon", "bsm.ip-service");
        vertex.setProperty("nodeId", nodeId);
        return vertex;
    }
}
