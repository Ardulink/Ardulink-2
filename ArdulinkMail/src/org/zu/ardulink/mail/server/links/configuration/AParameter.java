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

public class AParameter {
	
	private String type;
	private String value;
	
	@XmlElement(name="type", required=true)
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	@XmlElement(name="value", required=true)
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	// begin business methods
	
	public Class getClassType() throws ClassNotFoundException {
		Class retvalue;
		if("int".equals(type)) {
			retvalue = int.class;
		} else if("byte".equals(type)) {
			retvalue = byte.class;
		} else if("short".equals(type)) {
			retvalue = short.class;
		} else if("long".equals(type)) {
			retvalue = long.class;
		} else if("float".equals(type)) {
			retvalue = float.class;
		} else if("double".equals(type)) {
			retvalue = double.class;
		} else if("boolean".equals(type)) {
			retvalue = boolean.class;
		} else if("char".equals(type)) {
			retvalue = char.class;
		} else {
			retvalue = this.getClass().getClassLoader().loadClass(type);
		}
		
		return retvalue;
	}
	
	public Object getValueForClass() throws ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Class parameterType = getClassType();
		return getValueForClass(parameterType);
	}
	
	public Object getValueForClass(Class parameterType) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Object retvalue;
		if(parameterType.isAssignableFrom(int.class)) {
			retvalue = Integer.parseInt(getValue());
		} else if(parameterType.isAssignableFrom(byte.class)) {
			retvalue = Byte.parseByte(getValue());
		} else if(parameterType.isAssignableFrom(short.class)) {
			retvalue = Short.parseShort(getValue());
		} else if(parameterType.isAssignableFrom(long.class)) {
			retvalue = Long.parseLong(getValue());
		} else if(parameterType.isAssignableFrom(float.class)) {
			retvalue = Float.parseFloat(getValue());
		} else if(parameterType.isAssignableFrom(double.class)) {
			retvalue = Double.parseDouble(getValue());
		} else if(parameterType.isAssignableFrom(boolean.class)) {
			retvalue = Boolean.parseBoolean(getValue());
		} else if(parameterType.isAssignableFrom(char.class)) {
			retvalue = getValue().charAt(0);
		} else {
			Constructor constructor = findConstructor4Parameter(parameterType.getConstructors(), getValue());
			retvalue = constructor.newInstance(getValue());
		}
		
		return retvalue;
	}
	
	/**
	 * Return the default contructor if value is null a constructor with just a parameter otherwise
	 * @param constructors
	 * @param value
	 */
	@SuppressWarnings("rawtypes")
	private Constructor findConstructor4Parameter(Constructor[] constructors, String value) {
		Constructor retvalue = null;
		for (int i = 0; i < constructors.length; i++) {
			Constructor constructor = constructors[i];
			Class[] parameterTypes = constructor.getParameterTypes();
			if(value == null && parameterTypes.length == 0) {
				retvalue = constructor;
				break; // ...hhhmmm... I don't like this!
			} else if(value != null && parameterTypes.length == 1 && parameterTypes[0].isAssignableFrom(value.getClass())) {
				retvalue = constructor;
				break; // ...hhhmmm... I don't like this!
			} 
		}

		return retvalue;
	}
	
	// end business methods

}
