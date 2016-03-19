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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import ch.ntb.usb.USB;
import ch.ntb.usb.USBException;
import ch.ntb.usb.Usb_Bus;
import ch.ntb.usb.Usb_Device;
import ch.ntb.usb.Usb_Device_Descriptor;

import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.StreamConnection;
import org.ardulink.core.linkmanager.LinkFactory;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.impl.ArdulinkProtocol255;

public class DigisparkLinkFactory implements LinkFactory<DigisparkLinkConfig> {

	private Protocol proto = ArdulinkProtocol255.instance();

	@Override
	public String getName() {
		return "digispark";
	}

	// TODO not threadsafe: do not hold attributes inside factory
	private Usb_Device device;
	private long usbDevHandle;
	private String portName;
	private int divider;

	@Override
	public ConnectionBasedLink newLink(DigisparkLinkConfig config)
			throws IOException {
		checkState(proto.getSeparator().length == 1,
				"divider must be of length 1 (was %s)",
				proto.getSeparator().length);
		this.divider = proto.getSeparator()[0];
		Map<String, Usb_Device> deviceMap = getDevices();
		portName = config.getPortName();
		Usb_Device usbDevice = deviceMap.get(portName);
		checkState(usbDevice != null, "No device with portName %s found",
				portName);
		connect(portName);

		checkState(false, "not yet implemented!");
		checkState(false, "not yet implemented!");
		checkState(false, "not yet implemented!");

		InputStream is = null;
		OutputStream os = null;
		return new ConnectionBasedLink(new StreamConnection(is, os, proto),
				proto);
	}

	@Override
	public DigisparkLinkConfig newLinkConfig() {
		return new DigisparkLinkConfig();
	}

	private Map<String, Usb_Device> getDevices() throws USBException {
		Map<String, Usb_Device> deviceMap = new LinkedHashMap<String, Usb_Device>();
		USB.init();
		Usb_Bus bus = USB.getBus();
		int idx = 0;
		while (bus != null) {
			Usb_Device device = bus.getDevices();
			while (device != null) {
				Usb_Device_Descriptor devDesc = device.getDescriptor();
				if (devDesc.getIdVendor() == 0x16C0
						&& devDesc.getIdProduct() == 0x05DF) {
					deviceMap.put(usbDeviceName(++idx), device);
				}
				device = device.getNext();
			}
			bus = bus.getNext();
		}
		return deviceMap;
	}

	private String usbDeviceName(int idx) {
		return "Digispark (" + idx + ")";
	}

	private void connect(String portName) {
		if (isConnected()) {
			disconnect();
		}
		long usbDevHandle = usb_open(device);
		checkState(usbDevHandle != 0, "usb_open: %s", usb_strerror());
		checkState(isConnected(), "USB Digispark device not found on USB");
		claim_interface(usbDevHandle, 1, 0);
	}

	private void claim_interface(long usb_dev_handle, int configuration,
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
		return true;
	}

	private boolean isConnected() {
		return device != null;
	}

	private boolean writeSerial(String message) throws USBException {
		boolean success = true;

		byte[] out = message.getBytes();
		for (int i = 0; i < out.length; i++) {

			int len = usb_control_msg(usbDevHandle, (0x01 << 5), 0x09, 0,
					(int) out[i], new byte[0], 0, 0);
			if (len < 0) {
				tryARecover();
				len = usb_control_msg(usbDevHandle, (0x01 << 5), 0x09, 0,
						(int) out[i], new byte[0], 0, 0);
				checkState(len >= 0, "controlMsg: %s", usb_strerror());
			}
		}

		return success;
	}

	private void writeSerial(int numBytes, int[] message) throws USBException {
		for (int i = 0; i < numBytes; i++) {

			int len = usb_control_msg(usbDevHandle, (0x01 << 5), 0x09, 0,
					message[i], new byte[0], 0, 0);
			if (len < 0) {
				tryARecover();
				len = usb_control_msg(usbDevHandle, (0x01 << 5), 0x09, 0,
						message[i], new byte[0], 0, 0);
				checkState(len >= 0, "controlMsg: %s", usb_strerror());
			}
		}

		int len = usb_control_msg(usbDevHandle, (0x01 << 5), 0x09, 0, divider,
				new byte[0], 0, 0);
		checkState(len >= 0, "controlMsg: %s", usb_strerror());
	}

	private void tryARecover() throws USBException {
		try {
			MILLISECONDS.sleep(10);
			getDevices();
			MILLISECONDS.sleep(10);
			connect(portName);
			MILLISECONDS.sleep(10);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

}
