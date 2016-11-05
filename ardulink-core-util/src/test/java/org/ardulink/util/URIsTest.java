package org.ardulink.util;

import java.net.URI;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class URIsTest {
	
	@Test
	public void simpleURI() {
		
		URI uri = URIs.newURI("ardulink://serial-jssc?port=COM3");
		assertEquals(uri.getQuery(), "port=COM3");
	}

	@Test
	public void queryURIWithSpaceChar() {
		
		URI uri = URIs.newURI("http://serial-jssc?port=COM3&other=good enough");
		System.out.println(uri.getQuery());
		assertEquals(uri.getQuery(), "port=COM3&other=good enough");
	}
}
