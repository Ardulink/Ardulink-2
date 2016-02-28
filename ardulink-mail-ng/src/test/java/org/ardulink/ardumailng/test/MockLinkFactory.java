/**
Copyright 2013 project Ardulink http://www.ardulink.org/
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

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
		@Named("foo")
		private String foo;

		public int getNum() {
			return num;
		}

		public void setNum(int num) {
			this.num = num;
		}

		public String getFoo() {
			return foo;
		}

		public void setFoo(String foo) {
			this.foo = foo;
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
