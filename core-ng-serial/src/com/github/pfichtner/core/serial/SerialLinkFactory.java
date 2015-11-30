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

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.ConnectionBasedLink;
import com.github.pfichtner.ardulink.core.StreamConnection;
import com.github.pfichtner.ardulink.core.linkmanager.LinkFactory;

public class SerialLinkFactory implements
		LinkFactory<SerialConnectionConfig> {

	@Override
	public String getName() {
		return "serial";
	}

	@Override
	public Link newLink(SerialConnectionConfig config)
			throws NoSuchPortException, PortInUseException,
			UnsupportedCommOperationException, IOException {
		CommPortIdentifier portIdentifier = CommPortIdentifier
				.getPortIdentifier(checkNotNull(config.getPort(),
						"port must not be null"));
		checkState(!portIdentifier.isCurrentlyOwned(),
				"Port %s is currently in use", config.getPort());
		SerialPort serialPort = serialPort(config, portIdentifier);
		return new ConnectionBasedLink(new StreamConnection(
				serialPort.getInputStream(), serialPort.getOutputStream()),
				config.getProto());
	}

	private SerialPort serialPort(SerialConnectionConfig config,
			CommPortIdentifier portIdentifier) throws PortInUseException,
			UnsupportedCommOperationException {
		SerialPort serialPort = (SerialPort) portIdentifier.open(
				"RTBug_network", 2000);
		serialPort.setSerialPortParams(config.getSpeed(), DATABITS_8,
				STOPBITS_1, PARITY_NONE);
		return serialPort;
	}

	@Override
	public SerialConnectionConfig newLinkConfig() {
		return new SerialConnectionConfig();
	}

}
