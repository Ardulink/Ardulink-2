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
package org.ardulink.camel;

import static java.lang.Character.toUpperCase;
import static java.lang.Integer.parseInt;
import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.UriEndpointComponent;
import org.apache.camel.spi.UriEndpoint;
import org.ardulink.core.Pin;
import org.ardulink.util.Optional;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
@UriEndpoint(scheme = "ardulink", syntax = "ardulink:type", title = "Ardulink Link")
public class ArdulinkComponent extends UriEndpointComponent {

	public ArdulinkComponent() {
		super(ArdulinkEndpoint.class);
	}

	@Override
	protected Endpoint createEndpoint(String uri, String remaining,
			Map<String, Object> parameters) throws Exception {
		EndpointConfig config = new EndpointConfig()
				.type(remaining)
				.listenTo(parsePins(getOptional(parameters, "listenTo").or("")))
				.linkParams(parameters);
		parameters.clear();
		ArdulinkEndpoint endpoint = new ArdulinkEndpoint(uri, this, config);
		setProperties(endpoint, parameters);
		return endpoint;
	}

	private Set<Pin> parsePins(String pinsString) {
		Set<Pin> pins = new LinkedHashSet<Pin>();
		for (StringTokenizer tokenizer = new StringTokenizer(pinsString, ","); tokenizer
				.hasMoreTokens();) {
			pins.add(toPin(tokenizer.nextToken().trim()));
		}
		return pins;
	}

	private static Pin toPin(String pin) {
		if (pin.length() >= 2) {
			char pinType = toUpperCase(pin.charAt(0));
			int num = parseInt(pin.substring(1));
			if (pinType == 'A') {
				return analogPin(num);
			} else if (pinType == 'D') {
				return digitalPin(num);
			}
		}
		throw new IllegalStateException("Cannot parse " + pin + " as pin");
	}

	private Optional<String> getOptional(Map<String, Object> parameters,
			String key) {
		return parameters.containsKey(key) ? Optional.of(String
				.valueOf(parameters.remove(key))) : Optional.<String> absent();
	}

}
