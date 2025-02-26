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
package org.ardulink.core;

import static java.lang.reflect.Proxy.newProxyInstance;
import static org.ardulink.util.Primitives.findPrimitiveFor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.ardulink.util.Primitives;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public final class NullLink {

	private NullLink() {
		super();
	}

	private static final class SelfReferentialInvocationHandler<T> implements InvocationHandler {

		private final Class<T> proxyType;
		private T proxyInstance;

		public SelfReferentialInvocationHandler(Class<T> proxyType) {
			this.proxyType = proxyType;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			Class<?> returnType = method.getReturnType();
			return returnType.equals(proxyType) ? proxyInstance
					: findPrimitiveFor(returnType).map(Primitives::defaultValue).orElse(null);
		}

	}

	public static final Link NULL_LINK = createNullLink(Link.class);

	private static <T> T createNullLink(Class<T> proxyType) {
		SelfReferentialInvocationHandler<T> invocationHandler = new SelfReferentialInvocationHandler<>(proxyType);
		T instance = proxyType
				.cast(newProxyInstance(NullLink.class.getClassLoader(), new Class[] { proxyType }, invocationHandler));
		invocationHandler.proxyInstance = instance;
		return instance;
	}

}
