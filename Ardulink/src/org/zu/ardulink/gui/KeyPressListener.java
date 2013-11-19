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

package org.zu.ardulink.gui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;

import org.zu.ardulink.Link;

/**
 * [ardulinktitle] [ardulinkversion]
 * Class used by KeyPressController class to capture key press events.
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * @see KeyPressController
 * [adsense]
 *
 */
public class KeyPressListener extends KeyAdapter {

	private JLabel guiInteractionLabel = null;
	private Link link = Link.getDefaultInstance();
	
	public JLabel getGuiInteractionLabel() {
		return guiInteractionLabel;
	}

	public void setGuiInteractionLabel(JLabel guiInteractionLabel) {
		this.guiInteractionLabel = guiInteractionLabel;
	}

	public void removeGuiInteractionLabel() {
		this.guiInteractionLabel = null;
	}


	@Override
	public void keyPressed(KeyEvent e) {
		keyPressedGUIInteraction(e);
		
		link.sendKeyPressEvent(e.getKeyChar(), e.getKeyCode(), e.getKeyLocation(), e.getModifiers(), e.getModifiersEx());
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		keyReleasedGUIInteraction();
	}

	private void keyPressedGUIInteraction(KeyEvent e) {
		if(guiInteractionLabel != null) {
			String text = computeText4GUIInteraction(e);
			guiInteractionLabel.setText(text);
		}
	}

	private String computeText4GUIInteraction(KeyEvent e) {
		StringBuilder builder = new StringBuilder("Char: ");
		builder.append(e.getKeyChar());
		builder.append(" - Key Code: ");
		builder.append(e.getKeyCode());
		builder.append(" - Key Location: ");
		builder.append(e.getKeyLocation());
		builder.append(" - Modifiers: ");
		builder.append(e.getModifiers());
		builder.append(" - Modifiers Ex: ");
		builder.append(e.getModifiersEx());
		
		return builder.toString();
	}

	private void keyReleasedGUIInteraction() {
		if(guiInteractionLabel != null) {
			guiInteractionLabel.setText("");
		}
	}

	public void setLink(Link link) {
		this.link = link;
	}

}
