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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;

import com.github.pfichtner.beans.Attribute.AttributeReader;
import com.github.pfichtner.beans.Attribute.AttributeWriter;

public class FieldAccess implements AttributeReader, AttributeWriter {

	private final Object bean;
	private final String name;
	private final Field field;

	public FieldAccess(Object bean, String name, Field field) {
		this.bean = bean;
		this.name = name;
		this.field = field;
	}

	@Override
	public Object getValue() throws IllegalArgumentException,
			IllegalAccessException {
		return this.field.get(this.bean);
	}

	@Override
	public void setValue(Object value) throws IllegalArgumentException,
			IllegalAccessException {
		this.field.set(this.bean, value);
	}

	@Override
	public Class<?> getType() {
		return this.field.getType();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void addAnnotations(Collection<Annotation> annotations) {
		Collections.addAll(annotations, this.field.getAnnotations());
	}

}