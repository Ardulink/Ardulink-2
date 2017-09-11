package org.ardulink.camel.test;

import java.util.concurrent.TimeUnit;

import org.ardulink.core.linkmanager.LinkConfig;

public class TestLinkConfig implements LinkConfig {

	@Named("a")
	private String a;

	@Named("b")
	private TimeUnit b;

	public String getA() {
		return a;
	}

	public void setA(String a) {
		this.a = a;
	}

	public TimeUnit getB() {
		return b;
	}

	public void setB(TimeUnit b) {
		this.b = b;
	}

}
