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

import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.stream.Stream;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public final class ServiceLoaders {

	private ServiceLoaders() {
		super();
	}

	public static <T> Stream<T> services(Class<T> type) {
		return toStream(ServiceLoader.load(type));
	}

	public static <T> Stream<T> services(Class<T> type, ClassLoader classloader) {
		return toStream(ServiceLoader.load(type, classloader));
	}

	private static <T> Stream<T> toStream(ServiceLoader<T> serviceLoader) {
		return serviceLoader.stream().map(Provider::get);
	}

}
