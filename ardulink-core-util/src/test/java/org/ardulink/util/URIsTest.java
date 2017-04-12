package org.ardulink.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

public class URIsTest {

	// TODO if needed make URLBuilder public and convert to top-level-class. If
	// not needed, remove whole test class including URLBuilder
	public static class URLBuilder {

		private final StringBuilder base;

		public URLBuilder(String base) {
			this.base = new StringBuilder(base);
			if (!base.endsWith("?")) {
				this.base.append('?');
			}
		}

		public URLBuilder param(String name, String value) {
			this.base.append(URIs.encode(name)).append('=')
					.append(URIs.encode(value)).append('&');
			return this;
		}

		public URI build() {
			String string = this.base.toString();
			return URIs.newURI(string.endsWith("&") ? string.substring(0,
					string.length() - 1) : string);
		}

	}

	@Test
	public void simpleURI() throws URISyntaxException {
		URI uri = new URLBuilder("ardulink://serial-jssc")
				.param("port", "COM3").build();
		assertThat(uri, is(new URI("ardulink://serial-jssc?port=COM3")));
	}

	@Test
	public void queryURIWithSpaceChar() throws URISyntaxException {
		String base = "http://serial-jssc";
		URI uri = new URLBuilder(base).param("port", "COM3")
				.param("name with spaces", "value with spaces").build();
		assertThat(uri, is(new URI(base
				+ "?port=COM3&name+with+spaces=value+with+spaces")));
	}

}
