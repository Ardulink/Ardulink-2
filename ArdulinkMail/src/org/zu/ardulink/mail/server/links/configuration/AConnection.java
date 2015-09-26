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
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import org.zu.ardulink.connection.Connection;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class AConnection {
	
	private String name;
	private String className;
	private List<AParameter> constructorParameters;
	
	private Connection connection = null;
	
	@XmlElement(name="name", required=true)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@XmlElement(name="className", required=true)
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	
	@XmlElement(name="constructorParameters", required=false)
	public List<AParameter> getConstructorParameters() {
		return constructorParameters;
	}
	public void setConstructorParameters(List<AParameter> constructorParameters) {
		this.constructorParameters = constructorParameters;
	}
	
	// begin business methods
	
	public Connection getConnection() {
		if(connection == null) {
			try {
				Class<?> connectionClass = this.getClass().getClassLoader().loadClass(className);
				Class<?>[] parameterTypes = getParamenterTypes();
				Constructor<?> constructor = connectionClass.getConstructor(parameterTypes);
				connection = (Connection)constructor.newInstance(getParamenterValues(parameterTypes));
			}
			catch(Exception e)  {
				e.printStackTrace();
			}
		}
		
		return connection;
	}
	
	private Class<?>[] getParamenterTypes() throws ClassNotFoundException {
		Class<?>[] parameterTypes = new Class[constructorParameters.size()];
		int index = 0;
		for (AParameter aParameter : constructorParameters) {
			parameterTypes[index++] = aParameter.getClassType();
		}
		return parameterTypes;
	}

	private Object[] getParamenterValues(Class<?>[] parameterTypes) throws ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Object[] parametervalues = new Object[constructorParameters.size()];
		int index = 0;
		for (AParameter aParameter : constructorParameters) {
			parametervalues[index++] = aParameter
					.getValueForClass(parameterTypes[index]);
		}
		
		return parametervalues;
	}
	

	// end business methods
	
}
