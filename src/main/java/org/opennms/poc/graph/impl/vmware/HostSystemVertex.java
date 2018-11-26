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

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class HostSystemVertex extends VmwareVertex {
    private List<String> ipAddresses = new ArrayList<>();
    private short numCpuCores;
    private float memorySizeInMb;
    private PowerState powerState;

    public HostSystemVertex(String id) {
        super(id);
    }

    public void setIpAddresses(TreeSet<String> ipAddresses) {
        this.ipAddresses = new ArrayList<>(ipAddresses);
    }

    public void setCpuCores(short numCpuCores) {
        this.numCpuCores = numCpuCores;
    }

    public void setMemorySize(float memorySizeInMb) {
        this.memorySizeInMb = memorySizeInMb;
    }

    public void setPowerState(PowerState powerState) {
        this.powerState = powerState;
    }

    public List<String> getIpAddresses() {
        return ipAddresses;
    }

    public short getNumCpuCores() {
        return numCpuCores;
    }

    public float getMemorySizeInMb() {
        return memorySizeInMb;
    }

    public PowerState getPowerState() {
        return powerState;
    }

    // TODO MVR asGenericInfo
}
