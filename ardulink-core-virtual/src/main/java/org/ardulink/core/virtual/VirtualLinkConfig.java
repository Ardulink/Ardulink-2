package org.ardulink.core.virtual;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.TimeUnit;

import javax.validation.constraints.Positive;

import org.ardulink.core.linkmanager.LinkConfig;

public class VirtualLinkConfig implements LinkConfig {

	private static final String DELAY_UNIT = "delayUnit";

	@Named("delay")
	@Positive
	public long delay = 250;

	@Named(DELAY_UNIT)
	public TimeUnit delayUnit = MILLISECONDS;

	@ChoiceFor(DELAY_UNIT)
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
