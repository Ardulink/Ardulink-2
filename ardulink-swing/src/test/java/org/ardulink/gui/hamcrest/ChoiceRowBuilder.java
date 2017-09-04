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
package org.ardulink.gui.hamcrest;

import javax.swing.JPanel;

import org.hamcrest.Matcher;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ChoiceRowBuilder<T> {

	private final T[] choices;
	private final BaseBuilder baseBuilder;

	public ChoiceRowBuilder(BaseBuilder baseBuilder, T... choices) {
		this.baseBuilder = baseBuilder;
		this.choices = choices;
	}

	public Matcher<JPanel> withValue(T value) {
		return new ChoiceRowMatcher(baseBuilder, choices, value);
	}

}
