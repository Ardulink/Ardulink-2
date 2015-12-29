package ardulink.ardumailng;

import static com.github.pfichtner.ardulink.core.Pin.analogPin;
import static com.github.pfichtner.ardulink.core.Pin.digitalPin;

import java.net.URISyntaxException;

import com.github.pfichtner.ardulink.core.Link;

public class Commands {

	private static class SwitchDigitalPinCommand implements Command {

		private final int pin;
		private final boolean value;

		public SwitchDigitalPinCommand(int pin, boolean value) {
			this.pin = pin;
			this.value = value;
		}

		@Override
		public void execute(Link link) throws URISyntaxException, Exception {
			link.switchDigitalPin(digitalPin(pin), value);
		}

		@Override
		public String toString() {
			return "SwitchDigitalPinCommand [pin=" + pin + ", value=" + value
					+ "]";
		}

	}

	private static class SwitchAnalogPinCommand implements Command {

		private final int pin;
		private final int value;

		public SwitchAnalogPinCommand(int pin, int value) {
			this.pin = pin;
			this.value = value;
		}

		@Override
		public void execute(Link link) throws URISyntaxException, Exception {
			link.switchAnalogPin(analogPin(pin), value);
		}

		@Override
		public String toString() {
			return "SwitchAnalogPinCommand [pin=" + pin + ", value=" + value
					+ "]";
		}

	}

	public static Command switchDigitalPin(int pin, boolean value) {
		return new SwitchDigitalPinCommand(pin, value);
	}

	public static Command switchAnalogPin(int pin, int value) {
		return new SwitchAnalogPinCommand(pin, value);
	}

}
