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

package org.ardulink.core.beans.finder.impl;

import static java.lang.reflect.Modifier.isPublic;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import org.ardulink.core.beans.finder.api.AttributeFinder;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class FindByFieldAccess implements AttributeFinder {

	private FindByFieldAccess() {
		super();
	}

	public static AttributeFinder directFieldAccess() {
		return new FindByFieldAccess();
	}

	@Override
	public Iterable<FieldAccess> listReaders(Object bean) {
		return find(bean);
	}

	@Override
	public Iterable<FieldAccess> listWriters(Object bean) {
		return find(bean);
	}

	private Iterable<FieldAccess> find(Object bean) {
		return stream(bean.getClass().getDeclaredFields()).filter(f -> isPublic(f.getModifiers()))
				.map(f -> new FieldAccess(bean, f.getName(), f)).collect(toList());
	}

}
