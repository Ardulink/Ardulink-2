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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.zu.ardulink.io.ReadingException;
import org.zu.ardulink.io.WritingException;

public class ConfigurationFacade {

	private static Logger logger = Logger.getLogger(ConfigurationFacade.class.getName());
	private static AConfiguration configuration = null;
	private static Map<String, ALink> linksMap = new Hashtable<String, ALink>();
	private static Map<String, AConnection> connectionsMap = new Hashtable<String, AConnection>();
	
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
		Iterator<ACommand> it = configuration.getaCommandList().getACommands().iterator();
		while (it.hasNext()) {
			ACommand aCommand = (ACommand) it.next();
			if(aCommand.isForContent(content)) {
				retvalue.add(aCommand);
			}
		}
		return retvalue;
	}
	
	public static List<ALink> getALinks(List<String> aLinkNames) {
		
		List<ALink> retvalue = new LinkedList<ALink>();
		
		Iterator<String> it = aLinkNames.iterator();
		while (it.hasNext()) {
			String aLinkName = (String) it.next();
			ALink aLink = linksMap.get(aLinkName);
			if(aLink == null) {
				Iterator<ALink> itLink = configuration.getaLinkList().getALinks().iterator();
				boolean found = false;
				while (itLink.hasNext() && !found) {
					ALink aLinkInList = (ALink) itLink.next();
					if(aLinkName.equals(aLinkInList.getName())) {
						aLink = aLinkInList;
						linksMap.put(aLinkName, aLink);
						found = true;
					}
				}
				if(!found) {
					throw new RuntimeException("ALink name in ACommand is not found in ALinksList please check this name in config file: " + aLinkName);
				}
			}
			retvalue.add(aLink);
		}
		
		return retvalue;
	}

	public static AConnection getAConnection(String aConnectionName) {
		
		AConnection retvalue = connectionsMap.get(aConnectionName);
		if(retvalue == null) {
			Iterator<AConnection> itConnection = configuration.getaConnectionList().getAConnections().iterator();
			boolean found = false;
			while (itConnection.hasNext() && !found) {
				AConnection aConnectionInList = (AConnection) itConnection.next();
				if(aConnectionName.equals(aConnectionInList.getName())) {
					retvalue = aConnectionInList;
					connectionsMap.put(aConnectionName, aConnectionInList);
					found = true;
				}
			}
			if(!found) {
				throw new RuntimeException("AConnection name in ALink is not found in AConnectionsList please check this name in config file: " + aConnectionName);
			}
		}
		return retvalue;
	}

}
