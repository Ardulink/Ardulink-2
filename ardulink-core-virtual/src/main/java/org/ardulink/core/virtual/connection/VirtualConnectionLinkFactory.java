package org.ardulink.core.virtual.connection;

import java.io.ByteArrayOutputStream;
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

public class VirtualConnectionLinkFactory implements LinkFactory<VirtualConnectionConfig> {

	public class NullInputStream extends InputStream {

		private byte[] message;
		private int    byteReturned = 0;
		private int    millisWait = 100;
		
		public NullInputStream() {
			super();
			setMessage("MESSAGE", ArdulinkProtocol2.instance().getSeparator());
		}

		public NullInputStream(String message, int millisWait, byte[] separator) {
			super();
			this.millisWait = millisWait;
			setMessage(message, separator);
		}
		
		private void setMessage(String message, byte[] separator) {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try {
				os.write(message.getBytes());
				os.write(separator);
			} catch (IOException e) {}
			
			this.message = os.toByteArray();
		}
		
		@Override
		public int read() throws IOException {

			try {
				TimeUnit.MILLISECONDS.sleep(millisWait);
			} catch (InterruptedException e) {
				e.printStackTrace();
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
		StreamConnection connection = new StreamConnection(
				 new NullInputStream(input, 500, protocol.getSeparator())
				,new NullOutputStream()
				,protocol);

		return new ConnectionBasedLink(connection, protocol);
	}

	@Override
	public VirtualConnectionConfig newLinkConfig() {
		return new VirtualConnectionConfig();
	}

}
