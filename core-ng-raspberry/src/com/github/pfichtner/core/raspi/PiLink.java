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
		gpioController.shutdown();
		super.close();
	}

	@Override
	public void startListening(final Pin pin) throws IOException {
		GpioPin gpioPin = getByAddress(pin.pinNum());
		if (pin.is(ANALOG)) {
			gpioPin.setMode(ANALOG_INPUT);
			gpioPin.setPullResistance(PULL_DOWN);
			addListener(gpioPin, analogAdapter(pin));
		} else if (pin.is(DIGITAL)) {
			gpioPin.setMode(DIGITAL_INPUT);
			gpioPin.setPullResistance(PULL_DOWN);
			addListener(gpioPin, digitalAdapter(pin));
		}
		throw new IllegalStateException("Unknown pin type of pin " + pin);
	}

	@Override
	public void stopListening(Pin pin) throws IOException {
		GpioPin gpioPin = getByAddress(pin.pinNum());
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
		// TODO getByAddress digital/analog collisions?
		GpioPinPwmOutput pin = (GpioPinPwmOutput) getByAddress(analogPin
				.pinNum());
		pin.setMode(PWM_OUTPUT);
		pin.setPwm(value);
	}

	@Override
	public void switchDigitalPin(DigitalPin digitalPin, boolean value)
			throws IOException {
		// TODO getByAddress digital/analog collisions?
		GpioPinDigitalOutput pin = (GpioPinDigitalOutput) getByAddress(digitalPin
				.pinNum());
		pin.setMode(DIGITAL_OUTPUT);
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

	private GpioPin getByAddress(int address) {
		for (GpioPin gpioPin : gpioController.getProvisionedPins()) {
			if (gpioPin.getPin().getAddress() == address) {
				return gpioPin;
			}
		}
		return gpioController.provisionPin(getPinByName("GPIO " + address),
				DIGITAL_OUTPUT);
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

	private UnsupportedOperationException notSupported() {
		return new UnsupportedOperationException("not supported");
	}

}
