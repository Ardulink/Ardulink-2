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
		public static final String CONNECT_CMD = "ardulink:networkproxyserver:connect";

		public static final String NUMBER_OF_PORTS = "NUMBER_OF_PORTS=";
		public static final String OK = "OK";

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
			send(GET_PORT_LIST_CMD);
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

		public void send(String message) {
			printWriter.println(message);
			printWriter.flush();
		}

		public String read() throws IOException {
			return bufferedReader.readLine();
		}

		public void startBackgroundReader() {
			new Thread(runnable()) {
				{
					setDaemon(true);
					start();
				}
			};
		}

		private Runnable runnable() {
			return new Runnable() {
				@Override
				public void run() {
					throw new UnsupportedOperationException("not yet implemented");
				}
			};
		}
	}

	private static final int DEFAULT_LISTENING_PORT = 4478;

	private static final int DEFAULT_SPEED = 115200;

	private static final String TCP_HOST = "tcphost";

	private static final String TCP_PORT = "tcpport";

	private static final String PORT = "port";

	private static final String PROTO = "proto";

	private static final String SPEED = "speed";

	private String tcphost;
	private int tcpport = DEFAULT_LISTENING_PORT;

	private String port;
	private Protocol proto = ArdulinkProtocol.instance();
	private int speed = DEFAULT_SPEED;

	private ProxyConnectionToRemote remote;

	@Named(TCP_HOST)
	public void setTcpHost(String tcphost) {
		this.tcphost = tcphost;
	}

	@Named(TCP_PORT)
	public void setTcpPort(int tcpport) {
		this.tcpport = tcpport;
	}

	@Named(PORT)
	public void setPort(String port) {
		this.port = port;
	}

	@Named(PROTO)
	public void setProto(String proto) {
		this.proto = Protocols.getByName(proto);
	}

	@Named(SPEED)
	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public String getPort() {
		return port;
	}

	public Protocol getProto() {
		return proto;
	}

	public int getSpeed() {
		return speed;
	}

	public ProxyConnectionToRemote getRemote() throws UnknownHostException,
			IOException {
		if (this.remote == null) {
			this.remote = new ProxyConnectionToRemote(tcphost, tcpport);
		}
		return this.remote;
	}

	@PossibleValueFor(PORT)
	public List<String> getAvailablePorts() throws IOException {
		this.remote = new ProxyConnectionToRemote(tcphost, tcpport);
		return remote.getPortList();
	}

}
