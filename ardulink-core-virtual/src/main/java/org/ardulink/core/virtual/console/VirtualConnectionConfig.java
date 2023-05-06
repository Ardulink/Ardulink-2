package org.ardulink.core.virtual.console;

import static org.ardulink.util.Iterables.getFirst;

import java.util.List;

import org.ardulink.core.linkmanager.LinkConfig;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.api.Protocols;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;

public class VirtualConnectionConfig implements LinkConfig {

	private static final String PROTO = "proto";

	@Named(PROTO)
	private Protocol protoName = useProtoOrFallback(ArdulinkProtocol2.instance());

	private Protocol useProtoOrFallback(Protocol prefered) {
		return isAvailable(prefered) ? prefered : getFirst(Protocols.list()).orElse(null);
	}

	private boolean isAvailable(Protocol prefered) {
		return availableProtos().contains(prefered.getName());
	}

	public String getProtoName() {
		return protoName == null ? null : protoName.getName();
	}

	public void setProtoName(String protoName) {
		this.protoName = Protocols.getByName(protoName);
	}

	@ChoiceFor(PROTO)
	public List<String> availableProtos() {
		return Protocols.names();
	}

	public Protocol getProto() {
		return Protocols.getByName(getProtoName());
	}

}
