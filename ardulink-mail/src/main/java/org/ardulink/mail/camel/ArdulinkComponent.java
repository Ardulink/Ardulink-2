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

package org.ardulink.mail.camel;

import static java.lang.Boolean.parseBoolean;
import static org.ardulink.mail.Commands.switchAnalogPin;
import static org.ardulink.mail.Commands.switchDigitalPin;
import static org.ardulink.util.Integers.tryParse;
import static org.ardulink.util.Preconditions.checkNotNull;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.UriEndpointComponent;
import org.apache.camel.spi.UriEndpoint;
import org.ardulink.mail.Command;
import org.ardulink.util.Optional;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
/*, consumerClass = ArdulinkConsumer.class*/
@UriEndpoint(scheme = "ardulink", syntax = "ardulink:type", title = "Ardulink Link")
public class ArdulinkComponent extends UriEndpointComponent {

	private static final String SCENARIO_PREFIX = "scenario.";

	public ArdulinkComponent() {
		super(ArdulinkEndpoint.class);
	}

	@Override
	protected Endpoint createEndpoint(String uri, String remaining,
			Map<String, Object> parameters) throws Exception {

		ArdulinkEndpoint.Config config = new ArdulinkEndpoint.Config();
		config.setType(remaining);
		config.setTypeParams(getOptional(parameters, "linkparams").orNull());
		config.setValidFroms(get(parameters, "validfroms").split("\\;"));

		handleScenarios(parameters, config);

		ArdulinkEndpoint endpoint = new ArdulinkEndpoint(uri, this, config);
		setProperties(endpoint, parameters);
		return endpoint;
	}

	private void handleScenarios(Map<String, Object> parameters,
			ArdulinkEndpoint.Config config) {
		for (Iterator<Entry<String, Object>> it = parameters.entrySet()
				.iterator(); it.hasNext();) {
			Entry<String, Object> entry = it.next();
			if (entry.getKey().startsWith(SCENARIO_PREFIX)) {
				String scenario = String
						.valueOf(parameters.get(entry.getKey()));
				it.remove();
				String name = entry.getKey()
						.substring(SCENARIO_PREFIX.length());
				for (String pinAndValue : scenario.split("\\;")) {
					String[] pv = pinAndValue.split("\\=");
					String pinType = pv[0].substring(0, 1);
					int pin = checkNotNull(tryParse(pv[0].substring(1)),
							"Could not parse %s as int", pv[0].substring(1))
							.intValue();
					config.addCommand(name, createCommand(pinType, pin, pv));
				}
			}
		}
	}

	private Command createCommand(String pinType, int pin, String[] pv) {
		if ("D".equalsIgnoreCase(pinType)) {
			return (switchDigitalPin(pin, parseBoolean(pv[1])));
		} else if ("A".equalsIgnoreCase(pinType)) {
			return switchAnalogPin(pin, getInt(pv[1]));
		} else {
			throw new IllegalStateException("Unknown pin type " + pinType);
		}
	}

	private String get(Map<String, Object> parameters, String key) {
		return getOptional(parameters, key)
				.getOrThrow("%s not configured", key);
	}

	private Optional<String> getOptional(Map<String, Object> parameters,
			String key) {
		return parameters.containsKey(key) ? Optional.of(String
				.valueOf(parameters.remove(key))) : Optional.<String> absent();
	}

	private int getInt(String string) {
		return checkNotNull(tryParse(string), "Could not parse %s as int",
				string).intValue();
	}

}
