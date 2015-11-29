package com.github.pfichtner.core.serial;

import static com.github.pfichtner.ardulink.core.guava.Iterables.forEnumeration;
import static gnu.io.CommPortIdentifier.PORT_SERIAL;
import gnu.io.CommPortIdentifier;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.github.pfichtner.ardulink.core.connectionmanager.ConnectionConfig;

public class SerialConnectionConfig implements ConnectionConfig {

	private static final String PORT = "port";

	private static final String SPEED = "speed";

	private static final int DEFAULT_SPEED = 115200;

	private String port;
	private int speed = DEFAULT_SPEED;

	public String getPort() {
		return port;
	}

	@Named(PORT)
	public void setPort(String port) {
		this.port = port;
	}

	public int getSpeed() {
		return speed;
	}

	@Named(SPEED)
	public void setSpeed(int speed) {
		this.speed = speed;
	}

	@PossibleValueFor(PORT)
	public List<String> getPortList() {
		List<String> ports = new ArrayList<String>();
		for (CommPortIdentifier portIdentifier : portIdentifiers()) {
			if (portIdentifier.getPortType() == PORT_SERIAL) {
				ports.add(portIdentifier.getName());
			}
		}
		return ports;
	}

	@SuppressWarnings("unchecked")
	private Iterable<CommPortIdentifier> portIdentifiers() {
		return forEnumeration((Enumeration<CommPortIdentifier>) CommPortIdentifier
				.getPortIdentifiers());
	}

}
