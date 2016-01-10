package com.github.pfichtner.core.proxy;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;

import com.github.pfichtner.ardulink.core.linkmanager.LinkConfig;
import com.github.pfichtner.ardulink.core.proto.api.Protocol;
import com.github.pfichtner.ardulink.core.proto.api.Protocols;
import com.github.pfichtner.ardulink.core.proto.impl.ArdulinkProtocol2;

public class ProxyLinkConfig implements LinkConfig {

	private static final int DEFAULT_LISTENING_PORT = 4478;

	private static final int DEFAULT_SPEED = 115200;

	@Named("tcphost")
	private String tcphost;

	@Named("tcpport")
	private int tcpport = DEFAULT_LISTENING_PORT;

	@Named("port")
	private String port;

	@Named("speed")
	private int speed = DEFAULT_SPEED;

	@Named("proto")
	private Protocol proto = ArdulinkProtocol2.instance();

	private ProxyConnectionToRemote remote;

	public String getPort() {
		return port;
	}

	public int getSpeed() {
		return speed;
	}

	public String getTcphost() {
		return tcphost;
	}

	public int getTcpport() {
		return tcpport;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public void setProto(String proto) {
		this.proto = Protocols.getByName(proto);
	}

	public void setTcphost(String tcphost) {
		this.tcphost = tcphost;
	}

	public void setTcpport(int tcpport) {
		this.tcpport = tcpport;
	}

	@ChoiceFor("port")
	public List<String> getAvailablePorts() throws IOException {
		return getRemote().getPortList();
	}

	public synchronized ProxyConnectionToRemote getRemote()
			throws UnknownHostException, IOException {
		if (this.remote == null) {
			this.remote = new ProxyConnectionToRemote(tcphost, tcpport);
		}
		return this.remote;
	}

}
