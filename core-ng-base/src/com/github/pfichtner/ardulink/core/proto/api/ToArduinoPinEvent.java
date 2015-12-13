package com.github.pfichtner.ardulink.core.proto.api;

import com.github.pfichtner.ardulink.core.Pin;

public interface ToArduinoPinEvent {

	Pin getPin();

	Object getValue();

}