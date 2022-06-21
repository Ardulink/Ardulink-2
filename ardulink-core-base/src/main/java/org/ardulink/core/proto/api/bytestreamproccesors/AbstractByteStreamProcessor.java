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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.ardulink.core.messages.api.FromDeviceMessage;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public abstract class AbstractByteStreamProcessor implements ByteStreamProcessor {

	private final List<FromDeviceListener> listeners = new CopyOnWriteArrayList<FromDeviceListener>();

	@Override
	public void process(byte[] bytes) {
		for (byte b : bytes) {
			process(b);
		}
	}

	@Override
	public void addListener(FromDeviceListener listener) {
		listeners.add(listener);
	}
	
	@Override
	public void removeListener(FromDeviceListener listener) {
		listeners.remove(listener);
	}

	protected void fireEvent(FromDeviceMessage fromDevice) {
		for (FromDeviceListener listener : listeners) {
			listener.handle(fromDevice);
		}
	}

}