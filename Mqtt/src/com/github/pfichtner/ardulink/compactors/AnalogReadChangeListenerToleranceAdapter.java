package com.github.pfichtner.ardulink.compactors;

import org.zu.ardulink.event.AnalogReadChangeEvent;
import org.zu.ardulink.event.AnalogReadChangeListener;

public class AnalogReadChangeListenerToleranceAdapter extends
		AnalogReadChangeListenerAdapter {

	private Integer cachedValue;
	private final Tolerance tolerance;

	public AnalogReadChangeListenerToleranceAdapter(Tolerance tolerance,
			AnalogReadChangeListener delegate) {
		super(delegate);
		this.tolerance = tolerance;
	}

	@Override
	public void stateChanged(AnalogReadChangeEvent e) {
		int newValue = e.getValue();
		if (this.cachedValue == null
				|| !tolerance
						.inTolerance(this.cachedValue.intValue(), newValue)
				|| isHighOrLowValue(newValue)) {
			this.cachedValue = Integer.valueOf(newValue);
			getDelegate().stateChanged(e);
		}
	}

	private boolean isHighOrLowValue(int value) {
		return value == 0 || value == 255;
	}

}