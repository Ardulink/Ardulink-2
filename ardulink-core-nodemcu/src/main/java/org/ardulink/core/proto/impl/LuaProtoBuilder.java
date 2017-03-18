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
package org.ardulink.core.proto.impl;

import static java.lang.Boolean.TRUE;
import static java.util.regex.Pattern.quote;
import static org.ardulink.util.LoadStream.asString;
import static org.ardulink.util.Preconditions.checkArgument;
import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Preconditions.checkState;
import static org.ardulink.util.Throwables.propagate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.ardulink.util.Joiner;

public class LuaProtoBuilder {

	private static enum TemplateVariables {

		PIN("${PIN}"), STATE("${STATE}"), INTENSITY("${INTENSITY}"), VALUES(
				"${VALUES}");

		private final String quoted;

		private TemplateVariables(String nameInTemplate) {
			this.quoted = quote(nameInTemplate);
		}

	}

	public enum LuaProtocolKey {
		POWER_PIN_SWITCH {
			@Override
			public String message(LuaProtoBuilder builder) {
				checkNotNull(builder.pin, "pin has to be specified");
				checkNotNull(builder.values, "value has to be specified");
				checkArgument(builder.values.length == 1,
						"Exactly one value expected but found %s",
						builder.values.length);
				Object value = builder.values[0];
				checkArgument(value instanceof Boolean,
						"value not a Boolean but %s", value.getClass()
								.getName());

				String state = TRUE.equals(value) ? "HIGH" : "LOW";
				return String.format(
						"gpio.mode(%s,gpio.OUTPUT) gpio.write(%s,gpio.%s)",
						builder.pin, builder.pin, state);

			}
		}, //
		POWER_PIN_INTENSITY {
			@Override
			public String message(LuaProtoBuilder builder) {
				checkNotNull(builder.pin, "pin has to be specified");
				checkNotNull(builder.values, "value has to be specified");
				checkArgument(builder.values.length == 1,
						"Exactly one value expected but found %s",
						builder.values.length);
				Object value = builder.values[0];
				checkArgument(value instanceof Integer,
						"value not an Integer but %s", value.getClass()
								.getName());
				String intensity = String.valueOf(value);
				return String
						.format("pwm.setup(%s,1000,1023) pwm.start(%s) pwm.setduty(%s,%s)",
								builder.pin, builder.pin, builder.pin,
								intensity);
			}
		}, //
		CUSTOM_MESSAGE {
			private final Joiner joiner = Joiner.on(" ");

			@Override
			public String message(LuaProtoBuilder builder) {
				checkState(builder.pin == null, "pin must not specified");
				checkNotNull(builder.values, "value has to be specified");
				checkArgument(builder.values.length > 0,
						"value contains no data");
				return joiner.join(Arrays.asList(builder.values));

			}
		}, //
		START_LISTENING_DIGITAL {

			private final String snippet = loadSnippet("StartListeningDigitalTemplate.snippet");

			@Override
			public String message(LuaProtoBuilder builder) {
				checkState(builder.values == null, "value must not specified");
				checkNotNull(builder.pin, "pin has to be specified");
				String message = snippet;
				message = message.replaceAll(TemplateVariables.PIN.quoted,
						String.valueOf(builder.pin));
				return message;
			}
		}, //
		STOP_LISTENING_DIGITAL {
			@Override
			public String message(LuaProtoBuilder builder) {
				checkNotNull(builder.pin, "pin has to be specified");
				checkState(builder.values == null, "value must not specified");

				return String.format("gpio.mode(%s,gpio.OUTPUT)", builder.pin);
			}
		}; //

		public abstract String message(LuaProtoBuilder luaProtoBuilder);

		private static String loadSnippet(String snippet) {
			InputStream is = LuaProtoBuilder.class.getResourceAsStream(snippet);
			// Scripts on more than on line cause random error on NodeMCU
			// because its echo
			// We should investigate on ESPlorer code to understand how
			// improve this code.
			// Actually we remove CR and LF sending the script on a single
			// line.
			String content = asString(is).replaceAll("\\r", " ").replaceAll(
					"\\n", " ");
			try {
				is.close();
			} catch (IOException e) {
				throw propagate(e);
			}
			return content;
		}
	}

	public static LuaProtoBuilder getBuilder(LuaProtocolKey key) {
		return new LuaProtoBuilder(key);
	}

	private final LuaProtocolKey key;
	private Integer pin;
	private Object[] values;

	public LuaProtoBuilder(LuaProtocolKey key) {
		this.key = key;
	}

	public LuaProtoBuilder forPin(int pinNum) {
		this.pin = pinNum;
		return this;
	}

	public LuaProtoBuilder withValue(Object value) {
		return withValues(value);
	}

	public LuaProtoBuilder withValues(Object... values) {
		this.values = values;
		return this;
	}

	public String build() {
		return key.message(this);
	}

}
