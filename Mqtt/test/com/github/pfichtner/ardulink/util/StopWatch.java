package com.github.pfichtner.ardulink.util;

public class StopWatch {

	private Long started;

	public StopWatch start() {
		this.started = Long.valueOf(System.currentTimeMillis());
		return this;
	}

	public long getTime() {
		Long _started = started;
		return _started == null ? 0 : System.currentTimeMillis()
				- _started.longValue();
	}

}
