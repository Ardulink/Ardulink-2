package org.zu.ardulink.gui;

import java.util.Arrays;
import java.util.List;

import com.github.pfichtner.ardulink.core.linkmanager.LinkConfig;

public class DummyLinkConfig implements LinkConfig {

	@Named("a")
	private int a = 42;

	@Named("b")
	private String b;

	@Named("c")
	private Boolean c = Boolean.TRUE;

	public int getA() {
		return a;
	}

	public String getB() {
		return b;
	}

	@ChoiceFor("b")
	public List<String> someValuesForB() {
		return Arrays.asList("foo", "bar");
	}

	public Boolean getC() {
		return c;
	}

	public void setC(Boolean c) {
		this.c = c;
	}

}
