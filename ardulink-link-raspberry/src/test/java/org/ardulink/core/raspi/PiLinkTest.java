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

package org.ardulink.core.raspi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.condition.OS.LINUX;

import java.io.IOException;

import org.ardulink.core.Link;
import org.ardulink.core.convenience.Links;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.EnabledOnOs;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class PiLinkTest {

	@Test
	@DisabledOnOs(value = LINUX, architectures = "aarch64")
	void creatingInstanceWillFailOnX86withUnsatisfiedLinkError() {
		assertThatExceptionOfType(ExceptionInInitializerError.class)
				.isThrownBy(() -> Links.getLink("ardulink://raspberry"))
				.withRootCauseExactlyInstanceOf(IllegalStateException.class);
	}

	@Test
	@EnabledOnOs(value = LINUX, architectures = "aarch64")
	void creatingInstanceWillWorkOnLinuxAarch64() throws IOException {
		try (Link link = Links.getLink("ardulink://raspberry")) {
			assertThat(link).isNotNull();
		}
	}

}
