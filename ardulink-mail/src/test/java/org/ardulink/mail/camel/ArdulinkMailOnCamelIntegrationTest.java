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
import static java.lang.String.format;
import static java.lang.System.identityHashCode;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.camel.builder.AggregationStrategies.string;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.alpProtocolMessage;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.ANALOG_PIN_READ;
import static org.ardulink.core.proto.ardulink.ALProtoBuilder.ALPProtocolKey.DIGITAL_PIN_READ;
import static org.ardulink.mail.test.MailSender.mailFrom;
import static org.ardulink.mail.test.MailSender.send;
import static org.ardulink.testsupport.mock.TestSupport.getMock;
import static org.ardulink.util.Maps.toProperties;
import static org.ardulink.util.Strings.swapUpperLower;
import static org.ardulink.util.Throwables.propagate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

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
import org.ardulink.testsupport.mock.junit5.MockUri;
import org.ardulink.util.Joiner;
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
@Timeout(value = ArdulinkMailOnCamelIntegrationTest.TIMEOUT_SECS, unit = SECONDS)
class ArdulinkMailOnCamelIntegrationTest {

	static final int TIMEOUT_SECS = 30;

	@RegisterExtension
	GreenMailExtension mailMock = new GreenMailExtension(SMTP_IMAP);

	@Test
	void readsFromImap_controlsArdulink_sendsResultToEndpoint(@MockUri String mockUri) throws Exception {
		try (Link link = Links.getLink(mockUri)) {
			String receiverUser = "receiver";
			String username = "loginIdReceiver";
			String password = "secretOfReceiver";
			String receiver = receiverUser + "@" + "someReceiverDomain.com";
			createMailUser(receiver, username, password);

			String validSender = "valid.sender@someSenderDomain.com";
			createMailUser(validSender, "loginIdSender", "secretOfSender");
			String commandName = "usedScenario";
			send(mailFrom(validSender).to(receiver).withSubject(anySubject()).withText(commandName));

			Link mockLink = getMock(link);
			try (CamelContext context = new DefaultCamelContext()) {
				String switchDigitalPin = alpProtocolMessage(DIGITAL_PIN_READ).forPin(1).withState(true);
				String switchAnalogPin = alpProtocolMessage(ANALOG_PIN_READ).forPin(2).withValue(123);

				context.addRoutes(ardulinkProcessing(imapUri(username, password), swapUpperLower(validSender),
						commandName, asList(switchDigitalPin, switchAnalogPin), makeURI(mockUri, emptyMap()),
						"mock:result"));
				context.start();

				long timeoutMillis = SECONDS.toMillis(TIMEOUT_SECS);
				verify(mockLink, timeout(timeoutMillis)).switchDigitalPin(digitalPin(1), true);
				verify(mockLink, timeout(timeoutMillis)).switchAnalogPin(analogPin(2), 123);

				MockEndpoint mockEndpoint = context.getEndpoint("mock:result", MockEndpoint.class);
				mockEndpoint.expectedMessageCount(1);
				mockEndpoint.expectedBodiesReceived(switchDigitalPin + "=OK" + "\r\n" + switchAnalogPin + "=OK");
				mockEndpoint.assertIsSatisfied();
			}
			verify(mockLink).close();
			verifyNoMoreInteractions(mockLink);
		}

	}

	@Test
	void writesResultToSender(@MockUri String mockUri) throws Exception {
		String receiverUser = "receiver";
		String username = "loginIdReceiver";
		String password = "secretOfReceiver";
		String receiver = format("%s@someReceiverDomain.com", receiverUser);
		createMailUser(receiver, username, password);

		String validSender = "valid.sender@someSenderDomain.com";
		createMailUser(validSender, "loginIdSender", "secretOfSender");
		String commandName = "usedScenario";
		send(mailFrom(validSender).to(receiver).withSubject(anySubject()).withText(commandName));

		String ardulink = makeURI(mockUri, emptyMap());

		String switchDigitalPin = alpProtocolMessage(DIGITAL_PIN_READ).forPin(1).withState(true);
		String switchAnalogPin = alpProtocolMessage(ANALOG_PIN_READ).forPin(2).withValue(123);

		try (CamelContext context = new DefaultCamelContext()) {
			String smtpName = "direct:routeLink-" + UUID.randomUUID();
			context.addRoutes(setToAndFromHeaderAndSendTo(smtpName, smtpUri(username, password)));
			context.addRoutes(ardulinkProcessing(imapUri(username, password), validSender, commandName,
					asList(switchDigitalPin, switchAnalogPin), ardulink, smtpName));
			context.start();

			assertThat(fetchMails("loginIdSender", "secretOfSender")).singleElement().satisfies(
					m -> assertThat(m.getContent()).isEqualTo(switchDigitalPin + "=OK\r\n" + switchAnalogPin + "=OK"));
		}

	}

	@Test
	void writesResultToSender_ConfiguredViaProperties(@MockUri String mockUri) throws Exception {
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
				asList(command.split("\\,")), makeURI(mockUri, emptyMap()), smtpRouteStart));
		runInBackground(main);

		try {
			assertThat(fetchMails("loginIdSender", "secretOfSender")).singleElement()
					.satisfies(m -> assertThat(m.getContent()).isEqualTo(command1 + "=OK\r\n" + command2 + "=OK"));
		} finally {
			main.stop();
		}

	}

	private static RouteBuilder ardulinkProcessing(String from, String validSender, String commandName,
			List<String> commands, String ardulink, String to) {
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

	private static RouteBuilder setToAndFromHeaderAndSendTo(String from, String to) {
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
		return makeURI(format("imap://%s:%d", imapd.getBindTo(), imapd.getPort()), Map.of( //
				"username", username, //
				"password", password, //
				"delete", true, //
				"initialDelay", 0, //
				"delay", 10, //
				"timeUnit", MINUTES.name() //
		));
	}

	private String smtpUri(String username, String password) {
		SmtpServer smtpd = mailMock.getSmtp();
		return makeURI(format("smtp://%s:%d", smtpd.getBindTo(), smtpd.getPort()), Map.of( //
				"username", username, //
				"password", password, //
				"debugMode", true //
		));
	}

	private void runInBackground(Main main) {
		newSingleThreadExecutor().execute(() -> {
			try {
				main.run();
			} catch (Exception e) {
				throw propagate(e);
			}
		});
	}

	private void createMailUser(String email, String login, String password) {
		mailMock.setUser(email, login, password);
	}

	private List<Message> fetchMails(String loginId, String password) throws MessagingException, InterruptedException {
		ImapServer imapd = mailMock.getImap();
		AtomicReference<List<Message>> messages = new AtomicReference<>(emptyList());
		await().forever().pollInterval(100, MILLISECONDS).until(() -> {
			messages.set(retrieveViaImap(imapd.getBindTo(), imapd.getPort(), loginId, password));
			return !messages.get().isEmpty();
		});
		return messages.get();
	}

	private List<Message> retrieveViaImap(String host, int port, String user, String password)
			throws MessagingException {
		Session session = Session.getInstance(toProperties(Map.of( //
				"mail.store.protocol", "imap", //
				"mail.imap.port", String.valueOf(port)) //
		), null);
		Store store = session.getStore();
		store.connect(host, user, password);
		Folder inbox = store.getFolder("INBOX");
		inbox.open(Folder.READ_ONLY);
		return asList(inbox.getMessages());
	}

	private static String makeURI(String uri, Map<? extends Object, ? extends Object> kv) {
		return kv.isEmpty() ? uri
				: format("%s%s%s", uri, separator(uri), Joiner.on("&").withKeyValueSeparator("=").join(kv));
	}

	private static String separator(String uri) {
		return uri.contains("?") ? "&" : "?";
	}

	private String anySubject() {
		return "anySubject";
	}

}
