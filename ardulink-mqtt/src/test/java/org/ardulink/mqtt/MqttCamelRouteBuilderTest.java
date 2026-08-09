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

package org.ardulink.mqtt;

import static org.assertj.core.api.Assertions.assertThat;

import org.ardulink.mqtt.MqttCamelRouteBuilder.MqttConnectionProperties;
import org.ardulink.mqtt.Topics;
import org.junit.jupiter.api.Test;

class MqttCamelRouteBuilderTest {

	@Test
	void uriWithoutCredentialsDoesNotChange() {
		MqttConnectionProperties properties = new MqttConnectionProperties().name("someName").brokerHost("someHost")
				.brokerPort(1883);

		assertThat(properties.buildCamelURI(Topics.basedOn("topic1"))).isEqualTo(
				"paho:topic1/#?brokerUrl=tcp://someHost:1883&automaticReconnect=false&maxInflight=65535&clientId=someName&qos=0");
	}

	@Test
	void uriWithCredentialsDoesNotExposeCredentials() {
		MqttConnectionProperties properties = new MqttConnectionProperties().name("someName").brokerHost("someHost")
				.brokerPort(1883).user("topSecretUser").password("topSecretPassword".getBytes());

		String uri = properties.buildCamelURI(Topics.basedOn("topic1"));

		assertThat(uri).doesNotContain("topSecretUser") //
				.doesNotContain("topSecretPassword") //
				.doesNotContain("userName") //
				.doesNotContain("password");
	}
}
