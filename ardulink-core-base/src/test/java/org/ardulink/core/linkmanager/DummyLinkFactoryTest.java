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

package org.ardulink.core.linkmanager;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.Locale.CHINESE;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.Link;
import org.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import org.ardulink.core.linkmanager.LinkManager.Configurer;
import org.ardulink.core.linkmanager.LinkManager.NumberValidationInfo;
import org.ardulink.core.proto.impl.DummyProtocol;
import org.ardulink.util.URIs;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class DummyLinkFactoryTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void throwsExceptionOnInvalidNames() {
		String name = "non.existing.name";
		LinkManager connectionManager = LinkManager.getInstance();
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("No factory registered for \"" + name + "\"");
		connectionManager.getConfigurer(URIs.newURI("ardulink://" + name + ""));
	}

	@Test
	public void schemaHasToBeArdulink() {
		LinkManager connectionManager = LinkManager.getInstance();
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("schema not ardulink");
		connectionManager.getConfigurer(URIs.newURI("wrongSchema://dummy"));
	}

	@Test
	public void canCreateDummyDonnection() {
		LinkManager connectionManager = LinkManager.getInstance();
		Link link = connectionManager.getConfigurer(
				URIs.newURI("ardulink://dummyLink")).newLink();
		assertThat(link, is(notNullValue()));
	}

	@Test
	public void canConfigureDummyConnection() {
		LinkManager connectionManager = LinkManager.getInstance();
		String aValue = "aVal1";
		int bValue = 1;
		String cValue = "cValue";
		Link link = (Link) connectionManager.getConfigurer(
				URIs.newURI("ardulink://dummyLink?a=" + aValue + "&b=" + bValue
						+ "&c=" + cValue + "&proto=dummyProto")).newLink();

		assertThat(link.getClass().getName(),
				is(ConnectionBasedLink.class.getName()));
		DummyConnection connection = (DummyConnection) ((ConnectionBasedLink) link)
				.getConnection();
		DummyLinkConfig config = connection.getConfig();
		assertThat(config.a, is(aValue));
		assertThat(config.b, is(bValue));
		assertThat(config.c, is(cValue));
		assertThat(config.protocol.getClass().getName(), is(DummyProtocol
				.getInstance().getClass().getName()));
	}

	@Test
	public void throwsExceptionOnInvalidKey() {
		String nonExistingKey = "nonExistingKey";
		LinkManager connectionManager = LinkManager.getInstance();
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage("Could not determine attribute "
				+ nonExistingKey);
		connectionManager.getConfigurer(URIs.newURI("ardulink://dummyLink?"
				+ nonExistingKey + "=someValue"));
	}

	@Test
	public void canDefineChoiceValues() throws Exception {
		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(URIs
				.newURI("ardulink://dummyLink"));
		ConfigAttribute a = configurer.getAttribute("a");
		assertThat(a.hasChoiceValues(), is(TRUE));
		assertThat(a.getChoiceValues(), is(new Object[] { "aVal1", "aVal2" }));

		assertThat(configurer.getAttribute("b").hasChoiceValues(), is(FALSE));
		assertThat(configurer.getAttribute("c").hasChoiceValues(), is(FALSE));

		ConfigAttribute proto = configurer.getAttribute("proto");
		assertThat(proto.hasChoiceValues(), is(TRUE));
		assertThat(Arrays.asList(proto.getChoiceValues()),
				hasItems((Object) "dummyProto", "ardulink2"));
	}

	@Test
	public void cannotSetChoiceValuesThatDoNotExist_WithPreviousQuery() {
		Locale.setDefault(ENGLISH);
		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(URIs
				.newURI("ardulink://dummyLink"));
		ConfigAttribute a = configurer.getAttribute("a");
		assertThat(a.getChoiceValues(), is(new Object[] { "aVal1", "aVal2" }));
		String invalidValue = "aVal3IsNotAvalidValue";
		a.setValue(invalidValue);
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(invalidValue + " is not a valid value for "
				+ "A is meant just to be an example attribute"
				+ ", valid values are [aVal1, aVal2]");
		configurer.newLink();
	}

	@Test
	public void cannotSetChoiceValuesThatDoNotExist_WithoutPreviousQuery() {
		Locale.setDefault(ENGLISH);
		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(URIs
				.newURI("ardulink://dummyLink"));
		ConfigAttribute a = configurer.getAttribute("a");
		String invalidValue = "aVal3IsNotAvalidValue";
		a.setValue(invalidValue);
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(invalidValue + " is not a valid value for "
				+ "A is meant just to be an example attribute"
				+ ", valid values are [aVal1, aVal2]");
		configurer.newLink();
	}

	@Test
	public void attributeQithoutChoiceValueThrowsRTE() {
		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(URIs
				.newURI("ardulink://dummyLink"));
		ConfigAttribute c = configurer.getAttribute("c");
		assertThat(c.hasChoiceValues(), is(false));
		exception.expect(IllegalStateException.class);
		exception.expectMessage("attribute does not have choiceValues");
		c.getChoiceValues();
	}

	@Test
	public void canIterateRegisteredFactories() {
		LinkManager connectionManager = LinkManager.getInstance();
		assertThat(
				connectionManager.listURIs(),
				is(links("ardulink://dummyLink", "ardulink://dummyLink2",
						"ardulink://dependendAttributes")));
	}

	private List<URI> links(String... links) {
		List<URI> uris = new ArrayList<URI>(links.length);
		for (String link : links) {
			uris.add(URIs.newURI(link));
		}
		return uris;
	}

	@Test
	public void i18n_english() {
		Locale.setDefault(ENGLISH);
		assertThat(getName("a"),
				is("A is meant just to be an example attribute"));
		assertThat(getDescription("a"), is("The description of attribute A"));
	}

	@Test
	public void i18n_german() {
		Locale.setDefault(GERMAN);
		assertThat(getName("a"), is("A ist einfach ein Beispielattribut"));
		assertThat(getDescription("a"), is("Die Beschreibung für Attribut A"));
	}

	@Test
	public void i18n_localeWithoutMessageFileWillFallbackToEnglish() {
		Locale.setDefault(CHINESE);
		assertThat(getName("a"),
				is("A is meant just to be an example attribute"));
		assertThat(getDescription("a"), is("The description of attribute A"));
	}

	@Test
	public void i18n_english_untagged_attribute_returns_the_attributes_name() {
		Locale.setDefault(ENGLISH);
		assertThat(getName("b"), is("b"));
	}

	@Test
	public void hasMinValue() {
		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(URIs
				.newURI("ardulink://dummyLink"));
		ConfigAttribute a = configurer.getAttribute("b");
		NumberValidationInfo vi = (NumberValidationInfo) a.getValidationInfo();
		assertThat(((int) vi.min()), is(3));
		assertThat(((int) vi.max()), is(12));
	}

	private static String getName(String name) {
		return getAttribute(name).getName();
	}

	private static String getDescription(String name) {
		return getAttribute(name).getDescription();
	}

	private static ConfigAttribute getAttribute(String name) {
		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(URIs
				.newURI("ardulink://dummyLink"));
		return configurer.getAttribute(name);
	}

}
