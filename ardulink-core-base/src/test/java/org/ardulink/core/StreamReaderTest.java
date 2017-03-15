package org.ardulink.core;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.ardulink.util.Lists;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

public class StreamReaderTest {

	@Rule
	public Timeout timeout = new Timeout(15, SECONDS);

	private final List<String> received = Lists.newArrayList();

	@Test
	public void canHandleDataAlreadyPresentSeparatedByNewline()
			throws Exception {
		List<String> expected = Arrays.asList("a", "b", "c");
		StreamReader reader = process(
				new ByteArrayInputStream("a\nb\nc\n".getBytes()), "\n",
				expected);
		waitUntil(expected.size());
		assertThat(received, is(expected));
		reader.close();
	}

	private void waitUntil(int size) throws InterruptedException {
		while (received.size() != size) {
			MILLISECONDS.sleep(100);
		}
	}

	@Test
	public void canHandleDataAlreadyPresentSeparatedByComma() throws Exception {
		List<String> expected = Arrays.asList("a", "b", "c");
		StreamReader reader = process(
				new ByteArrayInputStream("a,b,c,".getBytes()), ",", expected);
		waitUntil(expected.size());
		assertThat(received, is(expected));
		reader.close();
	}

	@Test
	public void canHandleDataNotAlreadyPresentSeparatedByNewline()
			throws Exception {
		List<String> expected = Arrays.asList("a", "b", "c");

		PipedOutputStream os = new PipedOutputStream();
		PipedInputStream is = new PipedInputStream(os);

		StreamReader reader = process(is, "\n", expected);

		TimeUnit.SECONDS.sleep(2);
		os.write("a\nb\nc\n".getBytes());

		waitUntil(expected.size());
		assertThat(received, is(expected));
		reader.close();
	}

	@Test
	public void canHandleDataNotAlreadyPresentSeparatedByComma()
			throws Exception {
		List<String> expected = Arrays.asList("a", "b", "c");

		PipedOutputStream os = new PipedOutputStream();
		PipedInputStream is = new PipedInputStream(os);

		StreamReader reader = process(is, ",", expected);

		TimeUnit.SECONDS.sleep(2);
		os.write("a,b,c,".getBytes());

		waitUntil(expected.size());
		assertThat(received, is(expected));
		reader.close();
	}

	private StreamReader process(InputStream is, String separator,
			List<String> expected) throws InterruptedException, IOException {
		final CountDownLatch latch = new CountDownLatch(expected.size());

		StreamReader streamReader = new StreamReader(is) {
			@Override
			protected void received(byte[] bytes) {
				received.add(new String(bytes));
				latch.countDown();
			}
		};
		streamReader.runReaderThread(separator.getBytes());
		return streamReader;
	}

}
