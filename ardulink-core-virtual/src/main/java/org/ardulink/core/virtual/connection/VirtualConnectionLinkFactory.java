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
import org.ardulink.core.proto.api.ProtocolNG;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;

public class VirtualConnectionLinkFactory implements LinkFactory<VirtualConnectionConfig> {

	public static class NullInputStream extends InputStream {

		private final byte[] message;
		private final TimeUnit waitUnits;
		private final int waitTime;

		private int byteReturned;

		public NullInputStream(String message, int waitTime, TimeUnit timeUnit) {
			this.waitTime = waitTime;
			this.waitUnits = timeUnit;
			this.message = message.getBytes();
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

		StreamConnection connection ;
		if (protocol instanceof ProtocolNG) {
			ProtocolNG protocolNG = (ProtocolNG) protocol;
			ByteStreamProcessor byteStreamProcessor = protocolNG.newByteStreamProcessor();
			connection = new StreamConnection(new NullInputStream(input, 500, MILLISECONDS),
					new NullOutputStream(), byteStreamProcessor);
		} else {
			connection = new StreamConnection(new NullInputStream(input, 500, MILLISECONDS),
					new NullOutputStream(), protocol);
		}
		return new ConnectionBasedLink(connection, protocol);
	}

	@Override
	public VirtualConnectionConfig newLinkConfig() {
		return new VirtualConnectionConfig();
	}

}
