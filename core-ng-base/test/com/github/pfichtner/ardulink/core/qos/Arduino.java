package com.github.pfichtner.ardulink.core.qos;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

import org.junit.rules.ExternalResource;

import com.github.pfichtner.ardulink.core.qos.ArduinoDouble.Adder;
import com.github.pfichtner.ardulink.core.qos.ArduinoDouble.RegexAdder;

public class Arduino extends ExternalResource {

	private final ArduinoDouble arduinoDouble = create();

	public static Arduino newArduino() {
		return new Arduino();
	}

	private static ArduinoDouble create() {
		try {
			return new ArduinoDouble();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void after() {
		try {
			this.arduinoDouble.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Adder whenReceive(String string) {
		return arduinoDouble.whenReceive(string);
	}

	public RegexAdder whenReceive(Pattern pattern) {
		return arduinoDouble.whenReceive(pattern);
	}

	public InputStream getInputStream() {
		return arduinoDouble.getInputStream();
	}

	public OutputStream getOutputStream() {
		return arduinoDouble.getOutputStream();
	}

}
