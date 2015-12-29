package com.github.pfichtner.core.raspi;

import static com.github.pfichtner.ardulink.core.Pin.Type.ANALOG;
import static com.github.pfichtner.ardulink.core.Pin.Type.DIGITAL;
import static com.pi4j.io.gpio.PinMode.ANALOG_INPUT;
import static com.pi4j.io.gpio.PinMode.DIGITAL_INPUT;
import static com.pi4j.io.gpio.PinMode.DIGITAL_OUTPUT;
import static com.pi4j.io.gpio.PinMode.PWM_OUTPUT;
import static com.pi4j.io.gpio.PinPullResistance.PULL_DOWN;
import static com.pi4j.io.gpio.RaspiPin.getPinByName;
import static com.pi4j.io.gpio.event.PinEventType.ANALOG_VALUE_CHANGE;
import static com.pi4j.io.gpio.event.PinEventType.DIGITAL_STATE_CHANGE;
import static org.zu.ardulink.util.Preconditions.checkState;

import java.io.IOException;
import java.util.List;

import org.zu.ardulink.util.ListMultiMap;

import com.github.pfichtner.ardulink.core.AbstractListenerLink;
import com.github.pfichtner.ardulink.core.Pin;
import com.github.pfichtner.ardulink.core.Pin.AnalogPin;
import com.github.pfichtner.ardulink.core.Pin.DigitalPin;
import com.github.pfichtner.ardulink.core.Tone;
import com.github.pfichtner.ardulink.core.events.DefaultAnalogPinValueChangedEvent;
import com.github.pfichtner.ardulink.core.events.DefaultDigitalPinValueChangedEvent;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.PinMode;
import com.pi4j.io.gpio.event.GpioPinAnalogValueChangeEvent;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListener;
import com.pi4j.io.gpio.event.GpioPinListenerAnalog;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class PiLink extends AbstractListenerLink {

	private final GpioController gpioController = GpioFactory.getInstance();
	private final ListMultiMap<GpioPin, GpioPinListener> listeners = new ListMultiMap<GpioPin, GpioPinListener>();

	@Override
	public void close() throws IOException {
		this.gpioController.shutdown();
		super.close();
	}

	@Override
	public void startListening(final Pin pin) throws IOException {
		GpioPin gpioPin;
		GpioPinListener listener;
		if (pin.is(ANALOG)) {
			gpioPin = getByAddress(pin.pinNum(), ANALOG_INPUT);
			listener = analogAdapter(pin);
		} else if (pin.is(DIGITAL)) {
			gpioPin = getByAddress(pin.pinNum(), DIGITAL_INPUT);
			listener = digitalAdapter(pin);
		} else {
			throw new IllegalStateException("Unknown pin type of pin " + pin);
		}
		gpioPin.setPullResistance(PULL_DOWN);
		addListener(gpioPin, listener);
	}

	@Override
	public void stopListening(Pin pin) throws IOException {
		GpioPin gpioPin = getByAddress(pin.pinNum(), pi4jInputMode(pin));
		List<GpioPinListener> list = listeners.asMap().get(gpioPin);
		if (list != null) {
			for (GpioPinListener gpioPinListener : list) {
				gpioPin.removeListener(gpioPinListener);
				listeners.remove(gpioPin, gpioPinListener);
			}
		}
	}

	@Override
	public void switchAnalogPin(AnalogPin analogPin, int value)
			throws IOException {
		GpioPinPwmOutput pin = (GpioPinPwmOutput) getByAddress(
				analogPin.pinNum(), PWM_OUTPUT);
		pin.setPwm(value);
	}

	@Override
	public void switchDigitalPin(DigitalPin digitalPin, boolean value)
			throws IOException {
		GpioPinDigitalOutput pin = (GpioPinDigitalOutput) getByAddress(
				digitalPin.pinNum(), DIGITAL_OUTPUT);
		if (value) {
			pin.high();
		} else {
			pin.low();
		}
	}

	@Override
	public void sendKeyPressEvent(char keychar, int keycode, int keylocation,
			int keymodifiers, int keymodifiersex) throws IOException {
		throw notSupported();
	}

	@Override
	public void sendTone(Tone tone) throws IOException {
		throw notSupported();
	}

	@Override
	public void sendNoTone(AnalogPin analogPin) throws IOException {
		throw notSupported();
	}

	@Override
	public void sendCustomMessage(String... messages) throws IOException {
		throw notSupported();
	}

	private void addListener(GpioPin gpioPin, GpioPinListener listener) {
		gpioPin.addListener(listener);
		listeners.put(gpioPin, listener);
	}

	private GpioPin getByAddress(int address, PinMode pinMode) {
		for (GpioPin gpioPin : gpioController.getProvisionedPins()) {
			com.pi4j.io.gpio.Pin pin = gpioPin.getPin();
			if (pin.getAddress() == address) {
				checkState(pin.getSupportedPinModes().contains(pinMode),
						"Pin %sdoes not provide %s", pin, pinMode);
				return gpioPin;
			}
		}
		return gpioController.provisionPin(getPinByName("GPIO " + address),
				pinMode);
	}

	private GpioPinListenerDigital digitalAdapter(final Pin pin) {
		return new GpioPinListenerDigital() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(
					GpioPinDigitalStateChangeEvent event) {
				if (event.getEventType() == DIGITAL_STATE_CHANGE) {
					fireStateChanged(new DefaultDigitalPinValueChangedEvent(
							(DigitalPin) pin, event.getState().isHigh()));
				}
			}
		};
	}

	private GpioPinListenerAnalog analogAdapter(final Pin pin) {
		return new GpioPinListenerAnalog() {
			@Override
			public void handleGpioPinAnalogValueChangeEvent(
					GpioPinAnalogValueChangeEvent event) {
				if (event.getEventType() == ANALOG_VALUE_CHANGE) {
					fireStateChanged(new DefaultAnalogPinValueChangedEvent(
							(AnalogPin) pin, (int) event.getValue()));
				}
			}
		};
	}

	private static PinMode pi4jInputMode(Pin pin) {
		if (pin.is(ANALOG)) {
			return ANALOG_INPUT;
		} else if (pin.is(DIGITAL)) {
			return DIGITAL_INPUT;
		}
		throw new IllegalStateException("Illegal pin type of pin " + pin);
	}

	private UnsupportedOperationException notSupported() {
		return new UnsupportedOperationException("not supported");
	}

}
