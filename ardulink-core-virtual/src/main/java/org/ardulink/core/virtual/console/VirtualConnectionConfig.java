package org.ardulink.core.virtual.console;

import static org.ardulink.core.proto.api.Protocols.protoByName;
import static org.ardulink.core.proto.api.Protocols.protocolNames;
import static org.ardulink.core.proto.api.Protocols.tryProtoByName;
import static org.ardulink.util.Iterables.getFirst;
import static org.ardulink.util.Optionals.or;

import java.util.List;

import org.ardulink.core.linkmanager.LinkConfig;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.api.Protocols;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;

public class VirtualConnectionConfig implements LinkConfig {

	private static final String PROTO = "proto";

	@Named(PROTO)
	private Protocol protoName = useProtoOrFallback(ArdulinkProtocol2.NAME);

	private Protocol useProtoOrFallback(String prefered) {
		return or(tryProtoByName(prefered), () -> getFirst(Protocols.protocols())).orElse(null);
	}

	public String getProtoName() {
		return protoName == null ? null : protoName.getName();
	}

	public void setProtoName(String protoName) {
		this.protoName = protoByName(protoName);
	}

	@ChoiceFor(PROTO)
	public List<String> availableProtos() {
		return protocolNames();
	}

	public Protocol getProto() {
		return protoByName(getProtoName());
	}

}
