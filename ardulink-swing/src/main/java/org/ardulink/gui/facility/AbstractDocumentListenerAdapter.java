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
package org.ardulink.gui.facility;

import java.util.function.Consumer;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 */
public abstract class AbstractDocumentListenerAdapter implements DocumentListener {

	public static void addDocumentListener(JTextField textField, Consumer<DocumentEvent> consumer) {
		textField.getDocument().addDocumentListener(adapter(consumer));
	}

	public static AbstractDocumentListenerAdapter adapter(Consumer<DocumentEvent> consumer) {
		return new AbstractDocumentListenerAdapter() {
			@Override
			protected void updated(DocumentEvent documentEvent) {
				consumer.accept(documentEvent);
			}
		};
	}

	@Override
	public void insertUpdate(DocumentEvent documentEvent) {
		updated(documentEvent);
	}

	@Override
	public void removeUpdate(DocumentEvent documentEvent) {
		updated(documentEvent);
	}

	@Override
	public void changedUpdate(DocumentEvent documentEvent) {
		updated(documentEvent);
	}

	protected abstract void updated(DocumentEvent documentEvent);

}
