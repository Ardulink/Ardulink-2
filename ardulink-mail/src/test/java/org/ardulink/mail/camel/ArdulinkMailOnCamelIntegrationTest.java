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

package org.ardulink.mail.camel;

import static com.icegreen.greenmail.util.ServerSetupTest.SMTP_IMAP;
import static java.util.Collections.emptyMap;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.proto.impl.ALProtoBuilder.alpProtocolMessage;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;
import static org.ardulink.mail.camel.FromValidator.validateFromHeader;
import static org.ardulink.mail.camel.ScenarioProcessor.processScenario;
import static org.ardulink.mail.camel.StringJoiningStrategy.joinUsing;
import static org.ardulink.mail.test.MailSender.mailFrom;
import static org.ardulink.mail.test.MailSender.send;
import static org.ardulink.util.MapBuilder.newMapBuilder;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executors;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.main.Main;
import org.ardulink.core.Link;
import org.ardulink.core.convenience.LinkDelegate;
import org.ardulink.core.convenience.Links;
import org.ardulink.util.Joiner;
import org.ardulink.util.Throwables;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.Timeout;

import com.icegreen.greenmail.imap.ImapServer;
import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.smtp.SmtpServer;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ArdulinkMailOnCamelIntegrationTest {

	private static final String mockURI = "ardulink://mock";

	private Link link;

	@Rule
	public GreenMailRule mailMock = new GreenMailRule(SMTP_IMAP);

	@Rule
	public Timeout timeout = new Timeout(10, SECONDS);

	@Rule
	public TemporaryFolder tmpFolder = new TemporaryFolder();

	@Before
	public void setup() throws URISyntaxException, Exception {
		link = Links.getLink(mockURI);
	}

	@After
	public void tearDown() throws IOException {
		link.close();
	}

	@Test
	public void canProcessViaImap() throws Exception {
		String user = "receiver";
		String username = "loginIdReceiver";
		String password = "secretOfReceiver";
		String receiver = user + "@" + "someReceiverDomain.com";
		createMailUser(receiver, username, password);

		String validSender = "valid.sender@someSenderDomain.com";
		send(mailFrom(validSender).to(receiver).withSubject(anySubject()).withText("usedScenario"));

		String devNull = makeURI(mockURI, emptyMap());

		try (CamelContext context = new DefaultCamelContext()) {
			context.addRoutes(
					ardulinkProcessing(localImap(username, password), validSender,
							Arrays.asList(alpProtocolMessage(DIGITAL_PIN_READ).forPin(13).withState(true),
									alpProtocolMessage(ANALOG_PIN_READ).forPin(2).withValue(123)),
							devNull, "mock:noop"));
			context.start();

			Link mock = getMock();
			try {
				verify(mock, timeout(5_000)).switchDigitalPin(digitalPin(13), true);
				verify(mock, timeout(5_000)).switchAnalogPin(analogPin(2), 123);
			} finally {
				context.stop();
			}
			verify(mock).close();
			verifyNoMoreInteractions(mock);
		}
	}

	private String localImap(String username, String password) {
		return makeURI("imap://localhost:" + mailMock.getImap().getPort(), newMapBuilder() //
				.put("username", username) //
				.put("password", password) //
				.put("delete", true) //
				.put("initialDelay", 0) //
				.put("delay", 10) //
				.put("timeUnit", MINUTES.name()) //
				.build() //
		);
	}

	@Test
	public void writesResultToMock() throws Exception {
		String receiverUser = "receiver";
		String username = "loginIdReceiver";
		String password = "secretOfReceiver";
		String receiver = receiverUser + "@" + "someReceiverDomain.com";
		createMailUser(receiver, username, password);

		String validSender = "valid.sender@someSenderDomain.com";
		createMailUser(validSender, "loginIdSender", "secretOfSender");
		send(mailFrom(validSender).to(receiver).withSubject(anySubject()).withText("usedScenario"));

		String ardulink = makeURI(mockURI, emptyMap());

		try (CamelContext context = new DefaultCamelContext()) {
			String switchDigitalPin = alpProtocolMessage(DIGITAL_PIN_READ).forPin(1).withState(true);
			String switchAnalogPin = alpProtocolMessage(ANALOG_PIN_READ).forPin(2).withValue(123);

			context.addRoutes(ardulinkProcessing(localImap(username, password), validSender,
					Arrays.asList(switchDigitalPin, switchAnalogPin), ardulink, "mock:result"));
			context.start();
			try {
				MockEndpoint mockEndpoint = context.getEndpoint("mock:result", MockEndpoint.class);
				mockEndpoint.expectedMessageCount(1);
				mockEndpoint.expectedBodiesReceived(switchDigitalPin + "=OK" + "\r\n" + switchAnalogPin + "=OK");
				mockEndpoint.assertIsSatisfied();
			} finally {
				context.stop();
			}
		}

	}

	private RouteBuilder ardulinkProcessing(String from, String validSender, List<String> commands, String ardulink,
			String to) {
		return new RouteBuilder() {

			@Override
			public void configure() {
				List<String> dummy1 = Arrays.asList(alpProtocolMessage(DIGITAL_PIN_READ).forPin(13).withState(false),
						alpProtocolMessage(ANALOG_PIN_READ).forPin(2).withValue(42));
				List<String> dummy2 = Arrays.asList(alpProtocolMessage(DIGITAL_PIN_READ).forPin(13).withState(false),
						alpProtocolMessage(ANALOG_PIN_READ).forPin(2).withValue(21));
				from(from) //
						.process(validateFromHeader(Arrays.asList(validSender))) //
						.process(processScenario() //
								.withCommand("xxx", dummy1) //
								.withCommand("usedScenario", commands) //
								.withCommand("yyy", dummy2) //
				).split(body(), joinUsing("\r\n")).to(ardulink).end().to(to);
			}
		};
	}

	@Test
	public void writesResultToSender() throws Exception {
		String receiverUser = "receiver";
		String username = "loginIdReceiver";
		String password = "secretOfReceiver";
		String receiver = receiverUser + "@" + "someReceiverDomain.com";
		createMailUser(receiver, username, password);

		String validSender = "valid.sender@someSenderDomain.com";
		createMailUser(validSender, "loginIdSender", "secretOfSender");
		send(mailFrom(validSender).to(receiver).withSubject(anySubject()).withText("usedScenario"));

		String ardulink = makeURI(mockURI, newMapBuilder().build());

		String switchDigitalPin = alpProtocolMessage(DIGITAL_PIN_READ).forPin(1).withState(true);
		String switchAnalogPin = alpProtocolMessage(ANALOG_PIN_READ).forPin(2).withValue(123);

		try (CamelContext context = new DefaultCamelContext()) {
			String smtpName = "direct:routeLink-" + UUID.randomUUID();
			context.addRoutes(setToAndFrom(smtpName, smtpUri()));
			context.addRoutes(ardulinkProcessing(localImap(username, password), validSender,
					Arrays.asList(switchDigitalPin, switchAnalogPin), ardulink, smtpName));
			context.start();

			try {
				assertThat(((String) fetchMail("loginIdSender", "secretOfSender").getContent()),
						is(switchDigitalPin + "=OK\r\n" + switchAnalogPin + "=OK"));
			} finally {
				context.stop();
			}
		}

	}

	@Test
	public void writesResultToSender_ConfiguredViaProperties() throws Exception {
		String receiver = "receiver@someReceiverDomain.com";
		createMailUser(receiver, "loginIdReceiver", "secretOfReceiver");

		String validSender = "valid.sender@someSenderDomain.com";
		createMailUser(validSender, "loginIdSender", "secretOfSender");

		String commandName = "usedScenario";
		String username = "loginIdReceiver";
		String password = "secretOfReceiver";
		createMailUser(receiver, username, password);

		send(mailFrom(validSender).to(receiver).withSubject(anySubject()).withText(commandName));

		String switchDigitalPin = alpProtocolMessage(DIGITAL_PIN_READ).forPin(1).withState(true);
		String switchAnalogPin = alpProtocolMessage(ANALOG_PIN_READ).forPin(2).withValue(123);
		String ardulink = makeURI(mockURI, emptyMap());

		String smtpRouteStart = "direct:smtp-" + UUID.randomUUID();

		Main main = new Main();
		addProperties(main, commandName, Arrays.asList(switchDigitalPin, switchAnalogPin));
		main.addRoutesBuilder(setToAndFrom(smtpRouteStart, smtpUri()));
		main.addRoutesBuilder(ardulinkProcessing(localImap(username, password), validSender,
				Arrays.asList(switchDigitalPin, switchAnalogPin), ardulink, smtpRouteStart));
		runInBackground(main);

		try {
			assertThat(((String) fetchMail("loginIdSender", "secretOfSender").getContent()),
					is(is(alpProtocolMessage(DIGITAL_PIN_READ).forPin(1).withState(true) + "=OK\r\n"
							+ alpProtocolMessage(ANALOG_PIN_READ).forPin(2).withValue(123) + "=OK")));
		} finally {
			main.stop();
		}

	}

	private RouteBuilder setToAndFrom(String from, String to) {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from(from).setHeader("to", simple("${in.header.from}")).setHeader("from", simple("${in.header.to}"))
						.to(to);
			}
		};
	}

	private String smtpUri() {
		SmtpServer smtpd = mailMock.getSmtp();
		return "smtp://" + smtpd.getBindTo() + ":" + smtpd.getPort() + "?username=" + "loginIdReceiver" + "&password="
				+ "secretOfReceiver" + "&debugMode=true";
	}

	private void runInBackground(Main main) {
		Executors.newSingleThreadExecutor().execute(new Runnable() {
			@Override
			public void run() {
				try {
					main.run();
				} catch (Exception e) {
					Throwables.propagate(e);
				}
			}
		});
	}

	private void addProperties(Main main, String commandName, List<String> commands) {
		main.addProperty("imaphost", mailMock.getImap().getBindTo());
		main.addProperty("imapport", String.valueOf(mailMock.getImap().getPort()));
		main.addProperty("commandname", commandName);
		main.addProperty("command", Joiner.on(",").join(commands));
		main.addProperty("smtphost", mailMock.getSmtp().getBindTo());
		main.addProperty("smtpport", String.valueOf(mailMock.getSmtp().getPort()));
	}

	private void createMailUser(String email, String login, String password) {
		mailMock.setUser(email, login, password);
	}

	private Message fetchMail(String loginId, String password) throws MessagingException, InterruptedException {
		ImapServer imapd = mailMock.getImap();
		Message msg = null;
		while ((msg = retrieveViaImap(imapd.getBindTo(), imapd.getPort(), loginId, password)) == null) {
			MILLISECONDS.sleep(100);
		}
		return msg;
	}

	private Message retrieveViaImap(String host, int port, String user, String password) throws MessagingException {
		Properties props = new Properties();
		props.setProperty("mail.store.protocol", "imap");
		props.setProperty("mail.imap.port", String.valueOf(port));
		Session session = Session.getInstance(props, null);
		Store store = session.getStore();
		store.connect(host, user, password);
		Folder inbox = store.getFolder("INBOX");
		inbox.open(Folder.READ_ONLY);
		int messageCount = inbox.getMessageCount();
		return messageCount == 0 ? null : inbox.getMessage(1);
	}

	private String makeURI(String uri, Map<? extends Object, ? extends Object> kv) {
		return uri + "?" + Joiner.on("&").withKeyValueSeparator("=").join(kv);
	}

	private String anySubject() {
		return "Subject";
	}

	private Link getMock() {
		return getMock(link);
	}

	private Link getMock(Link link) {
		return ((LinkDelegate) link).getDelegate();
	}

}
