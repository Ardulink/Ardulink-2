package com.github.pfichtner.ardulink.util;

import static com.github.pfichtner.ardulink.util.Preconditions.checkArgument;

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

	public String withValue(int value) {
		return "alp://" + command + "/" + pin + "/" + value + "\n";
	}

	public ProtoBuilder forPin(int pin) {
		checkArgument(pin >= 0, "Pin must not be negative but was %s", pin);
		this.pin = pin;
		return this;
	}

}
