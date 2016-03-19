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

import java.awt.Component;
import java.util.Arrays;

import javax.swing.JPanel;
import javax.swing.JSpinner;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class NumberRowMatcher extends TypeSafeMatcher<JPanel> {

	private final Number value;
	private final BaseBuilder baseBuilder;

	public NumberRowMatcher(BaseBuilder baseBuilder, Number number) {
		this.baseBuilder = baseBuilder;
		this.value = number;
	}

	@Override
	public void describeTo(Description description) {
		description.appendText("Label ").appendText(baseBuilder.getLabel())
				.appendText(", value ").appendText(String.valueOf(value));
	}

	@Override
	protected void describeMismatchSafely(JPanel item,
			Description mismatchDescription) {
		mismatchDescription.appendText(Arrays.toString(item.getComponents()));
	}

	@Override
	protected boolean matchesSafely(JPanel jPanel) {
		return baseBuilder.labelMatch(jPanel) && valueEq(jPanel)
				&& isNumOnly(jPanel);
	}

	private boolean valueEq(JPanel jPanel) {
		Component component = baseBuilder.getComponent(jPanel);
		return component instanceof JSpinner
				&& value.equals((Number) ((JSpinner) component).getValue());
	}

	private boolean isNumOnly(JPanel jPanel) {
		Component component = baseBuilder.getComponent(jPanel);
		return component instanceof JSpinner;
	}
}
