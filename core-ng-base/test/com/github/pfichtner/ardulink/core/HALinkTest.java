package com.github.pfichtner.ardulink.core;

import static com.github.pfichtner.ardulink.core.Pin.analogPin;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.Timeout;
import org.zu.ardulink.util.Lists;

import com.github.pfichtner.ardulink.core.events.RplyEvent;
import com.github.pfichtner.ardulink.core.events.RplyListener;
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

		final List<RplyEvent> rplies = Lists.newArrayList();
		RplyListener listener = new RplyListener() {
			@Override
			public void rplyReceived(RplyEvent e) {
				rplies.add(e);
			}
		};
		haLink.addRplyListener(listener);

		try {
			haLink.sendNoTone(analogPin(3));
			MILLISECONDS.sleep(25);
			assertThat(rplies.size(), is(1));
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
				ArdulinkProtocol255.instance(), 500, TimeUnit.MILLISECONDS);

		final List<RplyEvent> rplies = Lists.newArrayList();
		RplyListener listener = new RplyListener() {
			@Override
			public void rplyReceived(RplyEvent e) {
				rplies.add(e);
			}
		};
		haLink.addRplyListener(listener);

		try {
			exceptions.expect(IllegalStateException.class);
			exceptions.expectMessage(allOf(containsString("response"),
					containsString("500 MILLISECONDS")));
			haLink.sendNoTone(Pin.analogPin(3));
			MILLISECONDS.sleep(25);
			assertThat(rplies.size(), is(1));
		} finally {
			haLink.close();
		}
	}

}
