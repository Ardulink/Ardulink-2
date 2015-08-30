package com.github.pfichtner.ardulink.compactors;

import java.util.List;
import java.util.Map.Entry;

import org.zu.ardulink.event.AnalogReadChangeEvent;
import org.zu.ardulink.event.AnalogReadChangeListener;

import com.github.pfichtner.ardulink.util.HashMultiMap;

public class TimeSliceCompactorAvg extends
		SlicedAnalogReadChangeListenerAdapter {

	private boolean firstCall = true;
	private final HashMultiMap<Integer, Integer> data = new HashMultiMap<Integer, Integer>();

	public TimeSliceCompactorAvg(AnalogReadChangeListener delegate) {
		super(delegate);
	}

	@Override
	public void stateChanged(AnalogReadChangeEvent event) {
		if (this.firstCall) {
			getDelegate().stateChanged(event);
			this.firstCall = false;
		} else {
			synchronized (this.data) {
				this.data.put(event.getPin(), event.getValue());
			}
		}
	}

	@Override
	public void ticked() {
		synchronized (this.data) {
			if (!data.isEmpty()) {
				for (Entry<Integer, List<Integer>> entry : data.asMap()
						.entrySet()) {
					getDelegate()
							.stateChanged(
									new AnalogReadChangeEvent(entry.getKey(),
											avg(entry.getValue()),
											"alp://todo/rebuild/message"));
				}
				data.clear();
			}
		}
	}

	private int avg(List<Integer> values) {
		return sum(values) / values.size();
	}

	protected int sum(List<Integer> values) {
		int sum = 0;
		for (Integer integer : values) {
			sum += integer;
		}
		return sum;
	}

}