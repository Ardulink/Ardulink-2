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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.ardulink.core.proto.impl.ArdulinkProtocol2;
import org.ardulink.core.proto.impl.DummyProtocol;
import org.junit.jupiter.api.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class ProtocolsTest {

	@Test
	void defaultAndDummyProtocolsAreRegistered() {
		assertThat(Protocols.names()).containsExactlyInAnyOrder(ArdulinkProtocol2.NAME, DummyProtocol.NAME);
	}

	@Test
	void canLoadByName() {
		assertThat(Protocols.getByName(DummyProtocol.NAME)).isExactlyInstanceOf(DummyProtocol.class);
	}

	@Test
	void getByNameThrowsExceptionOnUnknownProtocolNames() {
		String unknownProto = "XXXnonExistingProtocolNameXXX";
		assertThat(Protocols.tryByName(unknownProto)).isEmpty();
		assertThat(assertThrows(RuntimeException.class, () -> Protocols.getByName(unknownProto)))
				.hasMessageContaining(unknownProto);
	}

}
