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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;

import com.github.pfichtner.beans.Attribute.AttributeReader;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ReadMethod implements AttributeReader {

	private final Object bean;
	private final String name;
	private final Method readMethod;

	public ReadMethod(Object bean, String name, Method readMethod) {
		this.bean = bean;
		this.name = name;
		this.readMethod = readMethod;
	}

	@Override
	public Object getValue() throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		return this.readMethod.invoke(this.bean);
	}

	@Override
	public Class<?> getType() {
		return this.readMethod.getReturnType();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void addAnnotations(Collection<Annotation> annotations) {
		Collections.addAll(annotations, this.readMethod.getAnnotations());
	}

	public static boolean isReadMethod(Method method) {
		return method != null && isPublic(method.getModifiers())
				&& !anObjectsMethod(method)
				&& method.getParameterTypes().length == 0;
	}

	private static boolean anObjectsMethod(Method method) {
		return method.getDeclaringClass().equals(Object.class);
	}

}