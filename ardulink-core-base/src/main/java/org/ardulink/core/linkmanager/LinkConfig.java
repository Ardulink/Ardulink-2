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

package org.ardulink.core.linkmanager;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public interface LinkConfig {

	LinkConfig NO_ATTRIBUTES = new LinkConfig() {
		// no attributes
	};

	@Retention(RUNTIME)
	public @interface Named {
		String value();
	}

	@Retention(RUNTIME)
	public @interface ChoiceFor {
		/**
		 * The name of the attribute the values are choices for.
		 * 
		 * @return name of the attribute the values are choices for
		 */
		String value();

		/**
		 * Attributes this attribute depends on. If this attribute depends on no
		 * other attributes an empty array is returned.
		 * 
		 * @return attributes this attribute depends on
		 */
		String[] dependsOn() default {};
	}

	@Retention(RUNTIME)
	public @interface I18n {
		/**
		 * Name of the property file holding the i18n texts. If omitted the name
		 * has to have the same name like the LinkConfig class. In both cases
		 * the file has to resist in the same package like the LinkConfig class.
		 * 
		 * @return the name of the i18n property file
		 */
		String value() default "";
	}

}
