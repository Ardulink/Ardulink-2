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
package org.ardulink.extendedfeatures.deviceid;

import java.io.IOException;
import java.util.UUID;

import org.ardulink.core.Link;
import org.ardulink.core.convenience.Links;
import org.ardulink.core.events.RplyEvent;
import org.ardulink.core.qos.ResponseAwaiter;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/ This XFeature is able to retrieve
 * the Unique ID from an Arduino based board. If arduino doesn't have an unique
 * id this class suggests an id. Then in the reply message between parameters
 * DeviceID searches for a UniqueID parameter.
 * 
 * [adsense]
 *
 */
public class DeviceID {

	public static void main(String[] args) throws IOException {
		Link link = Links.getDefault();

		RplyEvent rplyEvent = ResponseAwaiter.onLink(link).waitForResponse(
				link.sendCustomMessage("getUniqueID", UUID.randomUUID()
						.toString()));

		System.out.println("UniqueID " + rplyEvent.getParameterValue(""));

	}

}
