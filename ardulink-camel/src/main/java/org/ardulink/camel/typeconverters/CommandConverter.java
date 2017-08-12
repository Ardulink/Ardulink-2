package org.ardulink.camel.typeconverters;

import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static org.ardulink.camel.command.Commands.*;
import static org.ardulink.util.Integers.tryParse;
import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Preconditions.checkState;

import org.apache.camel.Converter;
import org.apache.camel.Exchange;
import org.ardulink.camel.command.Command;

@Converter
public class CommandConverter {

	private CommandConverter() {
		super();
	}

	/**
	 * Translates a simple protocol into {@link Command}s.
	 * 
	 * <pre>
	 * A2=123 set analog 2 to 123
	 * D3=true set digital 3 to on
	 * SL=A4 enable listening of analog 4
	 * SL=D5 enable listening of digital 5
	 * </pre>
	 * 
	 * @param data
	 *            string to interpret
	 * @param exchange
	 *            exchange to read data from
	 * @return Command that can be executed
	 */
	@Converter
	public static Command toCommand(String data, Exchange exchange) {
		String[] split = data.split("\\=");
		checkState(split.length == 2,
				"Could not split %s into two parts using separator '='", data);

		String left = split[0];
		String right = split[1];

		if (left.equals("SL")) {
			if (type(right) == 'A') {
				return startListeningAnalogPin(pinNumber(right));
			} else if (type(right) == 'D') {
				return startListeningDigitalPin(pinNumber(right));
			}
			throw new IllegalStateException("Cannot handle " + data
					+ " since type " + type(right) + " is not A nor D");
		} else if (type(left) == 'A') {
			int intValue = parseInt(right);
			checkState(intValue >= 0,
					"analog value must not be negative but was %s", intValue);
			return switchAnalogPin(pinNumber(left), intValue);
		} else if (type(left) == 'D') {
			return switchDigitalPin(pinNumber(left), parseBoolean(right));
		}
		throw new IllegalStateException("Cannot handle " + data
				+ " since type " + type(left) + " is not A nor D");
	}

	private static char type(String string) {
		return string.charAt(0);
	}

	private static int pinNumber(String typeAndPin) {
		String pin = typeAndPin.substring(1, typeAndPin.length());
		return checkNotNull(tryParse(pin), "Cannot parse %s as pin number", pin)
				.intValue();
	}

}