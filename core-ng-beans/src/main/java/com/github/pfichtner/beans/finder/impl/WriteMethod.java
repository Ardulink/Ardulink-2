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

import com.github.pfichtner.beans.Attribute.AttributeWriter;

public class WriteMethod implements AttributeWriter {

	private final Object bean;
	private final String name;
	private final Method writeMethod;

	public WriteMethod(Object bean, String name, Method writeMethod) {
		this.bean = bean;
		this.name = name;
		this.writeMethod = writeMethod;
	}

	@Override
	public void setValue(Object value) throws IllegalArgumentException,
			IllegalAccessException, InvocationTargetException {
		writeMethod.invoke(bean, value);
	}

	@Override
	public Class<?> getType() {
		return writeMethod.getParameterTypes()[0];
	}

	@Override
	public String getName() {
		return this.name;
	}
	
	@Override
	public Annotation[] getAnnotations() {
		return this.writeMethod.getAnnotations();
	}

	public static boolean isWriteMethod(Method method) {
		return method != null && isPublic(method.getModifiers())
				&& method.getParameterTypes().length == 1;
	}

}