package com.github.pfichtner.ardulink.core;

public interface ConnectionListener {

	void connectionReady();
	
	void connectionLost();

	void reconnected();

}
