package org.ardulink.core.bluetooth;

import static org.ardulink.util.Throwables.propagate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

public class BluetoothDiscoveryUtil {

	private static final int SERVICE_NAME = 0x0100;
	private static final UUID SERIAL_PORT_SERVICE = new UUID(0x1101);

	public static Map<String, ServiceRecord> getDevices() {
		// TODO should be replaced by Semaphore
		Object lock = new Object();
		List<RemoteDevice> devices = new ArrayList<>();
		Map<String, ServiceRecord> ports = new HashMap<>();
		DiscoveryListener listener = listener(devices, ports, lock);
		DiscoveryAgent agent = getLocalDevice().getDiscoveryAgent();
		try {
			agent.startInquiry(DiscoveryAgent.GIAC, listener);
			synchronized (lock) {
				lock.wait();
			}
		} catch (Exception e) {
			throw propagate(e);
		}

		for (RemoteDevice device : devices) {
			try {
				agent.searchServices(serviceName(), serialPortService(), device, listener);
				synchronized (lock) {
					lock.wait();
				}
			} catch (Exception e) {
				throw propagate(e);
			}
		}
		return ports;
	}

	private static DiscoveryListener listener(List<RemoteDevice> devices, Map<String, ServiceRecord> ports,
			Object lock) {
		return new DiscoveryListener() {

			private final Map<RemoteDevice, ServiceRecord[]> services = new HashMap<>();

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
				for (Entry<RemoteDevice, ServiceRecord[]> entry : services.entrySet()) {
					ServiceRecord service = findService(entry.getValue());
					if (service != null) {
						ports.put(getName(entry.getKey()), service);
					}
				}
				synchronized (lock) {
					lock.notify();
				}
			}

			public String getName(RemoteDevice remoteDevice) {
				return getFriendlyName(remoteDevice) + " " + remoteDevice.getBluetoothAddress();
			}

			public String getFriendlyName(RemoteDevice remoteDevice) {
				try {
					return remoteDevice.getFriendlyName(false);
				} catch (Exception e) {
					return "noname";
				}
			}

			/**
			 * Find service for a device that is named: OBEX Object Push.
			 * 
			 * @param serviceRecords
			 * @return the service record
			 */
			private ServiceRecord findService(ServiceRecord[] serviceRecords) {
				if (serviceRecords.length == 1) {
					return serviceRecords[0];
				}
				return Arrays.stream(serviceRecords).filter(r -> isDevB(r)).findFirst().orElse(null);
			}

			private boolean isDevB(ServiceRecord r) {
				DataElement serviceName = r.getAttributeValue(SERVICE_NAME);
				return serviceName != null && "DevB".equals(serviceName.getValue());
			}

			@Override
			public void servicesDiscovered(int transID, ServiceRecord[] serviceRecords) {
				for (ServiceRecord serviceRecord : serviceRecords) {
					services.put(serviceRecord.getHostDevice(), serviceRecords);
				}
			}

		};
	}

	private static LocalDevice getLocalDevice() {
		try {
			return LocalDevice.getLocalDevice();
		} catch (BluetoothStateException e) {
			throw propagate(e);
		}
	}

	private static int[] serviceName() {
		return new int[] { SERVICE_NAME };
	}

	private static UUID[] serialPortService() {
		return new UUID[] { SERIAL_PORT_SERVICE };
	}

}
