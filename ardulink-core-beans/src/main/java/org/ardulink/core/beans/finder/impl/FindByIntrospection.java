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

import static java.beans.Introspector.getBeanInfo;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.ardulink.core.beans.finder.impl.ReadMethod.isReadMethod;
import static org.ardulink.core.beans.finder.impl.WriteMethod.isWriteMethod;
import static org.ardulink.util.Throwables.propagate;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.stream.Stream;

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

	public static final FindByIntrospection instance = new FindByIntrospection();

	private FindByIntrospection() {
		super();
	}

	@Override
	public Iterable<AttributeReader> listReaders(Object bean) {
		try {
			return propertyDescriptors(bean).filter(p -> isReadMethod(p.getReadMethod()))
					.map(pd -> new ReadMethod(bean, pd.getName(), pd.getReadMethod())).collect(toList());
		} catch (IntrospectionException e) {
			throw propagate(e);
		}
	}

	@Override
	public Iterable<AttributeWriter> listWriters(Object bean) {
		try {
			return propertyDescriptors(bean).filter(p -> isWriteMethod(p.getWriteMethod()))
					.map(pd -> new WriteMethod(bean, pd.getName(), pd.getWriteMethod())).collect(toList());
		} catch (IntrospectionException e) {
			throw propagate(e);
		}
	}

	private Stream<PropertyDescriptor> propertyDescriptors(Object bean) throws IntrospectionException {
		return stream(getBeanInfo(bean.getClass()).getPropertyDescriptors());
	}

}