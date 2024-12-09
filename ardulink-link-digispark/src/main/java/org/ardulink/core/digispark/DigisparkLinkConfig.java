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

import static java.util.Arrays.asList;
import static org.ardulink.core.digispark.DigisparkDiscoveryUtil.getDevices;
import static org.ardulink.util.Iterables.getFirst;

import java.util.List;
import java.util.Set;

import org.ardulink.core.linkmanager.LinkConfig;
import org.ardulink.core.proto.api.Protocol;

public class DigisparkLinkConfig implements LinkConfig {

	private static final String NAMED_PROTO = "proto";

	private static final String NAMED_DEVICE_NAME = "deviceName";

	// at the moment the only supported protocol is SimpleDigisparkProtocol
	// SimpleDigisparkProtocol is NOT registered as available protocol so don't try
	// to load it by name!
	private static final List<Protocol> VALID_PROTOS = asList(new SimpleDigisparkProtocol());

	@Named(NAMED_DEVICE_NAME)
	public String deviceName;

	@Named(NAMED_PROTO)
	public Protocol protocol = getFirst(VALID_PROTOS).orElseThrow(() -> new RuntimeException("valid protos is empty"));

	@ChoiceFor(NAMED_DEVICE_NAME)
	public Set<String> deviceNames() {
		return getDevices().keySet();
	}

	@ChoiceFor(NAMED_PROTO)
	public List<Protocol> protos() {
		return VALID_PROTOS;
	}

}
