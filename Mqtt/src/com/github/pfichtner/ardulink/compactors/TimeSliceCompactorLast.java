package com.github.pfichtner.ardulink.compactors;

import org.zu.ardulink.event.AnalogReadChangeEvent;
import org.zu.ardulink.event.AnalogReadChangeListener;

public class TimeSliceCompactorLast extends
		SlicedAnalogReadChangeListenerAdapter {

	private AnalogReadChangeEvent lastEvent;

	public TimeSliceCompactorLast(AnalogReadChangeListener delegate) {
		super(delegate);
	}

	@Override
	public void ticked() {
		AnalogReadChangeEvent e = lastEvent;
		if (e != null) {
			getDelegate().stateChanged(e);
		}
	}

	@Override
	public void stateChanged(AnalogReadChangeEvent e) {
		this.lastEvent = e;
	}

}