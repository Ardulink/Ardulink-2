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

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.zu.ardulink.io.ReadingException;
import org.zu.ardulink.io.WritingException;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ConfigurationFacade {

	private static Logger logger = Logger.getLogger(ConfigurationFacade.class.getName());
	private static AConfiguration configuration;
	private static final Map<String, ALink> linksMap = new Hashtable<String, ALink>();
	private static final Map<String, AConnection> connectionsMap = new Hashtable<String, AConnection>();
	
	static {
		try {
			loadConfiguration();
		} catch (ReadingException e) { // maybe config file doesn't exist I'll write it with default values
			try {
				configuration = new AConfiguration(); // Default
				saveConfiguration();
			} catch (WritingException e1) {
				e1.printStackTrace();
				logger.severe("CONFIGURATION ERROR. APPLICATION IS NOT ABLE TO CONFIGURE ITSELF. EXITING!");
				System.exit(-1);
			}
		}
	}
	
	public static AConfiguration getConfiguration() {
		return configuration;
	}
	
	public static void saveConfiguration() throws WritingException {
		ConfigurationSerializer.write(configuration, ConfigurationSerializer.CONFIGURATION_FILE_NAME);
	}
	
	public static AConfiguration loadConfiguration() throws ReadingException {
		configuration = ConfigurationSerializer.read(ConfigurationSerializer.CONFIGURATION_FILE_NAME);
		return configuration;
	}
	

	/**
	 * search for a list of ACommand that has a content hooks right for this content.
	 * @param content
	 * @return
	 */
	public static List<ACommand> findCommands(String content) {
		List<ACommand> retvalue = new LinkedList<ACommand>();
		for (ACommand aCommand : configuration.getaCommandList().getACommands()) {
			if(aCommand.isForContent(content)) {
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
				linksMap.put(
						aLinkName,
						linkByName(aLinkName, configuration.getaLinkList()
								.getALinks()));
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

	public static AConnection getAConnection(String aConnectionName) {
		AConnection retvalue = connectionsMap.get(aConnectionName);
		if (retvalue == null) {
			connectionsMap.put(
					aConnectionName,
					connectionByName(aConnectionName, configuration
							.getaConnectionList().getAConnections()));
		}
		return retvalue;
	}
	
	private static AConnection connectionByName(String aConnectionName, Iterable<AConnection> connections) {
		for (AConnection aConnection : connections) {
			if (aConnectionName.equals(aConnection.getName())) {
				return aConnection;
			}
		}
		throw new RuntimeException("AConnection name in ALink is not found in AConnectionsList please check this name in config file: " + aConnectionName);
	}

}
