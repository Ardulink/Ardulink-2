package com.github.pfichtner.core.serial;

import static com.github.pfichtner.ardulink.core.guava.Iterables.forEnumeration;
import static gnu.io.CommPortIdentifier.PORT_SERIAL;
import gnu.io.CommPortIdentifier;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import com.github.pfichtner.ardulink.core.connectionmanager.ConnectionConfig;

public class SerialConnectionConfig implements ConnectionConfig {

	private String port;
	private int speed = 115200;

	public String getPort() {
		return port;
	}

	@Named("port")
	public void setPort(String port) {
		this.port = port;
	}

	public int getSpeed() {
		return speed;
	}

	@Named("speed")
	public void setSpeed(int speed) {
		this.speed = speed;
	}

	@PossibleValueFor("port")
	public List<String> getPortList() {
		List<String> ports = new ArrayList<String>();
		for (CommPortIdentifier portIdentifier : forEnumeration(getPortIdentifiers())) {
			if (portIdentifier.getPortType() == PORT_SERIAL) {
				ports.add(portIdentifier.getName());
			}
		}
		return ports;
	}

	@SuppressWarnings("unchecked")
	private Enumeration<CommPortIdentifier> getPortIdentifiers() {
		return (Enumeration<CommPortIdentifier>) CommPortIdentifier
				.getPortIdentifiers();
	}

}
