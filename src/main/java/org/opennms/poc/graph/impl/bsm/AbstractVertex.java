/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2016 The OpenNMS Group, Inc.
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
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.poc.graph.impl.bsm;

import java.util.Set;

import org.opennms.poc.graph.api.Vertex;
import org.opennms.poc.graph.api.generic.GenericVertex;
import org.opennms.poc.graph.api.simple.SimpleVertex;

public abstract class AbstractVertex extends SimpleVertex implements Vertex {

    enum Type {
        BusinessService,
        IpService,
        ReductionKey,
    }

    protected final int level;

    /**
     * Creates a new {@link AbstractVertex}.
     *  @param id the unique id of this vertex. Must be unique overall the namespace.
     * @param label a human readable label
     * @param level the level of the vertex in the Business Service Hierarchy. The root element is level 0.
     */
    protected AbstractVertex(String id, String label, int level) {
        super(BsmGraphProvider.NAMESPACE, id);
        setLabel(label);
        this.level = level;
    }

    @Override
    public GenericVertex asGenericVertex() {
        final GenericVertex vertex = super.asGenericVertex();
        vertex.setProperty("level", level);
        vertex.setProperty("type", getType());
        vertex.setProperty("reductionKeys", getReductionKeys());
        return vertex;
    }

    public abstract Type getType();

    public abstract Set<String> getReductionKeys();

}
