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

import static org.ardulink.core.beans.finder.impl.ReadMethod.isReadMethod;
import static org.ardulink.core.beans.finder.impl.WriteMethod.isWriteMethod;
import static java.beans.Introspector.getBeanInfo;

import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;

import org.ardulink.core.beans.Attribute.AttributeReader;
import org.ardulink.core.beans.Attribute.AttributeWriter;
import org.ardulink.core.beans.finder.api.AttributeFinder;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class FindByIntrospection implements AttributeFinder {

	private static final FindByIntrospection instance = new FindByIntrospection();

	public static FindByIntrospection beanAttributes() {
		return instance;
	}

	private FindByIntrospection() {
		super();
	}

	@Override
	public Iterable<? extends AttributeReader> listReaders(Object bean)
			throws Exception {
		List<ReadMethod> readers = new ArrayList<ReadMethod>();
		for (PropertyDescriptor pd : getBeanInfo(bean.getClass())
				.getPropertyDescriptors()) {
			if (isReadMethod(pd.getReadMethod())) {
				readers.add(new ReadMethod(bean, pd.getName(), pd
						.getReadMethod()));
			}
		}
		return readers;
	}

	@Override
	public Iterable<? extends AttributeWriter> listWriters(Object bean)
			throws Exception {
		List<WriteMethod> writers = new ArrayList<WriteMethod>();
		for (PropertyDescriptor pd : getBeanInfo(bean.getClass())
				.getPropertyDescriptors()) {
			if (isWriteMethod(pd.getWriteMethod())) {
				writers.add(new WriteMethod(bean, pd.getName(), pd
						.getWriteMethod()));
			}
		}
		return writers;
	}

}