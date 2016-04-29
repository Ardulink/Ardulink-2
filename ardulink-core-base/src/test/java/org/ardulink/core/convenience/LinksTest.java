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

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.ardulink.core.Connection;
import org.ardulink.core.ConnectionBasedLink;
import org.ardulink.core.Link;
import org.ardulink.core.linkmanager.DummyConnection;
import org.ardulink.core.linkmanager.DummyLinkConfig;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class LinksTest {

	@Test
	public void returnsSerialConnectionWhenAvailableAndFallsbackToFirstAvailable()
			throws IOException {
		Link link = Links.getDefault();
		Connection connection = getConnection(link);
		assertThat(connection.getClass().getName(),
				is(DummyConnection.class.getName()));
		close(link);
	}

	@Test
	public void isConfiguredForAllChoiceValues() throws IOException {
		Link link = Links.getDefault();
		DummyLinkConfig config = getConnection(link).getConfig();
		assertThat(config.getA(), is("aVal1"));
		close(link);
	}

	@Test
	public void registeredSpecialNameDefault() throws Exception {
		Link link = Links.getLink(new URI("ardulink://default"));
		assertThat(link, sameInstance(Links.getDefault()));
		close(link);
	}

	@Test
	public void doesCacheLinks() throws Exception {
		Link link1 = Links.getLink(new URI("ardulink://dummyLink"));
		Link link2 = Links.getLink(new URI("ardulink://dummyLink"));
		assertThat(link1, notNullValue());
		assertThat(link2, notNullValue());
		assertAllSameInstances(link1, link2);
		close(link1, link2);
	}

	@Test
	public void doesCacheLinksWhenUsingDefaultValues() throws Exception {
		Link link1 = Links.getLink(new URI("ardulink://dummyLink"));
		Link link2 = Links.getLink(new URI("ardulink://dummyLink?a=&b=42&c="));
		assertThat(link1, notNullValue());
		assertThat(link2, notNullValue());
		assertAllSameInstances(link1, link2);
		close(link1, link2);
	}

	@Test
	public void canCloseConnection() throws Exception {
		Link link = getRandomLink();
		DummyConnection connection = getConnection(link);
		assertThat(connection.getCloseCalls(), is(0));
		close(link);
		assertThat(connection.getCloseCalls(), is(1));
	}

	@Test
	public void doesNotCloseConnectionIfStillInUse() throws Exception {
		URI randomURI = getRandomURI();
		Link[] links = { createConnectionBasedLink(randomURI),
				createConnectionBasedLink(randomURI),
				createConnectionBasedLink(randomURI) };
		// all links point to the same instance, so choose one of them
		Link link = assertAllSameInstances(links)[0];
		link.close();
		link.close();
		assertThat(getConnection(links[0]).getCloseCalls(), is(0));
		link.close();
		assertThat(getConnection(link).getCloseCalls(), is(1));
	}

	@Test
	public void afterClosingWeGetAfreshLink() throws Exception {
		URI randomURI = getRandomURI();
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
	public void twoDifferentURIsWithSameParamsMustNotBeenMixed()
			throws Exception {
		URI uri1 = new URI("ardulink://dummyLink?a=aVal1&b=4");
		URI uri2 = new URI("ardulink://dummyLink2?a=aVal1&b=4");
		Link link1 = Links.getLink(uri1);
		Link link2 = Links.getLink(uri2);
		assertThat(link1, not(sameInstance(link2)));
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

	private Link getRandomLink() throws Exception, URISyntaxException {
		return Links.getLink(getRandomURI());
	}

	private Link createConnectionBasedLink(URI randomURI) throws Exception {
		return Links.getLink(randomURI);
	}

	private DummyConnection getConnection(Link link) {
		return (DummyConnection) ((ConnectionBasedLink) ((LinkDelegate) link)
				.getDelegate()).getConnection();
	}

	private URI getRandomURI() throws URISyntaxException {
		return new URI("ardulink://dummyLink?a=" + "&b="
				+ String.valueOf(Thread.currentThread().getId()) + "&c="
				+ System.currentTimeMillis());
	}

}
