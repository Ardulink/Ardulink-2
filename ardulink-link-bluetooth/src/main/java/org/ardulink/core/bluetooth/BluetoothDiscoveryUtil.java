package org.ardulink.core.bluetooth;

import static java.util.Arrays.stream;
import static java.util.function.Predicate.isEqual;
import static org.ardulink.util.Maps.entry;
import static org.ardulink.util.Throwables.propagate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Semaphore;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DataElement;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;

public final class BluetoothDiscoveryUtil {

	private static final int SERVICE_NAME = 0x0100;
	private static final UUID SERIAL_PORT_SERVICE = new UUID(0x1101);

	private BluetoothDiscoveryUtil() {
		super();
	}

	public static Map<String, ServiceRecord> getDevices() {
		Semaphore semaphore = new Semaphore(0);
		List<RemoteDevice> devices = new ArrayList<>();
		Map<String, ServiceRecord> ports = new HashMap<>();
		DiscoveryListener listener = listener(devices, ports, semaphore);
		DiscoveryAgent agent = getLocalDevice().getDiscoveryAgent();
		try {
			agent.startInquiry(DiscoveryAgent.GIAC, listener);
			semaphore.acquire();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (BluetoothStateException e) {
			throw propagate(e);
		}

		for (RemoteDevice device : devices) {
			try {
				agent.searchServices(serviceName(), serialPortService(), device, listener);
				semaphore.acquire();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			} catch (BluetoothStateException e) {
				throw propagate(e);
			}
		}
		return ports;
	}

	private static DiscoveryListener listener(List<RemoteDevice> devices, Map<String, ServiceRecord> ports,
			Semaphore semaphore) {
		return new DiscoveryListener() {

			private final Map<RemoteDevice, ServiceRecord[]> services = new HashMap<>();

			@Override
			public void deviceDiscovered(RemoteDevice remoteDevice, DeviceClass deviceClass) {
				devices.add(remoteDevice);
			}

			@Override
			public void inquiryCompleted(int discType) {
				semaphore.release();
			}

			@Override
			public void serviceSearchCompleted(int transID, int respCode) {
				services.entrySet().stream() //
						.map(e -> entry(getName(e.getKey()), findService(e.getValue()))) //
						.filter(e -> e.getValue() != null) //
						.forEach(e -> ports.put(e.getKey(), e.getValue()));
				semaphore.release();
			}

			public String getName(RemoteDevice remoteDevice) {
				return getFriendlyName(remoteDevice) + " " + remoteDevice.getBluetoothAddress();
			}

			public String getFriendlyName(RemoteDevice remoteDevice) {
				try {
					return remoteDevice.getFriendlyName(false);
				} catch (IOException e) {
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
				return serviceRecords.length == 1 //
						? serviceRecords[0] //
						: stream(serviceRecords).filter(r -> isDevB(r)).findFirst().orElse(null);
			}

			private boolean isDevB(ServiceRecord serviceRecords) {
				return Optional.ofNullable(serviceRecords.getAttributeValue(SERVICE_NAME)) //
						.map(DataElement::getValue) //
						.filter(isEqual("DevB")) //
						.isPresent();
			}

			@Override
			public void servicesDiscovered(int transID, ServiceRecord[] serviceRecords) {
				stream(serviceRecords).forEach(r -> services.put(r.getHostDevice(), serviceRecords));
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
