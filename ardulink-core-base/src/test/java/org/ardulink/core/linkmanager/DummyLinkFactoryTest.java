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
import static org.ardulink.util.anno.LapsedWith.JDK8;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThrows;

import java.net.URI;
import java.util.ArrayList;
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
import org.ardulink.util.anno.LapsedWith;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class DummyLinkFactoryTest {

	private final LinkManager sut = LinkManager.getInstance();

	@Test
	public void throwsExceptionOnInvalidNames() {
		final String name = "non.existing.name";
		
		@LapsedWith(module = JDK8, value = "Lambda")
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, new ThrowingRunnable() {
			@Override
			public void run() throws Throwable {
				sut.getConfigurer(URIs.newURI("ardulink://" + name + ""));
			}
		});
		assertThat(exception.getMessage(), containsString("No factory registered for \"" + name + "\""));
	}

	@Test
	public void schemaHasToBeArdulink() {
		@LapsedWith(module = JDK8, value = "Lambda")
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, new ThrowingRunnable() {
			@Override
			public void run() throws Throwable {
				sut.getConfigurer(URIs.newURI("wrongSchema://dummy"));
			}
		});
		assertThat(exception.getMessage(), containsString("schema not ardulink"));
	}

	@Test
	public void canCreateDummyConnection() {
		Link link = sut.getConfigurer(URIs.newURI("ardulink://dummyLink"))
				.newLink();
		assertThat(link, is(notNullValue()));
	}

	@Test
	public void canConfigureDummyConnection() {
		String aValue = "aVal1";
		int bValue = 1;
		String cValue = "cValue";
		TimeUnit eValue = TimeUnit.DAYS;
		Link link = (Link) sut.getConfigurer(
				URIs.newURI("ardulink://dummyLink?a=" + aValue + "&b=" + bValue
						+ "&c=" + cValue + "&proto=dummyProto&e="
						+ eValue.name())).newLink();

		assertThat(link, instanceOf(ConnectionBasedLink.class));
		DummyConnection connection = (DummyConnection) ((ConnectionBasedLink) link)
				.getConnection();
		DummyLinkConfig config = connection.getConfig();
		assertThat(config.a, is(aValue));
		assertThat(config.b, is(bValue));
		assertThat(config.c, is(cValue));
		assertThat(config.protocol.getClass().getName(), is(DummyProtocol
				.getInstance().getClass().getName()));
		assertThat(config.e, is(eValue));

	}

	@Test
	public void enumsHaveDefaultChoiceValues() {
		// if type is an enum and there is no @ChoiceFor defined the enum's
		// constants should be returned
		Configurer configurer = sut.getConfigurer(URIs
				.newURI("ardulink://dummyLink"));
		ConfigAttribute e = configurer.getAttribute("e");
		assertThat(e.hasChoiceValues(), is(TRUE));
		assertThat(e.getChoiceValues(),
				CoreMatchers.is((Object[]) TimeUnit.values()));
	}

	@Test
	public void enumsWithChoiceValuesDoNotUseDefaultValues() {
		Configurer configurer = sut.getConfigurer(URIs
				.newURI("ardulink://dummyLink"));
		ConfigAttribute f = configurer.getAttribute("f");
		assertThat(f.hasChoiceValues(), is(TRUE));
		assertThat(Arrays.asList(f.getChoiceValues()),
				is(Arrays.<Object> asList(NANOSECONDS, DAYS)));
	}

	@Test
	public void throwsExceptionOnInvalidKey() {
		final String nonExistingKey = "nonExistingKey";
		@LapsedWith(module = JDK8, value = "Lambda")
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, new ThrowingRunnable() {
			@Override
			public void run() throws Throwable {
				sut.getConfigurer(URIs.newURI("ardulink://dummyLink?" + nonExistingKey
						+ "=someValue"));
			}
		});
		assertThat(exception.getMessage(), containsString("Could not determine attribute " + nonExistingKey));
	}

	@Test
	public void canDefineChoiceValues() throws Exception {
		Configurer configurer = sut.getConfigurer(URIs
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
		final Configurer configurer = sut.getConfigurer(URIs
				.newURI("ardulink://dummyLink"));
		ConfigAttribute a = configurer.getAttribute("a");
		assertThat(a.getChoiceValues(), is(new Object[] { "aVal1", "aVal2" }));
		String invalidValue = "aVal3IsNotAvalidValue";
		a.setValue(invalidValue);
		@LapsedWith(module = JDK8, value = "Lambda")
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, new ThrowingRunnable() {
			@Override
			public void run() throws Throwable {
				configurer.newLink();
			}
		});
		assertThat(exception.getMessage(), is(invalidValue + " is not a valid value for "
				+ "A is meant just to be an example attribute"
				+ ", valid values are [aVal1, aVal2]"));
	}

	@Test
	public void cannotSetChoiceValuesThatDoNotExist_WithoutPreviousQuery() {
		Locale.setDefault(ENGLISH);
		final Configurer configurer = sut.getConfigurer(URIs
				.newURI("ardulink://dummyLink"));
		ConfigAttribute a = configurer.getAttribute("a");
		String invalidValue = "aVal3IsNotAvalidValue";
		a.setValue(invalidValue);
		@LapsedWith(module = JDK8, value = "Lambda")
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, new ThrowingRunnable() {
			@Override
			public void run() throws Throwable {
				configurer.newLink();
			}
		});
		assertThat(exception.getMessage(), is(invalidValue + " is not a valid value for "
				+ "A is meant just to be an example attribute"
				+ ", valid values are [aVal1, aVal2]"));
	}

	@Test
	public void attributeWithoutChoiceValueThrowsRTE() {
		Configurer configurer = sut.getConfigurer(URIs
				.newURI("ardulink://dummyLink"));
		final ConfigAttribute configAttribute = configurer.getAttribute("c");
		assertThat(configAttribute.hasChoiceValues(), is(false));
		@LapsedWith(module = JDK8, value = "Lambda")
		IllegalStateException exception = assertThrows(IllegalStateException.class, new ThrowingRunnable() {
			@Override
			public void run() throws Throwable {
				configAttribute.getChoiceValues();
			}
		});
		assertThat(exception.getMessage(), is("attribute does not have choiceValues"));
	}

	@Test
	public void canIterateRegisteredFactories() {
		assertThat(
				sut.listURIs(),
				is(links( //
						"ardulink://dummyLink", //
						"ardulink://mock", //
						"ardulink://staticregistry", //
						"ardulink://aLinkWithoutArealLinkFactoryWithoutConfig", //
						"ardulink://aLinkWithoutArealLinkFactoryWithConfig" //
				)));
	}

	@LapsedWith(module = JDK8, value = "Streams")
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
		Configurer configurer = sut.getConfigurer(URIs
				.newURI("ardulink://dummyLink"));
		ConfigAttribute a = configurer.getAttribute("b");
		NumberValidationInfo vi = (NumberValidationInfo) a.getValidationInfo();
		assertThat(((int) vi.min()), is(3));
		assertThat(((int) vi.max()), is(12));
	}

	private String getName(String name) {
		return getAttribute(name).getName();
	}

	private String getDescription(String name) {
		return getAttribute(name).getDescription();
	}

	private ConfigAttribute getAttribute(String name) {
		Configurer configurer = sut.getConfigurer(URIs
				.newURI("ardulink://dummyLink"));
		return configurer.getAttribute(name);
	}

}
