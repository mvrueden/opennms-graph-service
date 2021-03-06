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

package org.opennms.poc.graph.impl.vmware;

import java.util.Objects;

public enum PowerState {
    Unknown("unknown"),
    PoweredOn("poweredOn"),
    PoweredOff("poweredOff"),
    Suspended("suspended"),
    StandBy("standBy");

    private final String label;

    PowerState(String label) {
        this.label = Objects.requireNonNull(label);
    }

    public static PowerState from(String powerState) {
        if ("".equals(powerState) || powerState == null) {
            return PowerState.Unknown;
        }
        for (PowerState eachState : values()) {
            if (eachState.label.equalsIgnoreCase(powerState)) {
                return eachState;
            }
        }
        return PowerState.Unknown;
    }
}
