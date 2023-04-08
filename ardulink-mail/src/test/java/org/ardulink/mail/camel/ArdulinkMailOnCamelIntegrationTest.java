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
import static java.lang.System.identityHashCode;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.joining;
import static org.apache.camel.builder.AggregationStrategies.string;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.proto.impl.ALProtoBuilder.alpProtocolMessage;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static org.ardulink.core.proto.impl.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;
import static org.ardulink.mail.test.MailSender.mailFrom;
import static org.ardulink.mail.test.MailSender.send;
import static org.ardulink.testsupport.mock.TestSupport.getMock;
import static org.ardulink.util.MapBuilder.newMapBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

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
import org.ardulink.core.convenience.Links;
import org.ardulink.util.Joiner;
import org.ardulink.util.Throwables;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.icegreen.greenmail.imap.ImapServer;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.smtp.SmtpServer;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
@Timeout(10)
class ArdulinkMailOnCamelIntegrationTest {

	private static final String mockURI = "ardulink://mock";

	private Link link;

	@RegisterExtension
	GreenMailExtension mailMock = new GreenMailExtension(SMTP_IMAP);

	@BeforeEach
	void setup() throws Exception {
		link = Links.getLink(mockURI);
	}

	@AfterEach
	void tearDown() throws IOException {
		link.close();
	}

	@Test
	void readsFromImap_controlsArdulink_sendsResultToEndpoint() throws Exception {
		String receiverUser = "receiver";
		String username = "loginIdReceiver";
		String password = "secretOfReceiver";
		String receiver = receiverUser + "@" + "someReceiverDomain.com";
		createMailUser(receiver, username, password);

		String validSender = "valid.sender@someSenderDomain.com";
		createMailUser(validSender, "loginIdSender", "secretOfSender");
		String commandName = "usedScenario";
		send(mailFrom(validSender).to(receiver).withSubject(anySubject()).withText(commandName));

		try (CamelContext context = new DefaultCamelContext()) {
			String switchDigitalPin = alpProtocolMessage(DIGITAL_PIN_READ).forPin(1).withState(true);
			String switchAnalogPin = alpProtocolMessage(ANALOG_PIN_READ).forPin(2).withValue(123);

			context.addRoutes(ardulinkProcessing(imapUri(username, password), swapUpperLower(validSender), commandName,
					asList(switchDigitalPin, switchAnalogPin), makeURI(mockURI, emptyMap()), "mock:result"));
			context.start();

			Link mockLink = getMock(link);
			try {
				verify(mockLink, timeout(SECONDS.toMillis(5))).switchDigitalPin(digitalPin(1), true);
				verify(mockLink, timeout(SECONDS.toMillis(5))).switchAnalogPin(analogPin(2), 123);

				MockEndpoint mockEndpoint = context.getEndpoint("mock:result", MockEndpoint.class);
				mockEndpoint.expectedMessageCount(1);
				mockEndpoint.expectedBodiesReceived(switchDigitalPin + "=OK" + "\r\n" + switchAnalogPin + "=OK");
				mockEndpoint.assertIsSatisfied();
			} finally {
				context.stop();
			}
			verify(mockLink).close();
			verifyNoMoreInteractions(mockLink);
		}

	}

	private String swapUpperLower(String in) {
		String swapped = in.codePoints().mapToObj(c -> Character.valueOf((char) c)).map(c -> {
			return Character.isUpperCase(c) //
					? Character.toLowerCase(c)
					: Character.isLowerCase(c) //
							? Character.toUpperCase(c)
							: c;
		}).map(String::valueOf).collect(joining());
		assert !swapped.equals(in);
		assert swapped.equalsIgnoreCase(in);
		return swapped;
	}

	@Test
	void writesResultToSender() throws Exception {
		String receiverUser = "receiver";
		String username = "loginIdReceiver";
		String password = "secretOfReceiver";
		String receiver = receiverUser + "@" + "someReceiverDomain.com";
		createMailUser(receiver, username, password);

		String validSender = "valid.sender@someSenderDomain.com";
		createMailUser(validSender, "loginIdSender", "secretOfSender");
		String commandName = "usedScenario";
		send(mailFrom(validSender).to(receiver).withSubject(anySubject()).withText(commandName));

		String ardulink = makeURI(mockURI, newMapBuilder().build());

		String switchDigitalPin = alpProtocolMessage(DIGITAL_PIN_READ).forPin(1).withState(true);
		String switchAnalogPin = alpProtocolMessage(ANALOG_PIN_READ).forPin(2).withValue(123);

		try (CamelContext context = new DefaultCamelContext()) {
			String smtpName = "direct:routeLink-" + UUID.randomUUID();
			context.addRoutes(setToAndFromHeaderAndSendTo(smtpName, smtpUri(username, password)));
			context.addRoutes(ardulinkProcessing(imapUri(username, password), validSender, commandName,
					asList(switchDigitalPin, switchAnalogPin), ardulink, smtpName));
			context.start();

			try {
				assertThat(fetchMails("loginIdSender", "secretOfSender")).singleElement()
						.satisfies(m -> assertThat(m.getContent())
								.isEqualTo(switchDigitalPin + "=OK\r\n" + switchAnalogPin + "=OK"));

			} finally {
				context.stop();
			}
		}

	}

	@Test
	void writesResultToSender_ConfiguredViaProperties() throws Exception {
		String receiver = "receiver@someReceiverDomain.com";
		String username = "loginIdReceiver";
		String password = "secretOfReceiver";
		createMailUser(receiver, username, password);

		String validSender = "valid.sender@someSenderDomain.com";
		createMailUser(validSender, "loginIdSender", "secretOfSender");

		String commandName = "usedScenario";
		String command1 = alpProtocolMessage(DIGITAL_PIN_READ).forPin(1).withState(true);
		String command2 = alpProtocolMessage(ANALOG_PIN_READ).forPin(2).withValue(123);
		String command = command1 + "," + command2;

		send(mailFrom(validSender).to(receiver).withSubject(anySubject()).withText(commandName));

		Main main = new Main();
		main.addProperty("from", imapUri(username, password));
		main.addProperty("to", smtpUri(username, password));
//		main.addProperty("commandName", commandName);
//		main.addProperty("command", command);

		String smtpRouteStart = "direct:smtp-" + UUID.randomUUID();
		main.configure().addRoutesBuilder(setToAndFromHeaderAndSendTo(smtpRouteStart, "{{to}}"));
		main.configure().addRoutesBuilder(ardulinkProcessing("{{from}}", validSender, commandName,
				asList(command.split("\\,")), makeURI(mockURI, emptyMap()), smtpRouteStart));
		runInBackground(main);

		try {
			assertThat(fetchMails("loginIdSender", "secretOfSender")).singleElement()
					.satisfies(m -> assertThat(m.getContent()).isEqualTo(command1 + "=OK\r\n" + command2 + "=OK"));
		} finally {
			main.stop();
		}

	}

	private RouteBuilder ardulinkProcessing(String from, String validSender, String commandName, List<String> commands,
			String ardulink, String to) {
		return new RouteBuilder() {
			@Override
			public void configure() {
				String splitter = "direct:splitter-" + identityHashCode(this);
				from(splitter).split(body(), string("\r\n")).to(ardulink).end().to(to);
				from(from) //
						.filter(header("From").isEqualToIgnoreCase(validSender)) //
						.choice() //
						.when(body().isEqualToIgnoreCase(commandName)).setBody(constant(commands)).to(splitter) //
						.otherwise().stop() //
				;
			}
		};
	}

	private RouteBuilder setToAndFromHeaderAndSendTo(String from, String to) {
		return new RouteBuilder() {
			@Override
			public void configure() throws Exception {
				from(from).setHeader("to", simple("${in.header.from}")).setHeader("from", simple("${in.header.to}"))
						.to(to);
			}
		};
	}

	private String imapUri(String username, String password) {
		ImapServer imapd = mailMock.getImap();
		return makeURI("imap://" + imapd.getBindTo() + ":" + imapd.getPort(), newMapBuilder() //
				.put("username", username) //
				.put("password", password) //
				.put("delete", true) //
				.put("initialDelay", 0) //
				.put("delay", 10) //
				.put("timeUnit", MINUTES.name()) //
				.build() //
		);
	}

	private String smtpUri(String username, String password) {
		SmtpServer smtpd = mailMock.getSmtp();
		return makeURI("smtp://" + smtpd.getBindTo() + ":" + smtpd.getPort(), newMapBuilder() //
				.put("username", username) //
				.put("password", password) //
				.put("debugMode", true) //
				.build());
	}

	private void runInBackground(Main main) {
		newSingleThreadExecutor().execute(() -> {
			try {
				main.run();
			} catch (Exception e) {
				Throwables.propagate(e);
			}
		});
	}

	private void createMailUser(String email, String login, String password) {
		mailMock.setUser(email, login, password);
	}

	private List<Message> fetchMails(String loginId, String password) throws MessagingException, InterruptedException {
		ImapServer imapd = mailMock.getImap();
		List<Message> messages = new ArrayList<>();
		await().pollInterval(100, MILLISECONDS)
				.until(() -> messages.addAll(retrieveViaImap(imapd.getBindTo(), imapd.getPort(), loginId, password)));
		return messages;
	}

	private List<Message> retrieveViaImap(String host, int port, String user, String password)
			throws MessagingException {
		Properties props = new Properties();
		props.setProperty("mail.store.protocol", "imap");
		props.setProperty("mail.imap.port", String.valueOf(port));
		Session session = Session.getInstance(props, null);
		Store store = session.getStore();
		store.connect(host, user, password);
		Folder inbox = store.getFolder("INBOX");
		inbox.open(Folder.READ_ONLY);
		return asList(inbox.getMessages());
	}

	private String makeURI(String uri, Map<? extends Object, ? extends Object> kv) {
		return uri + "?" + Joiner.on("&").withKeyValueSeparator("=").join(kv);
	}

	private String anySubject() {
		return "Subject";
	}

}
