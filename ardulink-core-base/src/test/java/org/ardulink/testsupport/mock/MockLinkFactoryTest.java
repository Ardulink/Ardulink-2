package org.ardulink.testsupport.mock;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.IsSame.sameInstance;
import static org.junit.Assert.assertThat;

import org.ardulink.core.Link;
import org.ardulink.core.convenience.Links;
import org.junit.Test;

public class MockLinkFactoryTest {

	private static final String MOCK_URI = "ardulink://mock";

	@Test
	public void twoDefaultLinksAreSame() {
		Link link1 = Links.getLink(MOCK_URI);
		Link link2 = Links.getLink(MOCK_URI);
		assertThat(link1, sameInstance(link2));
	}

	@Test
	public void namedNotDefault() {
		Link link1 = Links.getLink(MOCK_URI);
		Link link2 = Links.getLink(MOCK_URI + "?name=another");
		assertThat(link1, not(sameInstance(link2)));
	}

	@Test
	public void defaultNamedIsSameAsDefault() {
		Link link1 = Links.getLink(MOCK_URI);
		Link link2 = Links.getLink(MOCK_URI + "?name=default");
		assertThat(link1, sameInstance(link2));
	}

	@Test
	public void differentNameAreDifferentMocks() {
		Link link1 = Links.getLink(MOCK_URI + "?name=A");
		Link link2 = Links.getLink(MOCK_URI + "?name=B");
		assertThat(link1, not(sameInstance(link2)));
	}

}
