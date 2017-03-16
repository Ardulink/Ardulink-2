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

import static java.util.regex.Pattern.quote;
import static org.ardulink.util.LoadStream.asString;
import static org.ardulink.util.Throwables.propagate;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;

import org.ardulink.util.Throwables;

public class LuaProtoBuilder {

	public static final String PIN = "PIN";
	public static final String STATE = "STATE";
	public static final String INTENSITY = "INTENSITY";
	public static final String VALUES = "VALUES";

	public enum LuaProtocolKey {
		POWER_PIN_SWITCH("gpio.mode(" + var(PIN) + ",gpio.OUTPUT) gpio.write("
				+ var(PIN) + ",gpio." + var(STATE) + ")",
				new PowerPinSwitchMapper()), //
		POWER_PIN_INTENSITY("pwm.setup(" + var(PIN) + ",1000,1023) pwm.start("
				+ var(PIN) + ") pwm.setduty(" + var(PIN) + "," + var(INTENSITY)
				+ ")", new PowerPinIntensityMapper()), //
		CUSTOM_MESSAGE(var(VALUES), new CustomMessageMapper()), //
		START_LISTENING_DIGITAL(
				loadSnippet("StartListeningDigitalTemplate.snippet"),
				new StartListeningDigitalMapper()), //
		STOP_LISTENING_DIGITAL("gpio.mode(" + var(PIN) + ",gpio.OUTPUT)",
				new StopListeningDigitalMapper()); //

		private String messageTemplate;
		private Mapper mapper;

		private LuaProtocolKey(String messageTemplate, Mapper mapper) {
			this.messageTemplate = messageTemplate;
			this.mapper = mapper;
		}

		public String getMessageTemplate() {
			return messageTemplate;
		}

		public Mapper getMapper() {
			return mapper;
		}
	}

	private static String loadSnippet(String snippet) {
		InputStream is = LuaProtoBuilder.class.getResourceAsStream(snippet);
		String content = asString(is);
		// Scripts on more than on line cause random error on NodeMCU
		// because its echo
		// We should investigate on ESPlorer code to understand how
		// improve this code.
		// Actually we remove CR and LF sending the script on a single
		// line.
		content = content.replaceAll("\\r", " ");
		content = content.replaceAll("\\n", " ");
		try {
			is.close();
		} catch (IOException e) {
			propagate(e);
		}
		return content;
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

	private static String var(String name) {
		return "${" + name + "}";
	}

	public String build() {
		String message = key.getMessageTemplate();
		for (Entry<String, String> entry : key.getMapper()
				.buildMap(pin, values).entrySet()) {
			message = message.replaceAll(quote(var(entry.getKey())),
					entry.getValue());
		}
		return message;
	}

}
