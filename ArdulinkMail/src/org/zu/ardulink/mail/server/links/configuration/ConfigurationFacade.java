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

package org.zu.ardulink.mail.server.links.configuration;

import static java.util.Collections.synchronizedMap;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zu.ardulink.io.ReadingException;
import org.zu.ardulink.io.WritingException;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 *         [adsense]
 *
 */
public class ConfigurationFacade {

	private static final Logger logger = LoggerFactory
			.getLogger(ConfigurationFacade.class);
	private static AConfiguration configuration;
	private static final Map<String, ALink> linksMap = synchronizedMap(new HashMap<String, ALink>());

	static {
		try {
			loadConfiguration();
		} catch (ReadingException e) { // maybe config file doesn't exist I'll
										// write it with default values
			try {
				configuration = new AConfiguration(); // Default
				saveConfiguration();
			} catch (WritingException e1) {
				e1.printStackTrace();
				logger.error("CONFIGURATION ERROR. APPLICATION IS NOT ABLE TO CONFIGURE ITSELF. EXITING!");
				System.exit(-1);
			}
		}
	}

	public static AConfiguration getConfiguration() {
		return configuration;
	}

	public static void saveConfiguration() throws WritingException {
		ConfigurationSerializer.write(configuration,
				ConfigurationSerializer.CONFIGURATION_FILE_NAME);
	}

	public static AConfiguration loadConfiguration() throws ReadingException {

		configuration = loadConfigurationInClassPath();
		if (configuration == null) {
			configuration = ConfigurationSerializer
					.read(ConfigurationSerializer.CONFIGURATION_FILE_NAME);
		}

		return configuration;
	}

	private static AConfiguration loadConfigurationInClassPath()
			throws ReadingException {

		AConfiguration retvalue = null;
		ClassLoader classLoader = ConfigurationFacade.class.getClassLoader();
		InputStream is = classLoader
				.getResourceAsStream(ConfigurationSerializer.CONFIGURATION_FILE_NAME);
		if (is == null) {
			is = ClassLoader
					.getSystemResourceAsStream(ConfigurationSerializer.CONFIGURATION_FILE_NAME);
		}

		if (is != null) {
			retvalue = ConfigurationSerializer.read(is);
		}
		return retvalue;
	}

	/**
	 * search for a list of ACommand that has a content hooks right for this
	 * content.
	 * 
	 * @param content
	 * @return
	 */
	public static List<ACommand> findCommands(String content) {
		List<ACommand> retvalue = new LinkedList<ACommand>();
		for (ACommand aCommand : configuration.getaCommandList().getACommands()) {
			if (aCommand.isForContent(content)) {
				retvalue.add(aCommand);
			}
		}
		return retvalue;
	}

	public static List<ALink> getALinks(List<String> aLinkNames) {
		List<ALink> retvalue = new LinkedList<ALink>();
		for (String aLinkName : aLinkNames) {
			ALink aLink = linksMap.get(aLinkName);
			if (aLink == null) {
				aLink = linkByName(aLinkName, configuration.getaLinkList()
						.getALinks());
				linksMap.put(aLinkName, aLink);
			}
			retvalue.add(aLink);
		}
		return retvalue;
	}

	private static ALink linkByName(String aLinkName, Iterable<ALink> links) {
		for (ALink aLink : links) {
			if (aLinkName.equals(aLink.getName())) {
				return aLink;
			}
		}
		throw new RuntimeException(
				"ALink name in ACommand is not found in ALinksList please check this name in config file: "
						+ aLinkName);
	}

}
