package com.github.pfichtner.ardulink.core;

import static com.github.pfichtner.ardulink.core.Pin.analogPin;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.StringContains.containsString;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.regex.Pattern;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;

import com.github.pfichtner.ardulink.core.proto.impl.ArdulinkProtocol255;
import com.github.pfichtner.ardulink.core.proto.impl.ArdulinkProtocolN;

public class HALinkTest {

	@Rule
	public Timeout timeout = new Timeout(5, SECONDS);

	@Rule
	public ExpectedException exceptions = ExpectedException.none();

	@Test
	public void canDoGuranteedDelivery() throws Exception {

		PipedInputStream is1 = new PipedInputStream();
		PipedOutputStream os1 = new PipedOutputStream(is1);

		PipedInputStream is2 = new PipedInputStream();
		PipedOutputStream os2 = new PipedOutputStream(is2);

		Responder responder = new Responder(is1, os2,
				ArdulinkProtocolN.instance());
		responder.whenReceive(regex("alp:\\/\\/notn\\/3\\?id\\=(\\d)"))
				.thenRespond("alp://rply/ok?id=%s");

		Connection connection = new StreamConnection(is2, os1,
				ArdulinkProtocolN.instance());
		ConnectionBasedLink haLink = new HALink(connection,
				ArdulinkProtocol255.instance());

		try {
			haLink.sendNoTone(analogPin(3));
		} finally {
			haLink.close();
		}
	}

	private Pattern regex(String regex) {
		return Pattern.compile(regex);
	}

	@Test
	public void doesThrowExceptionIfNotResponseReceivedWithinHalfAsecond()
			throws Exception {
		PipedInputStream is1 = new PipedInputStream();
		PipedOutputStream os1 = new PipedOutputStream(is1);

		Connection connection = new StreamConnection(null, os1,
				ArdulinkProtocolN.instance());
		ConnectionBasedLink haLink = new HALink(connection,
				ArdulinkProtocol255.instance(), 500, MILLISECONDS);

		try {
			exceptions.expect(IllegalStateException.class);
			exceptions.expectMessage(allOf(containsString("response"),
					containsString("500 MILLISECONDS")));
			haLink.sendNoTone(analogPin(3));
		} finally {
			haLink.close();
		}
	}

	@Test
	public void secondCallPassesIfFirstOnKeepsUnresponded() throws Exception {
		PipedInputStream is1 = new PipedInputStream();
		PipedOutputStream os1 = new PipedOutputStream(is1);

		PipedInputStream is2 = new PipedInputStream();
		PipedOutputStream os2 = new PipedOutputStream(is2);

		Connection connection = new StreamConnection(is2, os1,
				ArdulinkProtocolN.instance());
		ConnectionBasedLink haLink = new HALink(connection,
				ArdulinkProtocol255.instance(), 500, MILLISECONDS);

		Responder responder = new Responder(is1, os2,
				ArdulinkProtocolN.instance());
		responder.whenReceive(regex("alp:\\/\\/tone\\/4/5/6\\?id\\=(\\d)"))
				.thenDoNotRespond();
		responder.whenReceive(regex("alp:\\/\\/notn\\/3\\?id\\=(\\d)"))
				.thenRespond("alp://rply/ok?id=%s");
		
		

		try {
			exceptions.expect(IllegalStateException.class);
			exceptions.expectMessage(allOf(containsString("response"),
					containsString("500 MILLISECONDS")));
			haLink.sendTone(Tone.forPin(analogPin(4)).withHertz(5).withDuration(6, MILLISECONDS));
			exceptions = ExpectedException.none();
			haLink.sendNoTone(analogPin(3));
		} finally {
			haLink.close();
		}
	}

}
