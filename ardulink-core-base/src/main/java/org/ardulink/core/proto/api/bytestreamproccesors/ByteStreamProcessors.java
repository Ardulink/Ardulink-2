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
package org.ardulink.core.proto.api.bytestreamproccesors;

import java.util.ArrayList;
import java.util.List;

import org.ardulink.core.messages.api.FromDeviceMessage;
import org.ardulink.core.proto.api.Protocol;
import org.ardulink.core.proto.api.bytestreamproccesors.ByteStreamProcessor.FromDeviceListener;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public final class ByteStreamProcessors {

	private ByteStreamProcessors() {
		super();
	}

	public static List<FromDeviceMessage> parse(Protocol protocol, byte[] bytes) {
		return parse(protocol.newByteStreamProcessor(), bytes);
	}

	public static List<FromDeviceMessage> parse(ByteStreamProcessor byteStreamProcessor, byte[] bytes) {
		List<FromDeviceMessage> messages = new ArrayList<>();
		FromDeviceListener listener = fromDeviceListener(messages);
		byteStreamProcessor.addListener(listener);
		try {
			byteStreamProcessor.process(bytes);
			return messages;
		} finally {
			byteStreamProcessor.removeListener(listener);
		}
	}

	private static FromDeviceListener fromDeviceListener(List<FromDeviceMessage> messages) {
		return messages::add;
	}

}
