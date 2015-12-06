package org.zu.ardulink.connection.proxy;

import static org.zu.ardulink.connection.proxy.NetworkProxyMessages.CONNECT_CMD;
import static org.zu.ardulink.connection.proxy.NetworkProxyMessages.GET_PORT_LIST_CMD;
import static org.zu.ardulink.connection.proxy.NetworkProxyMessages.NUMBER_OF_PORTS;
import static org.zu.ardulink.connection.proxy.NetworkProxyMessages.OK;
import static org.zu.ardulink.connection.proxy.NetworkProxyMessages.STOP_SERVER_CMD;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Scanner;

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.Configurer;
import com.github.pfichtner.ardulink.core.proto.api.Protocol;

public class Handshaker {

	private final Scanner scanner;
	private final PrintWriter printWriter;
	private final String separator;
	private final List<LinkContainer> links;

	public Handshaker(InputStream inputStream, OutputStream outputStream,
			List<LinkContainer> links, Protocol proto) {
		this.links = links;
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
				link = handleConnect();
			}
			printWriter.flush();
		}
		return link;
	}

	public Link handleConnect() throws Exception, IOException {
		Link link = connect(read(), new Integer(read()));
		write(OK);
		return link;
	}

	public void handleGetPortList() throws URISyntaxException, Exception,
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

	public String read() {
		return scanner.next();
	}

	private void write(Object object) throws IOException {
		String message = object instanceof String ? ((String) object) : String
				.valueOf(object);
		printWriter.write(message);
		printWriter.write(separator);
	}

	private Link connect(String portName, int baudRate) throws Exception {
		Configurer configurer = getSerialLink();
		configurer.getAttribute("port").setValue(portName);
		configurer.getAttribute("speed").setValue(baudRate);

		CacheKey cacheKey = new CacheKey(configurer);
		LinkContainer container = findExisting(cacheKey);
		if (container == null) {
			links.add(container = new LinkContainer(configurer.newLink(),
					cacheKey));
		} else {
			container.increaseUsageCounter();
		}
		return container.getLink();
	}

	private LinkContainer findExisting(CacheKey ck) {
		for (LinkContainer linkContainer : links) {
			if (linkContainer.getCacheKey().equals(ck)) {
				return linkContainer;
			}
		}
		return null;
	}

	private Object[] getPortList() throws URISyntaxException, Exception {
		return getSerialLink().getAttribute("port").getChoiceValues();
	}

	public Configurer getSerialLink() throws URISyntaxException {
		return LinkManager.getInstance().getConfigurer(
				new URI("ardulink://serial"));
	}

}
