package ardulink.ardumailng.test;

import static org.mockito.Mockito.mock;

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.linkmanager.LinkConfig;
import com.github.pfichtner.ardulink.core.linkmanager.LinkFactory;

public class MockLinkFactory implements LinkFactory<LinkConfig> {

	@Override
	public String getName() {
		return "mock";
	}

	@Override
	public Link newLink(LinkConfig config) {
		return mock(Link.class);
	}

	@Override
	public LinkConfig newLinkConfig() {
		return new LinkConfig() {
		};
	}

}
