package com.github.pfichtner.core.serial;

import static gnu.io.SerialPort.DATABITS_8;
import static gnu.io.SerialPort.PARITY_NONE;
import static gnu.io.SerialPort.STOPBITS_1;
import static org.zu.ardulink.util.Preconditions.checkNotNull;
import static org.zu.ardulink.util.Preconditions.checkState;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;

import com.github.pfichtner.ardulink.core.AbstractConnectionBasedLink;
import com.github.pfichtner.ardulink.core.ConnectionBasedLink;
import com.github.pfichtner.ardulink.core.StreamConnection;
import com.github.pfichtner.ardulink.core.convenience.LinkDelegate;
import com.github.pfichtner.ardulink.core.linkmanager.LinkFactory;
import com.github.pfichtner.ardulink.core.proto.api.Protocol;
import com.github.pfichtner.ardulink.core.proto.impl.ArdulinkProtocol255;
import com.github.pfichtner.ardulink.core.qos.ConnectionBasedQosLink;

public class SerialLinkFactory implements LinkFactory<SerialLinkConfig> {

	// TODO Ardulink sends with ArdulinkProtocol255 but receives with
	// ArdulinkProtocolN
	private static final Protocol READ_PROTO = ArdulinkProtocol255.instance();

	@Override
	public String getName() {
		return "serial";
	}

	@Override
	public LinkDelegate newLink(SerialLinkConfig config)
			throws NoSuchPortException, PortInUseException,
			UnsupportedCommOperationException, IOException {
		CommPortIdentifier portIdentifier = CommPortIdentifier
				.getPortIdentifier(checkNotNull(config.getPort(),
						"port must not be null"));
		checkState(!portIdentifier.isCurrentlyOwned(),
				"Port %s is currently in use", config.getPort());
		final SerialPort serialPort = serialPort(config, portIdentifier);
		StreamConnection connection = new StreamConnection(
				serialPort.getInputStream(), serialPort.getOutputStream(),
				READ_PROTO);

		return new LinkDelegate(createDelegateTo(config, connection)) {
			@Override
			public void close() throws IOException {
				super.close();
				serialPort.close();
			}
		};
	}

	@SuppressWarnings("resource")
	private AbstractConnectionBasedLink createDelegateTo(SerialLinkConfig config,
			StreamConnection connection) throws IOException {
		Protocol proto = config.getProto();
		return config.isQos() ? new ConnectionBasedQosLink(connection, proto)
				: new ConnectionBasedLink(connection, proto);
	}

	private SerialPort serialPort(SerialLinkConfig config,
			CommPortIdentifier portIdentifier) throws PortInUseException,
			UnsupportedCommOperationException {
		SerialPort serialPort = (SerialPort) portIdentifier.open(
				"RTBug_network", 2000);
		serialPort.setSerialPortParams(config.getBaudrate(), DATABITS_8,
				STOPBITS_1, PARITY_NONE);
		return serialPort;
	}

	@Override
	public SerialLinkConfig newLinkConfig() {
		return new SerialLinkConfig();
	}

}
