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
public class LinkExchanger {

	private final Link thisLink;
	private Link newLink;

	public LinkExchanger(Link thisLink) {
		this.thisLink = thisLink;
	}

	public static LinkExchanger exchange(Link link) {
		return new LinkExchanger(link);
	}

	public LinkExchanger with(Link newLink) {
		this.newLink = checkNotNull(newLink, "new link must not be null");
		return this;
	}

	public Link using(ConnectionListener connectionListener) {
		if (thisLink instanceof AbstractListenerLink) {
			((AbstractListenerLink) thisLink).removeConnectionListener(connectionListener);
		}
		if (newLink instanceof AbstractListenerLink) {
			((AbstractListenerLink) newLink).addConnectionListener(connectionListener);
		}
		callConnectionListener(newLink, connectionListener);
		return newLink;
	}

	private static void callConnectionListener(Link link, ConnectionListener connectionListener) {
		if (isNullLink(link)) {
			connectionListener.connectionLost();
		} else {
			connectionListener.reconnected();
		}
	}

}
