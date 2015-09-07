package com.github.pfichtner.ardulink.compactors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ThreadTimeSlicer implements TimeSlicer {

	private final List<SlicedAnalogReadChangeListenerAdapter> runnables = new ArrayList<SlicedAnalogReadChangeListenerAdapter>();

	public ThreadTimeSlicer(final long value, final TimeUnit timeUnit) {
		new Thread() {
			{
				setDaemon(true);
				start();
			}

			@Override
			public void run() {
				while (true) {
					try {
						timeUnit.sleep(value);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					for (SlicedAnalogReadChangeListenerAdapter runnable : runnables) {
						runnable.ticked();
					}
				}
			}
		};
	}

	public void add(SlicedAnalogReadChangeListenerAdapter runnable) {
		this.runnables.add(runnable);
	};

}
