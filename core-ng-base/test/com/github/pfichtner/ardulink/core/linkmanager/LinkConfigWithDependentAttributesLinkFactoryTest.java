package com.github.pfichtner.ardulink.core.linkmanager;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

import com.github.pfichtner.ardulink.core.Link;

public class LinkConfigWithDependentAttributesLinkFactoryTest {

	@Test
	public void canInstantiateLinkWithDependentAttributes()
			throws URISyntaxException, Exception {
		LinkManager connectionManager = LinkManager.getInstance();
		Link link = connectionManager
				.getConfigurer(
						new URI(
								"ardulink://dependendAttributes?devicePort=foo&host=h&port=1"))
				.newLink();
		assertThat(link, is(notNullValue()));
	}

}
