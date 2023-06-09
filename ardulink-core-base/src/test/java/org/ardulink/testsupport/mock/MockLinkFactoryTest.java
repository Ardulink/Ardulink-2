package org.ardulink.testsupport.mock;

import static java.lang.String.format;
import static org.ardulink.testsupport.mock.MockLinkFactory.MockLinkConfig.NAME_ATTRIBUTE;
import static org.assertj.core.api.Assertions.assertThat;

import org.ardulink.core.Link;
import org.ardulink.core.convenience.Links;
import org.junit.jupiter.api.Test;

class MockLinkFactoryTest {

	private static final String MOCK_URI = "ardulink://mock";

	@Test
	void twoDefaultLinksAreSame() {
		Link link1 = Links.getLink(format("%s", MOCK_URI));
		Link link2 = Links.getLink(format("%s", MOCK_URI));
		assertThat(link1).isSameAs(link2);
	}

	@Test
	void namedNotDefault() {
		Link link1 = Links.getLink(format("%s", MOCK_URI));
		Link link2 = Links.getLink(format("%s?%s=%s", MOCK_URI, NAME_ATTRIBUTE, "another"));
		assertThat(link1).isNotSameAs(link2);
	}

	@Test
	void defaultNamedIsSameAsDefault() {
		Link link1 = Links.getLink(format("%s", MOCK_URI));
		Link link2 = Links.getLink(format("%s?%s=%s", MOCK_URI, NAME_ATTRIBUTE, "default"));
		assertThat(link1).isSameAs(link2);
	}

	@Test
	void differentNameAreDifferentMocks() {
		Link link1 = Links.getLink(format("%s?%s=%s", MOCK_URI, NAME_ATTRIBUTE, "A"));
		Link link2 = Links.getLink(format("%s?%s=%s", MOCK_URI, NAME_ATTRIBUTE, "B"));
		assertThat(link1).isNotSameAs(link2);
	}

}
