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

import static org.ardulink.core.proto.api.Protocols.protoByName;
import static org.ardulink.core.proto.api.Protocols.protocolNames;
import static org.ardulink.core.proto.api.Protocols.tryProtoByName;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatRuntimeException;

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
		assertThat(protocolNames()).containsExactlyInAnyOrder(ArdulinkProtocol2.NAME, DummyProtocol.NAME);
	}

	@Test
	void canLoadByName() {
		assertThat(protoByName(DummyProtocol.NAME)).isExactlyInstanceOf(DummyProtocol.class);
		assertThat(tryProtoByName(DummyProtocol.NAME))
				.hasValueSatisfying(p -> assertThat(p).isExactlyInstanceOf(DummyProtocol.class));
	}

	@Test
	void getByNameThrowsExceptionOnUnknownProtocolNames() {
		String unknownProto = "XXXnonExistingProtocolNameXXX";
		assertThat(tryProtoByName(unknownProto)).isEmpty();
		assertThatRuntimeException().isThrownBy(() -> protoByName(unknownProto)).withMessageContainingAll(unknownProto,
				ArdulinkProtocol2.NAME, DummyProtocol.NAME);
		assertThat(tryProtoByName(unknownProto)).isEmpty();
	}

}
