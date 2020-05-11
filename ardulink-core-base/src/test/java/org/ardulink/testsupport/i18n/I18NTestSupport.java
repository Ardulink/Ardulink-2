package org.ardulink.testsupport.i18n;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.net.URI;

import org.ardulink.core.linkmanager.LinkManager;
import org.ardulink.core.linkmanager.LinkManager.Configurer;
import org.ardulink.util.URIs;

public final class I18NTestSupport {

	private I18NTestSupport() {
		super();
	}

	public static void assertAllAttributesHaveDescriptions(String uri) {
		assertAllAttributesHaveDescriptions(URIs.newURI(uri));
	}

	public static void assertAllAttributesHaveDescriptions(URI uri) {
		assertAllAttributesHaveDescriptions(LinkManager.getInstance().getConfigurer(uri));
	}

	public static void assertAllAttributesHaveDescriptions(Configurer configurer) {
		for (String name : configurer.getAttributes()) {
			String description = configurer.getAttribute(name).getDescription();
			assertThat(name + " has no description", description, is(notNullValue()));
		}
	}

}
