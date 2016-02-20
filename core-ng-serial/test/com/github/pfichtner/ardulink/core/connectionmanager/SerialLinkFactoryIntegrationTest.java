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

package com.github.pfichtner.ardulink.core.connectionmanager;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;

import com.github.pfichtner.ardulink.core.linkmanager.LinkManager;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.Configurer;

// because JNI dependent
@Ignore
public class SerialLinkFactoryIntegrationTest {

	@Test
	public void canConfigureSerialConnectionViaURI() throws Exception {
		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(new URI(
				"ardulink://serial?port=anyString&speed=9600"));
		assertNotNull(configurer.newLink());
	}

	@Test
	public void canConfigureSerialConnectionViaConfigurer() throws Exception {
		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(new URI(
				"ardulink://serial"));

		assertThat(new ArrayList<String>(configurer.getAttributes()),
				is(Arrays.asList("port", "proto", "speed")));

		ConfigAttribute port = configurer.getAttribute("port");
		ConfigAttribute proto = configurer.getAttribute("proto");
		ConfigAttribute speed = configurer.getAttribute("speed");

		assertThat(port.hasChoiceValues(), is(TRUE));
		assertThat(proto.hasChoiceValues(), is(FALSE));
		assertThat(speed.hasChoiceValues(), is(FALSE));

		assertThat(port.getChoiceValues(), is(notNullValue()));

		port.setValue("anyString");
		speed.setValue(115200);
	}

}
