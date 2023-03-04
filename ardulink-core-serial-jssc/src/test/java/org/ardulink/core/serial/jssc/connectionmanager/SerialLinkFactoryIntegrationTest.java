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

package org.ardulink.core.serial.jssc.connectionmanager;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.ardulink.util.Lists.newArrayList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.jupiter.api.Assumptions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.ardulink.core.Link;
import org.ardulink.core.events.CustomEvent;
import org.ardulink.core.events.CustomListener;
import org.ardulink.core.events.RplyEvent;
import org.ardulink.core.events.RplyListener;
import org.ardulink.core.linkmanager.LinkManager;
import org.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import org.ardulink.core.linkmanager.LinkManager.Configurer;
import org.ardulink.util.URIs;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import jssc.SerialPortList;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class SerialLinkFactoryIntegrationTest {

	private Link link;

	@Test
	void canConfigureSerialConnectionViaURI() throws Exception {
		String[] portNames = SerialPortList.getPortNames();
		assumeTrue(portNames.length > 0);

		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(URIs
				.newURI("ardulink://serial-jssc?port=" + portNames[0] + "&baudrate=9600&pingprobe=false&waitsecs=1"));
		Link link = configurer.newLink();
		assertNotNull(link);
		link.close();
	}

	@Test
	void canConfigureSerialConnectionViaConfigurer() {
		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(URIs.newURI("ardulink://serial-jssc"));

		assertThat(newArrayList(configurer.getAttributes()),
				is(newArrayList("port", "baudrate", "proto", "qos", "waitsecs", "pingprobe")));

		ConfigAttribute port = configurer.getAttribute("port");
		ConfigAttribute proto = configurer.getAttribute("proto");
		ConfigAttribute baudrate = configurer.getAttribute("baudrate");

		assertThat(port.hasChoiceValues(), is(TRUE));
		assertThat(proto.hasChoiceValues(), is(TRUE));
		assertThat(baudrate.hasChoiceValues(), is(FALSE));

		assertThat(port.getChoiceValues(), is(notNullValue()));

		port.setValue("anyString");
		baudrate.setValue(115200);
	}

	@Test
	@Disabled

	void connectionListeningTest() throws IOException, InterruptedException {
		Configurer configurer = LinkManager.getInstance().getConfigurer(URIs.newURI("ardulink://serial-jssc/"));

		ConfigAttribute portAttribute = configurer.getAttribute("port");
		System.out.println(portAttribute.getChoiceValues()[0]);
		portAttribute.setValue(portAttribute.getChoiceValues()[0]);

		ConfigAttribute pingprobeAttribute = configurer.getAttribute("pingprobe");
		pingprobeAttribute.setValue(false);

		link = configurer.newLink();

		link.addRplyListener(new RplyListener() {

			@Override
			public void rplyReceived(RplyEvent e) {
				Map<String, Object> parameters = e.getParameters();
				for (String key : parameters.keySet()) {
					System.out.println(key + "=" + parameters.get(key));
				}

			}
		});

		link.addCustomListener(new CustomListener() {

			@Override
			public void customEventReceived(CustomEvent e) {
				System.out.println(e.getMessage());
			}
		});

		TimeUnit.SECONDS.sleep(10);

		sendCustom();

		TimeUnit.SECONDS.sleep(10);

		link.close();
	}

	@Test
	void cantConnectWithoutPort() {
		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(URIs.newURI("ardulink://serial-jssc?baudrate=9600"));
		assertThrows(RuntimeException.class, () -> configurer.newLink());
	}

	public void sendCustom() throws IOException {
		link.sendCustomMessage("getUniqueID", "XXX");
	}

}
