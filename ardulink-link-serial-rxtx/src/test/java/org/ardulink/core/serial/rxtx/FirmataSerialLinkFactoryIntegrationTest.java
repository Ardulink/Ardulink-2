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

package org.ardulink.core.serial.rxtx;

import static org.ardulink.core.linkmanager.LinkManager.ARDULINK_SCHEME;
import static org.ardulink.testsupport.junit5.VirtualAvrTester.testSerialPinListening;
import static org.ardulink.testsupport.junit5.VirtualAvrTester.testSerialPinSwitching;
import static org.ardulink.util.URIBuilder.uriBuilder;

import java.net.URI;
import java.util.Map;

import org.ardulink.core.linkmanager.LinkManager;
import org.ardulink.core.linkmanager.LinkManager.Configurer;
import org.ardulink.testsupport.junit5.UseVirtualAvr;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.github.pfichtner.testcontainers.virtualavr.VirtualAvrContainer;

class FirmataSerialLinkFactoryIntegrationTest {

	private static final String FIRMATA_FIRMWARE = "classpath://firmware/StandardFirmata.hex";

	@Disabled("Link#close hangs since StreamReader calls read and this native method doesn't get interrupted even if the InputStream gets closed. That's the reason why RXTX's close does not get a writeLock since the lock remains locked")
	@Test
	@UseVirtualAvr(isolated = true, firmware = FIRMATA_FIRMWARE)
	void canInteractWithSerialLink(VirtualAvrContainer<?> virtualAvr) throws Exception {
		URI uri = uri(virtualAvr);
		Configurer configurer = LinkManager.getInstance().getConfigurer(uri);
		testSerialPinSwitching(virtualAvr, configurer);
		testSerialPinListening(virtualAvr, configurer);
	}

	static URI uri(VirtualAvrContainer<?> virtualAvr) {
		return uriBuilder(ARDULINK_SCHEME, SerialLinkFactory.NAME).params(Map.of( //
				"port", virtualAvr.serialPortDescriptor(), //
				"baudrate", 9600, //
				"proto", "Firmata", //
				"pingprobe", true, //
				"waitsecs", 10 //
		)).build();
	}

}
