package org.zu.ardulink.protocol.parser;

public class ParseException extends Exception {

	private static final long serialVersionUID = 1686450598892458046L;

	public ParseException() {
		super();
	}

	public ParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public ParseException(String message) {
		super(message);
	}

	public ParseException(Throwable cause) {
		super(cause);
	}
}
