package com.github.pfichtner.ardulink.compactors;

import java.util.List;
import java.util.Map.Entry;

import org.zu.ardulink.event.AnalogReadChangeEvent;
import org.zu.ardulink.event.AnalogReadChangeListener;

import com.github.pfichtner.ardulink.util.HashMultiMap;

public class TimeSliceCompactorAvg extends
		SlicedAnalogReadChangeListenerAdapter {

	private HashMultiMap<Integer, Integer> data = new HashMultiMap<Integer, Integer>();

	public TimeSliceCompactorAvg(AnalogReadChangeListener delegate) {
		super(delegate);
	}

	@Override
	public void ticked() {
		HashMultiMap<Integer, Integer> vs = data;
		if (!vs.isEmpty()) {
			data = new HashMultiMap<Integer, Integer>();
			for (Entry<Integer, List<Integer>> entry : vs.asMap().entrySet()) {
				getDelegate().stateChanged(
						new AnalogReadChangeEvent(entry.getKey(), avg(entry
								.getValue()), "alp://todo/rebuild/message"));
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

	@Override
	public void stateChanged(AnalogReadChangeEvent e) {
		this.data.put(e.getPin(), e.getValue());
	}

}