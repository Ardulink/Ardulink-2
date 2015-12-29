package com.github.pfichtner.core;

import java.net.URI;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.github.pfichtner.ardulink.core.linkmanager.LinkManager;
import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.Configurer;

public class PiLinkTest {

	@Rule
	public ExpectedException exceptions = ExpectedException.none();

	@Test
	public void creatingInstanceWillFailOnX86withUnsatisfiedLinkError()
			throws Exception {
		// TODO should do a Assume if we are on a raspi or not
		Configurer configurer = LinkManager.getInstance().getConfigurer(
				new URI("ardulink://raspberry"));
		exceptions.expect(UnsatisfiedLinkError.class);
		configurer.newLink();
	}

}
