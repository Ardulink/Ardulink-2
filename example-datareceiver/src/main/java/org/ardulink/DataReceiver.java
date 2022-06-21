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

package org.ardulink;

import static java.lang.String.format;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;

import org.ardulink.core.Connection;
import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.Link;
import org.ardulink.core.convenience.Links;
import org.ardulink.core.events.AnalogPinValueChangedEvent;
import org.ardulink.core.events.DigitalPinValueChangedEvent;
import org.ardulink.core.events.EventListener;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class DataReceiver {

	@Option(name = "-v", usage = "Be verbose")
	private boolean verbose;

	@Option(name = "-connection", usage = "Connection URI to the arduino")
	private String connection = "ardulink://serial";

	@Option(name = "-d", aliases = "--digital", usage = "Digital pins to listen to")
	private int[] digitals = new int[] { 2 };

	@Option(name = "-a", aliases = "--analog", usage = "Analog pins to listen to")
	private int[] analogs = new int[0];

	@Option(name = "-msga", aliases = "--analogMessage", usage = "Message format for analog pins")
	private String msgAnalog = "PIN state changed. Analog PIN: %s Value: %s";

	@Option(name = "-msgd", aliases = "--digitalMessage", usage = "Message format for digital pins")
	private String msgDigital = "PIN state changed. Digital PIN: %s Value: %s";

	private Link link;

	private static final Logger logger = LoggerFactory
			.getLogger(DataReceiver.class);

	public static void main(String[] args) throws Exception {
		new DataReceiver().doMain(args);
	}

	private void doMain(String[] args) throws Exception {
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

	private void work() throws Exception {
		this.link = Links.getLink(connection);
		link.addListener(eventListener());

		for (int analog : analogs) {
			link.startListening(analogPin(analog));
		}
		for (int digital : digitals) {
			link.startListening(digitalPin(digital));
		}

		if (verbose && link instanceof ConnectionBasedLink) {
			((ConnectionBasedLink) link).getConnection().addListener(rawDataListener());
		}
	}

	private EventListener eventListener() {
		return new EventListener() {
			@Override
			public void stateChanged(DigitalPinValueChangedEvent event) {
				logger.info(format(msgDigital, event.getPin(), event.getValue()));
			}

			@Override
			public void stateChanged(AnalogPinValueChangedEvent event) {
				logger.info(format(msgAnalog, event.getPin(), event.getValue()));
			}
		};
	}

	private Connection.Listener rawDataListener() {
		return new Connection.ListenerAdapter() {
			@Override
			public void received(byte[] bytes) {
				logger.info("Message from Arduino: %s", new String(bytes));
			}
		};
	}

}
