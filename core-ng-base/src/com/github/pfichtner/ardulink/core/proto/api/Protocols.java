package com.github.pfichtner.ardulink.core.proto.api;

import java.util.Iterator;
import java.util.ServiceLoader;

public final class Protocols {

	private Protocols() {
		super();
	}

	public static Protocol getByName(String name) {
		for (Iterator<Protocol> it = ServiceLoader.load(Protocol.class)
				.iterator(); it.hasNext();) {
			Protocol protocol = it.next();
			if (protocol.getName().equals(name)) {
				return protocol;
			}
		}
		throw new IllegalArgumentException("No protocol with name " + name
				+ " registered");
	}

}
