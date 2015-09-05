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

package org.zu.ardulink.mail.server;

import java.util.Iterator;
import java.util.List;

import javax.mail.MessagingException;

import org.zu.ardulink.mail.server.links.configuration.ACommand;
import org.zu.ardulink.mail.server.links.configuration.ConfigurationFacade;

public class ArdulinkExecutor {

	/**
	 * This method execute the request embedded in mail content. If is returned a string not null then it should be used as body for the reply message
	 * @param content
	 * @return
	 */
	public String execute(String content) throws MessagingException {
		StringBuilder builder = new StringBuilder();
		
		List<ACommand> commands = findCommands(content);
		Iterator<ACommand> it = commands.iterator();
		while (it.hasNext()) {
			ACommand aCommand = (ACommand) it.next();
			String execution = aCommand.execute(content);
			if(execution != null && execution.length() > 0) {
				builder.append("For command: ");
				builder.append(aCommand.getName());
				builder.append("\n");
				builder.append(execution);
				builder.append("\n");
			}
		}
		
		String retvalue = null;
		if(builder.length() > 0) {
			retvalue = builder.toString();
		}
		
		return retvalue;
	}

	private List<ACommand> findCommands(String content) throws MessagingException {
				
		List<ACommand> commands = ConfigurationFacade.findCommands(content);
		if(commands == null || commands.size() == 0) {
			throw new MessagingException("No command is found for content: " + content);
		}
		
		return commands;
	}

}
