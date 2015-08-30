package com.github.pfichtner.ardulink.compactors;

import org.zu.ardulink.event.AnalogReadChangeEvent;
import org.zu.ardulink.event.AnalogReadChangeListener;

public class TimeSliceCompactorLast extends
		SlicedAnalogReadChangeListenerAdapter {

	private boolean firstCall = true;
	private AnalogReadChangeEvent lastEvent;

	public TimeSliceCompactorLast(AnalogReadChangeListener delegate) {
		super(delegate);
	}

	@Override
	public void stateChanged(AnalogReadChangeEvent event) {
		if (this.firstCall) {
			getDelegate().stateChanged(event);
			this.firstCall = false;
		} else {
			this.lastEvent = event;
		}
	}

	@Override
	public void ticked() {
		AnalogReadChangeEvent event = this.lastEvent;
		if (event != null) {
			getDelegate().stateChanged(event);
		}
	}

}