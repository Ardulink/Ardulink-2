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

package org.ardulink.core.bluetooth;

import static org.ardulink.core.bluetooth.BluetoothDiscoveryUtil.getDevices;

import java.util.Set;

import org.ardulink.core.linkmanager.LinkConfig;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class BluetoothLinkConfig implements LinkConfig {

	private static final String DEVICE_NAME = "deviceName";

	private String deviceName;

	@Named(DEVICE_NAME)
	public String getDeviceName() {
		return deviceName;
	}

	@Named(DEVICE_NAME)
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	@ChoiceFor(DEVICE_NAME)
	public Set<String> listDevices() {
		return getDevices().keySet();
	}

}
