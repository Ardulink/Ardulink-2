package com.github.pfichtner.ardulink.compactors;

import org.zu.ardulink.event.AnalogReadChangeListener;

public abstract class AnalogReadChangeListenerAdapter implements
		AnalogReadChangeListener {

	private final AnalogReadChangeListener delegate;

	public AnalogReadChangeListenerAdapter(AnalogReadChangeListener delegate) {
		this.delegate = delegate;
	}

	public AnalogReadChangeListener getDelegate() {
		return delegate;
	}

	@Override
	public int getPinListening() {
		return delegate.getPinListening();
	}
}