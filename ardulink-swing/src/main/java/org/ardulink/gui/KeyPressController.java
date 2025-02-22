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

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.ardulink.core.Link;

/**
 * [ardulinktitle] [ardulinkversion]
 * This class captures key press events and sends messages to arduino board with key press
 * information.
* project Ardulink http://www.ardulink.org/
 * @see KeyPressListener
 * [adsense]
 *
 */
public class KeyPressController extends JPanel implements Linkable {

	private static final long serialVersionUID = -1842577141033747612L;
	private final transient KeyPressListener keyPressListener;

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

		keyPressListener = new KeyPressListener();
		addKeyListener(keyPressListener);
		keyPressListener.setGuiInteractionLabel(pressedKeyLabel);
		setFocusTraversalKeysEnabled(false);
	}

	@Override
	public void setLink(Link link) {
		keyPressListener.setLink(link);
	}

}
