package org.ardulink.core.virtual;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.TimeUnit;

import org.ardulink.core.linkmanager.LinkConfig;

public class VirtualLinkConfig implements LinkConfig {

	@Named("delay")
	private long delay = 250;

	private TimeUnit delayUnit = MILLISECONDS;

	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	@Named("delayUnit")
	public TimeUnit getDelayUnit() {
		return delayUnit;
	}

	@Named("delayUnit")
	public void setDelayUnit(TimeUnit delayUnit) {
		this.delayUnit = delayUnit;
	}

	@ChoiceFor("delayUnit")
	public TimeUnit[] getDelayUnits() {
		return TimeUnit.values();
	}

	public void delay() {
		try {
			this.delayUnit.sleep(delay);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

}
