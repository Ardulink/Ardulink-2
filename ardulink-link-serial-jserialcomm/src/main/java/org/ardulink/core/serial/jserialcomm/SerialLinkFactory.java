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

package org.ardulink.core.serial.jserialcomm;

import static com.fazecast.jSerialComm.SerialPort.NO_PARITY;
import static com.fazecast.jSerialComm.SerialPort.ONE_STOP_BIT;
import static com.fazecast.jSerialComm.SerialPort.TIMEOUT_READ_SEMI_BLOCKING;
import static com.fazecast.jSerialComm.SerialPort.getCommPort;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;

import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.Link;
import org.ardulink.core.StreamConnection;
import org.ardulink.core.convenience.LinkDelegate;
import org.ardulink.core.linkmanager.LinkFactory;
import org.ardulink.core.qos.QosLink;

import com.fazecast.jSerialComm.SerialPort;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class SerialLinkFactory implements LinkFactory<SerialLinkConfig> {

	protected static final String NAME = "serial-jserialcomm";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public LinkDelegate newLink(SerialLinkConfig config) throws IOException {
		SerialPort serialPort = serialPort(config);
		ConnectionBasedLink connectionBasedLink = new ConnectionBasedLink(new StreamConnection(
				serialPort.getInputStream(), serialPort.getOutputStream(), config.protocol().newByteStreamProcessor()));

		Link link = config.qos ? new QosLink(connectionBasedLink) : connectionBasedLink;

		if (!waitForArdulink(config, connectionBasedLink)) {
			connectionBasedLink.close();
			throw new IllegalStateException("Waited for arduino to boot but no response received");
		}

		return new LinkDelegate(link) {
			@Override
			public void close() throws IOException {
				super.close();
				serialPort.closePort();
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

	private SerialPort serialPort(SerialLinkConfig config) {
		SerialPort serialPort = getCommPort(config.port);
		serialPort.setComPortParameters(config.baudrate, 8, ONE_STOP_BIT, NO_PARITY);
		serialPort.openPort();
		serialPort.setComPortTimeouts(TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
		return serialPort;
	}

	@Override
	public SerialLinkConfig newLinkConfig() {
		return new SerialLinkConfig();
	}

}
