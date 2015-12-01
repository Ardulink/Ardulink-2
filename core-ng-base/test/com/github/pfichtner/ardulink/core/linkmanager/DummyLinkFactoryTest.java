package com.github.pfichtner.ardulink.core.linkmanager;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

import com.github.pfichtner.ardulink.core.ConnectionBasedLink;
import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.ConfigAttribute;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.Configurer;
import com.github.pfichtner.ardulink.core.proto.impl.DummyProtocol;

public class DummyLinkFactoryTest {

	@Test(expected = IllegalArgumentException.class)
	public void throwsExceptionOnInvalidNames() throws URISyntaxException {
		LinkManager connectionManager = LinkManager.getInstance();
		connectionManager.getConfigurer(new URI(
				"ardulink://non_registered_and_not_existing_name"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void schemaHasToBeArdulink() throws URISyntaxException {
		LinkManager connectionManager = LinkManager.getInstance();
		connectionManager.getConfigurer(new URI("wrongSchema://dummy"));
	}

	@Test
	public void canCreateDummyDonnection() throws Exception {
		LinkManager connectionManager = LinkManager.getInstance();
		Link link = connectionManager
				.getConfigurer(new URI("ardulink://dummyLink")).newLink();
		assertThat(link, is(notNullValue()));
	}

	@Test
	public void canConfigureDummyConnection() throws Exception {
		LinkManager connectionManager = LinkManager.getInstance();
		String aValue = "aValue";
		int bValue = 1;
		String cValue = "cValue";
		Link link = (Link) connectionManager.getConfigurer(
				new URI("ardulink://dummyLink?a=" + aValue + "&b=" + bValue + "&c="
						+ cValue + "&proto=dummyProto")).newLink();

		assertThat(link.getClass().getName(),
				is(ConnectionBasedLink.class.getName()));
		DummyConnection connection = (DummyConnection) ((ConnectionBasedLink) link)
				.getConnection();
		DummyLinkConfig config = connection.getConfig();
		assertThat(config.a, is(aValue));
		assertThat(config.b, is(bValue));
		assertThat(config.c, is(cValue));
		assertThat(config.protocol.getClass().getName(), is(DummyProtocol
				.getInstance().getClass().getName()));
	}

	@Test(expected = IllegalArgumentException.class)
	public void throwsExceptionOnInvalidKey() throws URISyntaxException {
		LinkManager connectionManager = LinkManager.getInstance();
		connectionManager.getConfigurer(new URI(
				"ardulink://dummyLink?nonExistingKey=someValue"));
	}

	@Test
	public void canDefinePossibleValues() throws Exception {
		LinkManager connectionManager = LinkManager.getInstance();
		Configurer configurer = connectionManager.getConfigurer(new URI(
				"ardulink://dummyLink"));
		ConfigAttribute a = configurer.getAttribute("a");
		assertThat(a.hasPossibleValues(), is(TRUE));
		assertThat(a.getPossibleValues(), is(new Object[] { "aVal1", "aVal2" }));

		assertThat(configurer.getAttribute("b").hasPossibleValues(), is(FALSE));
		assertThat(configurer.getAttribute("c").hasPossibleValues(), is(FALSE));

		ConfigAttribute proto = configurer.getAttribute("proto");
		assertThat(proto.hasPossibleValues(), is(TRUE));
		assertThat(
				new HashSet<Object>(Arrays.asList(proto.getPossibleValues())),
				is(new HashSet<Object>(Arrays.asList("dummyProto", "ardulink"))));
	}

	@Test
	public void canIterateRegisteredFactories() throws URISyntaxException {
		LinkManager connectionManager = LinkManager.getInstance();
		assertThat(connectionManager.listURIs(),
				is(Arrays.asList(new URI("ardulink://dummyLink"))));
	}

}
