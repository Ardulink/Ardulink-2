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

import static org.ardulink.gui.assertj.RowMatcherBuilder.row;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.net.URI;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;

import org.ardulink.core.linkmanager.LinkManager;
import org.ardulink.gui.DummyLinkConfig;
import org.ardulink.util.URIs;
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

	private final URI uri = URIs.newURI("ardulink://dummy");
	private final GenericPanelBuilder sut = new GenericPanelBuilder();

	@Test
	void canHandle() {
		assertThat(sut.canHandle(uri), is(true));
	}

	@Test
	void hasSubPanelWithConnectionIndividualComponents() {
		DummyLinkConfig dlc = new DummyLinkConfig();

		JPanel panel = sut.createPanel(LinkManager.getInstance().getConfigurer(uri));

		row(0).hasLabel("1_aIntValue").hasValue(dlc.getIntValue()).verify(panel);
		row(1).hasLabel("2_aBooleanValue").withYesNo().hasValue(dlc.getBooleanValue()).verify(panel);
		row(2).hasLabel("3_aStringValue").hasValue("").verify(panel);
		Object[] choices1 = dlc.someValuesForChoiceWithoutNull().toArray(new String[0]);
		row(3).hasLabel("4_aStringValueWithChoices").hasChoice(choices1).hasValue(choices1[0]).verify(panel);
		Object[] choices2 = dlc.someValuesForChoiceWithNull().toArray(new String[0]);
		row(4).hasLabel("5_aStringValueWithChoicesIncludingNull").hasChoice(choices2).hasValue(null).verify(panel);
		row(5).hasLabel("6_aEnumValue").hasChoice(TimeUnit.values()).hasValue(dlc.getEnumValue()).verify(panel);
	}

}
