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

package org.opennms.poc.graph.api.simple;

import org.opennms.poc.graph.api.Vertex;
import org.opennms.poc.graph.api.generic.GenericProperties;
import org.opennms.poc.graph.api.generic.GenericVertex;

public class SimpleVertex implements Vertex {

    private final String namespace;
    private final String id;
    private String iconKey;
    private String tooltip;
    private String label;

    public SimpleVertex(String namespace, String id) {
        this.namespace = namespace;
        this.id = id;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public String getId() {
        return id;
    }

    public String getIconKey() {
        return iconKey;
    }

    public void setIconKey(String iconKey) {
        this.iconKey = iconKey;
    }

    public String getTooltip() {
        return tooltip;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public GenericVertex asGenericVertex() {
        final GenericVertex vertex = new GenericVertex();
        vertex.setId(getId());
        vertex.setNamespace(getNamespace());
        if (getLabel() != null) {
            vertex.setProperty(GenericProperties.LABEL, getLabel());
        }
        if (getTooltip() != null) {
            vertex.setProperty(GenericProperties.TOOLTIP, getTooltip());
        }
        if (getIconKey() != null) {
            vertex.setProperty(GenericProperties.ICON_KEY, getIconKey());
        }
        return vertex;
    }
}
