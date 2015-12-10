package com.github.pfichtner.ardulink.core.convenience;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import com.github.pfichtner.ardulink.core.Connection;
import com.github.pfichtner.ardulink.core.ConnectionBasedLink;
import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.linkmanager.DummyConnection;
import com.github.pfichtner.ardulink.core.linkmanager.DummyLinkConfig;

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
	public void doesCacheLinks() throws Exception {
		Link link1 = Links.getLink(new URI("ardulink://dummyLink"));
		Link link2 = Links.getLink(new URI("ardulink://dummyLink"));
		assertThat(link1, notNullValue());
		assertThat(link2, notNullValue());
		assertThat(link1, sameInstance(link2));
		close(link1, link2);
	}

	@Test
	public void doesCacheLinksWhenUsingDefaultValues() throws Exception {
		Link link1 = Links.getLink(new URI("ardulink://dummyLink"));
		Link link2 = Links.getLink(new URI("ardulink://dummyLink?a=&b=42&c="));
		assertThat(link1, notNullValue());
		assertThat(link2, notNullValue());
		assertThat(link1, sameInstance(link2));
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
	public void afterClosingWeGetAfreshLink() throws Exception {
		URI randomURI = getRandomURI();
		Link link1 = createConnectionBasedLink(randomURI);
		Link link2 = createConnectionBasedLink(randomURI);
		assertThat(link1, sameInstance(link2));
		close(link1, link2);
		Link link3 = createConnectionBasedLink(randomURI);
		assertThat(link3, not(sameInstance(link1)));
		assertThat(link3, not(sameInstance(link2)));
		close(link3);
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
