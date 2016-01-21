package com.github.pfichtner.ardulink.core;

public interface ConnectionListener {

	void connectionLost();

	void reconnected();

}
