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

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class KeyPressController extends JPanel {

	private static final long serialVersionUID = -1842577141033747612L;

	/**
	 * Create the panel.
	 */
	public KeyPressController() {
		setLayout(new BorderLayout(0, 0));

		JLabel lblPressAnyKey = new JLabel("Press any key");
		lblPressAnyKey.setHorizontalAlignment(SwingConstants.CENTER);
		add(lblPressAnyKey, BorderLayout.NORTH);

		JLabel pressedKeyLabel = new JLabel("");
		pressedKeyLabel.setHorizontalAlignment(SwingConstants.CENTER);
		add(pressedKeyLabel, BorderLayout.CENTER);

		KeyPressListener keyPressListener = new KeyPressListener();
		addKeyListener(keyPressListener);
		keyPressListener.setGuiInteractionLabel(pressedKeyLabel);
		setFocusTraversalKeysEnabled(false);
	}

}
