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
import static org.ardulink.util.Preconditions.checkState;

import java.io.IOException;

import org.ardulink.core.AbstractListenerLink;
import org.ardulink.core.CBL;
import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.ConnectionBasedLinkNG;
import org.ardulink.core.Link;
import org.ardulink.core.StreamConnection;
import org.ardulink.core.convenience.LinkDelegate;
import org.ardulink.core.linkmanager.LinkFactory;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.api.ProtocolNG;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor;
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

	@Override
	public String getName() {
		return "serial-jssc";
	}

	@Override
	public LinkDelegate newLink(SerialLinkConfig config)
			throws SerialPortException, IOException {
		String portIdentifier = config.getPort();
		final SerialPort serialPort = serialPort(config, portIdentifier);

		Protocol proto = config.getProto();
		
		AbstractListenerLink connectionBasedLink;
		if (proto instanceof ProtocolNG) {
			ProtocolNG protocolNG = (ProtocolNG) proto;
			ByteStreamProcessor byteStreamProcessor = protocolNG.newByteStreamProcessor();
			connectionBasedLink = new ConnectionBasedLinkNG(
					new StreamConnection(new SerialInputStream(serialPort), new SerialOutputStream(serialPort), byteStreamProcessor),
					byteStreamProcessor);
		} else {
			connectionBasedLink = new ConnectionBasedLink(
					new StreamConnection(new SerialInputStream(serialPort), new SerialOutputStream(serialPort), proto),
					proto);
		}

		@SuppressWarnings("resource")
		Link link = config.isQos() ? new QosLink(connectionBasedLink)
				: connectionBasedLink;

		waitForArdulink(config, (CBL) connectionBasedLink);
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

	private void waitForArdulink(SerialLinkConfig config,
			CBL link) {
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
	}

	private SerialPort serialPort(SerialLinkConfig config, String portIdentifier)
			throws SerialPortException {
		SerialPort serialPort = new SerialPort(portIdentifier);
		serialPort.openPort();
		serialPort.setParams(config.getBaudrate(), DATABITS_8, STOPBITS_1,
				PARITY_NONE);
		return serialPort;
	}

	@Override
	public SerialLinkConfig newLinkConfig() {
		return new SerialLinkConfig();
	}

}
