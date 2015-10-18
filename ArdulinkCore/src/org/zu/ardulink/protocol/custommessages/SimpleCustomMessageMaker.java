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
package org.zu.ardulink.protocol.custommessages;


/**
 * [ardulinktitle] [ardulinkversion]
 * This is a simple implementation for CustomMessageMaker interface
 * custom messages.
 *
 * @author Luciano Zu
 * @see CustomMessageSender
 * @see CustomMessageMaker
 * 
 * [adsense]
 */
public class SimpleCustomMessageMaker implements CustomMessageMaker {

	@Override
	public String getCustomMessage(String... args) {
		
		StringBuilder builder = new StringBuilder();
		if(args.length > 0) {
			for (int i = 0; i < args.length - 1; i++) {
				builder.append(args[i]);
				builder.append("/");
			}
			builder.append(args[args.length - 1]);
		}

		String retvalue = builder.toString(); 
		return retvalue;
	}
}
