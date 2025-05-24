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

package org.ardulink.camel;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.testsupport.mock.TestSupport.getMock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.ardulink.core.Link;
import org.ardulink.core.convenience.Links;
import org.ardulink.testsupport.mock.junit5.MockUri;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
@Timeout(value = 5, unit = SECONDS)
class ArdulinkComponentListenerTest {

	@Test
	void startListeningOnPassedPins(@MockUri String mockUri) throws Exception {
		Link mock;
		try (Link link = Links.getLink(mockUri)) {
			haltCamel(startCamel(mockUri + "&listenTo=d1,d2,a1"));
			mock = getMock(link);
			verify(mock).startListening(digitalPin(1));
			verify(mock).startListening(digitalPin(2));
			verify(mock).startListening(analogPin(1));
		}
		verify(mock).close();
		verifyNoMoreInteractions(mock);
	}

	@Test
	void listeningIsCaseInsensitive(@MockUri String mockUri) throws Exception {
		Link mock;
		try (Link link = Links.getLink(mockUri)) {
			haltCamel(startCamel(mockUri + "&listenTo=d1,D2,a3,A4"));
			mock = getMock(link);
			verify(mock).startListening(digitalPin(1));
			verify(mock).startListening(digitalPin(2));
			verify(mock).startListening(analogPin(3));
			verify(mock).startListening(analogPin(4));
		}
		verify(mock).close();
		verifyNoMoreInteractions(mock);
	}

	@Test
	void ignoresMultipleOccurencesOfSamePin(@MockUri String mockUri) throws Exception {
		Link mock;
		try (Link link = Links.getLink(mockUri)) {
			haltCamel(startCamel(mockUri + "&listenTo=d1,D1,a2,A2"));
			mock = getMock(link);
			verify(mock).startListening(digitalPin(1));
			verify(mock).startListening(analogPin(2));
		}
		verify(mock).close();
		verifyNoMoreInteractions(mock);
	}

	private CamelContext haltCamel(CamelContext context) throws Exception {
		context.stop();
		return context;
	}

	private CamelContext startCamel(String uri) throws Exception {
		CamelContext context = new DefaultCamelContext();
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() {
				from(noMatterWhat()).to(uri);
			}
		});
		context.start();
		return context;
	}

	private String noMatterWhat() {
		return "direct:bean";
	}

}
