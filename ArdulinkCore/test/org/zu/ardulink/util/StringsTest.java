package org.zu.ardulink.util;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.zu.ardulink.protocol.ALProtocol;

public class StringsTest {

	
	@Test
	public void string2IntsTest() {
		
		String message = "alp://rply/ok?id=781";
		int[] expected = {97, 108, 112, 58, 47, 47, 114, 112, 108, 121, 47, 111, 107, 63, 105, 100, 61, 55, 56, 49};
//		byte[] bytes = message.getBytes();
//		for (int i = 0; i < bytes.length; i++) {
//			System.out.print(bytes[i] + ", ");
//		}

		assertTrue(Arrays.equals(expected, Strings.string2Ints(message)));
	}

	@Test
	public void bytes2StringTest() {
		
		assertEquals("\n", Strings.bytes2String(ALProtocol.DEFAULT_OUTGOING_MESSAGE_DIVIDER));
	}
}
