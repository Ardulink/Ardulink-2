package com.github.pfichtner.ardulink.core.proto.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import org.zu.ardulink.util.Optional;

public final class Protocols {

	private Protocols() {
		super();
	}

	public static Protocol getByName(String name) {
		return tryByName(name).getOrThrow(
				"No protocol with name " + name + " registered");
	}

	public static Optional<Protocol> tryByName(String name) {
		for (Iterator<Protocol> it = iterator(); it.hasNext();) {
			Protocol protocol = it.next();
			if (protocol.getName().equals(name)) {
				return Optional.of(protocol);
			}
		}
		return Optional.absent();
	}

	public static List<String> list() {
		List<String> names = new ArrayList<String>();
		for (Iterator<Protocol> it = iterator(); it.hasNext();) {
			names.add(it.next().getName());
		}
		return names;
	}

	private static Iterator<Protocol> iterator() {
		return ServiceLoader.load(Protocol.class).iterator();
	}

}
