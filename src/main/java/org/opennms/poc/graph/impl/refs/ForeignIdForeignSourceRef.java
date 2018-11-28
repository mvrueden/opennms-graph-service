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

package org.opennms.poc.graph.impl.refs;

import static org.opennms.poc.graph.impl.refs.NodeIdRef.createInfo;

import java.util.Objects;

import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.poc.graph.api.info.NodeInfo;

public class ForeignIdForeignSourceRef implements NodeRef {

    private final String foreignSource;
    private final String foreignId;

    public ForeignIdForeignSourceRef(String foreignSource, String foreignId) {
        this.foreignSource = Objects.requireNonNull(foreignSource);
        this.foreignId = Objects.requireNonNull(foreignId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ForeignIdForeignSourceRef that = (ForeignIdForeignSourceRef) o;
        return Objects.equals(foreignSource, that.foreignSource)
                && Objects.equals(foreignId, that.foreignId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(foreignSource, foreignId);
    }

    @Override
    public NodeInfo resolve(NodeDao nodeDao) {
        // TODO MVR what happens if node is not there anymore?
        final OnmsNode node = nodeDao.findByForeignId(foreignSource, foreignId);
        final NodeInfo nodeInfo = createInfo(node);
        return nodeInfo;
    }
}
