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
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.Link;
import org.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import org.ardulink.core.linkmanager.LinkManager.Configurer;
import org.ardulink.core.linkmanager.LinkManager.NumberValidationInfo;
import org.ardulink.core.proto.impl.DummyProtocol;
import org.ardulink.util.URIs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class DummyLinkFactoryTest {

	private final class ExecutableImplementation implements Executable {
		@Override
		public void execute() throws Throwable {
			sut.getConfigurer(URIs.newURI("wrongSchema://dummy"));
		}
	}

	private final LinkManager sut = LinkManager.getInstance();

	@Test
	void throwsExceptionOnInvalidNames() {
		String name = "non.existing.name";
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> sut.getConfigurer(URIs.newURI("ardulink://" + name + "")));
		assertThat(exception.getMessage()).contains("No factory registered for \"" + name + "\"");
	}

	@Test
	void schemaHasToBeArdulink() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				new ExecutableImplementation());
		assertThat(exception.getMessage()).contains("schema not ardulink");
	}

	@Test
	void canCreateDummyConnection() {
		Link link = sut.getConfigurer(URIs.newURI("ardulink://dummyLink")).newLink();
		assertThat(link).isNotNull();
	}

	@Test
	void canConfigureDummyConnection() {
		String aValue = "aVal1";
		int bValue = 1;
		String cValue = "cValue";
		TimeUnit eValue = TimeUnit.DAYS;
		Link link = sut.getConfigurer(URIs.newURI("ardulink://dummyLink?a=" + aValue + "&b=" + bValue + "&c="
				+ cValue + "&proto=dummyProto&e=" + eValue.name())).newLink();

		assertThat(link).isInstanceOf(ConnectionBasedLink.class);
		DummyConnection connection = (DummyConnection) ((ConnectionBasedLink) link).getConnection();
		DummyLinkConfig config = connection.getConfig();
		assertThat(config.a).isEqualTo(aValue);
		assertThat(config.b).isEqualTo(bValue);
		assertThat(config.c).isEqualTo(cValue);
		assertThat(config.protocol.getClass().getName()).isEqualTo(DummyProtocol.getInstance().getClass().getName());
		assertThat(config.e).isEqualTo(eValue);

	}

	@Test
	void enumsHaveDefaultChoiceValues() {
		// if type is an enum and there is no @ChoiceFor defined the enum's
		// constants should be returned
		Configurer configurer = sut.getConfigurer(URIs.newURI("ardulink://dummyLink"));
		ConfigAttribute e = configurer.getAttribute("e");
		assertThat(e.hasChoiceValues()).isEqualTo(TRUE);
		assertThat(e.getChoiceValues()).isEqualTo(TimeUnit.values());
	}

	@Test
	void enumsWithChoiceValuesDoNotUseDefaultValues() {
		Configurer configurer = sut.getConfigurer(URIs.newURI("ardulink://dummyLink"));
		ConfigAttribute f = configurer.getAttribute("f");
		assertThat(f.hasChoiceValues()).isEqualTo(TRUE);
		assertThat(Arrays.asList(f.getChoiceValues())).isEqualTo(Arrays.<Object>asList(NANOSECONDS, DAYS));
	}

	@Test
	void throwsExceptionOnInvalidKey() {
		String nonExistingKey = "nonExistingKey";
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> sut.getConfigurer(URIs.newURI("ardulink://dummyLink?" + nonExistingKey + "=someValue")));
		assertThat(exception.getMessage()).contains("Could not determine attribute " + nonExistingKey);
	}

	@Test
	void canDefineChoiceValues() throws Exception {
		Configurer configurer = sut.getConfigurer(URIs.newURI("ardulink://dummyLink"));
		ConfigAttribute a = configurer.getAttribute("a");
		assertThat(a.hasChoiceValues()).isEqualTo(TRUE);
		assertThat(a.getChoiceValues()).isEqualTo(new Object[] { "aVal1", "aVal2" });

		assertThat(configurer.getAttribute("b").hasChoiceValues()).isEqualTo(FALSE);
		assertThat(configurer.getAttribute("c").hasChoiceValues()).isEqualTo(FALSE);

		ConfigAttribute proto = configurer.getAttribute("proto");
		assertThat(proto.hasChoiceValues()).isEqualTo(TRUE);
		assertThat(Arrays.asList(proto.getChoiceValues())).contains("dummyProto", "ardulink2");
	}

	@Test
	void cannotSetChoiceValuesThatDoNotExist_WithPreviousQuery() {
		Locale.setDefault(ENGLISH);
		Configurer configurer = sut.getConfigurer(URIs.newURI("ardulink://dummyLink"));
		ConfigAttribute a = configurer.getAttribute("a");
		assertThat(a.getChoiceValues()).isEqualTo(new Object[] { "aVal1", "aVal2" });
		String invalidValue = "aVal3IsNotAvalidValue";
		a.setValue(invalidValue);
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> configurer.newLink());
		assertThat(exception.getMessage()).isEqualTo(invalidValue + " is not a valid value for "
				+ "A is meant just to be an example attribute" + ", valid values are [aVal1, aVal2]");
	}

	@Test
	void cannotSetChoiceValuesThatDoNotExist_WithoutPreviousQuery() {
		Locale.setDefault(ENGLISH);
		Configurer configurer = sut.getConfigurer(URIs.newURI("ardulink://dummyLink"));
		ConfigAttribute a = configurer.getAttribute("a");
		String invalidValue = "aVal3IsNotAvalidValue";
		a.setValue(invalidValue);
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> configurer.newLink());
		assertThat(exception.getMessage()).isEqualTo(invalidValue + " is not a valid value for "
				+ "A is meant just to be an example attribute" + ", valid values are [aVal1, aVal2]");
	}

	@Test
	void attributeWithoutChoiceValueThrowsRTE() {
		Configurer configurer = sut.getConfigurer(URIs.newURI("ardulink://dummyLink"));
		ConfigAttribute configAttribute = configurer.getAttribute("c");
		assertThat(configAttribute.hasChoiceValues()).isFalse();
		IllegalStateException exception = assertThrows(IllegalStateException.class,
				() -> configAttribute.getChoiceValues());
		assertThat(exception.getMessage()).isEqualTo("attribute does not have choiceValues");
	}

	@Test
	void canIterateRegisteredFactories() {
		assertThat(sut.listURIs()).isEqualTo(links( //
				"ardulink://dummyLink", //
				"ardulink://mock", //
				"ardulink://staticregistry", //
				"ardulink://aLinkWithoutArealLinkFactoryWithoutConfig", //
				"ardulink://aLinkWithoutArealLinkFactoryWithConfig" //
		));
	}

	private List<URI> links(String... links) {
		return Arrays.stream(links).map(URIs::newURI).collect(toList());
	}

	@Test
	void i18n_english() {
		Locale.setDefault(ENGLISH);
		assertThat(getName("a")).isEqualTo("A is meant just to be an example attribute");
		assertThat(getDescription("a")).isEqualTo("The description of attribute A");
	}

	@Test
	void i18n_german() {
		Locale.setDefault(GERMAN);
		assertThat(getName("a")).isEqualTo("A ist einfach ein Beispielattribut");
		assertThat(getDescription("a")).isEqualTo("Die Beschreibung für Attribut A");
	}

	@Test
	void i18n_localeWithoutMessageFileWillFallbackToEnglish() {
		Locale.setDefault(CHINESE);
		assertThat(getName("a")).isEqualTo("A is meant just to be an example attribute");
		assertThat(getDescription("a")).isEqualTo("The description of attribute A");
	}

	@Test
	void i18n_english_untagged_attribute_returns_the_attributes_name() {
		Locale.setDefault(ENGLISH);
		assertThat(getName("b")).isEqualTo("b");
	}

	@Test
	void hasMinValue() {
		Configurer configurer = sut.getConfigurer(URIs.newURI("ardulink://dummyLink"));
		ConfigAttribute a = configurer.getAttribute("b");
		NumberValidationInfo vi = (NumberValidationInfo) a.getValidationInfo();
		assertThat(((int) vi.min())).isEqualTo(3);
		assertThat(((int) vi.max())).isEqualTo(12);
	}

	private String getName(String name) {
		return getAttribute(name).getName();
	}

	private String getDescription(String name) {
		return getAttribute(name).getDescription();
	}

	private ConfigAttribute getAttribute(String name) {
		Configurer configurer = sut.getConfigurer(URIs.newURI("ardulink://dummyLink"));
		return configurer.getAttribute(name);
	}

}
