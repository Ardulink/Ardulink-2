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
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.stream.IntStream.range;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.convenience.Links.DEFAULT_URI;
import static org.ardulink.core.linkmanager.LinkConfig.NO_ATTRIBUTES;
import static org.ardulink.core.linkmanager.LinkManager.SCHEMA;
import static org.ardulink.core.linkmanager.providers.DynamicLinkFactoriesProvider.withRegistered;
import static org.ardulink.testsupport.mock.TestSupport.extractDelegated;
import static org.ardulink.testsupport.mock.TestSupport.getMock;
import static org.ardulink.util.Throwables.propagate;
import static org.assertj.core.api.Assertions.assertThat;
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
import java.util.Set;
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
import org.ardulink.core.proto.impl.ArdulinkProtocol2;
import org.ardulink.testsupport.mock.junit5.MockUri;
import org.ardulink.util.ListMultiMap;
import org.junit.jupiter.api.Test;

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

	private void isDummyConnection(Link link) {
		assertThat(getConnection(link)).isInstanceOf(DummyConnection.class);
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
			try (Link link = Links.getLink("ardulink://serial")) {
				assertLinkFactoryCreatedOneLink(factory);
			}
		});
	}

	private static String serialDash(String appendix) {
		return format("%s-%s", serial(), appendix);
	}

	private static String serial() {
		return "serial";
	}

	@Test
	void isConfiguredForAllChoiceValues() throws IOException {
		String dVal1 = "dVal1";
		DummyLinkConfig.choiceValuesOfD.set(new String[] { dVal1, "dVal2" });
		try (Link link = Links.getDefault()) {
			DummyLinkConfig config = getConfig(link);
			assertThat(config.getProtocol()).isInstanceOf(ArdulinkProtocol2.class);
			assertThat(config.getA()).isEqualTo("aVal1");
			assertThat(config.getD()).isEqualTo(dVal1);
			assertThat(config.getF1()).isEqualTo(NANOSECONDS);
			assertThat(config.getF2()).isEqualTo(NANOSECONDS);
		}
	}

	@Test
	void streamConsume() throws IOException {
		try (Link link = Links.getDefault()) {
			DummyLinkConfig config = getConfig(link);
			range(0, 42).forEach(__ -> assertThat(config.getF2()).isEqualTo(NANOSECONDS));
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
				Link link2 = Links.getLink("ardulink://dummyLink?a=&b=42&c=")) {
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
			range(0, links.length - 1).forEach(__ -> closeQuietly(link));
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
		String nameOther = "Not" + nameOrig;

		class DummyLinkFactoryExtension extends DummyLinkFactory {
			@Override
			public String getName() {
				return nameOther;
			}
		}

		withRegistered(new DummyLinkFactoryExtension()).execute(() -> {
			try (Link linkOrig = Links.getLink(makeUri(nameOrig)); Link linkOther = Links.getLink(makeUri(nameOther))) {
				assertThat(linkOrig).isNotSameAs(linkOther);
			}
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
		String randomName = "factory-" + randomUUID().toString();
		LinkFactoryForTest linkFactory = factory(randomName, () -> new MyLinkConfig());
		withRegistered(linkFactory).execute(() -> {
			try (Link link = Links.getLink(format("%s://%s", SCHEMA, randomName))) {
				assertThat(linkFactory.configsAndLinks.asMap().keySet()).singleElement()
						.isInstanceOfSatisfying(MyLinkConfig.class, c -> {
							assertThat(c.keepsNull).isNull();
							assertThat(c.turnsFirstChoiceValue).isEqualTo("Choice1");
							assertThat(c.getsNullAndSoKeepsNull).isNull();
							assertThat(c.keepsOldValue1).isEqualTo("keepsValue_noChoice");
							assertThat(c.keepsOldValue2).isEqualTo("keepsValue_withChoice");
						});
			}
		});
	}

	private static String makeUri(String name) {
		return format("ardulink://%s?a=aVal1&b=4", name);
	}

	@Test
	void aliasLinksAreSharedToo() throws Throwable {
		withRegistered(new AliasUsingLinkFactory()).execute(() -> {
			try (Link link1 = Links.getLink(format("%s://%s", SCHEMA, AliasUsingLinkFactory.NAME));
					Link link2 = Links.getLink(format("%s://%s", SCHEMA, AliasUsingLinkFactory.ALIAS_FACTORY_ALIAS))) {
				assertThat(link1).isSameAs(link2);
			}
		});
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

	private void close(Link... links) {
		Arrays.stream(links).forEach(LinksTest::closeQuietly);
	}

	private static void closeQuietly(Link link) {
		try {
			link.close();
		} catch (IOException e) {
			propagate(e);
		}
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
