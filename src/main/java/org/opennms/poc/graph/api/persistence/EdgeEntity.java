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

package org.opennms.poc.graph.api.persistence;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
@DiscriminatorValue("edge")
public class EdgeEntity extends AbstractGraphElementEntity {

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name="source_vertex_id", referencedColumnName = "id", nullable = false, updatable = false)
    private VertexEntity source;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name="target_vertex_id", referencedColumnName = "id", nullable = false, updatable = false)
    private VertexEntity target;

    public VertexEntity getSource() {
        return source;
    }

    public void setSource(VertexEntity source) {
        this.source = source;
    }

    public VertexEntity getTarget() {
        return target;
    }

    public void setTarget(VertexEntity target) {
        this.target = target;
    }
}
