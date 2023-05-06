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

import static org.ardulink.util.Throwables.propagate;

import java.util.Set;

import org.ardulink.core.linkmanager.LinkConfig;
import org.ardulink.core.proto.api.Protocol;

import ch.ntb.usb.USBException;

public class DigisparkLinkConfig implements LinkConfig {

	private static final String NAMED_PROTO = "proto";

	private static final String NAMED_DEVICE_NAME = "deviceName";

	@Named(NAMED_DEVICE_NAME)
	private String deviceName;

	@Named(NAMED_PROTO)
	private Protocol proto = SimpleDigisparkProtocol.instance();

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	@ChoiceFor(NAMED_DEVICE_NAME)
	public Set<String> listdeviceNames() {
		try {
			return DigisparkDiscoveryUtil.getDevices().keySet();
		} catch (USBException e) {
			throw propagate(e);
		}
	}

	public Protocol getProto() {
		return proto;
	}

	public void setProto(Protocol proto) {
		this.proto = proto;
	}

	@ChoiceFor(NAMED_PROTO)
	public Protocol[] protos() {
		// at the moment the only supported protocol is SimpleDigisparkProtocol
		return new Protocol[] { proto };
	}

}
