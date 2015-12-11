package com.github.pfichtner.ardulink.core.proto.api;

public class ToArduinoCustomMessage {

	public final String[] messages;

	public ToArduinoCustomMessage(String... messages) {
		this.messages = messages.clone();
	}

}
