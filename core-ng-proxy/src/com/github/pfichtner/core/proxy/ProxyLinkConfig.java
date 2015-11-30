package com.github.pfichtner.core.proxy;

import static org.zu.ardulink.util.Preconditions.checkNotNull;
import static org.zu.ardulink.util.Preconditions.checkState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.github.pfichtner.ardulink.core.linkmanager.LinkConfig;
import com.github.pfichtner.ardulink.core.proto.api.Protocol;
import com.github.pfichtner.ardulink.core.proto.api.Protocols;
import com.github.pfichtner.ardulink.core.proto.impl.ArdulinkProtocol;

public class ProxyLinkConfig implements LinkConfig {

	public static class ProxyConnectionToRemote {

		public static final String GET_PORT_LIST_CMD = "ardulink:networkproxyserver:get_port_list";
		public static final String NUMBER_OF_PORTS = "NUMBER_OF_PORTS=";

		private final String host;
		private final Socket socket;
		private final InputStream inputStream;
		private final BufferedReader bufferedReader;
		private final OutputStream outputStream;
		private final PrintWriter printWriter;

		public ProxyConnectionToRemote(String host, int port)
				throws UnknownHostException, IOException {
			this.host = host;
			socket = new Socket(host, port);
			inputStream = socket.getInputStream();
			bufferedReader = new BufferedReader(new InputStreamReader(
					inputStream));
			outputStream = socket.getOutputStream();
			printWriter = new PrintWriter(outputStream, true);
		}

		public Socket getSocket() {
			return socket;
		}

		public List<String> getPortList() throws IOException {
			printWriter.println(GET_PORT_LIST_CMD);
			printWriter.flush();
			String numberOfPorts = checkNotNull(bufferedReader.readLine(),
					"invalid response from %s, got null", host);
			checkState(numberOfPorts.startsWith(NUMBER_OF_PORTS),
					"invalid response: did not start with %s", NUMBER_OF_PORTS);
			int numOfPorts = Integer.parseInt(numberOfPorts
					.substring(NUMBER_OF_PORTS.length()));
			List<String> retvalue = new ArrayList<String>(numOfPorts);
			for (int i = 0; i < numOfPorts; i++) {
				retvalue.add(bufferedReader.readLine());
			}
			return retvalue;
		}

	}

	private static final int DEFAULT_LISTENING_PORT = 4478;

	private static final String HOST = "host";

	private static final String PORT = "port";

	private static final String PORTLIST = "portlist";

	private static final String PROTO = "proto";

	private String host;
	private int port = DEFAULT_LISTENING_PORT;
	private String portlist;
	private Protocol proto = ArdulinkProtocol.instance();

	private ProxyConnectionToRemote remote;

	@Named(HOST)
	public void setHost(String host) {
		this.host = host;
	}

	@Named(PORT)
	public void setPort(int port) {
		this.port = port;
	}

	@Named(PORTLIST)
	public void setPortlist(String portlist) {
		this.portlist = portlist;
	}

	@Named(PROTO)
	public void setProto(String proto) {
		this.proto = Protocols.getByName(proto);
	}

	public String getPortlist() {
		return portlist;
	}

	public Protocol getProto() {
		return proto;
	}

	public ProxyConnectionToRemote getRemote() throws UnknownHostException,
			IOException {
		if (this.remote == null) {
			this.remote = new ProxyConnectionToRemote(host, port);
		}
		return this.remote;
	}

	@PossibleValueFor(PORTLIST)
	public List<String> getPortList() throws IOException {
		this.remote = new ProxyConnectionToRemote(host, port);
		return remote.getPortList();
	}

}
