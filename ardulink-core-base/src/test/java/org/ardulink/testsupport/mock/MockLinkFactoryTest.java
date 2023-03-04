package org.ardulink.testsupport.mock;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsSame.sameInstance;

import org.ardulink.core.Link;
import org.ardulink.core.convenience.Links;
import org.junit.jupiter.api.Test;

class MockLinkFactoryTest {

	private static final String MOCK_URI = "ardulink://mock";

	@Test
	void twoDefaultLinksAreSame() {
		Link link1 = Links.getLink(MOCK_URI);
		Link link2 = Links.getLink(MOCK_URI);
		assertThat(link1, sameInstance(link2));
	}

	@Test
	void namedNotDefault() {
		Link link1 = Links.getLink(MOCK_URI);
		Link link2 = Links.getLink(MOCK_URI + "?name=another");
		assertThat(link1, not(sameInstance(link2)));
	}

	@Test
	void defaultNamedIsSameAsDefault() {
		Link link1 = Links.getLink(MOCK_URI);
		Link link2 = Links.getLink(MOCK_URI + "?name=default");
		assertThat(link1, sameInstance(link2));
	}

	@Test
	void differentNameAreDifferentMocks() {
		Link link1 = Links.getLink(MOCK_URI + "?name=A");
		Link link2 = Links.getLink(MOCK_URI + "?name=B");
		assertThat(link1, not(sameInstance(link2)));
	}

}
