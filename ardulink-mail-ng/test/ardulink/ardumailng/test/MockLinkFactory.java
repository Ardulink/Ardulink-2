package ardulink.ardumailng.test;

import static org.mockito.Mockito.mock;
import ardulink.ardumailng.test.MockLinkFactory.MockLinkConfig;

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.linkmanager.LinkConfig;
import com.github.pfichtner.ardulink.core.linkmanager.LinkFactory;

public class MockLinkFactory implements LinkFactory<MockLinkConfig> {

	public static class MockLinkConfig implements LinkConfig {
		@Named("num")
		private int num;

		public int getNum() {
			return num;
		}

		public void setNum(int num) {
			this.num = num;
		}
	}

	@Override
	public String getName() {
		return "mock";
	}

	@Override
	public Link newLink(MockLinkConfig config) {
		return mock(Link.class);
	}

	@Override
	public MockLinkConfig newLinkConfig() {
		return new MockLinkConfig();
	}

}
