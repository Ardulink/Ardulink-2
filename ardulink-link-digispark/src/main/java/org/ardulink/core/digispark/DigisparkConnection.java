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

import static org.ardulink.util.Preconditions.checkState;
import static org.usb4java.LibUsb.RECIPIENT_INTERFACE;
import static org.usb4java.LibUsb.REQUEST_TYPE_CLASS;
import static org.usb4java.LibUsb.SUCCESS;

import java.nio.ByteBuffer;

import org.ardulink.core.AbstractConnection;
import org.usb4java.Device;
import org.usb4java.DeviceHandle;
import org.usb4java.LibUsb;

public class DigisparkConnection extends AbstractConnection {

	private final String deviceName;
	private Device usbDevice;
	private boolean connected;

	private DeviceHandle usbDevHandle = new DeviceHandle();

	public DigisparkConnection(DigisparkLinkConfig config) {
		this.deviceName = config.deviceName;
		this.usbDevice = DigisparkDiscoveryUtil.getDevices().get(deviceName);
		checkState(usbDevice != null, "No device with portName %s found", deviceName);
		connect();
	}

	private void connect() {
		disconnect();
		int rc = LibUsb.open(usbDevice, usbDevHandle);
		checkState(rc == SUCCESS, "open: %s", LibUsb.strError(rc));
		rc = LibUsb.setConfiguration(usbDevHandle, 1);
		checkState(rc == SUCCESS, "setConfiguration: %s", LibUsb.strError(rc));
		rc = LibUsb.claimInterface(usbDevHandle, 0);
		checkState(rc == SUCCESS, "claimInterface: %s", LibUsb.strError(rc));
		connected = true;
	}

	private boolean disconnect() {
		if (connected) {
			int rc = LibUsb.releaseInterface(usbDevHandle, 0);
			checkState(rc == SUCCESS, "releaseInterface: %s", LibUsb.strError(rc));

			LibUsb.close(usbDevHandle);
			connected = false;
		}
		return true;
	}

	@Override
	public void close() {
		disconnect();
	}

	@Override
	public void write(byte[] bytes) {
		int rc = LibUsb.controlTransfer(usbDevHandle, (byte) (REQUEST_TYPE_CLASS | RECIPIENT_INTERFACE), (byte) 0x09,
				(short) 2, (short) 1, byteBuffer(bytes), 0);
		checkState(rc >= 0, "controlTransfer: %s", LibUsb.strError(rc));
		fireSent(bytes);
	}

	private ByteBuffer byteBuffer(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
		buffer.put(bytes);
		return buffer;
	}

}
