package org.ardulink.core.virtual;

import java.util.concurrent.TimeUnit;

import org.ardulink.core.linkmanager.LinkConfig;

public class VirtualLinkConfig implements LinkConfig {

	@Named("delay")
	private long delay = 250;

	private TimeUnit delayUnit = TimeUnit.MILLISECONDS;

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	@Named("delayUnit")
	public String getDelayUnit() {
		return delayUnit.name();
	}

	@Named("delayUnit")
	public void setDelayUnit(String delayUnit) {
		this.delayUnit = TimeUnit.valueOf(delayUnit);
	}

	public void delay() {
		try {
			this.delayUnit.sleep(delay);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

}
