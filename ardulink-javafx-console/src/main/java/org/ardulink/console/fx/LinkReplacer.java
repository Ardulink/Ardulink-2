package org.ardulink.console.fx;

import static org.ardulink.core.NullLink.isNullLink;

import org.ardulink.core.AbstractListenerLink;
import org.ardulink.core.ConnectionListener;
import org.ardulink.core.Link;

public final class LinkReplacer {

	private LinkReplacer() {
	}

	public static Link replace(Link oldLink, Link newLink, ConnectionListener listener) {
		if (oldLink instanceof AbstractListenerLink) {
			((AbstractListenerLink) oldLink).removeConnectionListener(listener);
		}
		if (newLink instanceof AbstractListenerLink) {
			((AbstractListenerLink) newLink).addConnectionListener(listener);
		}
		if (isNullLink(newLink)) {
			listener.connectionLost();
		} else {
			listener.reconnected();
		}
		return newLink;
	}
}
