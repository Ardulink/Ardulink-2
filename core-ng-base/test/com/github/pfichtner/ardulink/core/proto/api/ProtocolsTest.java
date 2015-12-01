package com.github.pfichtner.ardulink.core.proto.api;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

public class ProtocolsTest {

	@Test
	public void defaultAndDummyProtocolsAreRegistered() {
		assertThat(
				new HashSet<String>(Protocols.list()),
				is(new HashSet<String>(Arrays.asList("ardulink", "dummyProto"))));
	}

}
