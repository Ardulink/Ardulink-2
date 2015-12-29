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
import static org.mockito.Mockito.times;
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

import org.junit.Before;
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

	public void tearDown() throws IOException {
		link.close();
	}

	@Test
	public void canProcessViaImap() throws Exception {
		String receiver = "receiver@someReceiverDomain.com";
		mailMock.setUser(receiver, "loginId1", "secret1");

		String validSender = "valid.sender@someSenderDomain.com";
		sendMailTo(receiver).from(validSender).withSubject("Subject")
				.andText("usedScenario");

		ArdulinkBuilder to = ardulink(mockURI).validFroms(validSender)
				.addScenario("xxx", "D13:false;A2:42")
				.addScenario("usedScenario", "D13:true;A2:123")
				.addScenario("yyy", "D13:false;A2:21");
		ArdulinkMail ardulinkMail = ArdulinkMail.builder()
				.from(localImap(receiver)).to(to).start();

		waitUntilMailWasFetched();
		ardulinkMail.stop();

		Link mock = getMock();
		verify(mock).switchDigitalPin(digitalPin(13), true);
		verify(mock).switchAnalogPin(analogPin(2), 123);
		verify(mock, times(2)).close();
		verifyNoMoreInteractions(mock);
	}

	private ImapBuilder localImap(String receiver) {
		return imap().user(receiver).login("loginId1").password("secret1")
				.host("localhost").port(imapServerPort()).folderName("INBOX")
				.unseen(true).delete(true).consumerDelay(10, MINUTES);
	}

	@Test
	public void canProcessMultipleLinks() throws Exception {
		String receiver = "receiver@someReceiverDomain.com";
		mailMock.setUser(receiver, "loginId1", "secret1");

		String validSender = "valid.sender@someSenderDomain.com";
		sendMailTo(receiver).from(validSender).withSubject("Subject")
				.andText("usedScenario");

		Link link1 = Links.getLink(new URI(mockURI + "?num=1&foo=bar"));
		Link link2 = Links.getLink(new URI(mockURI + "?num=2&foo=bar"));

		try {
			ArdulinkBuilder to1 = ardulink(mockURI).linkParams("num=1&foo=bar")
					.validFroms(validSender)
					.addScenario("usedScenario", "D11:true;A12:11");
			ArdulinkBuilder to2 = ardulink(mockURI).linkParams("num=2&foo=bar")
					.validFroms(validSender)
					.addScenario("usedScenario", "D21:true;A22:23");
			ArdulinkMail ardulinkMail = ArdulinkMail.builder()
					.from(localImap(receiver)).to(to1).to(to2).start();

			waitUntilMailWasFetched();
			ardulinkMail.stop();

			Link mock1 = getMock(link1);
			verify(mock1).switchDigitalPin(digitalPin(11), true);
			verify(mock1).switchAnalogPin(analogPin(12), 11);
			verify(mock1, times(2)).close();
			verifyNoMoreInteractions(mock1);

			Link mock2 = getMock(link2);
			verify(mock2).switchDigitalPin(digitalPin(21), true);
			verify(mock2).switchAnalogPin(analogPin(22), 23);
			verify(mock2, times(2)).close();
			verifyNoMoreInteractions(mock2);
		} finally {
			link1.close();
			link2.close();
		}

	}

	@Test
	public void canRespondViaMail() throws Exception {
		String receiver = "receiver@someReceiverDomain.com";
		mailMock.setUser(receiver, "loginId1", "secret1");

		String validSender = "valid.sender@someSenderDomain.com";
		mailMock.setUser(validSender, "loginId2", "secret2");
		sendMailTo(receiver).from(validSender).withSubject("Subject")
				.andText("usedScenario");

		SmtpServer smtpd = mailMock.getSmtp();
		ArdulinkBuilder ardulink = ardulink(mockURI).validFroms(validSender)
				.addScenario("usedScenario", "D1:true");
		String smtp = "smtp://" + smtpd.getBindTo() + ":" + smtpd.getPort()
				+ "?username=" + "loginId1" + "&password=" + "secret1";

		ArdulinkMail ardulinkMail = ArdulinkMail.builder()
				.from(localImap(receiver)).to(ardulink).to(smtp).start();

		// TODO fetch response mail for invalid.sender
//		 waitUntilMailWasFetched();

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
		verify(mock, times(2)).close();
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
