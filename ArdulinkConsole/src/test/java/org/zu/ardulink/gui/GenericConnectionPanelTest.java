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

package org.zu.ardulink.gui;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.zu.ardulink.gui.RowMatcher.componentsOf;
import static org.zu.ardulink.gui.RowMatcher.row;

import java.awt.Component;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.zu.ardulink.util.Optional;

public class GenericConnectionPanelTest {

	@Test
	public void panelHasComboBoxWithValues() {
		List<? extends Component> componentsOfConnectionsPanel = componentsOf(new GenericConnectionPanel());
		JComboBox comboBox = findFirst(JComboBox.class,
				componentsOfConnectionsPanel).get();
		assertThat(comboBox, not(nullValue()));
		Object[] items = RowMatcher.items(comboBox);
		assertThat(items, is(new Object[] { "ardulink://dummy",
				"ardulink://serial", "ardulink://proxy", "ardulink://mqtt" }));
	}

	@Test
	public void hasSubPanelWithConnectionIndividualComponents() {
		List<? extends Component> componentsOfConnectionsPanel = componentsOf(new GenericConnectionPanel());
		JComboBox comboBox = findFirst(JComboBox.class,
				componentsOfConnectionsPanel).get();
		comboBox.setSelectedItem("ardulink://dummy");
		JPanel panel = findFirst(JPanel.class, componentsOfConnectionsPanel)
				.get();
		assertThat(panel, has(row(0).withLabel("a").withValue(42)));
		assertThat(panel, has(row(1).withLabel("b").withChoice("foo", "bar")
				.withValue("foo")));
		assertThat(panel, has(row(2).withLabel("c").withChoice(TRUE, FALSE)
				.withValue(TRUE)));
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
