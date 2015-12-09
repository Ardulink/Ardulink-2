package com.github.pfichtner.ardulink.core.convenience;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;

import java.net.URI;

import org.junit.Test;

import com.github.pfichtner.ardulink.core.Connection;
import com.github.pfichtner.ardulink.core.ConnectionBasedLink;
import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.linkmanager.DummyConnection;
import com.github.pfichtner.ardulink.core.linkmanager.DummyLinkConfig;

public class LinksTest {

	@Test
	public void returnsSerialConnectionWhenAvailableAndFallsbackToFirstAvailable() {
		Link link = Links.getDefault();
		Connection connection = ((ConnectionBasedLink) link).getConnection();
		assertThat(connection.getClass().getName(),
				is(DummyConnection.class.getName()));
	}

	@Test
	public void isConfiguredForAllChoiceValues() {
		Link link = Links.getDefault();
		DummyLinkConfig config = ((DummyConnection) ((ConnectionBasedLink) link)
				.getConnection()).getConfig();
		assertThat(config.getA(), is("aVal1"));
	}

	@Test
	public void doesCacheLinks() throws Exception {
		Link link1 = Links.getLink(new URI("ardulink://dummyLink"));
		Link link2 = Links.getLink(new URI("ardulink://dummyLink"));
		assertThat(link1, notNullValue());
		assertThat(link2, notNullValue());
		assertThat(link1, sameInstance(link2));
	}

	@Test
	public void doesCacheLinksWhenUsingDefaultValues() throws Exception {
		Link link1 = Links.getLink(new URI("ardulink://dummyLink"));
		Link link2 = Links.getLink(new URI("ardulink://dummyLink?a=&b=42&c="));
		assertThat(link1, notNullValue());
		assertThat(link2, notNullValue());
		assertThat(link1, sameInstance(link2));
	}

}
