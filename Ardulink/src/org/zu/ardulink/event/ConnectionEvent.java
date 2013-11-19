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

package org.zu.ardulink.event;

import java.util.Date;

/**
 * [ardulinktitle] [ardulinkversion]
 * Event raised when Network connection occurs.
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ConnectionEvent {
	private String networkId = null;
	private String portName = null;
	private Date timestamp = new Date();

	public ConnectionEvent() {
	}
	
	public ConnectionEvent(String networkId) {
		this.networkId = networkId;
	}
	
	public ConnectionEvent(String networkId, String portName) {
		this.networkId = networkId;
		this.portName = portName;
	}

	public String getNetworkId() {
		return networkId;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public String getPortName() {
		return portName;
	}
}
