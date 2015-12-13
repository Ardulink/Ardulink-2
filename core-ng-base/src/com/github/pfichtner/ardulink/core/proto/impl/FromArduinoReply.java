package com.github.pfichtner.ardulink.core.proto.impl;

import com.github.pfichtner.ardulink.core.proto.api.Protocol.FromArduino;

public class FromArduinoReply implements FromArduino {

	private final boolean ok;
	private final long id;

	public FromArduinoReply(final boolean ok, long id) {
		this.ok = ok;
		this.id = id;
	}

	public boolean isOk() {
		return ok;
	}

	public long getId() {
		return id;
	}

}
