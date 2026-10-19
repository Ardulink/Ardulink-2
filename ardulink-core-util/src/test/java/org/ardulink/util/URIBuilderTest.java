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

package org.ardulink.util;

import static org.ardulink.util.URIBuilder.uriBuilder;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class URIBuilderTest {

	String base = "ardulink://serial-jssc";

	@Test
	void simpleURI() throws URISyntaxException {
		URI uri = uriBuilder(base).build();
		assertThat(uri).isEqualTo(new URI(base));
	}

	@Test
	void singleParam() throws URISyntaxException {
		URI uri = uriBuilder(base).param("port", "COM3").build();
		assertThat(uri).isEqualTo(new URI(base + "?port=COM3"));
	}

	@Test
	void queryURIWithSpaceChar() throws URISyntaxException {
		URI uri = uriBuilder(base).param("port", "COM3").param("name with spaces", "value with spaces").build();
		assertThat(uri).isEqualTo(new URI(base + "?port=COM3&name+with+spaces=value+with+spaces"));
	}

	@Test
	void queryURIWithMap() throws URISyntaxException {
		Map<String, Object> params = new LinkedHashMap<>();
		params.put("port", "COM3");
		params.put("someInt", 42);
		params.put("someBool", true);
		params.put("name with spaces", "value with spaces");
		URI uri = uriBuilder(base).params(params).build();
		assertThat(uri).isEqualTo(new URI( //
				base + "?port=COM3" //
						+ "&someInt=42" //
						+ "&someBool=true" //
						+ "&name+with+spaces=value+with+spaces" //
		));
	}

}
