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

package org.opennms.poc.graph.rest;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.poc.graph.api.Edge;

@XmlRootElement(name = "link")
@XmlAccessorType(XmlAccessType.FIELD)
public class LinkDTO {

    private VertexDTO source;
    private VertexDTO target;

    public LinkDTO() {

    }

    public LinkDTO(Edge edge) {
        this.source = new VertexDTO(edge.getSource());
        this.target = new VertexDTO(edge.getTarget());
    }

    public VertexDTO getSource() {
        return source;
    }

    public void setSource(VertexDTO source) {
        this.source = source;
    }

    public VertexDTO getTarget() {
        return target;
    }

    public void setTarget(VertexDTO target) {
        this.target = target;
    }
}
