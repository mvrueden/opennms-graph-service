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
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.poc.graph.impl.bsm;

import org.opennms.netmgt.bsm.service.model.functions.map.MapFunction;
import org.opennms.netmgt.bsm.service.model.graph.GraphEdge;
import org.opennms.poc.graph.api.generic.GenericEdge;
import org.opennms.poc.graph.api.simple.SimpleEdge;

public class BusinessServiceEdge<V extends AbstractVertex> extends SimpleEdge<V> {

    private final MapFunction mapFunction;
    private final float weight;

    public BusinessServiceEdge(GraphEdge graphEdge, V source, V target) {
        super(source, target);
        setTooltip(String.format("Map function: %s, Weight: %s", getMapFunction().getClass().getSimpleName(), getWeight()));
        this.mapFunction = graphEdge.getMapFunction();
        this.weight = graphEdge.getWeight();
    }

    @Override
    public String getNamespace() {
        return BsmGraphProvider.NAMESPACE;
    }

    @Override
    public String getId() {
        return String.format("connection:%s:%s", getSource().getId(), getTarget().getId());
    }

    public MapFunction getMapFunction() {
        return mapFunction;
    }

    public float getWeight() {
        return weight;
    }

    @Override
    public GenericEdge asGenericEdge() {
        final GenericEdge genericEdge = super.asGenericEdge();
        genericEdge.setProperty("mapFunction", mapFunction.getClass().getSimpleName().toLowerCase());
        genericEdge.setProperty("weight", weight);
        return genericEdge;
    }
}
