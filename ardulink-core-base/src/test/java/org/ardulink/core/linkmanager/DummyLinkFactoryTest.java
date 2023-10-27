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
import static java.lang.String.format;
import static java.net.URI.create;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.ardulink.core.linkmanager.LinkManager.SCHEMA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.Link;
import org.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import org.ardulink.core.linkmanager.LinkManager.Configurer;
import org.ardulink.core.linkmanager.LinkManager.NumberValidationInfo;
import org.ardulink.core.proto.impl.DummyProtocol;
import org.assertj.core.api.AutoCloseableSoftAssertions;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.DefaultLocale;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class DummyLinkFactoryTest {

	private final LinkManager sut = LinkManager.getInstance();

	@Test
	void throwsExceptionOnInvalidNames() {
		String name = "non.existing.name";
		assertThatIllegalArgumentException()
				.isThrownBy(() -> sut.getConfigurer(create(format("%s://%s", SCHEMA, name))))
				.withMessageContainingAll("No factory registered", name);

	}

	@Test
	void schemaHasToBeArdulink() {
		assertThatIllegalArgumentException().isThrownBy(() -> sut.getConfigurer(create(not(SCHEMA) + "://dummy")))
				.withMessageContaining("schema not ardulink");
	}

	@Test
	void canCreateDummyConnection() throws IOException {
		try (Link link = sut.getConfigurer(dummyLinkURI()).newLink()) {
			assertThat(link).isNotNull();
		}
	}

	@Test
	void canConfigureDummyConnection() throws IOException {
		String aValue = "aVal1";
		int bValue = 1;
		String cValue = "cValue";
		TimeUnit eValue = TimeUnit.DAYS;
		try (Link link = sut.getConfigurer(create(SCHEMA + "://dummyLink?a=" + aValue + "&b=" + bValue + "&c=" + cValue
				+ "&proto=" + DummyProtocol.NAME + "&e=" + eValue.name() + "&i1=&i2=&i3=&i4=")).newLink()) {
			assertThat(link).isInstanceOf(ConnectionBasedLink.class);
			DummyConnection connection = (DummyConnection) ((ConnectionBasedLink) link).getConnection();
			DummyLinkConfig config = connection.getConfig();
			try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
				softly.assertThat(config.a).isEqualTo(aValue);
				softly.assertThat(config.b).isEqualTo(bValue);
				softly.assertThat(config.c).isEqualTo(cValue);
				softly.assertThat(config.protocol).isExactlyInstanceOf(DummyProtocol.class);
				softly.assertThat(config.e).isEqualTo(eValue);
				softly.assertThat(config.i1).isNull();
				softly.assertThat(config.i2).isNull();
				softly.assertThat(config.i3).isZero();
				softly.assertThat(config.i4).isNull();
			}
		}
	}

	@Test
	void enumsHaveDefaultChoiceValues() {
		// if type is an enum and there is no @ChoiceFor defined the enum's
		// constants should be returned
		Configurer configurer = sut.getConfigurer(dummyLinkURI());
		ConfigAttribute e = configurer.getAttribute("e");
		assertThat(e.hasChoiceValues()).isEqualTo(TRUE);
		assertThat(e.getChoiceValues()).isEqualTo(TimeUnit.values());
	}

	@Test
	void enumsWithChoiceValuesDoNotUseDefaultValues() {
		Configurer configurer = sut.getConfigurer(dummyLinkURI());
		ConfigAttribute f1 = configurer.getAttribute("f1");
		assertThat(f1.hasChoiceValues()).isEqualTo(TRUE);
		assertThat(f1.getChoiceValues()).containsExactly(NANOSECONDS);
	}

	@Test
	void throwsExceptionOnInvalidKey() {
		String nonExistingKey = "nonExistingKey";
		assertThatIllegalArgumentException()
				.isThrownBy(
						() -> sut.getConfigurer(create(format("%s://dummyLink?%s=someValue", SCHEMA, nonExistingKey))))
				.withMessageContaining("Could not determine attribute " + nonExistingKey);
	}

	@Test
	void canDefineChoiceValues() throws Exception {
		Configurer configurer = sut.getConfigurer(dummyLinkURI());
		ConfigAttribute a = configurer.getAttribute("a");
		assertThat(a.hasChoiceValues()).isEqualTo(TRUE);
		assertThat(a.getChoiceValues()).isEqualTo(new Object[] { "aVal1", "aVal2" });

		assertThat(configurer.getAttribute("b").hasChoiceValues()).isEqualTo(FALSE);
		assertThat(configurer.getAttribute("c").hasChoiceValues()).isEqualTo(FALSE);

		ConfigAttribute proto = configurer.getAttribute("proto");
		assertThat(proto.hasChoiceValues()).isEqualTo(TRUE);
		assertThat(proto.getChoiceValues()).contains("dummyProto", "ardulink2");
	}

	@Test
	@DefaultLocale(language = "en")
	void cannotSetChoiceValuesThatDoNotExist_WithPreviousQuery() {
		Configurer configurer = sut.getConfigurer(dummyLinkURI());
		ConfigAttribute a = configurer.getAttribute("a");
		assertThat(a.getChoiceValues()).isEqualTo(new Object[] { "aVal1", "aVal2" });
		String invalidValue = "aVal3IsNotAvalidValue";
		a.setValue(invalidValue);
		assertThatIllegalArgumentException().isThrownBy(configurer::newLink)
				.withMessage("'" + invalidValue + "' is not a valid value for "
						+ "A is meant just to be an example attribute" + ", valid values are [aVal1, aVal2]");
	}

	@Test
	@DefaultLocale(language = "en")
	void cannotSetChoiceValuesThatDoNotExist_WithoutPreviousQuery() {
		Configurer configurer = sut.getConfigurer(dummyLinkURI());
		ConfigAttribute a = configurer.getAttribute("a");
		String invalidValue = "aVal3IsNotAvalidValue";
		a.setValue(invalidValue);
		assertThatIllegalArgumentException().isThrownBy(configurer::newLink)
				.withMessage("'" + invalidValue + "' is not a valid value for "
						+ "A is meant just to be an example attribute" + ", valid values are [aVal1, aVal2]");
	}

	@Test
	void attributeWithoutChoiceValueThrowsRTE() {
		Configurer configurer = sut.getConfigurer(dummyLinkURI());
		ConfigAttribute configAttribute = configurer.getAttribute("c");
		assertThat(configAttribute.hasChoiceValues()).isFalse();
		assertThatIllegalStateException().isThrownBy(configAttribute::getChoiceValues)
				.withMessage("attribute does not have choiceValues");
	}

	@Test
	void canIterateRegisteredFactories() {
		assertThat(sut.listURIs()).containsExactlyInAnyOrder(links( //
				format("%s://dummyLink", SCHEMA), //
				format("%s://mock", SCHEMA), //
				format("%s://aLinkWithoutArealLinkFactoryWithConfig", SCHEMA)));
	}

	private URI[] links(String... links) {
		return Arrays.stream(links).map(URI::create).toArray(URI[]::new);
	}

	@Test
	@DefaultLocale(language = "en")
	void i18n_english() {
		assertThat(getName("a")).isEqualTo("A is meant just to be an example attribute");
		assertThat(getDescription("a")).isEqualTo("The description of attribute A");
	}

	@Test
	@DefaultLocale(language = "de")
	void i18n_german() {
		assertThat(getName("a")).isEqualTo("A ist einfach ein Beispielattribut");
		assertThat(getDescription("a")).isEqualTo("Die Beschreibung für Attribut A");
	}

	@Test
	@DefaultLocale(language = "ch")
	void i18n_localeWithoutMessageFileWillFallbackToEnglish() {
		assertThat(getName("a")).isEqualTo("A is meant just to be an example attribute");
		assertThat(getDescription("a")).isEqualTo("The description of attribute A");
	}

	@Test
	@DefaultLocale(language = "en")
	void i18n_english_untagged_attribute_returns_the_attributes_name() {
		assertThat(getName("b")).isEqualTo("b");
	}

	@Test
	void hasMinValue() {
		Configurer configurer = sut.getConfigurer(dummyLinkURI());
		hasMinMax(configurer.getAttribute("b"), 3, 12);
	}

	@Test
	void minMaxValuesOfDatatypes() {
		Configurer configurer = sut.getConfigurer(dummyLinkURI());
		hasMinMax(configurer.getAttribute("intNoMinMax"), Integer.MIN_VALUE, Integer.MAX_VALUE);
		hasMinMax(configurer.getAttribute("intMinMax"), -1, +2);

		hasMinMax(configurer.getAttribute("longNoMinMax"), Long.MIN_VALUE, Long.MAX_VALUE);
		hasMinMax(configurer.getAttribute("longMinMax"), -1, +2);

		isNan(configurer.getAttribute("doubleNoMinMax"));
		hasMinMax(configurer.getAttribute("doubleMinMax"), -1, +2);

		isNan(configurer.getAttribute("floatNoMinMax"));
		hasMinMax(configurer.getAttribute("floatMinMax"), -1, +2);

		hasMinMax(configurer.getAttribute("byteNoMinMax"), Byte.MIN_VALUE, Byte.MAX_VALUE);
		hasMinMax(configurer.getAttribute("byteMinMax"), -1, +2);

		hasMinMax(configurer.getAttribute("charNoMinMax"), Character.MIN_VALUE, Character.MAX_VALUE);
		hasMinMax(configurer.getAttribute("charMinMax"), -1, +2);
	}

	@Test
	void positiveAnnotated() {
		Configurer configurer = sut.getConfigurer(dummyLinkURI());
		hasMinMax(configurer.getAttribute("positiveAnnotated"), +1, +2);
		hasMinMax(configurer.getAttribute("positiveOrZeroAnnotated"), 0, +2);
	}

	@Test
	void negativeAnnotated() {
		Configurer configurer = sut.getConfigurer(dummyLinkURI());
		hasMinMax(configurer.getAttribute("negativeAnnotated"), -2, -1);
		hasMinMax(configurer.getAttribute("negativeOrZeroAnnotated"), -2, 0);
	}

	private void hasMinMax(ConfigAttribute attribute, long min, long max) {
		assertThat(attribute.getValidationInfo()).isInstanceOfSatisfying(NumberValidationInfo.class, nvi -> {
			try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
				softly.assertThat(nvi.min()).isEqualTo(min);
				softly.assertThat(nvi.max()).isEqualTo(max);
			}
		});
	}

	private void isNan(ConfigAttribute attribute) {
		assertThat(attribute.getValidationInfo()).isInstanceOfSatisfying(NumberValidationInfo.class, nvi -> {
			try (AutoCloseableSoftAssertions softly = new AutoCloseableSoftAssertions()) {
				softly.assertThat(nvi.min()).isNaN();
				softly.assertThat(nvi.max()).isNaN();
			}
		});
	}

	private String getName(String name) {
		return getAttribute(name).getName();
	}

	private String getDescription(String name) {
		return getAttribute(name).getDescription();
	}

	private ConfigAttribute getAttribute(String name) {
		Configurer configurer = sut.getConfigurer(dummyLinkURI());
		return configurer.getAttribute(name);
	}

	private static String not(String value) {
		return "not" + value;
	}

	private static URI dummyLinkURI() {
		return create(format("%s://dummyLink", SCHEMA));
	}

}
