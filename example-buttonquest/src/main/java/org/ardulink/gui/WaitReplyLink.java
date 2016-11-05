package org.ardulink.gui;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Throwables.propagate;

import java.awt.Component;
import java.io.IOException;

import javax.swing.JOptionPane;

import org.ardulink.core.Link;
import org.ardulink.core.events.RplyEvent;
import org.ardulink.core.qos.ResponseAwaiter;
import org.ardulink.legacy.Link.LegacyLinkAdapter;

public class WaitReplyLink extends LegacyLinkAdapter {

	private Component component;
	private String key;
	
	public WaitReplyLink(Link delegate, Component component, String key) {
		super(delegate);
		this.component = checkNotNull(component, "Component can't be null");
		this.key = checkNotNull(key, "String key can't be null");
	}

	@Override
	public void sendCustomMessage(String... messages) {
		try {

			
			RplyEvent rplyEvent = ResponseAwaiter.onLink(getDelegate())
					.withTimeout(500, MILLISECONDS)
					.waitForResponse(getDelegate().sendCustomMessage(messages));
			
			JOptionPane.showMessageDialog(component, checkNotNull(rplyEvent.getParameterValue(key), "Reply doesn't contain %s key", key).toString());
		} catch (IOException e) {
			throw propagate(e);
		}
		
	}
	

}
