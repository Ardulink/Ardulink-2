package org.zu.ardulink.connection.proxy;

import static org.zu.ardulink.connection.proxy.NetworkProxyMessages.CONNECT_CMD;
import static org.zu.ardulink.connection.proxy.NetworkProxyMessages.GET_PORT_LIST_CMD;
import static org.zu.ardulink.connection.proxy.NetworkProxyMessages.NUMBER_OF_PORTS;
import static org.zu.ardulink.connection.proxy.NetworkProxyMessages.*;
import static org.zu.ardulink.connection.proxy.NetworkProxyMessages.STOP_SERVER_CMD;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.Scanner;

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.Configurer;
import com.github.pfichtner.ardulink.core.proto.api.Protocol;

public abstract class Handshaker {

	private final Scanner scanner;
	private final PrintWriter printWriter;
	private final String separator;
	private final Configurer configurer;

	public Handshaker(InputStream inputStream, OutputStream outputStream,
			Configurer configurer, Protocol proto) {
		this.configurer = configurer;
		this.printWriter = new PrintWriter(outputStream);
		this.separator = new String(proto.getSeparator());
		this.scanner = new Scanner(inputStream).useDelimiter(separator);
	}

	public Link doHandshake() throws Exception {
		Link link = null;
		while (scanner.hasNext() && link == null) {
			String input = read();
			if (input.equals(STOP_SERVER_CMD)) {
				NetworkProxyServer.stop();
			} else if (GET_PORT_LIST_CMD.equals(input)) {
				handleGetPortList();
			} else if (CONNECT_CMD.equals(input)) {
				try {
					configurer.getAttribute("port").setValue(read());
					configurer.getAttribute("speed").setValue(
							new Integer(read()));
					link = newLink(configurer);
					write(OK);
				} catch (Exception e) {
					write(KO);
				}
			}
			printWriter.flush();
		}
		return link;
	}

	private void handleGetPortList() throws URISyntaxException, Exception,
			IOException {
		Object[] portList = getPortList();
		if (portList == null) {
			portList = new Object[0];
		}
		write(NUMBER_OF_PORTS + portList.length);
		for (Object port : portList) {
			write(port);
		}
	}

	private String read() {
		return scanner.next();
	}

	private void write(Object object) throws IOException {
		String message = object instanceof String ? ((String) object) : String
				.valueOf(object);
		printWriter.write(message);
		printWriter.write(separator);
	}

	protected abstract Link newLink(Configurer configurer) throws Exception;

	private Object[] getPortList() throws URISyntaxException, Exception {
		return configurer.getAttribute("port").getChoiceValues();
	}

}
