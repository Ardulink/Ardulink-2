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

package org.ardulink.util;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

class ThrowablesTest {

	private static class A extends Exception {

		private static final long serialVersionUID = 1L;

		public A(Exception cause) {
			super(cause);
		}

	}

	private static class B extends Exception {

		private static final long serialVersionUID = 1L;

		public B(Exception cause) {
			super(cause);
		}

	}

	private static class C extends Exception {

		private static final long serialVersionUID = 1L;

		public C() {
			super();
		}
	}

	C c = new C();
	B b = new B(c);
	A a = new A(b);

	@Test
	void testGetCauses() {
		assertThat(Throwables.getCauses(a).collect(toList()), is(asList(a, b, c)));
	}

	@Test
	void testGetRootCause() {
		assertThat(Throwables.getRootCause(a), is(sameInstance(c)));
	}

}
