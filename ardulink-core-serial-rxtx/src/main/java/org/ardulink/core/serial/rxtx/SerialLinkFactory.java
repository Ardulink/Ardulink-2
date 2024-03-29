/**
Copyright 2013 project Ardulink http://www.ardulink.org/
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */

package org.ardulink.core.serial.rxtx;

import static gnu.io.SerialPort.DATABITS_8;
import static gnu.io.SerialPort.PARITY_NONE;
import static gnu.io.SerialPort.STOPBITS_1;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ardulink.util.Preconditions.checkState;

import java.io.IOException;

import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.Link;
import org.ardulink.core.StreamConnection;
import org.ardulink.core.convenience.LinkDelegate;
import org.ardulink.core.linkmanager.LinkFactory;
import org.ardulink.core.qos.QosLink;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class SerialLinkFactory implements LinkFactory<SerialLinkConfig> {

	protected static final String NAME = "serial-rxtx";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public LinkDelegate newLink(SerialLinkConfig config)
			throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException {
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(config.port);
		checkState(!portIdentifier.isCurrentlyOwned(), "Port %s is currently in use", config.port);
		SerialPort serialPort = serialPort(config, portIdentifier);

		StreamConnection connection = new StreamConnection(serialPort.getInputStream(), serialPort.getOutputStream(),
				config.protocol().newByteStreamProcessor());

		ConnectionBasedLink connectionBasedLink = new ConnectionBasedLink(connection);
		Link link = config.qos ? new QosLink(connectionBasedLink) : connectionBasedLink;

		if (!waitForArdulink(config, connectionBasedLink)) {
			connection.close();
			throw new IllegalStateException("Waited for arduino to boot but no response received");
		}

		return new LinkDelegate(link) {
			@Override
			public void close() throws IOException {
				super.close();
				serialPort.close();
			}
		};
	}

	private boolean waitForArdulink(SerialLinkConfig config, ConnectionBasedLink link) {
		if (config.pingprobe) {
			return link.waitForArduinoToBoot(config.waitsecs, SECONDS);
		}
		try {
			SECONDS.sleep(config.waitsecs);
			return true;
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return false;
	}

	private SerialPort serialPort(SerialLinkConfig config, CommPortIdentifier portIdentifier)
			throws PortInUseException, UnsupportedCommOperationException {
		SerialPort serialPort = portIdentifier.open("RTBug_network", 2000);
		serialPort.setSerialPortParams(config.baudrate, DATABITS_8, STOPBITS_1, PARITY_NONE);
		return serialPort;
	}

	@Override
	public SerialLinkConfig newLinkConfig() {
		return new SerialLinkConfig();
	}

}
