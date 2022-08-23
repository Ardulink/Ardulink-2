/**
Copyright 2013 project Ardulink http://www.ardulink.org/
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package org.ardulink.core.digispark;

import static ch.ntb.usb.LibusbJava.usb_claim_interface;
import static ch.ntb.usb.LibusbJava.usb_close;
import static ch.ntb.usb.LibusbJava.usb_control_msg;
import static ch.ntb.usb.LibusbJava.usb_open;
import static ch.ntb.usb.LibusbJava.usb_release_interface;
import static ch.ntb.usb.LibusbJava.usb_set_configuration;
import static ch.ntb.usb.LibusbJava.usb_strerror;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.ardulink.util.Preconditions.checkState;
import static org.ardulink.util.Throwables.propagate;

import java.io.IOException;
import java.util.Map;

import org.ardulink.core.AbstractConnection;

import ch.ntb.usb.USBException;
import ch.ntb.usb.Usb_Device;

public class DigisparkConnection extends AbstractConnection {

	private static final byte[] ZERO_BYTES = new byte[0];

	private final String deviceName;
	private Usb_Device usbDevice;

	private long usbDevHandle;

	public DigisparkConnection(DigisparkLinkConfig config) {
		this.deviceName = config.getDeviceName();
		this.usbDevice = getDevices().get(deviceName);
		checkState(usbDevice != null, "No device with portName %s found",
				deviceName);
		connect();
	}

	private Map<String, Usb_Device> getDevices() {
		try {
			return DigisparkDiscoveryUtil.getDevices();
		} catch (USBException e) {
			throw propagate(e);
		}
	}

	private void connect() {
		disconnect();
		usbDevHandle = usb_open(usbDevice);
		checkState(usbDevHandle != 0, "usb_open: %s", usb_strerror());
		checkState(isConnected(), "USB Digispark device not found on USB");
		claimInterface(usbDevHandle, 1, 0);
	}

	private void claimInterface(long usb_dev_handle, int configuration,
			int interface_) {
		if (usb_set_configuration(usb_dev_handle, configuration) < 0) {
			usbDevHandle = 0;
			throw new RuntimeException("usb_set_configuration: "
					+ usb_strerror());
		}
		if (usb_claim_interface(usb_dev_handle, interface_) < 0) {
			usbDevHandle = 0;
			throw new RuntimeException("usb_claim_interface: " + usb_strerror());
		}
	}

	private boolean disconnect() {
		if (isConnected()) {
			if (usb_release_interface(usbDevHandle, 0) < 0) {
				usbDevHandle = 0;
				throw new RuntimeException("usb_release_interface: "
						+ usb_strerror());
			}

			if (usb_close(usbDevHandle) < 0) {
				usbDevHandle = 0;
				throw new RuntimeException("usb_close: " + usb_strerror());
			}
		}
		usbDevHandle = 0;
		return true;
	}

	private boolean isConnected() {
		return usbDevHandle != 0;
	}

	@Override
	public void close() throws IOException {
		try {
			disconnect();
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	@Override
	public void write(byte[] bytes) throws IOException {
		for (int i = 0; i < bytes.length; i++) {
			if (send(bytes[i]) < 0) {
				tryRecover();
				checkState(send(bytes[i]) >= 0, "controlMsg: %s", usb_strerror());
			}
		}
		fireSent(bytes);
	}

	private int send(byte b) {
		return usb_control_msg(usbDevHandle, (0x01 << 5), 0x09, 0, b, ZERO_BYTES, 0, 0);
	}

	private void tryRecover() throws USBException {
		try {
			MILLISECONDS.sleep(10);
			Map<String, Usb_Device> deviceMap = getDevices();
			usbDevice = deviceMap.get(deviceName);
			checkState(usbDevice != null, "No device with portName %s found",
					deviceName);
			MILLISECONDS.sleep(10);
			connect();
			MILLISECONDS.sleep(10);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
