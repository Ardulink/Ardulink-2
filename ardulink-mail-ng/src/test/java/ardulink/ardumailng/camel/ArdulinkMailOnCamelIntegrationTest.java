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

package ardulink.ardumailng.camel;

import static ardulink.ardumailng.test.MailSender.sendMailTo;
import static com.github.pfichtner.ardulink.core.Pin.analogPin;
import static com.github.pfichtner.ardulink.core.Pin.digitalPin;
import static com.icegreen.greenmail.util.ServerSetupTest.SMTP_IMAP;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.zu.ardulink.util.MapBuilder.newMapBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Scanner;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.processor.aggregate.UseOriginalAggregationStrategy;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.zu.ardulink.util.Joiner;
import org.zu.ardulink.util.MapBuilder;

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.convenience.LinkDelegate;
import com.github.pfichtner.ardulink.core.convenience.Links;
import com.icegreen.greenmail.imap.ImapServer;
import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.smtp.SmtpServer;

public class ArdulinkMailOnCamelIntegrationTest {

	private static final String mockURI = "ardulink://mock";

	private static final Map<? extends String, ? extends Object> IMAP_DEFAULTS = MapBuilder
			.<String, Object> newMapBuilder().put("host", "localhost")
			.put("port", 143).put("folderName", "INBOX").put("unseen", true)
			.build();

	private Link link;

	@Rule
	public GreenMailRule mailMock = new GreenMailRule(SMTP_IMAP);

	@Rule
	public Timeout timeout = new Timeout(10, SECONDS);

	@Before
	public void setup() throws URISyntaxException, Exception {
		link = Links.getLink(new URI(mockURI));
	}

	@After
	public void tearDown() throws IOException {
		link.close();
	}

	@Test
	public void canProcessViaImap() throws Exception {
		final String receiver = "receiver@someReceiverDomain.com";
		createMailUser(receiver, "loginIdReceiver", "secretOfReceiver");

		String validSender = "valid.sender@someSenderDomain.com";
		sendMailTo(receiver).from(validSender).withSubject(anySubject())
				.andText("usedScenario");

		final String to = makeURI(
				mockURI,
				newMapBuilder().put("validfroms", validSender)
						.put("scenario.xxx", "D13=false;A2=42")
						.put("scenario.usedScenario", "D13=true;A2=123")
						.put("scenario.yyy", "D13=false;A2=21").build());

		CamelContext context = new DefaultCamelContext();
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() {
				from(localImap(receiver)).to(to);
			}
		});
		context.start();

		waitUntilMailWasFetched();
		context.stop();

		Link mock = getMock();
		verify(mock).switchDigitalPin(digitalPin(13), true);
		verify(mock).switchAnalogPin(analogPin(2), 123);
		verify(mock).close();
		verifyNoMoreInteractions(mock);
	}

	private String localImap(String receiver) {
		ImapServer imapd = mailMock.getImap();
		return makeURI(
				"imap://" + receiver,
				newMapBuilder().putAll(IMAP_DEFAULTS)
						.put("username", "loginIdReceiver")
						.put("password", "secretOfReceiver")
						.put("port", imapd.getPort()).put("unseen", true)
						.put("delete", true)
						.put("consumer.delay", MINUTES.toMillis(10)).build());
	}

	@Test
	public void canProcessMultipleLinks() throws Exception {
		final String receiver = "receiver@someReceiverDomain.com";
		createMailUser(receiver, "loginIdReceiver", "secretOfReceiver");

		String validSender = "valid.sender@someSenderDomain.com";
		sendMailTo(receiver).from(validSender).withSubject(anySubject())
				.andText("usedScenario");

		Link link1 = Links.getLink(new URI(mockURI + "?num=1&foo=bar"));
		Link link2 = Links.getLink(new URI(mockURI + "?num=2&foo=bar"));

		final String to1 = makeURI(
				mockURI,
				newMapBuilder().put("linkparams", encode("num=1&foo=bar"))
						.put("validfroms", validSender)
						.put("scenario.usedScenario", "D11=true;A12=11")
						.build());

		final String to2 = makeURI(
				mockURI,
				newMapBuilder().put("linkparams", encode("num=2&foo=bar"))
						.put("validfroms", validSender)
						.put("scenario.usedScenario", "D21=true;A22=23")
						.build());

		try {
			CamelContext context = new DefaultCamelContext();
			context.addRoutes(new RouteBuilder() {
				@Override
				public void configure() {
					from(localImap(receiver))
							.multicast()
							.setAggregationStrategy(
									new UseOriginalAggregationStrategy())
							.to(to1, to2);
				}
			});
			context.start();

			waitUntilMailWasFetched();
			context.stop();

			Link mock1 = getMock(link1);
			verify(mock1).switchDigitalPin(digitalPin(11), true);
			verify(mock1).switchAnalogPin(analogPin(12), 11);
			verify(mock1).close();
			verifyNoMoreInteractions(mock1);

			Link mock2 = getMock(link2);
			verify(mock2).switchDigitalPin(digitalPin(21), true);
			verify(mock2).switchAnalogPin(analogPin(22), 23);
			verify(mock2).close();
			verifyNoMoreInteractions(mock2);
		} finally {
			link1.close();
			link2.close();
		}

	}

	@Test
	public void canProcessMultipleLinksWhenCommandNotKnownOnLink2()
			throws Exception {
		final String receiver = "receiver@someReceiverDomain.com";
		createMailUser(receiver, "loginIdReceiver", "secretOfReceiver");

		String validSender = "valid.sender@someSenderDomain.com";
		sendMailTo(receiver).from(validSender).withSubject(anySubject())
				.andText("usedScenario");

		Link link1 = Links.getLink(new URI(mockURI + "?num=1&foo=bar"));
		Link link2 = Links.getLink(new URI(mockURI + "?num=2&foo=bar"));

		final String to1 = makeURI(
				mockURI,
				newMapBuilder().put("linkparams", encode("num=1&foo=bar"))
						.put("validfroms", validSender)
						.put("scenario.usedScenario", "D11=true;A12=11")
						.build());
		final String to2 = makeURI(
				mockURI,
				newMapBuilder().put("linkparams", encode("num=2&foo=bar"))
						.put("validfroms", validSender).build());

		try {
			CamelContext context = new DefaultCamelContext();
			context.addRoutes(new RouteBuilder() {
				@Override
				public void configure() {
					from(localImap(receiver))
							.multicast()
							.setAggregationStrategy(
									new UseOriginalAggregationStrategy())
							.to(to1, to2);
				}
			});
			context.start();

			waitUntilMailWasFetched();
			context.stop();

			Link mock1 = getMock(link1);
			verify(mock1).switchDigitalPin(digitalPin(11), true);
			verify(mock1).switchAnalogPin(analogPin(12), 11);
			verify(mock1).close();
			verifyNoMoreInteractions(mock1);

			Link mock2 = getMock(link2);
			verify(mock2).close();
			verifyNoMoreInteractions(mock2);
		} finally {
			link1.close();
			link2.close();
		}
	}

	@Test
	public void writesResultToMock() throws Exception {
		final String receiver = "receiver@someReceiverDomain.com";
		createMailUser(receiver, "loginIdReceiver", "secretOfReceiver");

		String validSender = "valid.sender@someSenderDomain.com";
		createMailUser(validSender, "loginIdSender", "secretOfSender");
		sendMailTo(receiver).from(validSender).withSubject(anySubject())
				.andText("usedScenario");

		final String ardulink = makeURI(
				mockURI,
				newMapBuilder().put("validfroms", validSender)
						.put("scenario.usedScenario", "D1=true").build());

		CamelContext context = new DefaultCamelContext();
		final MockEndpoint mockEndpoint = context.getEndpoint("mock:result",
				MockEndpoint.class);

		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() {
				from(localImap(receiver)).to(ardulink).to(mockEndpoint);
			}
		});
		context.start();
		try {
			mockEndpoint.expectedMessageCount(1);
			mockEndpoint.expectedBodiesReceived("SwitchDigitalPinCommand "
					+ "[pin=1, value=true]=OK");
			mockEndpoint.assertIsSatisfied();
		} finally {
			context.stop();
		}
	}

	@Test
	public void writesResultToSender() throws Exception {
		final String receiver = "receiver@someReceiverDomain.com";
		createMailUser(receiver, "loginIdReceiver", "secretOfReceiver");

		String validSender = "valid.sender@someSenderDomain.com";
		createMailUser(validSender, "loginIdSender", "secretOfSender");
		sendMailTo(receiver).from(validSender).withSubject(anySubject())
				.andText("usedScenario");

		final String ardulink = makeURI(
				mockURI,
				newMapBuilder().put("validfroms", validSender)
						.put("scenario.usedScenario", "D1=true;A2=123").build());

		SmtpServer smtpd = mailMock.getSmtp();
		final String smtp = "smtp://" + smtpd.getBindTo() + ":"
				+ smtpd.getPort() + "?username=" + "loginIdReceiver"
				+ "&password=" + "secretOfReceiver" + "&debugMode=true";

		CamelContext context = new DefaultCamelContext();
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() {
				from(localImap(receiver)).to(ardulink)
						.setHeader("to", simple("${in.header.from}"))
						.setHeader("from", simple("${in.header.to}")).to(smtp);
			}
		});
		context.start();

		try {
			assertThat(((String) fetchMail("loginIdSender", "secretOfSender")
					.getContent()),
					is("SwitchDigitalPinCommand [pin=1, value=true]=OK\r\n"
							+ "SwitchAnalogPinCommand [pin=2, value=123]=OK"));
		} finally {
			context.stop();
		}

	}

	@Test
	public void writesResultToSender_ConfiguredViaXML() throws Exception {
		String receiver = "receiver@someReceiverDomain.com";
		createMailUser(receiver, "loginIdReceiver", "secretOfReceiver");

		String validSender = "valid.sender@someSenderDomain.com";
		createMailUser(validSender, "loginIdSender", "secretOfSender");
		String commandName = "nameMe";
		sendMailTo(receiver).from(validSender).withSubject(anySubject())
				.andText(commandName);

		InputStream is = getClass().getResourceAsStream("/ardulinkmail.xml");
		try {
			CamelContext context = new DefaultCamelContext();
			loadRoutesDefinition(is, context, commandName);
			context.start();

			try {
				assertThat(
						((String) fetchMail("loginIdSender", "secretOfSender")
								.getContent()),
						is("SwitchDigitalPinCommand [pin=1, value=true]=OK\r\n"
								+ "SwitchAnalogPinCommand [pin=2, value=123]=OK"));
			} finally {
				context.stop();
			}
		} finally {
			is.close();
		}

	}

	private void loadRoutesDefinition(InputStream resourceAsStream,
			CamelContext context, String commandName) throws Exception {
		SmtpServer smtpd = mailMock.getSmtp();
		ImapServer imapd = mailMock.getImap();
		Map<String, Object> values = MapBuilder
				.<String, Object> newMapBuilder()
				.put("imaphost", imapd.getBindTo())
				.put("imapport", imapd.getPort())
				.put("commandname", commandName)
				.put("command", "D1=true;A2=123")
				.put("smtphost", smtpd.getBindTo())
				.put("smtpport", smtpd.getPort()).build();
		String xml = replacePlaceHolders(toString(resourceAsStream), values);
		InputStream is = new ByteArrayInputStream(xml.getBytes());
		try {
			context.addRouteDefinitions(context.loadRoutesDefinition(is)
					.getRoutes());
		} finally {
			is.close();
		}
	}

	private static String replacePlaceHolders(String in, Map<?, ?> values) {
		StringBuilder builder = new StringBuilder(in);
		for (Entry<?, ?> entry : values.entrySet()) {
			String pattern = "${" + entry.getKey() + "}";
			String value = entry.getValue().toString();
			int start;
			while ((start = builder.indexOf(pattern)) != -1) {
				builder.replace(start, start + pattern.length(), value);
			}
		}
		return builder.toString();
	}

	private static String toString(InputStream inputStream) {
		Scanner scanner = new Scanner(inputStream, "UTF-8");
		try {
			return scanner.useDelimiter("\\A").next();
		} finally {
			scanner.close();
		}
	}

	private void createMailUser(String receiver, String id, String password) {
		mailMock.setUser(receiver, id, password);
	}

	private Message fetchMail(String loginId, String password)
			throws MessagingException, InterruptedException {
		ImapServer imapd = mailMock.getImap();
		Message msg = null;
		while ((msg = retrieveViaImap(imapd.getBindTo(), imapd.getPort(),
				loginId, password)) == null) {
			MILLISECONDS.sleep(100);
		}
		return msg;
	}

	private Message retrieveViaImap(String host, int port, String user,
			String password) throws MessagingException {
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

	private String makeURI(String uri,
			Map<? extends Object, ? extends Object> kv) {
		return uri + "?" + Joiner.on("&").withKeyValueSeparator("=").join(kv);
	}

	private static String encode(String string) {
		return "RAW(" + string + ")";
	}

	private String anySubject() {
		return "Subject";
	}

	private void waitUntilMailWasFetched() throws InterruptedException {
		while (mailMock.getReceivedMessages().length > 0) {
			MILLISECONDS.sleep(50);
		}
	}

	private Link getMock() {
		return getMock(link);
	}

	private Link getMock(Link l) {
		return ((LinkDelegate) l).getDelegate();
	}

}
