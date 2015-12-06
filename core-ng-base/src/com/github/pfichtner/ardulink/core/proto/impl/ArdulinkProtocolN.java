package com.github.pfichtner.ardulink.core.proto.impl;

import com.github.pfichtner.ardulink.core.proto.api.Protocol;

public class ArdulinkProtocolN extends AbstractArdulinkProtocol {

	public ArdulinkProtocolN() {
		super("ardulink", "\n".getBytes());
	}

	private static final ArdulinkProtocolN instance = new ArdulinkProtocolN();

	public static Protocol instance() {
		return instance;
	}

}
