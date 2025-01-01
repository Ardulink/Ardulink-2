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

package org.ardulink.core.convenience;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.stream.IntStream.range;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.convenience.Links.DEFAULT_URI;
import static org.ardulink.core.linkmanager.LinkConfig.NO_ATTRIBUTES;
import static org.ardulink.core.linkmanager.LinkManager.ARDULINK_SCHEME;
import static org.ardulink.core.linkmanager.providers.DynamicLinkFactoriesProvider.withRegistered;
import static org.ardulink.testsupport.mock.TestSupport.extractDelegated;
import static org.ardulink.testsupport.mock.TestSupport.getMock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.Link;
import org.ardulink.core.Pin;
import org.ardulink.core.linkmanager.AliasUsingLinkFactory;
import org.ardulink.core.linkmanager.DummyConnection;
import org.ardulink.core.linkmanager.DummyLinkConfig;
import org.ardulink.core.linkmanager.DummyLinkFactory;
import org.ardulink.core.linkmanager.LinkConfig;
import org.ardulink.core.linkmanager.LinkFactory;
import org.ardulink.core.proto.ardulink.ArdulinkProtocol2;
import org.ardulink.testsupport.mock.junit5.MockUri;
import org.ardulink.util.Closeables;
import org.ardulink.util.ListMultiMap;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ExpectedToFail;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class LinksTest {

	@Test
	void whenRequestingDefaultLinkReturnsFirstAvailableConnectionIfSerialNotAvailable() throws IOException {
		try (Link link1 = Links.getDefault(); Link link2 = Links.getLink(DEFAULT_URI)) {
			assertThat(asList(link1, link2)).doesNotContainNull();
			assertThat(link1).isSameAs(link2);
			isDummyConnection(link1);
			isDummyConnection(link2);
		}
	}

	@Test
	void whenRequestingDefaultLinkSerialHasPriorityOverAllOthers() throws Throwable {
		LinkFactory<LinkConfig> factory = spy(factoryNamed(serial()));
		Link dummyLink = mock(Link.class, withSettings().stubOnly());
		doReturn(dummyLink).when(factory).newLink(any(LinkConfig.class));
		withRegistered(factoryNamed(serialDash("a")), factoryNamed("a"), factory, factoryNamed("z"),
				factoryNamed(serialDash("z"))).execute(() -> {
					try (Link link = Links.getDefault()) {
						assertLinkFactoryCreatedOneLink(factory);
					}
				});
	}

	@Test
	void whenRequestingDefaultLinkStartingWithSerialDashHasPriorityOverAllOthers() throws Throwable {
		LinkFactory<LinkConfig> factory = spy(factoryNamed(serialDash("appendix-does-not-matter")));
		withRegistered(factoryNamed("a"), factory, factoryNamed("z")).execute(() -> {
			try (Link link = Links.getDefault()) {
				assertLinkFactoryCreatedOneLink(factory);
			}
		});
	}

	@Test
	void serialDashDoesHandleSerial() throws Throwable {
		LinkFactory<LinkConfig> factory = spy(factoryNamed(serialDash("appendix-does-not-matter")));
		withRegistered(factory).execute(() -> {
			try (Link link = link(serial())) {
				assertLinkFactoryCreatedOneLink(factory);
			}
		});
	}

	@Test
	void isConfiguredForAllChoiceValues() throws IOException {
		String dVal1 = "dVal1";
		DummyLinkConfig.choiceValuesOfD.set(new String[] { dVal1, "dVal2" });
		try (Link link = Links.getDefault()) {
			DummyLinkConfig config = getConfig(link);
			assertSoftly(s -> {
				s.assertThat(config.getProtocol()).isExactlyInstanceOf(ArdulinkProtocol2.class);
				s.assertThat(config.getA()).isEqualTo("aVal1");
				s.assertThat(config.getD()).isEqualTo(dVal1);
				s.assertThat(config.getF1()).isEqualTo(NANOSECONDS);
				s.assertThat(config.getF2()).isEqualTo(MINUTES);
			});
		}
	}

	@Test
	void streamConsume() throws IOException {
		try (Link link = Links.getDefault()) {
			DummyLinkConfig config = getConfig(link);
			range(0, 42).forEach(__ -> assertThat(config.getF2()).isEqualTo(MINUTES));
		}
	}

	@Test
	void registeredSpecialNameDefault() throws Throwable {
		LinkFactory<LinkConfig> serial = spy(factoryNamed(serial()));
		assert serial.newLinkConfig().equals(NO_ATTRIBUTES)
				: "ardulink://default would differ if the config has attributes";
		withRegistered(serial).execute(() -> {
			try (Link link1 = Links.getLink("ardulink://default"); Link link2 = Links.getDefault()) {
				assertThat(link1).isSameAs(link2);
			}
		});
	}

	@Test
	void doesCacheLinks() throws IOException {
		String uri = "ardulink://dummyLink";
		try (Link link1 = Links.getLink(uri); Link link2 = Links.getLink(uri)) {
			assertThat(asList(link1, link2)).doesNotContainNull();
			assertThat(link1).isSameAs(link2);
		}
	}

	@Test
	void doesCacheLinksWhenUsingDefaultValues() throws IOException {
		try (Link link1 = Links.getLink("ardulink://dummyLink");
				Link link2 = Links.getLink("ardulink://dummyLink?b=42&f1=NANOSECONDS&f2=MINUTES")) {
			assertThat(asList(link1, link2)).doesNotContainNull();
			assertThat(link1).isSameAs(link2);
		}
	}

	@Test
	void canCloseConnection(@MockUri String mockUri) throws IOException {
		try (Link link = Links.getLink(mockUri)) {
			verify(getMock(link), never()).close();
			close(link);
			verify(getMock(link), times(1)).close();
		}
	}

	@Test
	void doesNotCloseConnectionIfStillInUse(@MockUri String mockUri) throws IOException {
		Link[] links = range(0, 3).mapToObj(__ -> Links.getLink(mockUri)).toArray(Link[]::new);
		assertThat(links).allSatisfy(l -> assertThat(l).isSameAs(links[0]));
		// all links point to the same instance, so choose one of them
		try (Link link = links[0]) {
			stream(links).skip(1).forEach(Closeables::closeQuietly);
			verify(getMock(link), never()).close();
			link.close();
			verify(getMock(link), times(1)).close();
		}
	}

	@Test
	void afterClosingWeGetAfreshLink(@MockUri String mockUri) throws IOException {
		try (Link link1 = Links.getLink(mockUri); Link link2 = Links.getLink(mockUri)) {
			assertThat(link1).isSameAs(link2);
			close(link1, link2);
			try (Link link3 = Links.getLink(mockUri)) {
				assertThat(link3).isNotSameAs(link1).isNotSameAs(link2);
			}
		}
	}

	@Test
	void stopsListenigAfterAllCallersLikeToStopListening(@MockUri String mockUri) throws IOException {
		try (Link link1 = Links.getLink(mockUri); Link link2 = Links.getLink(mockUri)) {
			assertThat(link1).isSameAs(link2);
			Link mock = getMock(link1);
			Pin anyDigitalPin = digitalPin(3);
			link1.startListening(anyDigitalPin);
			link2.startListening(anyDigitalPin);

			link1.stopListening(anyDigitalPin);
			// stop on others (not listening-started) pins
			link1.stopListening(analogPin(anyDigitalPin.pinNum()));
			link1.stopListening(analogPin(anyDigitalPin.pinNum() + 1));
			link1.stopListening(digitalPin(anyDigitalPin.pinNum() + 1));
			verify(mock, never()).stopListening(anyDigitalPin);

			link2.stopListening(anyDigitalPin);
			verify(mock, times(1)).stopListening(anyDigitalPin);
		}
	}

	@Test
	void twoDifferentURIsWithSameParamsMustNotBeenMixed() throws Throwable {
		String nameOrig = new DummyLinkFactory().getName();
		String nameOther = "NOT" + nameOrig;

		withRegistered(new DummyLinkFactory() {
			@Override
			public String getName() {
				return nameOther;
			}
		}).execute(() -> {
			String params = "?a=aVal1&b=4";
			try (Link linkOrig = link(nameOrig + params); Link linkOther = link(nameOther + params)) {
				assertThat(linkOrig).isNotSameAs(linkOther);
			}
		});
	}

	@Test
	void aFilteredFactoryGetsIgnored() throws Throwable {
		AtomicBoolean isActive = new AtomicBoolean();
		String name = "Name" + UUID.randomUUID();

		withRegistered(new DummyLinkFactory() {
			@Override
			public String getName() {
				return name;
			}

			@Override
			public boolean isActive() {
				return isActive.get();
			}

		}).execute(() -> {
			isActive.set(true);
			try (Link link = link(name)) {
				assertThat(link).isNotNull();
			}
			isActive.set(false);
			assertThatIllegalArgumentException().isThrownBy(() -> link(name)).withMessageContaining("No factory", name);
		});
	}

	private static class LinkFactoryForTest implements LinkFactory<LinkConfig> {

		private final String name;
		private final Supplier<LinkConfig> linkConfigSupplier;
		private final ListMultiMap<LinkConfig, Link> configsAndLinks = new ListMultiMap<>();

		public LinkFactoryForTest(String name, Supplier<LinkConfig> linkConfigSupplier) {
			this.name = name;
			this.linkConfigSupplier = linkConfigSupplier;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Link newLink(LinkConfig config) {
			Link mock = mock(Link.class);
			configsAndLinks.put(config, mock);
			return mock;
		}

		@Override
		public LinkConfig newLinkConfig() {
			return linkConfigSupplier.get();
		}
	}

	public static class MyLinkConfig implements LinkConfig {

		private static final String A1 = "noChoiceValue_keepsNull";
		private static final String A2 = "hasChoiceValueWithoutNullValue_turnsFirstChoiceValue";
		private static final String A3 = "hasChoiceValueWithNullValue_getsNull_andSoKeepsNull";
		private static final String A4 = "initializedVarWithoutChoiceValue_keepsOldValue";
		private static final String A5 = "initializedVarWithChoiceValue_keepsOldValue";
		private static final String A6 = "hasDefault_isNulledViaUriParamToNewValue";

		@Named(A1)
		public String keepsNull;
		@Named(A2)
		public String turnsFirstChoiceValue;
		@Named(A3)
		public String getsNullAndSoKeepsNull;
		@Named(A4)
		public String keepsOldValue1 = "keepsValue_noChoice";
		@Named(A5)
		public String keepsOldValue2 = "keepsValue_withChoice";
		@Named(A6)
		public String isNulledViaUriParamToNewValue = "shouldDisappear";

		@ChoiceFor(A2)
		public String[] a2ChoicesWithoutNull() {
			return new String[] { "Choice1", "Choice2", "Choice3" };
		}

		@ChoiceFor(A3)
		public String[] a3ChoicesWithNull() {
			return new String[] { null, "Choice1", "Choice2", "Choice3" };
		}

		@ChoiceFor(A5)
		public String[] mustAtLeastHoldTheCurrentValue() {
			return new String[] { "Choice1", "Choice2", "keepsValue_withChoice", "Choice3" };
		}

	}

	@Test
	void handlesChoiceValuesCorrectly() throws Throwable {
		String randomName = "factory-" + randomUUID();
		LinkFactoryForTest linkFactory = factory(randomName, () -> new MyLinkConfig());
		withRegistered(linkFactory).execute(() -> {
			try (Link link = link(randomName)) {
				assertThat(linkFactory.configsAndLinks.asMap().keySet()).singleElement()
						.isInstanceOfSatisfying(MyLinkConfig.class, c -> {
							assertSoftly(s -> {
								s.assertThat(c.keepsNull).isNull();
								s.assertThat(c.turnsFirstChoiceValue).isEqualTo("Choice1");
								s.assertThat(c.getsNullAndSoKeepsNull).isNull();
								s.assertThat(c.keepsOldValue1).isEqualTo("keepsValue_noChoice");
								s.assertThat(c.keepsOldValue2).isEqualTo("keepsValue_withChoice");
							});
						});
			}
		});
	}

	@Test
	void aliasLinksAreSharedToo() throws Throwable {
		withRegistered(new AliasUsingLinkFactory()).execute(() -> {
			try (Link link1 = link(AliasUsingLinkFactory.NAME);
					Link link2 = link(AliasUsingLinkFactory.ALIAS_FACTORY_ALIAS)) {
				assertThat(link1).isSameAs(link2);
			}
		});
	}

	@Test
	@ExpectedToFail("Needs a ReferenceQueue in Links class")
	void closesUnunsedLinksThatGetGCed(@MockUri String mockUri) throws IOException {
		Link link = Links.getLink(mockUri);
		link = null;
		System.gc();
		verify(getMock(link), times(1)).close();
	}

	private static Link link(String name) {
		return Links.getLink(format("%s://%s", ARDULINK_SCHEME, name));
	}

	private void isDummyConnection(Link link) {
		assertThat(getConnection(link)).isInstanceOf(DummyConnection.class);
	}

	private void assertLinkFactoryCreatedOneLink(LinkFactory<LinkConfig> factory) throws Exception {
		verify(factory, times(1)).newLink(any(LinkConfig.class));
	}

	private LinkFactory<LinkConfig> factoryNamed(String name) {
		return factory(name, () -> NO_ATTRIBUTES);
	}

	private LinkFactoryForTest factory(String name, Supplier<LinkConfig> linkConfigSupplier) {
		return new LinkFactoryForTest(name, linkConfigSupplier);
	}

	private static String serialDash(String appendix) {
		return format("%s-%s", serial(), appendix);
	}

	private static String serial() {
		return "serial";
	}

	private void close(Link... links) {
		Arrays.stream(links).forEach(Closeables::closeQuietly);
	}

	private DummyLinkConfig getConfig(Link link) {
		return getConnection(link).getConfig();
	}

	private DummyConnection getConnection(Link link) {
		return (DummyConnection) getDelegate(link).getConnection();
	}

	private ConnectionBasedLink getDelegate(Link link) {
		return (ConnectionBasedLink) extractDelegated(link);
	}

}
