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
package org.ardulink.camel.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.ardulink.camel.ArdulinkEndpoint;
import org.ardulink.camel.test.translate.DummyToArdulinkMessageProcessor;
import org.ardulink.core.Link;
import org.ardulink.core.virtual.VirtualLink;
import org.junit.Ignore;
import org.junit.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ArdulinkComponentTest {

	private static final String IN = "direct:in";
	private static final String OUT = "mock:result";

	@Test
	public void canGetRegisteredXXXXXXXXXX() {
		ArdulinkEndpoint endpoint = new DefaultCamelContext().getEndpoint(
				"ardulink://virtual", ArdulinkEndpoint.class);
		Link link = endpoint.getLink();
		assertThat(link.getClass().getName(), is(VirtualLink.class.getName()));
	}

	@Test
	public void canProcessCustomMessage() throws Exception {
		CamelContext context = addProcessorBeanRoute(new DefaultCamelContext());
		context.start();
		try {
			MockEndpoint mockEndpoint = getMockEndpoint(context);
			mockEndpoint.expectedBodiesReceived("dummy");
			send(context, "send Custom Message");
			mockEndpoint.assertIsSatisfied();
		} finally {
			context.stop();
		}
	}

	@Test
	@Ignore
	public void setFaultFlagsOnUnkwnonMessage() throws Exception {
		CamelContext context = addProcessorBeanRoute(new DefaultCamelContext());
		context.start();
		try {
			MockEndpoint mockEndpoint = getMockEndpoint(context);
			send(context, "this should do nothing");
			// ...
			mockEndpoint.assertIsSatisfied();
		} finally {
			context.stop();
		}
	}

	private void send(CamelContext context, String body) {
		context.createProducerTemplate().sendBody(IN, body);
	}

	private MockEndpoint getMockEndpoint(CamelContext context) {
		return context.getEndpoint(OUT, MockEndpoint.class);
	}

	private CamelContext addProcessorBeanRoute(CamelContext context)
			throws Exception {
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() {
				from(IN).bean(new DummyToArdulinkMessageProcessor()).to(OUT);
			}
		});
		return context;
	}

}
