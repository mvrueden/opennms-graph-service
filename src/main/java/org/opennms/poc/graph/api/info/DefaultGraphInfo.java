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

package org.opennms.poc.graph.api.info;

import java.util.Objects;

import org.opennms.poc.graph.api.Vertex;

import com.google.common.base.MoreObjects;

public class DefaultGraphInfo implements GraphInfo {

    private String namespace;
    private String description;
    private String label;
    private Class<? extends Vertex> vertexType;

    public DefaultGraphInfo(final String namespace, Class<? extends Vertex> vertexType) {
        this.namespace = Objects.requireNonNull(namespace);
        this.vertexType = Objects.requireNonNull(vertexType);
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public Class<? extends Vertex> getVertexType() {
        return vertexType;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public DefaultGraphInfo withLabel(String label) {
        setLabel(label);
        return this;
    }

    public DefaultGraphInfo withDescription(String description) {
        setDescription(description);
        return this;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("namespace", namespace)
                .add("label", label)
                .add("description", description)
                .toString();
    }
}
