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

import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;
import static org.ardulink.core.linkmanager.LinkConfig.NO_ATTRIBUTES;
import static org.ardulink.core.linkmanager.providers.LinkFactoriesProvider4Test.withRegistered;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.Link;
import org.ardulink.core.Pin;
import org.ardulink.core.linkmanager.DummyConnection;
import org.ardulink.core.linkmanager.DummyLinkConfig;
import org.ardulink.core.linkmanager.DummyLinkFactory;
import org.ardulink.core.linkmanager.LinkConfig;
import org.ardulink.core.linkmanager.LinkFactory;
import org.ardulink.core.linkmanager.LinkManagerTest.AliasUsingLinkFactory;
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
		Link link = Links.getDefault();
		assertThat(getConnection(link), instanceOf(DummyConnection.class));
		close(link);
	}

	@Test
	void whenRequestingDefaultLinkSerialHasPriorityOverAllOthers() throws Throwable {
		final LinkFactory<LinkConfig> serial = spy(factoryNamed(serial()));
		withRegistered(factoryNamed(serialDash("a")), factoryNamed("a"), serial, factoryNamed("z"),
				factoryNamed(serialDash("z"))).execute(() -> assertLinkWasCreatedBy(Links.getDefault(), serial));
	}

	@Test
	void whenRequestingDefaultLinkStartingWithSerialDashHasPriorityOverAllOthers() throws Throwable {
		final LinkFactory<LinkConfig> serialDashAnything = spy(factoryNamed(serialDash("appendix-does-not-matter")));
		withRegistered(factoryNamed("a"), serialDashAnything, factoryNamed("z"))
				.execute(() -> assertLinkWasCreatedBy(Links.getDefault(), serialDashAnything));
	}

	@Test
	void serialDashDoesHandleSerial() throws Throwable {
		final LinkFactory<LinkConfig> serialDashAnything = spy(factoryNamed(serialDash("appendix-does-not-matter")));
		withRegistered(serialDashAnything)
				.execute(() -> assertLinkWasCreatedBy(Links.getLink("ardulink://serial"), serialDashAnything));
	}

	private static String serialDash(String appendix) {
		return serial() + "-" + appendix;
	}

	private static String serial() {
		return "serial";
	}

	@Test
	void isConfiguredForAllChoiceValues() throws IOException {
		Link link = Links.getDefault();
		DummyLinkConfig config = getConnection(link).getConfig();
		assertThat(config.getA(), is("aVal1"));
		close(link);
	}

	@Test
	void registeredSpecialNameDefault() throws Throwable {
		final LinkFactory<LinkConfig> serial = spy(factoryNamed(serial()));
		assert serial.newLinkConfig().equals(NO_ATTRIBUTES)
				: "ardulink://default would differ if the config has attributes";
		withRegistered(serial).execute(() -> {
			Link link1 = Links.getLink("ardulink://default");
			Link link2 = Links.getDefault();
			assertAllSameInstances(link1, link2);
			close(link1, link2);
		});
	}

	@Test
	void doesCacheLinks() throws IOException {
		String uri = "ardulink://dummyLink";
		Link link1 = Links.getLink(uri);
		Link link2 = Links.getLink(uri);
		assertThat(link1, notNullValue());
		assertThat(link2, notNullValue());
		assertAllSameInstances(link1, link2);
		close(link1, link2);
	}

	@Test
	void doesCacheLinksWhenUsingDefaultValues() throws IOException {
		Link link1 = Links.getLink("ardulink://dummyLink");
		Link link2 = Links.getLink("ardulink://dummyLink?a=&b=42&c=");
		assertThat(link1, notNullValue());
		assertThat(link2, notNullValue());
		assertAllSameInstances(link1, link2);
		close(link1, link2);
	}

	@Test
	void canCloseConnection() throws IOException {
		Link link = getRandomLink();
		DummyConnection connection = getConnection(link);
		verify(connection, times(0)).close();
		close(link);
		verify(connection, times(1)).close();
	}

	@Test
	void doesNotCloseConnectionIfStillInUse() throws IOException {
		String randomURI = getRandomURI();
		Link[] links = { createConnectionBasedLink(randomURI), createConnectionBasedLink(randomURI),
				createConnectionBasedLink(randomURI) };
		// all links point to the same instance, so choose one of them
		Link link = assertAllSameInstances(links)[0];
		link.close();
		link.close();
		verify(getConnection(link), times(0)).close();
		link.close();
		verify(getConnection(link), times(1)).close();
	}

	@Test
	void afterClosingWeGetAfreshLink() throws IOException {
		String randomURI = getRandomURI();
		Link link1 = createConnectionBasedLink(randomURI);
		Link link2 = createConnectionBasedLink(randomURI);
		assertAllSameInstances(link1, link2);
		close(link1, link2);
		Link link3 = createConnectionBasedLink(randomURI);
		assertThat(link3, not(sameInstance(link1)));
		assertThat(link3, not(sameInstance(link2)));
		close(link3);
	}

	@Test
	void stopsListenigAfterAllCallersLikeToStopListening() throws IOException {
		String randomURI = getRandomURI();
		Link link0 = createConnectionBasedLink(randomURI);
		Link link1 = createConnectionBasedLink(randomURI);
		ConnectionBasedLink delegate = assertAllSameInstances(getDelegate(link0), getDelegate(link1))[0];
		Pin anyPin = digitalPin(3);
		link0.startListening(anyPin);
		link1.startListening(anyPin);

		link0.stopListening(anyPin);
		// stop on others (not listening-started) pins
		link0.stopListening(analogPin(anyPin.pinNum()));
		link0.stopListening(analogPin(anyPin.pinNum() + 1));
		link0.stopListening(digitalPin(anyPin.pinNum() + 1));
		verify(delegate, times(0)).stopListening(anyPin);

		link1.stopListening(anyPin);
		verify(delegate, times(1)).stopListening(anyPin);
		close(link0, link1);
	}

	@Test
	void twoDifferentURIsWithSameParamsMustNotBeenMixed() throws Throwable {
		final String name1 = new DummyLinkFactory().getName();
		final String name2 = "DummyLINK";
		assert name1.equalsIgnoreCase(name2) && !name1.equals(name2);
		class DummyLinkFactoryExtension extends DummyLinkFactory {
			@Override
			public String getName() {
				return name2;
			}
		}

		withRegistered(new DummyLinkFactoryExtension()).execute(() -> {
			Link link1 = Links.getLink(makeUri(name1));
			Link link2 = Links.getLink(makeUri(name2));
			assertThat(link1, not(sameInstance(link2)));
			close(link1, link2);
		});
	}

	private static String makeUri(String name) {
		return String.format("ardulink://%s?a=aVal1&b=4", name);
	}

	@Test
	void aliasLinksAreSharedToo() throws Throwable {
		withRegistered(new AliasUsingLinkFactory()).execute(() -> {
			Link link1 = Links.getLink("ardulink://aliasLink");
			Link link2 = Links.getLink("ardulink://aliasLinkAlias");
			assertAllSameInstances(link1, link2);
			close(link1, link2);
		});
	}

	private void assertLinkWasCreatedBy(Link link, LinkFactory<LinkConfig> serial) throws Exception {
		verify(serial, times(1)).newLink(any(LinkConfig.class));
		link.close();
	}

	private LinkFactory<LinkConfig> factoryNamed(final String name) {
		return new LinkFactory<LinkConfig>() {

			@Override
			public String getName() {
				return name;
			}

			@Override
			public Link newLink(LinkConfig config) {
				return mock(Link.class);
			}

			@Override
			public LinkConfig newLinkConfig() {
				return NO_ATTRIBUTES;
			}

		};
	}

	private static <T> T[] assertAllSameInstances(T... objects) {
		for (int i = 0; i < objects.length - 1; i++) {
			assertThat(objects[i], sameInstance(objects[i + 1]));
		}
		return objects;
	}

	private void close(Link... links) throws IOException {
		for (Link link : links) {
			link.close();
		}
	}

	private Link getRandomLink() {
		return Links.getLink(getRandomURI());
	}

	private Link createConnectionBasedLink(String uri) {
		return Links.getLink(uri);
	}

	private DummyConnection getConnection(Link link) {
		return (DummyConnection) getDelegate(link).getConnection();
	}

	private ConnectionBasedLink getDelegate(Link link) {
		return (ConnectionBasedLink) ((LinkDelegate) link).getDelegate();
	}

	private String getRandomURI() {
		return "ardulink://dummyLink?a=" + "&b=" + String.valueOf(Thread.currentThread().getId()) + "&c="
				+ System.currentTimeMillis();
	}

}
