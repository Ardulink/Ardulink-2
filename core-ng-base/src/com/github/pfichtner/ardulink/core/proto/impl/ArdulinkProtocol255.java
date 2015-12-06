package com.github.pfichtner.ardulink.core.proto.impl;

import com.github.pfichtner.ardulink.core.proto.api.Protocol;

public class ArdulinkProtocol255 extends AbstractArdulinkProtocol {

	public ArdulinkProtocol255() {
		super("ardulink255", new byte[] { (byte) 255 });
	}

	private static Protocol instance = new ArdulinkProtocol255();

	public static Protocol instance() {
		return instance;
	}

}
