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
package org.ardulink.core.proto.lua;

import static java.lang.Boolean.TRUE;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.regex.Pattern.quote;
import static java.util.stream.Collectors.joining;
import static org.ardulink.util.Preconditions.checkArgument;
import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Preconditions.checkState;
import static org.ardulink.util.Throwables.propagate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LuaProtoBuilder {

	private enum TemplateVariables {

		PIN("${PIN}"), STATE("${STATE}"), INTENSITY("${INTENSITY}"), VALUES("${VALUES}");

		private final String quoted;

		TemplateVariables(String nameInTemplate) {
			this.quoted = quote(nameInTemplate);
		}

	}

	private static final String PIN_HAS_TO_BE_SPECIFIED = "pin has to be specified";
	private static final String PIN_MUST_NOT_SPECIFIED = "pin must not specified";
	private static final String VALUE_HAS_TO_BE_SPECIFIED = "value has to be specified";
	private static final String VALUE_MUST_NOT_SPECIFIED = "value must not specified";

	public enum LuaProtocolKey {

		POWER_PIN_SWITCH {
			@Override
			public String message(LuaProtoBuilder builder) {
				Integer pinNumber = checkNotNull(builder.pin, PIN_HAS_TO_BE_SPECIFIED);
				return format("gpio.mode(%s,gpio.OUTPUT) gpio.write(%s,gpio.%s)", pinNumber, pinNumber,
						TRUE.equals(firstValue(builder.values, Boolean.class)) ? "HIGH" : "LOW");

			}
		}, //
		POWER_PIN_INTENSITY {
			@Override
			public String message(LuaProtoBuilder builder) {
				Integer pinNumber = checkNotNull(builder.pin, PIN_HAS_TO_BE_SPECIFIED);
				return format("pwm.setup(%s,1000,1023) pwm.start(%s) pwm.setduty(%s,%s)", pinNumber, pinNumber,
						pinNumber, firstValue(builder.values, Integer.class));
			}
		}, //
		CUSTOM_MESSAGE {
			@Override
			public String message(LuaProtoBuilder builder) {
				checkState(builder.pin == null, PIN_MUST_NOT_SPECIFIED);
				checkArgument(checkNotNull(builder.values, VALUE_HAS_TO_BE_SPECIFIED).length > 0,
						"value contains no data");
				return stream(builder.values).map(String::valueOf).collect(joining(" "));
			}
		}, //
		START_LISTENING_DIGITAL {

			private final String snippet = loadSnippet("StartListeningDigitalTemplate.snippet");

			@Override
			public String message(LuaProtoBuilder builder) {
				checkState(builder.values == null, VALUE_MUST_NOT_SPECIFIED);
				Integer pinNumber = checkNotNull(builder.pin, PIN_HAS_TO_BE_SPECIFIED);
				return snippet.replaceAll(TemplateVariables.PIN.quoted, String.valueOf(pinNumber));
			}
		}, //
		STOP_LISTENING_DIGITAL {
			@Override
			public String message(LuaProtoBuilder builder) {
				Integer pinNumber = checkNotNull(builder.pin, PIN_HAS_TO_BE_SPECIFIED);
				checkState(builder.values == null, VALUE_MUST_NOT_SPECIFIED);
				return format("gpio.mode(%s,gpio.OUTPUT)", pinNumber);
			}
		}; //

		public abstract String message(LuaProtoBuilder luaProtoBuilder);

		private static String loadSnippet(String snippet) {
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(LuaProtoBuilder.class.getResourceAsStream(snippet)))) {
				// Scripts on more than on line cause random error on NodeMCU because its echo
				// We should investigate on ESPlorer code to understand how improve this code.
				// Actually we remove CR and LF sending the script on a single line.
				return reader.lines().map(String::trim).collect(joining(" "));
			} catch (IOException e) {
				throw propagate(e);
			}
		}
	}

	private static <T> T firstValue(Object[] values, Class<T> type) {
		return verifyType(firstValue(values), type);
	}

	private static Object firstValue(Object[] values) {
		checkArgument(checkNotNull(values, VALUE_HAS_TO_BE_SPECIFIED).length == 1,
				"Exactly one value expected but %s contains %s", values, values.length);
		return values[0];
	}

	private static <T> T verifyType(Object value, Class<T> clazz) {
		checkArgument(clazz.isInstance(value), "value not a %s but %s", clazz.getName(), value.getClass().getName());
		return clazz.cast(value);
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
