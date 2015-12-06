package com.github.pfichtner.core.bluetooth;

import static org.zu.ardulink.util.Preconditions.checkState;

import java.io.IOException;
import java.util.ArrayList;
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
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import com.github.pfichtner.ardulink.core.ConnectionBasedLink;
import com.github.pfichtner.ardulink.core.linkmanager.LinkFactory;
import com.github.pfichtner.ardulink.core.proto.api.Protocol;
import com.github.pfichtner.ardulink.core.proto.impl.ArdulinkProtocolN;

public class BluetoothLinkFactory implements LinkFactory<BluetoothLinkConfig> {

	private Protocol proto = ArdulinkProtocolN.instance();

	@Override
	public String getName() {
		return "bluetooth";
	}

	@Override
	public ConnectionBasedLink newLink(BluetoothLinkConfig config)
			throws IOException {
		Map<String, ServiceRecord> ports = getPortList();
		ServiceRecord serviceRecord = ports.get(config.getDeviceName());
		checkState(serviceRecord != null,
				"The connection could not be made. Device not discovered");
		String url = serviceRecord.getConnectionURL(
				ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
		checkState(url != null,
				"The connection could not be made. Connection url not found");
		StreamConnection streamConnection = getStreamConnection(Connector
				.open(url));
		return new ConnectionBasedLink(
				new com.github.pfichtner.ardulink.core.StreamConnection(
						streamConnection.openInputStream(),
						streamConnection.openOutputStream(), proto), proto);
	}

	public StreamConnection getStreamConnection(Connection connection)
			throws IOException {
		if (connection instanceof StreamConnectionNotifier) {
			return ((StreamConnectionNotifier) connection).acceptAndOpen();
		} else if (connection instanceof StreamConnection) {
			return (StreamConnection) connection;
		} else {
			throw new IllegalStateException("Connection class not known. "
					+ connection.getClass().getName());
		}
	}

	private Map<String, ServiceRecord> getPortList() {
		// TODO should be replaced by Semaphore
		Object lock = new Object();
		DiscoveryAgent agent = getLocalDevice().getDiscoveryAgent();
		List<RemoteDevice> devices = new ArrayList<RemoteDevice>();
		Map<String, ServiceRecord> ports = new HashMap<String, ServiceRecord>();
		DiscoveryListener listener = listener(devices, ports, lock);
		try {
			agent.startInquiry(DiscoveryAgent.GIAC, listener);
			synchronized (lock) {
				lock.wait();
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		for (RemoteDevice device : devices) {
			try {
				agent.searchServices(serviceName(), serialPortService(),
						device, listener);
				synchronized (lock) {
					lock.wait();
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return ports;
	}

	public DiscoveryListener listener(final List<RemoteDevice> devices,
			Map<String, ServiceRecord> ports, final Object lock) {
		return new DiscoveryListener() {

			private final Map<RemoteDevice, ServiceRecord[]> services = new HashMap<RemoteDevice, ServiceRecord[]>();

			@Override
			public void deviceDiscovered(RemoteDevice remoteDevice,
					DeviceClass deviceClass) {
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

				for (Entry<RemoteDevice, ServiceRecord[]> entry : services
						.entrySet()) {
					RemoteDevice remoteDevice = entry.getKey();
					ServiceRecord service = findService(entry.getValue());
					if (service != null) {
						ports.put(getName(remoteDevice), service);
					}
				}
				synchronized (lock) {
					lock.notify();
				}
			}

			public String getName(RemoteDevice remoteDevice) {
				String name = getFriendlyName(remoteDevice) + " "
						+ remoteDevice.getBluetoothAddress();
				return name;
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
				for (int i = 0; i < serviceRecords.length; i++) {
					DataElement serviceName = serviceRecords[i]
							.getAttributeValue(0x0100);
					if (serviceName != null
							&& "DevB".equals(serviceName.getValue())) {
						return serviceRecords[i];
					}
				}
				return null;
			}

			@Override
			public void servicesDiscovered(int transID,
					ServiceRecord[] serviceRecords) {
				for (ServiceRecord serviceRecord : serviceRecords) {
					services.put(serviceRecord.getHostDevice(), serviceRecords);
				}
			}

		};
	}

	public LocalDevice getLocalDevice() {
		try {
			return LocalDevice.getLocalDevice();
		} catch (BluetoothStateException e) {
			throw new RuntimeException(e);
		}
	}

	private static int[] serviceName() {
		return new int[] { 0x0100 };
	}

	private static UUID[] serialPortService() {
		return new UUID[] { new UUID(0x1101) };
	}

	@Override
	public BluetoothLinkConfig newLinkConfig() {
		return new BluetoothLinkConfig();
	}

}
