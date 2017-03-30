package org.ardulink.util;

import static org.ardulink.util.Preconditions.checkNotNull;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

public final class URIs {

	private URIs() {
		super();
	}

	public static URI newURI(String asciiString) {
		try {
			return new URI(checkNotNull(asciiString,
					"asciiString must not be null"));
		} catch (URISyntaxException e) {
			throw Throwables.propagate(e);
		}
	}

	public static String encode(String asciiString) {
		try {
			return URLEncoder.encode(
					checkNotNull(asciiString, "asciiString must not be null"),
					"UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw Throwables.propagate(e);
		}
	}

}
