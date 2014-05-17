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

package org.zu.ardulink.connection.usb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.zu.ardulink.connection.Connection;
import org.zu.ardulink.connection.ConnectionContact;

import ch.ntb.usb.LibusbJava;
import ch.ntb.usb.USB;
import ch.ntb.usb.USBException;
import ch.ntb.usb.Usb_Bus;
import ch.ntb.usb.Usb_Device;
import ch.ntb.usb.Usb_Device_Descriptor;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class DigisparkUSBConnection implements Connection {

	private Map<String, Usb_Device> deviceMap = new HashMap<String, Usb_Device>();
	private boolean portListRequested = false;
	
	private Usb_Device openedDevice = null;
	private long usbDevHandle;

	public static final String DEFAULT_ID = "defaultDigisparkUSBConnection";
	
	/**
	 * <b>String</b> identifying the specific instance of the DigisparkUSBConnection-class. While
	 * having only a single instance, 'id' is irrelevant. However, having more
	 * than one open connection (using more than one instance of {@link DigisparkUSBConnection}
	 * ), 'id' helps identifying which connection a message or a log
	 * entry came from.
	 */
	private String id;

	/**
	 * Communicating between threads, showing the {@link #reader} when the
	 * connection has been closed, so it can {@link Thread#join()}.
	 */
	private boolean end = false;

	/**
	 * The Thread used to receive the data from the Serial interface.
	 */
	private Thread reader;

	/**
	 * A small <b>int</b> representing the number to be used to distinguish
	 * between two consecutive packages. It can only take a value between 0 and
	 * 255. Note that data is only sent to
	 * {@link org.zu.ardulink.connection.ConnectionContact#parseInput(int, int, int[])} once the following
	 * 'divider' could be identified.
	 * 
	 * As a default, <b>255</b> is used as a divider (unless specified otherwise
	 * in the constructor).
	 * 
	 * @see org.zu.ardulink.connection.serial.SerialConnection#SerialConnection(int, ConnectionContact, int)
	 */
	private int divider;
	public static final int DEFAULT_DIVIDER = 255;
	
	public DigisparkUSBConnection() {
		this.id = DEFAULT_ID;
		this.divider = DEFAULT_DIVIDER;
	}

	public DigisparkUSBConnection(String id) {
		this.id = id;
		this.divider = DEFAULT_DIVIDER;
	}

	public DigisparkUSBConnection(String id, int divider) {
		this.id = id;
		this.divider = divider;
	}

	/**
	 * Link to the instance of the class implementing {@link org.zu.ardulink.connection.ConnectionContact}.
	 */
	private ConnectionContact contact;
	
	@Override
	public List<String> getPortList() {
		List<String> retvalue = new ArrayList<String>();
		deviceMap.clear();
		try {
			Usb_Bus bus = USB.getBus();
			Usb_Device device = null;
			int internal_index = 0;
			while(bus != null) {
				device = bus.getDevices();
				while (device != null) {
					Usb_Device_Descriptor devDesc = device.getDescriptor();
					if(devDesc.getIdVendor() == 0x16C0 && devDesc.getIdProduct() == 0x05DF) {
						internal_index++;
						String deviceName = getUsbDeviceName(internal_index);
						retvalue.add(deviceName);
						deviceMap.put(deviceName, device);
					}
					device = device.getNext();
				}
				bus = bus.getNext();
			}
		}
		catch(USBException e) {
			e.printStackTrace();
			deviceMap.clear();
			retvalue.clear();
			throw new RuntimeException(e);
		}
		
		if(contact != null) {
			contact.writeLog(id, "found the following ports:");
			Iterator<String> it = retvalue.iterator();
			while (it.hasNext()) {
				contact.writeLog(id, "   " + it.next());
			}
		}
		
		return retvalue;
	}

	private String getUsbDeviceName(int internal_index) {
		String deviceName = "Digispark (" + internal_index + ")";
		return deviceName;
	}
	
	@Override
	public boolean connect(Object... params) {
		String portName = null;
		if(params == null || params.length < 1) {
			throw new RuntimeException("This connection accepts a String port name. Null or zero arguments passed.");
		}
		if(!(params[0] instanceof String)) {
			throw new RuntimeException("This connection accepts a String port name. First argument was not a String");
		} else {
			portName =(String)params[0]; 
		}
		if(params.length > 1) {
			throw new RuntimeException("This connection accepts a String port name. More than one argument passed");
		}
		boolean retvalue = false;
		retvalue = connect(portName);

		return retvalue;
	}

	public boolean connect(String portName) {
		boolean retvalue = false;
		
		if(openedDevice != null) { // Already opened, close first!
			disconnect();
		}
		
		if(deviceMap.size() == 0 && !portListRequested) {
			getPortList();
		}
		
		Usb_Device usbDevice = deviceMap.get(portName);
		if(usbDevice != null) {
			openedDevice = usbDevice;
			
			long res = LibusbJava.usb_open(openedDevice);
			if (res == 0) {
				throw new RuntimeException("LibusbJava.usb_open: "
						+ LibusbJava.usb_strerror());
			}
			usbDevHandle = res;

			if (openedDevice == null || usbDevHandle == 0) {
				throw new RuntimeException("USB Digispark device with not found on USB");
			}
			claim_interface(usbDevHandle, 1, 0);

			reader = (new Thread(new DigisparkUSBReader()));
			end = false;
			reader.start();

			if(contact != null) {
				contact.writeLog(id, "connection on " + portName
						+ " established");
				contact.connected(portName, portName);
			}
			retvalue = true;
		}
		
		return retvalue;
	}

	private void claim_interface(long usb_dev_handle, int configuration, int interface_) {
		if (LibusbJava.usb_set_configuration(usb_dev_handle, configuration) < 0) {
			usbDevHandle = 0;
			throw new RuntimeException("LibusbJava.usb_set_configuration: "
					+ LibusbJava.usb_strerror());
		}
		if (LibusbJava.usb_claim_interface(usb_dev_handle, interface_) < 0) {
			usbDevHandle = 0;
			throw new RuntimeException("LibusbJava.usb_claim_interface: "
					+ LibusbJava.usb_strerror());
		}
	}
	
	
	@Override
	public boolean disconnect() {
		
		if(openedDevice != null) {
			
			if (LibusbJava.usb_release_interface(usbDevHandle, 0) < 0) {
				usbDevHandle = 0;
				throw new RuntimeException("LibusbJava.usb_release_interface: "
						+ LibusbJava.usb_strerror());
			}
			
			if (LibusbJava.usb_close(usbDevHandle) < 0) {
				usbDevHandle = 0;
				throw new RuntimeException("LibusbJava.usb_close: "
						+ LibusbJava.usb_strerror());
			}
			
			end = true;
			try {
				reader.join();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			
		}

		openedDevice = null;
		deviceMap.clear();
		portListRequested = false;
		if(contact != null) {
			contact.disconnected(id);
			contact.writeLog(id, "connection disconnected");
		}

		return true;
	}

	@Override
	public boolean isConnected() {
		boolean retvalue = false;
		if(openedDevice != null) {
			retvalue = true;
		}
		return retvalue;
	}

	public boolean writeSerial(String message) {
		
		boolean success = true;
		
		byte[] out = message.getBytes();
		for (int i = 0; i < out.length; i++) {
			
			int len = LibusbJava.usb_control_msg(usbDevHandle, (0x01 << 5), 0x09, 0, (int) out[i], new byte[0], 0, 0);
			if (len < 0) {
				throw new RuntimeException("LibusbJava.controlMsg: "
						+ LibusbJava.usb_strerror());
			}			
		}
		
		return success;
	}

	@Override
	public boolean writeSerial(int numBytes, int[] message) {
		boolean success = true;
		
		for (int i = 0; i < numBytes; i++) {
			
			int len = LibusbJava.usb_control_msg(usbDevHandle, (0x01 << 5), 0x09, 0, message[i], new byte[0], 0, 0);
			if (len < 0) {
				throw new RuntimeException("LibusbJava.controlMsg: "
						+ LibusbJava.usb_strerror());
			}			
		}

		int len = LibusbJava.usb_control_msg(usbDevHandle, (0x01 << 5), 0x09, 0, divider, new byte[0], 0, 0);
		if (len < 0) {
			throw new RuntimeException("LibusbJava.controlMsg: "
					+ LibusbJava.usb_strerror());
		}			
		
		return success;
	}

	@Override
	public void setConnectionContact(ConnectionContact connectionContact) {
		contact = connectionContact;
	}

	
	/**
	 * A separate class. It is run as a
	 * separate {@link Thread} and manages the incoming data, packaging them
	 * using {@link org.zu.ardulink.connection.serial.SerialConnection#divider} into arrays of <b>int</b>s and
	 * forwarding them using
	 * {@link org.zu.ardulink.connection.ConnectionContact#parseInput(int, int, int[])}.
	 * 
	 */
	private class DigisparkUSBReader implements Runnable {

		private int[] tempBytes = new int[1024];;
		int numTempBytes = 0;

		public DigisparkUSBReader() {
		}

		public void run() {
			byte[] readbyte = new byte[1];
			readbyte[0] = 0;
			try {
				while(!end) {
					int result = LibusbJava.usb_control_msg(usbDevHandle, (0x01 << 5) | 0x80, 0x01, 0, 0, readbyte, 1, 0);
					if(result > 0) {
						byte temp = readbyte[0];
						if (temp == divider) {
							if  (numTempBytes > 0) {
								contact.parseInput(id, numTempBytes, tempBytes);
							}
							numTempBytes = 0;
						} else {
							tempBytes[numTempBytes] = temp;
							++numTempBytes;
						}
					}
				}
			}
			catch(Exception e) {
				end = true;
				e.printStackTrace();
				disconnect();
			}
		}		
		
	}
}
