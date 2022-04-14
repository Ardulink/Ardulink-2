package org.ardulink.testsupport.i18n;

import static org.ardulink.util.anno.LapsedWith.JDK8;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.net.URI;
import java.util.List;

import org.ardulink.core.linkmanager.LinkManager;
import org.ardulink.core.linkmanager.LinkManager.Configurer;
import org.ardulink.util.Lists;
import org.ardulink.util.URIs;
import org.ardulink.util.anno.LapsedWith;

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
		assertThat(attributesWithoutDescription + " has no descriptions", attributesWithoutDescription.size(), equalTo(0));
	}

	@LapsedWith(module = JDK8, value = "lambda")
	private static List<String> attributesWithoutDescription(Configurer configurer) {
		List<String> attributesWithoutDescription = Lists.newArrayList();
		for (String name : configurer.getAttributes()) {
			if (configurer.getAttribute(name).getDescription() == null) {
				attributesWithoutDescription.add(name);
			}
		}
		return attributesWithoutDescription;
	}

}
