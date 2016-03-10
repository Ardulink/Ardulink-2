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

package com.github.pfichtner.core.serial;

import static gnu.io.SerialPort.DATABITS_8;
import static gnu.io.SerialPort.PARITY_NONE;
import static gnu.io.SerialPort.STOPBITS_1;
import static java.util.concurrent.TimeUnit.SECONDS;
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
import com.github.pfichtner.ardulink.core.proto.impl.ArdulinkProtocol2;
import com.github.pfichtner.ardulink.core.qos.ConnectionBasedQosLink;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class SerialLinkFactory implements LinkFactory<SerialLinkConfig> {

	private static final Protocol proto = ArdulinkProtocol2.instance();

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
				proto);

		return new LinkDelegate(waitForArdulink(config,
				createDelegateTo(config, connection))) {
			@Override
			public void close() throws IOException {
				super.close();
				serialPort.close();
			}
		};
	}

	private AbstractConnectionBasedLink waitForArdulink(
			SerialLinkConfig config, AbstractConnectionBasedLink link) {
		if (config.isPingprobe()) {
			checkState(
					link.waitForArduinoToBoot(config.getWaitsecs(), SECONDS),
					"Waited for arduino to boot but no response received");
		} else {
			try {
				SECONDS.sleep(config.getWaitsecs());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
		return link;
	}

	@SuppressWarnings("resource")
	private AbstractConnectionBasedLink createDelegateTo(
			SerialLinkConfig config, StreamConnection connection)
			throws IOException {
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
