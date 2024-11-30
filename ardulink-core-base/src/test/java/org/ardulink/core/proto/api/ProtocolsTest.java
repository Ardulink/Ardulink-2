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
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.ardulink.core.proto.impl.ArdulinkProtocol2;
import org.ardulink.core.proto.impl.DummyProtocol;
import org.ardulink.core.proto.impl.InactiveProtocol;
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
		assertSoftly(s -> {
			s.assertThat(protoByName(DummyProtocol.NAME)).isExactlyInstanceOf(DummyProtocol.class);
			s.assertThat(tryProtoByName(DummyProtocol.NAME))
					.hasValueSatisfying(t -> assertThat(t).isExactlyInstanceOf(DummyProtocol.class));
		});
	}

	@Test
	void getByNameThrowsExceptionOnUnknownProtocolNames() {
		String unknownProto = "XXXnonExistingProtocolNameXXX";
		assertSoftly(s -> {
			s.assertThatRuntimeException().isThrownBy(() -> protoByName(unknownProto))
					.withMessageContainingAll(unknownProto, ArdulinkProtocol2.NAME, DummyProtocol.NAME);
			s.assertThat(tryProtoByName(unknownProto)).isEmpty();
		});
	}

	@Test
	void inactiveProtcolGetsFilteredOut() {
		assert verifyInactiveProtocolIsLoadableAtAll();
		assertThat(tryProtoByName(InactiveProtocol.NAME)).isEmpty();
	}

	private static boolean verifyInactiveProtocolIsLoadableAtAll() {
		InactiveProtocol.isActive.set(true);
		try {
			return tryProtoByName(InactiveProtocol.NAME).isPresent();
		} finally {
			InactiveProtocol.isActive.set(false);
		}
	}

}
