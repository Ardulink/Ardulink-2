/**
Copyright 2013 Luciano Zu project Ardulink http://www.ardulink.org/

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

@author Luciano Zu
 */

package org.zu.ardulink.mail.server.links.configuration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.xml.bind.annotation.XmlElement;

import org.zu.ardulink.util.Primitive;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class AParameter {

	private String type;
	private String value;

	@XmlElement(name = "type", required = true)
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@XmlElement(name = "value", required = true)
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	// begin business methods

	public Class<?> getClassType() throws ClassNotFoundException {
		Primitive primitive = Primitive.forClassName(type);
		return primitive == null ? getClass().getClassLoader().loadClass(type)
				: primitive.getType();
	}

	public Object getValueForClass() throws ClassNotFoundException,
			IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		Class<?> parameterType = getClassType();
		return getValueForClass(parameterType);
	}

	public Object getValueForClass(Class<?> parameterType)
			throws IllegalArgumentException, InstantiationException,
			IllegalAccessException, InvocationTargetException {
		Object object = Primitive.parseAs(parameterType, getValue());
		if (object != null) {
			return object;
		}
		Constructor<?> constructor = findConstructor4Parameter(
				parameterType.getConstructors(), getValue());
		return constructor.newInstance(getValue());
	}

	/**
	 * Return the default constructor if value is null a constructor with just a
	 * parameter otherwise
	 * 
	 * @param constructors
	 * @param value
	 */
	@SuppressWarnings("rawtypes")
	private Constructor findConstructor4Parameter(Constructor[] constructors,
			Object value) {
		Constructor retvalue = null;
		for (Constructor constructor : constructors) {
			Class[] parameterTypes = constructor.getParameterTypes();
			if (value == null && parameterTypes.length == 0) {
				retvalue = constructor;
				break; // ...hhhmmm... I don't like this!
			} else {
				if (value != null
						&& parameterTypes.length == 1
						&& ((Class<?>) parameterTypes[0])
								.isAssignableFrom(value.getClass())) {
					retvalue = constructor;
					break; // ...hhhmmm... I don't like this!
				}
			}
		}

		return retvalue;
	}

	// end business methods

}
