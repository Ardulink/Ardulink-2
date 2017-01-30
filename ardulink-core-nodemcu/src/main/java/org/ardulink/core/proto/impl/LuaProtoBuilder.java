package org.ardulink.core.proto.impl;

import java.util.Map;
import java.util.Set;

public class LuaProtoBuilder {

	public enum LuaProtocolKey {

		 POWER_PIN_SWITCH("gpio.mode(${PIN}, gpio.OUTPUT);gpio.write(${PIN}, gpio.${STATE});", new PowerPinSwitchMapper())
//		,POWER_PIN_INTENSITY("ppin")
//		,DIGITAL_PIN_READ("dred")
//		,ANALOG_PIN_READ("ared")
//		,START_LISTENING_DIGITAL("srld")
//		,START_LISTENING_ANALOG("srla")
//		,STOP_LISTENING_DIGITAL("spld")
//		,STOP_LISTENING_ANALOG("spla")
//		,CHAR_PRESSED("kprs")
//		,TONE("tone")
//		,NOTONE("notn")
//		,CUSTOM_MESSAGE("cust")
//		,RPLY("rply")
//		,READY("ready")
//		,CUSTOM_EVENT("cevnt")
		 ;

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
	
	public static LuaProtoBuilder getBuilder(LuaProtocolKey key) {
		return new LuaProtoBuilder(key);
	}

	private LuaProtocolKey key;
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
		
		Map<String, String> mappedValues = key.getMapper().buildMap(pin, values);
		String retvalue = key.getMessageTemplate();
		Set<String> keys = mappedValues.keySet();
		for (String key : keys) {
			String variableName = "\\$\\{" + key + "\\}";
			retvalue = retvalue.replaceAll(variableName, mappedValues.get(key));
		}
		return retvalue;
	}
}
