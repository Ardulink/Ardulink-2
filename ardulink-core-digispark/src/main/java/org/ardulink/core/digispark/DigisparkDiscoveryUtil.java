package org.ardulink.core.digispark;

import java.util.LinkedHashMap;
import java.util.Map;

import ch.ntb.usb.USB;
import ch.ntb.usb.USBException;
import ch.ntb.usb.Usb_Bus;
import ch.ntb.usb.Usb_Device;
import ch.ntb.usb.Usb_Device_Descriptor;

public class DigisparkDiscoveryUtil {
	
	public static Map<String, Usb_Device> getDevices() throws USBException {
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
	
	private static String usbDeviceName(int idx) {
		return "Digispark_" + idx;
	}
}
