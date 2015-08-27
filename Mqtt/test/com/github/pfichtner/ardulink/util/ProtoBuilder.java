package com.github.pfichtner.ardulink.util;

public class ProtoBuilder {

	private final String command;
	private int pin;

	public enum ALPProtocolKeys {

		POWER_PIN_SWITCH("ppsw"), POWER_PIN_INTENSITY("ppin"), DIGITAL_PIN_READ(
				"dred"), ANALOG_PIN_READ("ared");

		private String proto;

		private ALPProtocolKeys(String proto) {
			this.proto = proto;
		}
	}

	public static ProtoBuilder alpProtocolMessage(ALPProtocolKeys command) {
		return new ProtoBuilder(command.proto);
	}

	public static ProtoBuilder arduinoCommand(String command) {
		return new ProtoBuilder(command);
	}

	private ProtoBuilder(String command) {
		this.command = command;
	}

	public String valueChangedTo(int value) {
		return setValue(value);
	}

	public String setValue(int value) {
		return "alp://" + command + "/" + pin + "/" + value + "\n";
	}

	public ProtoBuilder forPin(int pin) {
		this.pin = pin;
		return this;
	}

}
