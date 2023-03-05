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

package org.ardulink.gui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JLabel;

import org.ardulink.legacy.Link;

/**
 * [ardulinktitle] [ardulinkversion]
 * Class used by KeyPressController class to capture key press events.
* project Ardulink http://www.ardulink.org/
 * @see KeyPressController
 * [adsense]
 *
 */
public class KeyPressListener extends KeyAdapter {

	private JLabel guiInteractionLabel;
	private Link link;
	
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
        String builder = "Char: " + e.getKeyChar() +
                " - Key Code: " +
                e.getKeyCode() +
                " - Key Location: " +
                e.getKeyLocation() +
                " - Modifiers: " +
                e.getModifiers() +
                " - Modifiers Ex: " +
                e.getModifiersEx();
		
		return builder;
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
