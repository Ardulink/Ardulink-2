package com.github.pfichtner.core.proxy;

import static com.github.pfichtner.core.proxy.ProxyLinkConfig.ProxyConnectionToRemote.Command.GET_PORT_LIST_CMD;
import static org.zu.ardulink.util.Preconditions.checkNotNull;
import static org.zu.ardulink.util.Preconditions.checkState;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.github.pfichtner.ardulink.core.linkmanager.LinkConfig;
import com.github.pfichtner.ardulink.core.proto.api.Protocol;
import com.github.pfichtner.ardulink.core.proto.api.Protocols;
import com.github.pfichtner.ardulink.core.proto.impl.ArdulinkProtocolN;

public class ProxyLinkConfig implements LinkConfig {

	public static class ProxyConnectionToRemote {

		public static enum Command {

			GET_PORT_LIST_CMD("get_port_list"), CONNECT_CMD("connect");

			private static final String PREFIX = "ardulink:networkproxyserver:";
			private final String command;

			private Command(String command) {
				this.command = PREFIX + command;
			}

			public String getCommand() {
				return command;
			}

			@Override
			public String toString() {
				return getCommand();
			}

		}

		public static final String NUMBER_OF_PORTS = "NUMBER_OF_PORTS=";
		public static final String OK = "OK";

		private final String host;
		private final Socket socket;
		private final Protocol protocol;
		private final BufferedReader bufferedReader;
		private final PrintWriter printWriter;

		public ProxyConnectionToRemote(String host, int port, Protocol protocol)
				throws UnknownHostException, IOException {
			this.host = host;
			this.protocol = protocol;
			socket = new Socket(host, port);
			this.bufferedReader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			this.printWriter = new PrintWriter(socket.getOutputStream(), false);
		}

		public List<String> getPortList() throws IOException {
			send(GET_PORT_LIST_CMD.getCommand());
			String numberOfPorts = checkNotNull(read(),
					"invalid response from %s, got null", host);
			checkState(numberOfPorts.startsWith(NUMBER_OF_PORTS),
					"invalid response: did not start with %s", NUMBER_OF_PORTS);
			int numOfPorts = Integer.parseInt(numberOfPorts
					.substring(NUMBER_OF_PORTS.length()));
			List<String> retvalue = new ArrayList<String>(numOfPorts);
			for (int i = 0; i < numOfPorts; i++) {
				retvalue.add(read());
			}
			return retvalue;
		}

		public Socket getSocket() {
			return socket;
		}

		public String read() throws IOException {
			return bufferedReader.readLine();
		}

		private Runnable runnable() {
			return new Runnable() {
				@Override
				public void run() {
					throw new UnsupportedOperationException(
							"not yet implemented");
				}
			};
		}

		public void send(String message) {
			printWriter.print(message);
			printWriter.print(new String(protocol.getSeparator()));
			printWriter.flush();
		}

		public void startBackgroundReader() {
			new Thread(runnable()) {
				{
					setDaemon(true);
					start();
				}
			};
		}
	}

	private static final int DEFAULT_LISTENING_PORT = 4478;

	private static final int DEFAULT_SPEED = 115200;

	@Named("tcphost")
	private String tcphost;

	@Named("tcpport")
	private int tcpport = DEFAULT_LISTENING_PORT;

	@Named("tcpproto")
	private Protocol tcpproto = ArdulinkProtocolN.instance();

	@Named("port")
	private String port;

	@Named("speed")
	private int speed = DEFAULT_SPEED;

	@Named("proto")
	private Protocol proto = ArdulinkProtocolN.instance();

	@Named("networkproto")
	private ProxyConnectionToRemote remote;

	private Protocol networkProto = ArdulinkProtocolN.instance();;

	public Protocol getNetworkProto() {
		return networkProto;
	}

	public String getPort() {
		return port;
	}

	public int getSpeed() {
		return speed;
	}

	public Protocol getProto() {
		return proto;
	}

	public String getTcphost() {
		return tcphost;
	}

	public int getTcpport() {
		return tcpport;
	}

	public Protocol getTcpproto() {
		return tcpproto;
	}

	public void setNetworkProto(Protocol networkProto) {
		this.networkProto = networkProto;
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

	public void setTcpproto(String tcpproto) {
		this.tcpproto = Protocols.getByName(tcpproto);
	}

	@ChoiceFor("port")
	public List<String> getAvailablePorts() throws IOException {
		return getRemote().getPortList();
	}

	public synchronized ProxyConnectionToRemote getRemote()
			throws UnknownHostException, IOException {
		if (this.remote == null) {
			this.remote = new ProxyConnectionToRemote(tcphost, tcpport,
					tcpproto);
		}
		return this.remote;
	}

}
