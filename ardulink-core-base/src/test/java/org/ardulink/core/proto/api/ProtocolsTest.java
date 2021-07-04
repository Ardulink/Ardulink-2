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

package org.ardulink.core.proto.api;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.HashSet;

import org.ardulink.core.messages.api.FromDeviceMessage;
import org.ardulink.core.messages.api.FromDeviceMessageCustom;
import org.ardulink.core.messages.api.FromDeviceMessageReply;
import org.ardulink.core.proto.impl.ArdulinkProtocol2;
import org.junit.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ProtocolsTest {

	@Test
	public void defaultAndDummyProtocolsAreRegistered() {
		assertThat(
				new HashSet<String>(Protocols.names()),
				is(new HashSet<String>(Arrays.asList("ardulink2", "dummyProto"))));
	}
	
	@Test
	public void ardulinkProtocol2ReceiveCustomEvent() {
		Protocol protocol = ArdulinkProtocol2.instance();
		
		String message = "alp://cevnt/foo=w/some=42";
		
		FromDeviceMessage fromDevice = protocol.fromDevice(message.getBytes());
		
		assertThat(fromDevice, instanceOf(FromDeviceMessageCustom.class));
		assertEquals(((FromDeviceMessageCustom)fromDevice).getMessage(), "foo=w/some=42");
		
	}

	@Test
	public void ardulinkProtocol2ReceiveRply() {
		Protocol protocol = ArdulinkProtocol2.instance();
		
		String message = "alp://rply/ok?id=1&UniqueID=456-2342-2342&ciao=boo";
				
		FromDeviceMessage fromDevice = protocol.fromDevice(message.getBytes());
		
		assertThat(fromDevice, instanceOf(FromDeviceMessageReply.class));
		assertEquals(((FromDeviceMessageReply)fromDevice).isOk(), true);
		assertEquals(((FromDeviceMessageReply)fromDevice).getId(), 1);
		assertEquals(((FromDeviceMessageReply)fromDevice).getParameters().get("UniqueID"), "456-2342-2342");
		assertEquals(((FromDeviceMessageReply)fromDevice).getParameters().get("ciao"), "boo");
	}

}
