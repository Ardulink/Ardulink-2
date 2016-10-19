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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.ardulink.core.convenience.Links.setChoiceValues;
import static org.ardulink.util.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.ardulink.core.Link;
import org.ardulink.core.events.RplyEvent;
import org.ardulink.core.linkmanager.LinkManager;
import org.ardulink.core.qos.ResponseAwaiter;
import org.ardulink.util.URIs;
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
public class UniqueID {

	private static final String GET_UNIQUE_ID_CUSTOM_MESSAGE = "getUniqueID";
	private static final String UNIQUE_ID_PARAMETER_VALUE_KEY = "UniqueID";

	@Option(name = "-delay", usage = "Do a n seconds delay after connecting")
	private int sleepSecs = 10;

	@Option(name = "-connection", usage = "Connection URI to the arduino")
	private String connString = "ardulink://serial-jssc";

	private Link link;

	private String sugestedUniqueID;

	private static final Logger logger = LoggerFactory
			.getLogger(UniqueID.class);

	public static void main(String[] args) throws Exception {
		new UniqueID().doMain(args);
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
		this.link = createLink();

		try {
			logger.info("Wait a while for Arduino boot");
			TimeUnit.SECONDS.sleep(sleepSecs);
			logger.info("Ok, now it should be ready...");

			logger.info("Asking ID...");
			RplyEvent rplyEvent = ResponseAwaiter.onLink(link)
					.withTimeout(500, MILLISECONDS)
					.waitForResponse(sendUniqueIdCustomMsg(link));
			
			if(rplyEvent.isOk()) {
				String uniqueID = checkNotNull(rplyEvent.getParameterValue(UNIQUE_ID_PARAMETER_VALUE_KEY), "Reply doesn't contain UniqueID").toString();

				if(sugestedUniqueID.equals(uniqueID)) {
					logger.info("Device hadn't an ID. Now it is set to: " + uniqueID);
				} else {
					logger.info("Device ID is: " + uniqueID);
				}
				
			} else {
				logger.info("Something went wrong.");
			}
			
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}

	}

	private long sendUniqueIdCustomMsg(Link link) throws IOException {
		return link.sendCustomMessage(GET_UNIQUE_ID_CUSTOM_MESSAGE,	getSuggestedUniqueID());
	}
	
	private String getSuggestedUniqueID() {
		sugestedUniqueID = UUID.randomUUID().toString();
		return sugestedUniqueID;
	}

	private Link createLink() {
		return setChoiceValues(
				LinkManager.getInstance()
						.getConfigurer(URIs.newURI(connString))).newLink();
	}

}
