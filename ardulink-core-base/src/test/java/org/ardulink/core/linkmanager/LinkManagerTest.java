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
import static java.util.Arrays.asList;
import static org.ardulink.core.linkmanager.DummyLinkConfig.XXX;
import static org.ardulink.core.linkmanager.LinkManager.ARDULINK_SCHEME;
import static org.ardulink.core.linkmanager.providers.DynamicLinkFactoriesProvider.withRegistered;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatRuntimeException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.ardulink.core.Link;
import org.ardulink.core.linkmanager.LinkFactory.Alias;
import org.ardulink.core.linkmanager.LinkManager.Configurer;
import org.ardulink.core.linkmanager.LinkManager.DefaultConfigurer.CacheKey;
import org.ardulink.core.linkmanager.viaservices.AlLinkWithoutArealLinkFactoryWithConfig;
import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class LinkManagerTest {

	LinkManager sut = LinkManager.getInstance();

	@Test
	void onceQueriedChoiceValuesStayValid() throws Exception {
		Configurer configurer = sut.getConfigurer(create(format("%s://dummyLink", ARDULINK_SCHEME)));

		choiceValuesOfDNowAre("x", "y");

		// let's query the possible values
		assertThat(configurer.getAttribute("d").getChoiceValues()).containsExactly("x", "y");

		// now the possible values change from x and y to 1 and 2
		choiceValuesOfDNowAre("1", "2");

		// but because the client queried for x and y those two values should
		// stay valid beside 1 and 2 now are the valid choices
		configurer.getAttribute("d").setValue("y");

		// but when querying the choice values again the changes are reflected
		assertThat(configurer.getAttribute("d").getChoiceValues()).containsExactly("1", "2");
	}

	private void choiceValuesOfDNowAre(String... values) {
		DummyLinkConfig.choiceValuesOfD.set(values);
	}

	@Test
	void canLoadViaMetaInfServicesArdulinkLinkfactoryWithConfig() throws IOException {
		try (Link link = sut
				.getConfigurer(create(format("%s://aLinkWithoutArealLinkFactoryWithConfig", ARDULINK_SCHEME)))
				.newLink()) {
			assertThat(link).isExactlyInstanceOf(AlLinkWithoutArealLinkFactoryWithConfig.class);
		}
	}

	@Test
	void nonExistingNameWitllThrowRTE() throws IOException {
		assertThatRuntimeException().isThrownBy(
				() -> sut.getConfigurer(create(format("%s://XXX-aNameThatIsNotRegistered-XXX", ARDULINK_SCHEME))))
				.withMessageContainingAll("registered", "factory");
	}

	@Test
	void canLoadDummyLinkViaAlias() throws Throwable {
		withRegistered(new AliasUsingLinkFactory())
				.execute(() -> assertThat(sut.getConfigurer(aliasUri())).isNotNull());
	}

	@Test
	void aliasNameNotListed() throws Throwable {
		withRegistered(new AliasUsingLinkFactory()).execute(() -> {
			assertThat(sut.listURIs()).contains(aliasUri())
					.doesNotContain(create(format("%s://aliasLinkAlias", ARDULINK_SCHEME)));
		});
	}

	@Test
	void nameHasPriorityOverAlias() throws Throwable {
		DummyLinkFactory nameFactorySpy = spy(new DummyLinkFactory());
		AliasUsingLinkFactory aliasFactorySpy = spy(new AliasUsingLinkFactory());
		assert aliasNames(aliasFactorySpy).contains(nameFactorySpy.getName());

		withRegistered(aliasFactorySpy, nameFactorySpy).execute(() -> {
			try (Link link = sut.getConfigurer(create(format("%s://%s", ARDULINK_SCHEME, nameFactorySpy.getName())))
					.newLink()) {
				assertAll(() -> {
					verify(aliasFactorySpy, never()).newLink(any(LinkConfig.class));
					verify(nameFactorySpy, times(1)).newLink(any(DummyLinkConfig.class));
				});
			}
		});
	}

	@Test
	void canProgramaticallyDisable() throws Throwable {
		DummyLinkFactory factory = new DummyLinkFactory();
		withRegistered(factory).execute(() -> {
			Configurer configurer = sut.getConfigurer(create(format("%s://%s", ARDULINK_SCHEME, factory.getName())));
			assertSoftly(s -> {
				DummyLinkConfig.doDisableXXX.set(FALSE);
				s.assertThat(configurer.getAttributes()).contains(XXX);
				DummyLinkConfig.doDisableXXX.set(TRUE);
				s.assertThat(configurer.getAttributes()).doesNotContain(XXX);
			});
		});
	}

	@Test
	void equalsContract() {
		assertDoesNotThrow(EqualsVerifier.forClass(CacheKey.class)::verify);
	}

	List<String> aliasNames(LinkFactory<?> factory) {
		return asList(factory.getClass().getAnnotation(Alias.class).value());
	}

	URI aliasUri() {
		return create(format("%s://%s", ARDULINK_SCHEME, AliasUsingLinkFactory.NAME));
	}

}
