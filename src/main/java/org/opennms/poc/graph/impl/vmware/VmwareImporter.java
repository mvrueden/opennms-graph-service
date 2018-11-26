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

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.opennms.poc.graph.api.events.AddEdgeEvent;
import org.opennms.poc.graph.api.events.AddVertexEvent;
import org.opennms.poc.graph.api.events.GraphDiscoveryFinishedEvent;
import org.opennms.poc.graph.api.events.GraphDiscoveryStartedEvent;
import org.opennms.protocols.vmware.VmwareViJavaAccess;

import com.google.common.eventbus.EventBus;
import com.vmware.vim25.HostRuntimeInfo;
import com.vmware.vim25.HostSystemPowerState;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.VirtualMachineRuntimeInfo;
import com.vmware.vim25.mo.Datastore;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.Network;
import com.vmware.vim25.mo.VirtualMachine;

public class VmwareImporter {
    public static final String NAMESPACE = "vmware";

    private final String username;
    private final String hostname;
    private final String password;
    private final EventBus eventBus;

    // Vmware Host Name -> Vertex
    private final Map<String, HostSystemVertex> hostSystemVertexMap = new HashMap<>();

    public VmwareImporter(EventBus eventBus, String hostname, String username, String password) {
        this.hostname = hostname;
        this.username = username;
        this.password = password;
        this.eventBus = eventBus;
    }

    public void startImport() throws RemoteException, MalformedURLException {
        final VmwareViJavaAccess vmwareViJavaAccess = new VmwareViJavaAccess(hostname, username, password);
        vmwareViJavaAccess.connect();
        vmwareViJavaAccess.setTimeout(10 * 1000);

        try {
            eventBus.post(new GraphDiscoveryStartedEvent(NAMESPACE)); // TODO MVR allow setting additional properties, e.g. api version etc.
            iterateHostSystems(vmwareViJavaAccess);
            iterateVirtualMachines(vmwareViJavaAccess);
        } finally {
            eventBus.post(new GraphDiscoveryFinishedEvent(NAMESPACE)); // TODO MVR send with errors or success maybe ?!
            vmwareViJavaAccess.disconnect();
        }

        // TODO MVR ...
//
//        /*
//         * For now we use displaycategory, notifycategory and pollercategory for storing
//         * the vcenter Ip address, the username and the password
//         */
//
//        RequisitionAsset requisitionAssetHostname = new RequisitionAsset("vmwareManagementServer", request.getHostname());
//        requisitionNode.putAsset(requisitionAssetHostname);
//
//        RequisitionAsset requisitionAssetType = new RequisitionAsset("vmwareManagedEntityType", (managedEntity instanceof HostSystem ? "HostSystem" : "VirtualMachine"));
//        requisitionNode.putAsset(requisitionAssetType);
//
//        RequisitionAsset requisitionAssetId = new RequisitionAsset("vmwareManagedObjectId", managedEntity.getMOR().getVal());
//        requisitionNode.putAsset(requisitionAssetId);
//
//        RequisitionAsset requisitionAssetTopologyInfo = new RequisitionAsset("vmwareTopologyInfo", vmwareTopologyInfo.toString());
//        requisitionNode.putAsset(requisitionAssetTopologyInfo);
//
//        RequisitionAsset requisitionAssetState = new RequisitionAsset("vmwareState", powerState);
//        requisitionNode.putAsset(requisitionAssetState);
//
//        requisitionNode.putCategory(new RequisitionCategory("VMware" + apiVersion));
//
//        return requisitionNode;
    }

    private void iterateHostSystems(VmwareViJavaAccess vmwareViJavaAccess) throws RemoteException {
        final ManagedEntity[] managedEntities = vmwareViJavaAccess.searchManagedEntities("HostSystem");
        if (managedEntities != null) {
            Arrays.stream(managedEntities)
                    .forEach(managedEntity -> {
                        final HostSystem hostSystem = (HostSystem) managedEntity;
                        final HostSystemVertex hostSystemVertex = new HostSystemVertex(hostSystem.getMOR().getVal());
                        hostSystemVertex.setLabel(hostSystem.getName());
                        TreeSet<String> ipAddresses = vmwareViJavaAccess.getHostSystemIpAddresses(hostSystem);
                        hostSystemVertex.setIpAddresses(ipAddresses);
                        hostSystemVertex.setCpuCores(hostSystem.getHardware().getCpuInfo().getNumCpuCores());
                        hostSystemVertex.setMemorySize(hostSystem.getHardware().getMemorySize() / 1000000f); // MB

                        final HostRuntimeInfo hostRuntimeInfo = hostSystem.getRuntime();
                        if (hostRuntimeInfo != null) {
                            HostSystemPowerState hostSystemPowerState = hostRuntimeInfo.getPowerState();
                            PowerState powerState = PowerState.from(hostSystemPowerState != null ? hostSystemPowerState.name() : null);
                            hostSystemVertex.setPowerState(powerState);
                        }
                        hostSystemVertexMap.put(hostSystemVertex.getId(), hostSystemVertex);

                        try {
                            for (Network network : hostSystem.getNetworks()) {
                                // TODO MVR network vertex
                                final VmwareVertex networkVertex = new VmwareVertex(network.getMOR().getVal());
                                final VmwareEdge edge = new VmwareEdge(hostSystemVertex, networkVertex);
                                eventBus.post(new AddEdgeEvent<>(edge));
                            }
                        } catch (RemoteException re) {
                            // TODO MVR
                        }

                        try {
                            for (Datastore datastore : hostSystem.getDatastores()) {
                                final VmwareVertex datastoreVertex = new VmwareVertex(datastore.getMOR().getVal());
                                final VmwareEdge edge = new VmwareEdge(hostSystemVertex, datastoreVertex);
                                eventBus.post(new AddEdgeEvent<>(edge));
                            }
                        } catch (RemoteException re) {
                            // TODO MVR
                        }
                        eventBus.post(new AddVertexEvent<>(hostSystemVertex));
                    });
        }
    }

    private void iterateVirtualMachines(VmwareViJavaAccess vmwareViJavaAccess) throws RemoteException {
        final ManagedEntity[] managedEntities = vmwareViJavaAccess.searchManagedEntities("VirtualMachine");
        if (managedEntities != null) {
            Arrays.stream(managedEntities)
                    .forEach(managedEntity -> {
                        final VirtualMachine virtualMachine = (VirtualMachine) managedEntity;
                        final VirtualMachineVertex virtualMachineVertex = new VirtualMachineVertex(virtualMachine.getMOR().getVal());
                        virtualMachineVertex.setLabel(virtualMachine.getName());

                        final TreeSet<String> ipAddresses = vmwareViJavaAccess.getVirtualMachineIpAddresses(virtualMachine);
                        virtualMachineVertex.setIpAddresses(ipAddresses);
                        virtualMachineVertex.setOperatingSystem(virtualMachine.getGuest().getGuestFullName());
                        virtualMachineVertex.setNumCpus(virtualMachine.getConfig().getHardware().getNumCPU());
                        virtualMachineVertex.setRAM(virtualMachine.getConfig().getHardware().getMemoryMB());

                        final VirtualMachineRuntimeInfo virtualMachineRuntimeInfo = virtualMachine.getRuntime();
                        if (virtualMachineRuntimeInfo != null) {
                            final VirtualMachinePowerState virtualMachinePowerState = virtualMachineRuntimeInfo.getPowerState();
                            PowerState powerState = PowerState.from(virtualMachinePowerState != null ? virtualMachinePowerState.name() : null);
                            virtualMachineVertex.setPowerState(powerState);
                        }

                        final VmwareEdge edge = new VmwareEdge(hostSystemVertexMap.get(virtualMachine.getRuntime().getHost().getVal()), virtualMachineVertex);
                        eventBus.post(new AddEdgeEvent<>(edge));
                    });
        }
    }
}
