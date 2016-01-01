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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

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

import ardulink.ardumailng.ArdulinkMail;

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.convenience.LinkDelegate;
import com.github.pfichtner.ardulink.core.convenience.Links;
import com.icegreen.greenmail.imap.ImapServer;
import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.smtp.SmtpServer;

public class ArdulinkMailOnCamelIntegrationTest {

	private static final String mockURI = "ardulink://mock";

	private static final Map<? extends String, ? extends Object> imapDefaults = MapBuilder
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
		createMailUser(receiver, "loginId1", "secret1");

		String validSender = "valid.sender@someSenderDomain.com";
		sendMailTo(receiver).from(validSender).withSubject(anySubject())
				.andText("usedScenario");

		final String to = makeURI(
				mockURI,
				newMapBuilder().put("validfroms", validSender)
						.put("scenario.xxx", "D13:false;A2:42")
						.put("scenario.usedScenario", "D13:true;A2:123")
						.put("scenario.yyy", "D13:false;A2:21").build());

		ArdulinkMail ardulinkMail = new ArdulinkMail(new RouteBuilder() {
			@Override
			public void configure() {
				from(localImap(receiver)).to(to);
			}
		}).start();

		waitUntilMailWasFetched();
		ardulinkMail.stop();

		Link mock = getMock();
		verify(mock).switchDigitalPin(digitalPin(13), true);
		verify(mock).switchAnalogPin(analogPin(2), 123);
		verify(mock).close();
		verifyNoMoreInteractions(mock);
	}

	private String localImap(String receiver) {
		return makeURI(
				"imap://" + receiver,
				newMapBuilder().putAll(imapDefaults)
						.put("username", "loginId1").put("password", "secret1")
						.put("port", imapServerPort()).put("unseen", true)
						.put("delete", true)
						.put("consumer.delay", MINUTES.toMillis(10)).build());
	}

	@Test
	public void canProcessMultipleLinks() throws Exception {
		final String receiver = "receiver@someReceiverDomain.com";
		createMailUser(receiver, "loginId1", "secret1");

		String validSender = "valid.sender@someSenderDomain.com";
		sendMailTo(receiver).from(validSender).withSubject(anySubject())
				.andText("usedScenario");

		Link link1 = Links.getLink(new URI(mockURI + "?num=1&foo=bar"));
		Link link2 = Links.getLink(new URI(mockURI + "?num=2&foo=bar"));

		final String to1 = makeURI(
				mockURI,
				newMapBuilder().put("linkparams", encode("num=1&foo=bar"))
						.put("validfroms", validSender)
						.put("scenario.usedScenario", "D11:true;A12:11")
						.build());

		final String to2 = makeURI(
				mockURI,
				newMapBuilder().put("linkparams", encode("num=2&foo=bar"))
						.put("validfroms", validSender)
						.put("scenario.usedScenario", "D21:true;A22:23")
						.build());

		try {

			ArdulinkMail ardulinkMail = new ArdulinkMail(new RouteBuilder() {
				@Override
				public void configure() {
					from(localImap(receiver))
							.multicast()
							.setAggregationStrategy(
									new UseOriginalAggregationStrategy())
							.to(to1, to2);
				}
			}).start();

			waitUntilMailWasFetched();
			ardulinkMail.stop();

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
		createMailUser(receiver, "loginId1", "secret1");

		String validSender = "valid.sender@someSenderDomain.com";
		sendMailTo(receiver).from(validSender).withSubject(anySubject())
				.andText("usedScenario");

		Link link1 = Links.getLink(new URI(mockURI + "?num=1&foo=bar"));
		Link link2 = Links.getLink(new URI(mockURI + "?num=2&foo=bar"));

		final String to1 = makeURI(
				mockURI,
				newMapBuilder().put("linkparams", encode("num=1&foo=bar"))
						.put("validfroms", validSender)
						.put("scenario.usedScenario", "D11:true;A12:11")
						.build());
		final String to2 = makeURI(
				mockURI,
				newMapBuilder().put("linkparams", encode("num=2&foo=bar"))
						.put("validfroms", validSender).build());

		try {
			ArdulinkMail ardulinkMail = new ArdulinkMail(new RouteBuilder() {
				@Override
				public void configure() {
					from(localImap(receiver))
							.multicast()
							.setAggregationStrategy(
									new UseOriginalAggregationStrategy())
							.to(to1, to2);
				}
			}).start();

			waitUntilMailWasFetched();
			ardulinkMail.stop();

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
		createMailUser(receiver, "loginId1", "secret1");

		String validSender = "valid.sender@someSenderDomain.com";
		createMailUser(validSender, "loginId2", "secret2");
		sendMailTo(receiver).from(validSender).withSubject(anySubject())
				.andText("usedScenario");

		final String ardulink = makeURI(
				mockURI,
				newMapBuilder().put("validfroms", validSender)
						.put("scenario.usedScenario", "D1:true").build());

		DefaultCamelContext context = new DefaultCamelContext();
		final MockEndpoint mockEndpoint = context.getEndpoint("mock:result",
				MockEndpoint.class);

		ArdulinkMail ardulinkMail = new ArdulinkMail(context,
				new RouteBuilder() {
					@Override
					public void configure() {
						from(localImap(receiver)).to(ardulink).to(mockEndpoint);
					}
				}).start();
		try {
			mockEndpoint.expectedMessageCount(1);
			mockEndpoint.expectedBodiesReceived("SwitchDigitalPinCommand "
					+ "[pin=1, value=true]=OK");
			mockEndpoint.assertIsSatisfied();
		} finally {
			ardulinkMail.stop();
		}
	}

	@Test
	public void writesResultToSender() throws Exception {
		final String receiver = "receiver@someReceiverDomain.com";
		createMailUser(receiver, "loginId1", "secret1");

		String validSender = "valid.sender@someSenderDomain.com";
		createMailUser(validSender, "loginId2", "secret2");
		sendMailTo(receiver).from(validSender).withSubject(anySubject())
				.andText("usedScenario");

		SmtpServer smtpd = mailMock.getSmtp();

		final String ardulink = makeURI(
				mockURI,
				newMapBuilder().put("validfroms", validSender)
						.put("scenario.usedScenario", "D1:true").build());

		final String smtp = "smtp://" + smtpd.getBindTo() + ":"
				+ smtpd.getPort() + "?username=" + "loginId1" + "&password="
				+ "secret1" + "&debugMode=true";

		ArdulinkMail ardulinkMail = new ArdulinkMail(new RouteBuilder() {
			@Override
			public void configure() {
				from(localImap(receiver)).to(ardulink)
						.setHeader("to", simple("${in.header.from}"))
						.setHeader("from", simple("${in.header.to}")).to(smtp);
			}
		}).start();

		try {
			assertThat(
					((String) fetchMail("loginId2", "secret2").getContent()),
					is("SwitchDigitalPinCommand [pin=1, value=true]=OK"));
		} finally {
			ardulinkMail.stop();
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

	private int imapServerPort() {
		return mailMock.getImap().getPort();
	}

	private Link getMock() {
		return getMock(link);
	}

	private Link getMock(Link l) {
		return ((LinkDelegate) l).getDelegate();
	}

}
