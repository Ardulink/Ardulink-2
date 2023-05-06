package org.ardulink.core.virtual.console;

import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.Link;
import org.ardulink.core.StreamConnection;
import org.ardulink.core.linkmanager.LinkFactory;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;

public class VirtualConnectionLinkFactory implements LinkFactory<VirtualConnectionConfig> {

	@Override
	public String getName() {
		return "virtual-console";
	}

	@Override
	public Link newLink(VirtualConnectionConfig config) throws Exception {
		System.out.println("Created a link that writes it's output to and gets it's input from here");
		ByteStreamProcessor byteStreamProcessor = config.getProto().newByteStreamProcessor();
		return new ConnectionBasedLink(new StreamConnection(System.in, System.out, byteStreamProcessor),
				byteStreamProcessor);
	}

	@Override
	public VirtualConnectionConfig newLinkConfig() {
		return new VirtualConnectionConfig();
	}

}
