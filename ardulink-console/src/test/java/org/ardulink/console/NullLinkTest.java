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
package org.ardulink.console;

import static java.util.stream.IntStream.range;
import static org.ardulink.util.Primitives.findPrimitiveFor;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;

import org.ardulink.core.Link;
import org.ardulink.util.Primitives;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class NullLinkTest {

	Link sut = NullLink.NULL_LINK;

	@ParameterizedTest
	@MethodSource("declaredMethods")
	void canBeCalledAndDoesNotReturnNull(Method method) throws Exception {
		assertThat(method.invoke(sut, params(method))).isNotNull();
	}

	static Method[] declaredMethods() {
		return Link.class.getDeclaredMethods();
	}

	static Object[] params(Method method) {
		return range(0, method.getParameterCount()) //
				.mapToObj(i -> method.getParameterTypes()[i]) //
				.map(p -> defaultOrNull(p)) //
				.toArray(Object[]::new);
	}

	static Object defaultOrNull(Class<?> clazz) {
		return findPrimitiveFor(clazz).map(Primitives::defaultValue).orElse(null);
	}

}
