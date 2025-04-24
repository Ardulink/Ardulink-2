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

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.ardulink.gui.KeyPressController.TextFragment.textFragment;
import static org.ardulink.util.Preconditions.checkNotNull;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.ardulink.core.Link;
import org.ardulink.util.Throwables;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 * 
 */
public class KeyPressController extends JPanel implements Linkable {

	static class TextFragment {

		private final String key;
		private final Function<KeyEvent, Object> getValueFunction;

		private TextFragment(String key, Function<KeyEvent, Object> getValueFunction) {
			this.key = key;
			this.getValueFunction = getValueFunction;
		}

		public static TextFragment textFragment(String key, Function<KeyEvent, Object> getValueFunction) {
			return new TextFragment(key, getValueFunction);
		}

		private String toString(KeyEvent event) {
			return format("%s: %s", key, getValueFunction.apply(event));
		}

	}

	private static final long serialVersionUID = -1842577141033747612L;
	private Link link;

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

		addKeyListener(listener(pressedKeyLabel));
		setFocusTraversalKeysEnabled(false);
	}

	private KeyAdapter listener(JLabel pressedKeyLabel) {

		final List<TextFragment> fragments = List.of( //
				textFragment("Char", KeyEvent::getKeyChar), //
				textFragment("Key Code", KeyEvent::getKeyCode), //
				textFragment("Key Location", KeyEvent::getKeyLocation), //
				textFragment("Modifiers", KeyEvent::getModifiers), //
				textFragment("Modifiers Ex", KeyEvent::getModifiersEx) //
		);

		return new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent event) {
				pressedKeyLabel.setText(labelText(fragments, event));
				sendKeyPressEvent(event);
			}

			private String labelText(final List<TextFragment> fragments, KeyEvent event) {
				return fragments.stream().map(v -> v.toString(event)).collect(joining(" - "));
			}

			@Override
			public void keyReleased(KeyEvent __) {
				pressedKeyLabel.setText("");
			}

			private void sendKeyPressEvent(KeyEvent event) {
				try {
					link.sendKeyPressEvent(event.getKeyChar(), event.getKeyCode(), //
							event.getKeyLocation(), event.getModifiers(), event.getModifiersEx());
				} catch (IOException e) {
					throw Throwables.propagate(e);
				}
			}
		};
	}

	@Override
	public void setLink(Link link) {
		this.link = checkNotNull(link, "link must not be null");
	}

}
