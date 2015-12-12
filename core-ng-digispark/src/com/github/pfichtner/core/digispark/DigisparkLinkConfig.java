package com.github.pfichtner.core.digispark;

import com.github.pfichtner.ardulink.core.linkmanager.LinkConfig;

public class DigisparkLinkConfig implements LinkConfig {

	// @Named("id")
	// private int id;
	//
	// public int getId() {
	// return id;
	// }
	//
	// public void setId(int id) {
	// this.id = id;
	// }

	@Named("portName")
	private String portName;

	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

}
