package com.github.pfichtner.ardulink.core.proto.api;

public interface ToArduinoKeyPressEvent {

	char getKeychar();

	int getKeycode();

	int getKeylocation();

	int getKeymodifiers();

	int getKeymodifiersex();

}