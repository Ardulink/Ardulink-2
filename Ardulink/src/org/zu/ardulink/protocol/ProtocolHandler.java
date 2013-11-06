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

package org.zu.ardulink.protocol;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

/**
 * [ardulinktitle]
 * Class to manage the protocol available set. With this class you can install (and set as preferred) a new protocol for ardulink.
 * For instance you can supply a Firmata protocol implementation and install it.
 * A protocol for Ardulink must implements IProtocol interface.
 * 
 * At the moment the only ready protocol is ALProtocol (Ardulink Protocol)
 * 
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * @see IProtocol
 * @see ALProtocol
 * 
 * [adsense]
 *
 */
public class ProtocolHandler {
	
	private static IProtocol currentProtocolImplementation = null;
	private static Map<String, IProtocol> installedProtocolImplementations = new Hashtable<String, IProtocol>();

	static {
		ALProtocol alProtocol = new ALProtocol();
		installedProtocolImplementations.put(alProtocol.getProtocolName(), alProtocol);
		currentProtocolImplementation = alProtocol;
	}

	/**
	 * @return the current selected protocol (the preferred one)
	 */
	public static IProtocol getCurrentProtocolImplementation() {
		return currentProtocolImplementation;
	}

	/**
	 * @param implementationName
	 * @return return the protocol implementation called implementationName (if installed)
	 */
	public static IProtocol getProtocolImplementation(String implementationName) {
		return installedProtocolImplementations.get(implementationName);
	}

	/**
	 * @return all available protocol implementation
	 */
	public static Set<String> getInstalledProtocolImplementationNames() {
		return installedProtocolImplementations.keySet();
	}

	/**
	 * Select a protocol. Set it as preferred.
	 * @param implementationName
	 * @return true if protocol is found, if is returned false then remain valid the last selected protocol.
	 */
	public static boolean setCurrentProtocolImplementation(String implementationName) {
		boolean retvalue = false;
		IProtocol selected = installedProtocolImplementations.get(implementationName);
		if(selected != null) {
			currentProtocolImplementation = selected;
			retvalue = true;
		}
		return retvalue;
	}
	
	/**
	 * Install a protocol.
	 * @param protocol
	 * @return
	 */
	public static boolean installProtocolImplementation(IProtocol protocol) {
		boolean retvalue = false;
		if(protocol != null) {
			installedProtocolImplementations.put(protocol.getProtocolName(), protocol);
			retvalue = true;
		}
		return retvalue;
	}
}
