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

package org.ardulink.core.beans;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public interface Attribute {

	interface TypedAttributeProvider {
		String getName();

		Class<?> getType();
	}

	interface AttributeReader extends TypedAttributeProvider {
		Object getValue() throws Exception;

		void addAnnotations(Collection<Annotation> annotations);
	}

	interface AttributeWriter extends TypedAttributeProvider {
		void setValue(Object value) throws Exception;

		void addAnnotations(Collection<Annotation> annotations);
	}

	String getName();

	Class<?> getType();

	boolean canRead();

	Object readValue() throws Exception;

	boolean canWrite();

	void writeValue(Object value) throws Exception;

	Annotation[] getAnnotations();

	<T extends Annotation> T getAnnotation(Class<T> annotationClass);

}
