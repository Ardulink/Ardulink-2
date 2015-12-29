package com.github.pfichtner.core;

import static org.junit.Assert.assertNotNull;

import java.net.URI;

import org.junit.Ignore;
import org.junit.Test;

import com.github.pfichtner.ardulink.core.linkmanager.LinkManager;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.Configurer;

@Ignore // ignored since jni load
public class PiLinkTest {

	@Test
	public void canCreateIstances() throws Exception {
		Configurer configurer = LinkManager.getInstance().getConfigurer(
				new URI("ardulink://raspberry"));
		assertNotNull(configurer.newLink());
	}

}
