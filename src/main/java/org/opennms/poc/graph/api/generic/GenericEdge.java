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

package org.opennms.poc.graph.api.generic;

import java.util.Objects;

import org.opennms.poc.graph.api.Edge;
import org.opennms.poc.graph.api.Vertex;

// TODO MVR the edge does not have a namespace
public class GenericEdge extends AbstractElement implements Edge<GenericVertex> {
    private final GenericVertex source;
    private final GenericVertex target;

    // TODO MVR set namespace and id
    public GenericEdge(Vertex source, Vertex target) {
        this(Objects.requireNonNull(source).asGenericVertex(),
             Objects.requireNonNull(target).asGenericVertex());
    }

    public GenericEdge(GenericVertex source, GenericVertex target) {
        this.source = source;
        this.target = target;
        this.setNamespace(source.getNamespace());
        this.setId(source.getNamespace()  + "/" + source.getId() + ":" + target.getNamespace() + "/" + target.getId());
    }

    @Override
    public GenericVertex getSource() {
        return source;
    }

    @Override
    public GenericVertex getTarget() {
        return target;
    }

    @Override
    public GenericEdge asGenericEdge() {
        return this;
    }
}
