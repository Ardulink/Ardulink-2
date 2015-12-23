package ardulink.ardumailng.camel;

import static ardulink.ardumailng.test.MailSender.sendMailTo;
import static com.github.pfichtner.ardulink.core.Pin.analogPin;
import static com.github.pfichtner.ardulink.core.Pin.digitalPin;
import static com.icegreen.greenmail.util.ServerSetupTest.SMTP_IMAP;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import ardulink.ardumailng.ArdulinkMail;

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.convenience.LinkDelegate;
import com.github.pfichtner.ardulink.core.convenience.Links;
import com.icegreen.greenmail.junit.GreenMailRule;

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
		String receiver = "receiver@localhost.invalid";
		mailMock.setUser(receiver, "loginId1", "secret1");

		String validSender = "valid.sender@localhost.invalid";
		sendMailTo(receiver).from(validSender).withSubject("Subject")
				.andText("usedScenario");

		String from = "imap://" + receiver + "?host=localhost&port="
				+ imapServerPort() + "&username=" + "loginId1" + "&password="
				+ "secret1" + "&folderName=" + "INBOX" + "&unseen=true"
				+ "&delete=" + "true" + "&consumer.delay="
				+ MINUTES.toMillis(10);
		String to = mockURI + "?validfroms=" + validSender
				+ "&scenario.xxx=D13:false;A2:42"
				+ "&scenario.usedScenario=D13:true;A2:123"
				+ "&scenario.yyy=D13:false;A2:21";

		ArdulinkMail ardulinkMail = new ArdulinkMail(from, to).start();
		waitUntilMailWasFetched();
		ardulinkMail.stop();

		Link mock = getMock();
		verify(mock).switchDigitalPin(digitalPin(13), true);
		verify(mock).switchAnalogPin(analogPin(2), 123);
		verify(mock, times(2)).close();
		verifyNoMoreInteractions(mock);
	}

	@Test
	public void canProcessMultipleLinks() throws Exception {
		String receiver = "receiver@localhost.invalid";
		mailMock.setUser(receiver, "loginId1", "secret1");

		String validSender = "valid.sender@localhost.invalid";
		sendMailTo(receiver).from(validSender).withSubject("Subject")
				.andText("usedScenario");

		Link link1 = Links.getLink(new URI(mockURI + "?num=1&foo=bar"));
		Link link2 = Links.getLink(new URI(mockURI + "?num=2&foo=bar"));

		try {
			String from = "imap://" + receiver + "?host=localhost&port="
					+ imapServerPort() + "&username=" + "loginId1"
					+ "&password=" + "secret1" + "&folderName=" + "INBOX"
					+ "&unseen=true" + "&delete=" + "true" + "&consumer.delay="
					+ MINUTES.toMillis(10);
			String to1 = mockURI + "?validfroms=" + validSender
					+ "&linkparams=" + encode("num=1&foo=bar")
					+ "&scenario.usedScenario=D11:true;A12:11";
			String to2 = mockURI + "?validfroms=" + validSender
					+ "&linkparams=" + encode("num=2&foo=bar")
					+ "&scenario.usedScenario=D21:true;A22:23";

			ArdulinkMail ardulinkMail = new ArdulinkMail(from, to1, to2)
					.start();
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
	public void canRespondViaMail() {
		fail("Not yet implemented");
	}

	private String encode(String string) {
		return "RAW(" + string + ")";
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
