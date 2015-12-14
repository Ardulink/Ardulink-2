package com.github.pfichtner.ardulink.core.qos;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

import org.junit.rules.ExternalResource;

import com.github.pfichtner.ardulink.core.qos.ArduinoDouble.RegexAdder;

public class Arduino extends ExternalResource {

	private ArduinoDouble arduinoDouble;

	public static Arduino newArduino() {
		return new Arduino();
	}

	@Override
	protected void before() throws IOException {
		this.arduinoDouble = new ArduinoDouble();
	}

	@Override
	protected void after() {
		try {
			this.arduinoDouble.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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
