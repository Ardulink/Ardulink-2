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

package org.zu.ardulink.mail.server.contentmanagement;

import java.util.List;

import org.zu.ardulink.mail.server.links.configuration.utils.ConfigurationUtility;

import com.github.pfichtner.ardulink.core.Link;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ProtocolContentManager implements IContentManager {

	@Override
	public boolean isForContent(String content, List<String> mailContentHooks) {
		for (String hook : mailContentHooks) {
			if (content.toUpperCase().contains(hook.toUpperCase())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String execute(String content, List<String> values,
			List<String> mailContentHooks, List<String> aLinkNames) {
		StringBuilder builder = new StringBuilder();

		List<Link> links = ConfigurationUtility.getConnectedLinks(aLinkNames);
		for (Link link : links) {
			for (String value : values) {
				String returnMessage;
				boolean messageSent = false;
				try {
					returnMessage = sendMessage(link, value);
					messageSent = true;
				} catch (Exception e) {
					e.printStackTrace();
					returnMessage = e.getMessage();
				}
				builder.append("message ");
				builder.append(value);
				if (messageSent) {
					builder.append(" sent for link: ");
				} else {
					builder.append(" NOT sent for link: ");
				}
				builder.append(link);
				builder.append(" with this result: ");
				builder.append(returnMessage);
				builder.append("\n");
			}
		}

		return builder.toString();
	}

	private String sendMessage(Link link, String value) {
		// TODO call the right method on link
		return null;
	}

}
