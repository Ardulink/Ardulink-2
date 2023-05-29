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

import static org.ardulink.util.URIs.newURI;
import static org.assertj.core.api.Assertions.assertThat;

import org.ardulink.core.linkmanager.LinkManager;
import org.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import org.ardulink.core.linkmanager.LinkManager.Configurer;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;
import org.junit.jupiter.api.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class SerialLinkFactoryIntegrationTest {

	LinkManager connectionManager = LinkManager.getInstance();
	Configurer configurer = connectionManager.getConfigurer(newURI("ardulink://" + new SerialLinkFactory().getName()));

	@Test
	void canConfigureSerialConnectionViaConfigurer() {
		assertThat(configurer.getAttributes()).containsExactly("port", "baudrate", "proto", "qos", "waitsecs",
				"pingprobe");

		assertThat(attribute("port").hasChoiceValues()).isTrue();
		assertThat(attribute("proto").hasChoiceValues()).isTrue();
		assertThat(attribute("baudrate").hasChoiceValues()).isFalse();
		assertThat(attribute("qos").hasChoiceValues()).isFalse();
		assertThat(attribute("waitsecs").hasChoiceValues()).isFalse();

		assertThat(attribute("port").getChoiceValues()).isNotNull();

		attribute("proto").setValue(ardulink2());
		attribute("baudrate").setValue(115200);
		attribute("qos").setValue(true);
		attribute("waitsecs").setValue(42);
	}

	private ConfigAttribute attribute(String name) {
		return configurer.getAttribute(name);
	}

	private static String ardulink2() {
		return ArdulinkProtocol2.instance().getName();
	}

}
