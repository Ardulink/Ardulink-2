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

package org.ardulink.gui.connectionpanel;

import static java.net.URI.create;
import static org.ardulink.gui.assertj.PanelAssert.assertThat;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;

import org.ardulink.core.linkmanager.LinkManager;
import org.ardulink.gui.DummyLinkConfig;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class GenericConnectionPanelTest {

	private final URI uri = create("ardulink://dummy");
	private final GenericPanelBuilder sut = new GenericPanelBuilder();

	@Test
	void canHandle() {
		Assertions.assertThat(sut.canHandle(uri)).isTrue();
	}

	@Test
	void hasSubPanelWithConnectionIndividualComponents() {
		DummyLinkConfig config = new DummyLinkConfig();
		JPanel panel = sut.createPanel(LinkManager.getInstance().getConfigurer(uri));

		assertThat(panel).hasRow(0).labeled("1_aIntValue").havingValue(config.getIntValue());
		assertThat(panel).hasRow(1).labeled("2_aBooleanValue").havingYesNoValue(config.getBooleanValue());
		assertThat(panel).hasRow(2).labeled("3_aStringValue").havingValue("");
		Object[] choicesRow3 = config.someValuesForChoiceWithoutNull().toArray(new String[0]);
		assertThat(panel).hasRow(3).labeled("4_aStringValueWithChoices").havingChoice(choicesRow3, choicesRow3[0]);
		Object[] choicesRow4 = config.someValuesForChoiceWithNull().toArray(new String[0]);
		assertThat(panel).hasRow(4).labeled("5_aStringValueWithChoicesIncludingNull").havingChoice(choicesRow4, null);
		assertThat(panel).hasRow(5).labeled("6_aEnumValue").havingChoice(TimeUnit.values(), config.getEnumValue());
	}

}
