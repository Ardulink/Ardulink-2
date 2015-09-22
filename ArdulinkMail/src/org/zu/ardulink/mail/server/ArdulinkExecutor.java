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

import java.util.List;

import javax.mail.MessagingException;

import org.zu.ardulink.mail.server.links.configuration.ACommand;
import org.zu.ardulink.mail.server.links.configuration.ConfigurationFacade;

public class ArdulinkExecutor {

	/**
	 * This method execute the request embedded in mail content. If is returned
	 * a string not null then it should be used as body for the reply message
	 * 
	 * @param content
	 * @return
	 */
	public String execute(String content) throws MessagingException {
		List<ACommand> commands = findCommands(content);
		for (ACommand aCommand : commands) {
			aCommand.execute(content);
		}

		return null;
	}

	private List<ACommand> findCommands(String content)
			throws MessagingException {
		List<ACommand> commands = ConfigurationFacade.findCommands(content);
		if (commands == null || commands.isEmpty()) {
			throw new MessagingException("No command is found for content: "
					+ content);
		}
		return commands;
	}

}
