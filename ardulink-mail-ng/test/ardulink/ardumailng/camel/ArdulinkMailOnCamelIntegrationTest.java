package ardulink.ardumailng.camel;

import static ardulink.ardumailng.ArdulinkMail.Builder.ardulink;
import static ardulink.ardumailng.ArdulinkMail.Builder.imap;
import static ardulink.ardumailng.test.MailSender.sendMailTo;
import static com.github.pfichtner.ardulink.core.Pin.analogPin;
import static com.github.pfichtner.ardulink.core.Pin.digitalPin;
import static com.icegreen.greenmail.util.ServerSetupTest.SMTP_IMAP;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.MulticastDefinition;
import org.apache.camel.processor.aggregate.UseOriginalAggregationStrategy;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import ardulink.ardumailng.ArdulinkMail;
import ardulink.ardumailng.ArdulinkMail.ArdulinkBuilder;
import ardulink.ardumailng.ArdulinkMail.ImapBuilder;

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.convenience.LinkDelegate;
import com.github.pfichtner.ardulink.core.convenience.Links;
import com.icegreen.greenmail.imap.ImapServer;
import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.smtp.SmtpServer;

public class ArdulinkMailOnCamelIntegrationTest {

	private static final String mockURI = "ardulink://mock";

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
		mailMock.setUser(receiver, "loginId1", "secret1");

		String validSender = "valid.sender@someSenderDomain.com";
		sendMailTo(receiver).from(validSender).withSubject("Subject")
				.andText("usedScenario");

		final ArdulinkBuilder to = ardulink(mockURI).validFroms(validSender)
				.addScenario("xxx", "D13:false;A2:42")
				.addScenario("usedScenario", "D13:true;A2:123")
				.addScenario("yyy", "D13:false;A2:21");
		ArdulinkMail ardulinkMail = new ArdulinkMail(new RouteBuilder() {
			@Override
			public void configure() {
				from(localImap(receiver).makeURI()).to(to.makeURI());
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

	private ImapBuilder localImap(String receiver) {
		return imap().user(receiver).login("loginId1").password("secret1")
				.host("localhost").port(imapServerPort()).folderName("INBOX")
				.unseen(true).delete(true).consumerDelay(10, MINUTES);
	}

	@Test
	public void canProcessMultipleLinks() throws Exception {
		final String receiver = "receiver@someReceiverDomain.com";
		mailMock.setUser(receiver, "loginId1", "secret1");

		String validSender = "valid.sender@someSenderDomain.com";
		sendMailTo(receiver).from(validSender).withSubject("Subject")
				.andText("usedScenario");

		Link link1 = Links.getLink(new URI(mockURI + "?num=1&foo=bar"));
		Link link2 = Links.getLink(new URI(mockURI + "?num=2&foo=bar"));

		try {
			final ArdulinkBuilder to1 = ardulink(mockURI)
					.linkParams("num=1&foo=bar").validFroms(validSender)
					.addScenario("usedScenario", "D11:true;A12:11");
			final ArdulinkBuilder to2 = ardulink(mockURI)
					.linkParams("num=2&foo=bar").validFroms(validSender)
					.addScenario("usedScenario", "D21:true;A22:23");

			ArdulinkMail ardulinkMail = new ArdulinkMail(new RouteBuilder() {
				@Override
				public void configure() {
					MulticastDefinition md = from(localImap(receiver).makeURI())
							.multicast();
					md.setAggregationStrategy(new UseOriginalAggregationStrategy());
					md.to(to1.makeURI(), to2.makeURI());
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
		mailMock.setUser(receiver, "loginId1", "secret1");

		String validSender = "valid.sender@someSenderDomain.com";
		sendMailTo(receiver).from(validSender).withSubject("Subject")
				.andText("usedScenario");

		Link link1 = Links.getLink(new URI(mockURI + "?num=1&foo=bar"));
		Link link2 = Links.getLink(new URI(mockURI + "?num=2&foo=bar"));

		try {
			final ArdulinkBuilder to1 = ardulink(mockURI)
					.linkParams("num=1&foo=bar").validFroms(validSender)
					.addScenario("usedScenario", "D11:true;A12:11");
			final ArdulinkBuilder to2 = ardulink(mockURI).linkParams(
					"num=2&foo=bar").validFroms(validSender);

			ArdulinkMail ardulinkMail = new ArdulinkMail(new RouteBuilder() {
				@Override
				public void configure() {
					MulticastDefinition md = from(localImap(receiver).makeURI())
							.multicast();
					md.setAggregationStrategy(new UseOriginalAggregationStrategy());
					md.to(to1.makeURI(), to2.makeURI());
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
		mailMock.setUser(receiver, "loginId1", "secret1");

		String validSender = "valid.sender@someSenderDomain.com";
		mailMock.setUser(validSender, "loginId2", "secret2");
		sendMailTo(receiver).from(validSender).withSubject("Subject")
				.andText("usedScenario");

		final ArdulinkBuilder ardulink = ardulink(mockURI).validFroms(
				validSender).addScenario("usedScenario", "D1:true");

		DefaultCamelContext context = new DefaultCamelContext();
		final MockEndpoint mockEndpoint = context.getEndpoint("mock:result",
				MockEndpoint.class);

		ArdulinkMail ardulinkMail = new ArdulinkMail(context,
				new RouteBuilder() {
					@Override
					public void configure() {
						from(localImap(receiver).makeURI()).to(
								ardulink.makeURI()).to(mockEndpoint);
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
	@Ignore
	public void writesResultToSender() throws Exception {
		final String receiver = "receiver@someReceiverDomain.com";
		mailMock.setUser(receiver, "loginId1", "secret1");

		String validSender = "valid.sender@someSenderDomain.com";
		mailMock.setUser(validSender, "loginId2", "secret2");
		sendMailTo(receiver).from(validSender).withSubject("Subject")
				.andText("usedScenario");

		SmtpServer smtpd = mailMock.getSmtp();
		final ArdulinkBuilder ardulink = ardulink(mockURI).validFroms(
				validSender).addScenario("usedScenario", "D1:true");
		final String smtp = "smtp://" + smtpd.getBindTo() + ":"
				+ smtpd.getPort() + "?username=" + "loginId1" + "&password="
				+ "secret1";

		ArdulinkMail ardulinkMail = new ArdulinkMail(new RouteBuilder() {
			@Override
			public void configure() {
				from(localImap(receiver).makeURI()).to(ardulink.makeURI()).to(
						smtp);
			}
		}).start();

		// TODO fetch response mail for invalid.sender
		// waitUntilMailWasFetched();

		ImapServer imapd = mailMock.getImap();
		Message msg = null;
		while ((msg = retrieveViaImap(imapd.getBindTo(), imapd.getPort(),
				"loginId2", "secret2")) == null) {
			TimeUnit.MILLISECONDS.sleep(100);
		}

		System.out.println("*****************" + msg.getContent());

		ardulinkMail.stop();

		Link mock = getMock();
		verify(mock).switchDigitalPin(digitalPin(1), true);
		verifyNoMoreInteractions(mock);
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
