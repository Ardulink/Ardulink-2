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

package com.github.pfichtner.beans.finder.impl;

import static java.lang.reflect.Modifier.isPublic;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.github.pfichtner.beans.finder.api.AttributeFinder;

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
	public Iterable<FieldAccess> listReaders(Object bean) throws Exception {
		return find(bean);
	}

	@Override
	public Iterable<FieldAccess> listWriters(Object bean) throws Exception {
		return find(bean);
	}

	private Iterable<FieldAccess> find(Object bean) {
		List<FieldAccess> accessors = new ArrayList<FieldAccess>();
		for (Field field : bean.getClass().getDeclaredFields()) {
			if (isPublic(field.getModifiers())) {
				accessors.add(new FieldAccess(bean, field.getName(), field));
			}
		}
		return accessors;
	}

}
