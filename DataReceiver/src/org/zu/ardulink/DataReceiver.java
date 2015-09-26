/**
Copyright 2013 Luciano Zu project Ardulink http://www.ardulink.org/

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

@author Luciano Zu
 */

package org.zu.ardulink;

import static java.lang.String.format;
import static org.zu.ardulink.connection.proxy.NetworkProxyConnection.DEFAULT_LISTENING_PORT;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.zu.ardulink.connection.proxy.NetworkProxyConnection;
import org.zu.ardulink.event.AnalogReadChangeEvent;
import org.zu.ardulink.event.AnalogReadChangeListener;
import org.zu.ardulink.event.ConnectionEvent;
import org.zu.ardulink.event.ConnectionListener;
import org.zu.ardulink.event.DigitalReadChangeEvent;
import org.zu.ardulink.event.DigitalReadChangeListener;
import org.zu.ardulink.event.DisconnectionEvent;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class DataReceiver {

	@Option(name = "-delay", usage = "Do a n seconds delay after connecting")
	private int sleepSecs = 10;

	@Option(name = "-v", usage = "Be verbose")
	private boolean verbose;

	@Option(name = "-remote", usage = "Host and port of a remote arduino")
	private String remote;

	@Option(name = "-d", aliases = "--digital", usage = "Digital pins to listen to")
	private int[] digitals = new int[] { 2 };

	@Option(name = "-a", aliases = "--analog", usage = "Analog pins to listen to")
	private int[] analogs = new int[0];

	@Option(name = "-msga", aliases = "--analogMessage", usage = "Message format for analog pins")
	private String msgAnalog = "PIN state changed. Analog PIN: %s Value: %s";

	@Option(name = "-msgd", aliases = "--digitalMessage", usage = "Message format for digital pins")
	private String msgDigital = "PIN state changed. Digital PIN: %s Value: %s";

	private Link link;

	private static final Logger log = Logger.getLogger(DataReceiver.class
			.getName());

	public static void main(String[] args) {
		new DataReceiver().doMain(args);
	}

	private void doMain(String[] args) {
		CmdLineParser cmdLineParser = new CmdLineParser(this);
		try {
			cmdLineParser.parseArgument(args);
		} catch (CmdLineException e) {
			System.err.println(e.getMessage());
			cmdLineParser.printUsage(System.err);
			return;
		}
		work();
	}

	private void work() {
		link = createLink();
		List<String> portList = link.getPortList();
		if (portList != null && !portList.isEmpty()) {

			// Register this class as connection listener
			link.addConnectionListener(connectionListener());

			String port = portList.get(0);
			log.info("Trying to connect to: " + port);
			boolean connected = link.connect(port, 115200);
			if (!connected) {
				throw new RuntimeException("Connection failed!");
			}
			try {
				log.info("Wait a while for Arduino boot");
				TimeUnit.SECONDS.sleep(sleepSecs);
				log.info("Ok, now it should be ready...");

				link.addAnalogReadChangeListener(analogReadChangeListener());
				for (int analog : analogs) {
					link.startListenAnalogPin(analog);
				}

				link.addDigitalReadChangeListener(digitalReadChangeListener());
				for (int digital : digitals) {
					link.startListenDigitalPin(digital);
				}

				if (verbose) {
					link.addRawDataListener(rawDataListener());
				}

			} catch (InterruptedException e1) {
				throw new RuntimeException(e1);
			}
		} else {
			throw new RuntimeException("No port found!");
		}
	}

	private ConnectionListener connectionListener() {
		return new ConnectionListener() {
			@Override
			public void connected(ConnectionEvent e) {
				log.info("Connected! Port: " + e.getPortName() + " ID: "
						+ e.getConnectionId());
			}

			@Override
			public void disconnected(DisconnectionEvent e) {
				log.info("Disconnected! ID: " + e.getConnectionId());
			}

		};
	}

	private RawDataListener rawDataListener() {
		return new RawDataListener() {

			/**
			 * All messages from Arduino are sent to this method in their raw
			 * format
			 */
			@Override
			public void parseInput(String id, int numBytes, int[] message) {

				log.info("Message from: " + id);
				StringBuilder builder = new StringBuilder(numBytes);
				for (int i = 0; i < numBytes; i++) {
					builder.append((char) message[i]);
				}

				log.info("Message: " + builder.toString());
			}

		};
	}

	private DigitalReadChangeListener digitalReadChangeListener() {
		return new DigitalReadChangeListener() {

			@Override
			public void stateChanged(DigitalReadChangeEvent e) {
				log.info(format(msgDigital, e.getPin(), e.getValue()));
			}

			@Override
			public int getPinListening() {
				return DigitalReadChangeListener.ALL_PINS;
			}

		};
	}

	private AnalogReadChangeListener analogReadChangeListener() {
		return new AnalogReadChangeListener() {

			@Override
			public void stateChanged(AnalogReadChangeEvent e) {
				log.info(format(msgAnalog, e.getPin(), e.getValue()));
			}

			@Override
			public int getPinListening() {
				return AnalogReadChangeListener.ALL_PINS;
			}
		};
	}

	/**
	 * Return the Link used from this example
	 * 
	 * @return
	 */
	private Link createLink() {
		if (remote == null || remote.isEmpty()) {
			return Link.getDefaultInstance();
		}

		String[] hostAndPort = remote.split("\\:");
		try {
			int port = hostAndPort.length == 1 ? DEFAULT_LISTENING_PORT
					: Integer.parseInt(hostAndPort[1]);
			return Link.createInstance("network", new NetworkProxyConnection(
					hostAndPort[0], port));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
