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

	@Override
	public String getName() {
		return "virtualConnectionBased";
	}

	@Override
	public Link newLink(VirtualConnectionConfig config) throws Exception {
		OutputStream outputStream = Protocol.NULL_OUTPUTSTREAM;
		ByteStreamProcessor byteStreamProcessor = config.getProto().newByteStreamProcessor(outputStream);
		return new ConnectionBasedLink(new StreamConnection(new NullInputStream(config.getInput(), 500, MILLISECONDS),
				outputStream, byteStreamProcessor), byteStreamProcessor);
	}

	@Override
	public VirtualConnectionConfig newLinkConfig() {
		return new VirtualConnectionConfig();
	}

}
