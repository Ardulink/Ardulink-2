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

package org.ardulink.core.featureflags;

import static java.lang.System.getProperty;
import static org.ardulink.util.Strings.nullOrEmpty;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public final class PreviewFeature {

	/**
	 * To enable Firmata protocol (work in progress) set the system property
	 * {@value #FIRMATA_ENABLED_PROPERTY_FEATURE} to any non-empty value.
	 */
	public static final String FIRMATA_ENABLED_PROPERTY_FEATURE = "protocol.firmata.enabled";

	/**
	 * To enable SimpleProtocol in general set the system property
	 * {@value #SIMPLE_PROTOCOL_ENABLED_PROPERTY_FEATURE} to any non-empty value.
	 * 
	 * That way you can use SimpleProtocol in general and not only within the
	 * DigisparkConnection.
	 */
	public static final String SIMPLE_PROTOCOL_ENABLED_PROPERTY_FEATURE = "protocol.simple.enabled";

	/**
	 * To enable listing of <code>/dev/serial-by-id</code> serial links set the
	 * system property {@value #SERIAL_LINKS_BY_ID_ENABLED_PROPERTY_FEATURE} to any
	 * non-empty value.
	 */
	public static final String SERIAL_LINKS_BY_ID_ENABLED_PROPERTY_FEATURE = "with.serial.links.byid";

	private PreviewFeature() {
		super();
	}

	private static boolean isPropertySet(String property) {
		return !nullOrEmpty(getProperty(property));
	}

	public static boolean isFirmataProtocolFeatureEnabled() {
		return isPropertySet(FIRMATA_ENABLED_PROPERTY_FEATURE);
	}

	public static boolean isSimpleProtocolFeatureEnabledInGeneral() {
		return isPropertySet(SIMPLE_PROTOCOL_ENABLED_PROPERTY_FEATURE);
	}

	public static boolean isSerialLinksByIdFeatureEnabled() {
		return isPropertySet(SERIAL_LINKS_BY_ID_ENABLED_PROPERTY_FEATURE);
	}

}
