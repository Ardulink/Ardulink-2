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

package org.ardulink.gui.util;

import static org.ardulink.core.NullLink.isNullLink;
import static org.ardulink.util.Preconditions.checkNotNull;

import org.ardulink.core.AbstractListenerLink;
import org.ardulink.core.ConnectionListener;
import org.ardulink.core.Link;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class LinkReplacer {

	protected Link oldLink;

	private static class ConnectionListenerLinkReplacer extends LinkReplacer {

		private ConnectionListener connectionListener;

		public ConnectionListenerLinkReplacer(ConnectionListener listener) {
			this.connectionListener = listener;
		}

		public Link with(Link newLink) {
			newLink = super.with(newLink);
			if (oldLink instanceof AbstractListenerLink) {
				((AbstractListenerLink) oldLink).removeConnectionListener(connectionListener);
			}
			if (newLink instanceof AbstractListenerLink) {
				((AbstractListenerLink) newLink).addConnectionListener(connectionListener);
			}
			callConnectionListener(newLink, connectionListener);
			return newLink;
		}

	}

	public static LinkReplacer withConnectionListener(ConnectionListener listener) {
		return new ConnectionListenerLinkReplacer(listener);
	}

	public static LinkReplacer doReplace(Link link) {
		return new LinkReplacer().replace(link);
	}

	public LinkReplacer replace(Link oldLink) {
		this.oldLink = oldLink;
		return this;
	}

	public Link with(Link newLink) {
		return checkNotNull(newLink, "new link must not be null");
	}

	private static void callConnectionListener(Link link, ConnectionListener connectionListener) {
		if (isNullLink(link)) {
			connectionListener.connectionLost();
		} else {
			connectionListener.reconnected();
		}
	}

}
