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

import static java.util.Arrays.asList;
import static org.ardulink.core.linkmanager.LinkConfig.NO_ATTRIBUTES;
import static org.ardulink.core.linkmanager.providers.LinkFactoriesProvider4Test.withRegistered;
import static org.ardulink.util.URIs.newURI;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.ardulink.core.Link;
import org.ardulink.core.linkmanager.LinkFactory.Alias;
import org.ardulink.core.linkmanager.LinkManager.Configurer;
import org.ardulink.core.linkmanager.viaservices.AlLinkWithoutArealLinkFactoryWithConfig;
import org.ardulink.core.linkmanager.viaservices.AlLinkWithoutArealLinkFactoryWithoutConfig;
import org.junit.jupiter.api.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class LinkManagerTest {

	@Alias({ "aliasLinkAlias", AliasUsingLinkFactory.ALREADY_TAKEN_NAME })
	public static class AliasUsingLinkFactory implements LinkFactory<LinkConfig> {

		protected static final String ALREADY_TAKEN_NAME = DummyLinkFactory.DUMMY_LINK_NAME;

		@Override
		public String getName() {
			return "aliasLink";
		}

		@Override
		public Link newLink(LinkConfig config) {
			return mock(Link.class);
		}

		@Override
		public LinkConfig newLinkConfig() {
			return NO_ATTRIBUTES;
		}

	}

	LinkManager sut = LinkManager.getInstance();

	@Test
	void onceQueriedChoiceValuesStayValid() throws Exception {
		Configurer configurer = sut.getConfigurer(newURI("ardulink://dummyLink"));

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
	void canLoadViaMetaInfServicesArdulinkLinkfactoryWithoutConfig() throws IOException {
		try (Link link = sut.getConfigurer(newURI("ardulink://aLinkWithoutArealLinkFactoryWithoutConfig")).newLink()) {
			assertThat(link).isInstanceOf(AlLinkWithoutArealLinkFactoryWithoutConfig.class);
		}
	}

	@Test
	void canLoadViaMetaInfServicesArdulinkLinkfactoryWithConfig() throws IOException {
		try (Link link = sut.getConfigurer(newURI("ardulink://aLinkWithoutArealLinkFactoryWithConfig")).newLink()) {
			assertThat(link).isInstanceOf(AlLinkWithoutArealLinkFactoryWithConfig.class);
		}
	}

	@Test
	void nonExistingNameWitllThrowRTE() throws IOException {
		RuntimeException exception = assertThrows(RuntimeException.class,
				() -> sut.getConfigurer(newURI("ardulink://XXX-aNameThatIsNotRegistered-XXX")));
		assertThat(exception).hasMessageContaining("registered").hasMessageContaining("factory");
	}

	@Test
	void canLoadDummyLinkViaAlias() throws Throwable {
		withRegistered(new AliasUsingLinkFactory()).execute(() -> sut.getConfigurer(aliasUri()));
	}

	@Test
	void aliasNameNotListed() throws Throwable {
		withRegistered(new AliasUsingLinkFactory()).execute(() -> {
			assertThat(sut.listURIs()).contains(aliasUri()).doesNotContain(newURI("ardulink://aliasLinkAlias"));
		});
	}

	@Test
	void nameHasPriorityOverAlias() throws Throwable {
		AliasUsingLinkFactory factory = new AliasUsingLinkFactory();
		String dummyLinkFactoryName = new DummyLinkFactory().getName();
		assert aliasNames(factory).contains(dummyLinkFactoryName);

		AliasUsingLinkFactory spy = spy(factory);
		withRegistered(spy).execute(() -> {
			try (Link link = sut.getConfigurer(newURI("ardulink://" + dummyLinkFactoryName)).newLink()) {
				verify(spy, never()).newLink(any(LinkConfig.class));
			}
		});
	}

	private List<String> aliasNames(LinkFactory<?> factory) {
		return asList(factory.getClass().getAnnotation(Alias.class).value());
	}

	private URI aliasUri() {
		return newURI("ardulink://aliasLink");
	}

}
