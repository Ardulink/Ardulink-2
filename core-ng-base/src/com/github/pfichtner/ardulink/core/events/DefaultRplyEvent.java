package com.github.pfichtner.ardulink.core.events;

public class DefaultRplyEvent implements RplyEvent {

	private boolean ok;
	private long id;

	public DefaultRplyEvent(boolean ok, long id) {
		this.ok = ok;
		this.id = id;
	}

	@Override
	public boolean isOk() {
		return ok;
	}

	@Override
	public long getId() {
		return id;
	}

}
