package org.ardulink.core.digispark;

import static org.usb4java.LibUsb.SUCCESS;

import java.util.LinkedHashMap;
import java.util.Map;

import org.usb4java.Context;
import org.usb4java.Device;
import org.usb4java.DeviceDescriptor;
import org.usb4java.DeviceList;
import org.usb4java.LibUsb;

public final class DigisparkDiscoveryUtil {

	private DigisparkDiscoveryUtil() {
		super();
	}

	public static Map<String, Device> getDevices() {
		Context context = new Context();
		try {
			Map<String, Device> deviceMap = new LinkedHashMap<>();

			// Initialize the USB library
			if (LibUsb.init(context) != SUCCESS) {
				System.err.println("Error initializing USB library");
				return deviceMap;
			}

			// List all USB devices
			DeviceList list = new DeviceList();

			try {
				LibUsb.getDeviceList(context, list);

				// Iterate over the devices and filter Digispark devices
				for (Device device : list) {
					DeviceDescriptor descriptor = new DeviceDescriptor();
					if (LibUsb.getDeviceDescriptor(device, descriptor) == SUCCESS) {
						if (isDigispark(descriptor.idVendor(), descriptor.idProduct())) {
							deviceMap.put(usbDeviceName(device), device);
						}
					}
				}
			} finally {
				LibUsb.freeDeviceList(list, true);
			}
			return deviceMap;
		} finally {
			LibUsb.exit(context);
		}

	}

	private static boolean isDigispark(short vendorId, short productId) {
		return vendorId == 0x16C0 && productId == 0x05DF;
	}

	private static String usbDeviceName(Device device) {
		return "Digispark_" + device.getPointer();
	}

}
