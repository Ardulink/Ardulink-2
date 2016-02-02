package com.github.pfichtner.core.serial;

import static gnu.io.CommPortIdentifier.PORT_SERIAL;
import static org.zu.ardulink.util.Iterables.forEnumeration;
import static org.zu.ardulink.util.Iterables.getFirst;
import gnu.io.CommPortIdentifier;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.zu.ardulink.util.Iterables;
import org.zu.ardulink.util.Optional;

import com.github.pfichtner.ardulink.core.linkmanager.LinkConfig;
import com.github.pfichtner.ardulink.core.proto.api.Protocol;
import com.github.pfichtner.ardulink.core.proto.api.Protocols;
import com.github.pfichtner.ardulink.core.proto.impl.ArdulinkProtocol2;

public class SerialLinkConfig implements LinkConfig {

	@Named("port")
	private String port;

	@Named("baudrate")
	private int baudrate = 115200;

	@Named("proto")
	private Protocol protoName = defaultProto();

	@Named("qos")
	private boolean qos;

	@Named("waitsecs")
	private int waitsecs = 10;

	@Named("pingprobe")
	private boolean pingprobe = true;

	public int getBaudrate() {
		return baudrate;
	}

	private Protocol defaultProto() {
		List<String> available = availableProtos();
		Optional<Protocol> proto = isAvailable(ArdulinkProtocol2.instance());
		if (proto.isPresent()) {
			return proto.get();
		}
		Optional<String> firstProtoName = getFirst(available);
		return firstProtoName.isPresent() ? Protocols.getByName(firstProtoName
				.get()) : null;
	}

	private Optional<Protocol> isAvailable(Protocol prefered) {
		return availableProtos().contains(prefered.getName()) ? Optional
				.of(prefered) : Optional.<Protocol> absent();
	}

	public String getPort() {
		return port;
	}

	@ChoiceFor("port")
	public String[] listPorts() {
		List<String> ports = new ArrayList<String>();
		for (CommPortIdentifier portIdentifier : portIdentifiers()) {
			if (portIdentifier.getPortType() == PORT_SERIAL) {
				ports.add(portIdentifier.getName());
			}
		}
		return ports.toArray(new String[ports.size()]);
	}

	@ChoiceFor("proto")
	public List<String> availableProtos() {
		return Protocols.list();
	}

	public String getProtoName() {
		return protoName == null ? null : protoName.getName();
	}

	public Protocol getProto() {
		return Protocols.getByName(getProtoName());
	}

	public int getWaitsecs() {
		return waitsecs;
	}

	public boolean isPingprobe() {
		return pingprobe;
	}

	public boolean isQos() {
		return this.qos;
	}

	@SuppressWarnings("unchecked")
	private Iterable<CommPortIdentifier> portIdentifiers() {
		return forEnumeration((Enumeration<CommPortIdentifier>) CommPortIdentifier
				.getPortIdentifiers());
	}

	public void setBaudrate(int baudrate) {
		this.baudrate = baudrate;
	}

	public void setPingprobe(boolean pingprobe) {
		this.pingprobe = pingprobe;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public void setProtoName(String protoName) {
		this.protoName = Protocols.getByName(protoName);
	}

	public void setQos(boolean qos) {
		this.qos = qos;
	}

	public void setWaitsecs(int waitsecs) {
		this.waitsecs = waitsecs;
	}

}
