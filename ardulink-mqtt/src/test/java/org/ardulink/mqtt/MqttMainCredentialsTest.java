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

import static org.ardulink.mqtt.MqttMain.PASSWORD_ENV_NAME;
import static org.ardulink.mqtt.MqttMain.USER_ENV_NAME;
import static org.assertj.core.api.Assertions.assertThat;

import org.ardulink.mqtt.MqttCamelRouteBuilder.MqttConnectionProperties;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearEnvironmentVariable;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

class MqttMainCredentialsTest {

	@Test
	@ClearEnvironmentVariable(key = USER_ENV_NAME)
	@ClearEnvironmentVariable(key = PASSWORD_ENV_NAME)
	void credentialsAreOnlyAppliedWhenUserAndPasswordAreSet() {
		MqttMain main = new MqttMain(new CommandLineArguments());

		MqttConnectionProperties properties = new MqttConnectionProperties().name("someName").brokerHost("someHost")
				.brokerPort(1883);

		assertThat(main.appendAuth(properties).hasAuth()).isFalse();
	}

	@Test
	@SetEnvironmentVariable(key = USER_ENV_NAME, value = "someUser")
	@ClearEnvironmentVariable(key = PASSWORD_ENV_NAME)
	void partialCredentialsAreIgnored() {
		MqttMain main = new MqttMain(new CommandLineArguments());

		MqttConnectionProperties properties = new MqttConnectionProperties().name("someName").brokerHost("someHost")
				.brokerPort(1883);

		assertThat(main.appendAuth(properties).hasAuth()).isFalse();
	}

	@Test
	@SetEnvironmentVariable(key = USER_ENV_NAME, value = "someUser")
	@SetEnvironmentVariable(key = PASSWORD_ENV_NAME, value = "somePassword")
	void credentialsAreAppliedWhenUserAndPasswordAreSet() {
		MqttMain main = new MqttMain(new CommandLineArguments());

		MqttConnectionProperties properties = new MqttConnectionProperties().name("someName").brokerHost("someHost")
				.brokerPort(1883);

		assertThat(main.appendAuth(properties).hasAuth()).isTrue();
	}

}
