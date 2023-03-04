package org.ardulink.testsupport.i18n;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.net.URI;
import java.util.List;

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
		List<String> attributesWithoutDescription = attributesWithoutDescription(configurer);
		assertThat(attributesWithoutDescription + " has no descriptions", attributesWithoutDescription.size(),
				equalTo(0));
	}

	private static List<String> attributesWithoutDescription(Configurer configurer) {
		return configurer.getAttributes().stream()
				.filter(name -> configurer.getAttribute(name).getDescription() == null).collect(toList());
	}

}
