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
package org.ardulink.gui.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class StringRowMatcher {

	private final String value;
	private final BaseBuilder baseBuilder;

	public StringRowMatcher(BaseBuilder baseBuilder, String value) {
		this.baseBuilder = baseBuilder;
		this.value = value;
	}

	public void verify(JPanel jPanel) {
		baseBuilder.assertLabelMatch(jPanel);
		assertValueEq(jPanel);
		assertIsNumOnly(jPanel);
	}

	private void assertValueEq(JPanel jPanel) {
		assertIsNumOnly(jPanel);
		assertThat(((JTextField) baseBuilder.getComponent(jPanel)).getText()).isEqualTo(value);
	}

	private boolean assertIsNumOnly(JPanel jPanel) {
		Component component = baseBuilder.getComponent(jPanel);
		return component instanceof JTextField;
	}
}
