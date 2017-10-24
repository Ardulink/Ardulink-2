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

import static org.ardulink.gui.hamcrest.RowMatcherBuilder.componentsOf;
import static org.ardulink.gui.hamcrest.RowMatcherBuilder.row;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import java.awt.Component;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.ardulink.core.linkmanager.LinkManager;
import org.ardulink.gui.DummyLinkConfig;
import org.ardulink.util.Optional;
import org.ardulink.util.URIs;
import org.hamcrest.Matcher;
import org.junit.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class GenericConnectionPanelTest {

	private final URI uri = URIs.newURI("ardulink://dummy");
	private final GenericPanelBuilder sut = new GenericPanelBuilder();

	@Test
	public void canHandle() {
		assertThat(sut.canHandle(uri), is(true));
	}

	@Test
	public void hasSubPanelWithConnectionIndividualComponents() {

		DummyLinkConfig dlc = new DummyLinkConfig();

		JPanel panel = sut.createPanel(LinkManager.getInstance().getConfigurer(
				uri));
		JComboBox comboBox = findFirst(JComboBox.class, componentsOf(panel))
				.getOrThrow("No %s found on panel %s",
						JComboBox.class.getName(), panel);
		comboBox.setSelectedItem("ardulink://dummy");
		assertThat(panel,
				has(row(0).withLabel("1_aIntValue")
						.withValue(dlc.getIntValue())));
		assertThat(panel, has(row(1).withLabel("2_aBooleanValue").withYesNo()
				.withValue(dlc.getBooleanValue())));
		assertThat(panel, has(row(2).withLabel("3_aStringValue").withValue("")));
		Object[] choices1 = dlc.someValuesForChoiceWithoutNull().toArray(
				new String[0]);
		assertThat(panel, has(row(3).withLabel("4_aStringValueWithChoices")
				.withChoice(choices1).withValue(choices1[0])));
		Object[] choices2 = dlc.someValuesForChoiceWithNull().toArray(
				new String[0]);
		assertThat(panel,
				has(row(4).withLabel("5_aStringValueWithChoicesIncludingNull")
						.withChoice(choices2).withValue(null)));
		Object[] timeUnits = TimeUnit.values();
		assertThat(panel,
				has(row(5).withLabel("6_aEnumValue").withChoice(timeUnits)
						.withValue(dlc.getEnumValue())));
	}

	private <T> Optional<T> findFirst(Class<T> clazz,
			List<? extends Component> components) {
		for (Component component : components) {
			if (clazz.isInstance(component)) {
				return Optional.of(clazz.cast(component));
			}
		}
		return Optional.<T> absent();
	}

	private <T> Matcher<T> has(Matcher<T> matcher) {
		return matcher;
	}

}
