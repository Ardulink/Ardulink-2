package com.github.pfichtner.ardulink.core.proto.impl;

import com.github.pfichtner.ardulink.core.proto.api.ToArduinoCustomMessage;

public class DefaultToArduinoCustomMessage implements ToArduinoCustomMessage {

	private final String[] messages;

	public DefaultToArduinoCustomMessage(String... messages) {
		this.messages = messages.clone();
	}

	@Override
	public String[] getMessages() {
		return messages.clone();
	}

}
