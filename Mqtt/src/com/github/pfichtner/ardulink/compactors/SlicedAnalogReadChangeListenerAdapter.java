package com.github.pfichtner.ardulink.compactors;

import org.zu.ardulink.event.AnalogReadChangeListener;

public abstract class SlicedAnalogReadChangeListenerAdapter extends
		AnalogReadChangeListenerAdapter {

	public SlicedAnalogReadChangeListenerAdapter(
			AnalogReadChangeListener delegate) {
		super(delegate);
	}

	public abstract void ticked();

}
