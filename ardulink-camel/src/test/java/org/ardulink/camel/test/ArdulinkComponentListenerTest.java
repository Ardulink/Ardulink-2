package org.ardulink.camel.test;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.ardulink.core.Link;
import org.ardulink.core.convenience.LinkDelegate;
import org.ardulink.core.convenience.Links;
import org.ardulink.util.URIs;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

public class ArdulinkComponentListenerTest {

	@Rule
	public Timeout timeout = new Timeout(5, SECONDS);

	private static final String MOCK_URI = "ardulink://mock";

	private Link link;

	@Before
	public void setup() throws Exception {
		link = Links.getLink(URIs.newURI(MOCK_URI));
	}

	@After
	public void tearDown() throws IOException {
		link.close();
	}

	@Test
	public void startListeningOnPassedPins() throws Exception {
		haltCamel(startCamel("listenTo=d1,d2,a1"));
		Link mock = getMock(link);
		verify(mock).startListening(digitalPin(1));
		verify(mock).startListening(digitalPin(2));
		verify(mock).startListening(analogPin(1));
		verify(mock).close();
		verifyNoMoreInteractions(mock);
	}

	@Test
	public void listeningIsCaseInsensitive() throws Exception {
		haltCamel(startCamel("listenTo=d1,D2,a3,A4"));
		Link mock = getMock(link);
		verify(mock).startListening(digitalPin(1));
		verify(mock).startListening(digitalPin(2));
		verify(mock).startListening(analogPin(3));
		verify(mock).startListening(analogPin(4));
		verify(mock).close();
		verifyNoMoreInteractions(mock);
	}

	@Test
	public void ignoresMultipleOccurencesOfSamePin() throws Exception {
		haltCamel(startCamel("listenTo=d1,D1,a2,A2"));
		Link mock = getMock(link);
		verify(mock).startListening(digitalPin(1));
		verify(mock).startListening(analogPin(2));
		verify(mock).close();
		verifyNoMoreInteractions(mock);
	}

	private CamelContext haltCamel(CamelContext context) throws Exception {
		context.stop();
		return context;
	}

	private CamelContext startCamel(final String args) throws Exception {
		CamelContext context = new DefaultCamelContext();
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() {
				from(noMatterWhat()).to(MOCK_URI + "?" + args);
			}
		});
		context.start();
		return context;
	}

	private String noMatterWhat() {
		return "direct:bean";
	}

	private Link getMock(Link link) {
		return ((LinkDelegate) link).getDelegate();
	}

}
