package org.ardulink.core.virtual.connection;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.Link;
import org.ardulink.core.StreamConnection;
import org.ardulink.core.linkmanager.LinkFactory;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;
import org.ardulink.util.Bytes;

public class VirtualConnectionLinkFactory implements
		LinkFactory<VirtualConnectionConfig> {

	public static class NullInputStream extends InputStream {

		private final byte[] message;
		private final TimeUnit waitUnits;
		private final int waitTime;

		private int byteReturned;

		// TODO LZ Is this constructor really needed? Why is ArdulinkProtocol2's
		// separator refereed here?
		public NullInputStream() {
			this("MESSAGE", 100, MILLISECONDS, ArdulinkProtocol2.instance()
					.getSeparator());
		}

		public NullInputStream(String message, int waitTime, TimeUnit timeUnit,
				byte[] separator) {
			this.waitTime = waitTime;
			this.waitUnits = timeUnit;
			this.message = Bytes.concat(message.getBytes(), separator);
		}

		@Override
		public int read() throws IOException {
			try {
				waitUnits.sleep(waitTime);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			int retvalue = message[byteReturned];
			byteReturned = (byteReturned + 1) % message.length;
			return retvalue;
		}
	}

	public class NullOutputStream extends OutputStream {
		@Override
		public void write(int b) throws IOException {
			// Nothing to do!
		}
	}

	@Override
	public String getName() {
		return "virtualConnectionBased";
	}

	@Override
	public Link newLink(VirtualConnectionConfig config) throws Exception {
		String input = config.getInput();
		Protocol protocol = config.getProto();
		StreamConnection connection = new StreamConnection(new NullInputStream(
				input, 500, MILLISECONDS, protocol.getSeparator()),
				new NullOutputStream(), protocol);

		return new ConnectionBasedLink(connection, protocol);
	}

	@Override
	public VirtualConnectionConfig newLinkConfig() {
		return new VirtualConnectionConfig();
	}

}
