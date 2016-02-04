package com.github.pfichtner.ardulink.core.linkmanager;

import static org.zu.ardulink.util.Preconditions.checkNotNull;

public class LinkConfigWithDependentAttributes implements LinkConfig {

	@Named("host")
	private String host;
	@Named("port")
	private Integer port;
	@Named("devicePort")
	private String devicePort;

	@ChoiceFor("devicePort")
	public String[] availableDevicePort() {
		checkNotNull(host, "host must not be null");
		checkNotNull(port, "port must not be null");
		return new String[] { "foo", "bar" };
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port == null ? -1 : port.intValue();
	}

	public void setPort(int port) {
		this.port = Integer.valueOf(port);
	}

	public String getDevicePort() {
		return devicePort;
	}

	public void setDevicePort(String devicePort) {
		this.devicePort = devicePort;
	}

}
