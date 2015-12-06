package com.github.pfichtner.core.bluetooth;

import com.github.pfichtner.ardulink.core.linkmanager.LinkConfig;

public class BluetoothLinkConfig implements LinkConfig {

	@Named("deviceName")
	private String deviceName;

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

}
