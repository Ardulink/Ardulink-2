/**
Copyright 2013 Luciano Zu project Ardulink http://www.ardulink.org/

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

@author Luciano Zu
*/

package org.zu.ardulink.connection.bluetooth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 */
public class ArdulinkDiscoveryListener implements DiscoveryListener {

    private List<RemoteDevice> devices = new ArrayList<RemoteDevice>();
    private Map<RemoteDevice, ServiceRecord[]> services = new HashMap<RemoteDevice, ServiceRecord[]>();
    
    private BluetoothConnection bluetoothConnection;
    private Object lock;

    public ArdulinkDiscoveryListener(BluetoothConnection bluetoothConnection, Object lock) {
		this.bluetoothConnection = bluetoothConnection;
		this.lock = lock;
	}

	@Override
	public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass deviceClass) {
        devices.add(remoteDevice);
	}

	@Override
	public void inquiryCompleted(int arg0) {
        synchronized (lock) {
            lock.notify();
        }
	}

	@Override
	public void serviceSearchCompleted(int arg0, int arg1) {
		
		Map<String, ServiceRecord> ports = new HashMap<String, ServiceRecord>();
		
		Iterator<RemoteDevice> iterator = services.keySet().iterator();
		while (iterator.hasNext()) {
			RemoteDevice remoteDevice = (RemoteDevice) iterator.next();

			ServiceRecord service = findService(services.get(remoteDevice));
			if(service != null) {
				String name = "noname";
		        try {
		            name = remoteDevice.getFriendlyName(false);
		        } catch (Exception e) {
		        }
		        
		        name += " " + remoteDevice.getBluetoothAddress();
				
				ports.put(name, service);
			}
		}
		
		bluetoothConnection.setPorts(ports);
		
        synchronized (lock) {
            lock.notify();
        }
	}

	/**
	 * Find service for a device that is named: OBEX Object Push
	 * @param serviceRecords
	 * @return the service record
	 */
	private ServiceRecord findService(ServiceRecord[] serviceRecords) {
		ServiceRecord retvalue = null;
		if(serviceRecords.length == 1) {
			retvalue = serviceRecords[0];
		} else {
			for (int i = 0; i < serviceRecords.length; i++) {
	            DataElement serviceName = serviceRecords[i].getAttributeValue(0x0100);
	            if (serviceName != null && serviceName.getValue().equals("DevB")) {
	            	retvalue = serviceRecords[i];
	            }
			}
		}
		return retvalue;
	}

	@Override
	public void servicesDiscovered(int transID, ServiceRecord[] serviceRecords) {
		for(ServiceRecord serviceRecord : serviceRecords) {
			RemoteDevice currentDevice = serviceRecord.getHostDevice();
			services.put(currentDevice, serviceRecords);
		}
	}

	public List<RemoteDevice> getDevices() {
		return devices;
	}

	public Map<RemoteDevice, ServiceRecord[]> getServices() {
		return services;
	}

	/**
	 * Clear all resources used by this listener.
	 */
	public void reset() {
		devices.clear();
		services.clear();
	}
}
