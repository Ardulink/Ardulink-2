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

import org.zu.ardulink.Link;
import org.zu.ardulink.mail.server.links.configuration.utils.ConfigurationUtility;
import org.zu.ardulink.protocol.IProtocol;

/**
 * This simple IContentManager implementation sends strings in values ACommand instance when mail body content
 * contains (case insensitive) one or more than a mail content hook in a ACommand instance. It sends strings with Link.writeSerial(String) method.
 * Link.writeSerial(String) method doesn't care about protocol in use with the Link instance.
 *   
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 *
 */
public class SimpleContentManager implements IContentManager {

	@Override
	public boolean isForContent(String content, List<String> mailContentHooks) {
		for (String hook : mailContentHooks) {
			if(content.toUpperCase().contains(hook.toUpperCase())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String execute(String content, List<String> values, List<String> mailContentHooks, List<String> aLinkNames) {
		
		StringBuilder builder = new StringBuilder();
		
		List<Link> links = ConfigurationUtility.getConnectedLinks(aLinkNames);
		for (Link link : links) {
			for (String string : values) {
				StringBuilder value = new StringBuilder(string);
				value.append(new String(new byte[] { IProtocol.DEFAULT_OUTGOING_MESSAGE_DIVIDER }));
				boolean isOk = link.writeSerial(value.toString());
				builder.append("message ").append(value).append(" ");
				if (!isOk) {
					builder.append("NOT ");
				}
				builder.append("sent for link: ").append(link.getName());
				builder.append("\n");
			}
		}
		
		return builder.toString();
	}

}
