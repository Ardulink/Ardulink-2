package com.github.pfichtner.ardulink.core.linkmanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.github.pfichtner.ardulink.core.Connection;

public class DummyConnection implements Connection {

	private final DummyLinkConfig config;
	private final List<Listener> listeners = new ArrayList<Listener>();

	public DummyConnection(DummyLinkConfig config) {
		this.config = config;
	}

	@Override
	public void close() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void write(byte[] bytes) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addListener(Listener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removeListener(Listener listener) {
		this.listeners.remove(listener);
	}

	public DummyLinkConfig getConfig() {
		return config;
	}

}