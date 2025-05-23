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

package org.ardulink.core.serial.jssc;

import static java.util.concurrent.TimeUnit.SECONDS;
import static jssc.SerialPort.DATABITS_8;
import static jssc.SerialPort.PARITY_NONE;
import static jssc.SerialPort.STOPBITS_1;
import static org.ardulink.util.Preconditions.checkNotNull;

import java.io.IOException;

import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.Link;
import org.ardulink.core.StreamConnection;
import org.ardulink.core.convenience.LinkDelegate;
import org.ardulink.core.linkmanager.LinkFactory;
import org.ardulink.core.qos.QosLink;

import jssc.SerialPort;
import jssc.SerialPortException;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class SerialLinkFactory implements LinkFactory<SerialLinkConfig> {

	protected static final String NAME = "serial-jssc";

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public LinkDelegate newLink(SerialLinkConfig config) throws SerialPortException, IOException {
		SerialPort serialPort = serialPort(config);
		ConnectionBasedLink connectionBasedLink = new ConnectionBasedLink(
				new StreamConnection(new SerialInputStream(serialPort), new SerialOutputStream(serialPort),
						config.protocol().newByteStreamProcessor()));

		Link link = config.qos ? new QosLink(connectionBasedLink) : connectionBasedLink;

		if (!waitForArdulink(config, connectionBasedLink)) {
			connectionBasedLink.close();
			throw new IllegalStateException("Waited for arduino to boot but no response received");
		}

		return new LinkDelegate(link) {
			@Override
			public void close() throws IOException {
				super.close();
				try {
					serialPort.closePort();
				} catch (SerialPortException e) {
					throw new IOException(e);
				}
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

	private SerialPort serialPort(SerialLinkConfig config) throws SerialPortException {
		SerialPort serialPort = new SerialPort(checkNotNull(config.port, "Port must not be null"));
		serialPort.openPort();
		serialPort.setParams(config.baudrate, DATABITS_8, STOPBITS_1, PARITY_NONE);
		return serialPort;
	}

	@Override
	public SerialLinkConfig newLinkConfig() {
		return new SerialLinkConfig();
	}

}
