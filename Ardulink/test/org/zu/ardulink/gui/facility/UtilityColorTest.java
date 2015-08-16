package org.zu.ardulink.gui.facility;

import static java.awt.Color.BLACK;
import static java.awt.Color.RED;
import static java.awt.Color.WHITE;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class UtilityColorTest {

	@Test
	public void canDecodeBlack() {
		assertThat(UtilityColor.toColor("#000000"), is(BLACK));
	}

	@Test
	public void canDecodeWhiteWithoutHashtag() {
		assertThat(UtilityColor.toColor("FFFFFF"), is(WHITE));
	}

	@Test
	public void decodeCanHandleNull() {
		assertThat(UtilityColor.toColor(null), is(BLACK));
	}

	@Test
	public void decodeCanHandleEmptyString() {
		assertThat(UtilityColor.toColor(""), is(BLACK));
	}

	@Test
	public void decodeCanHandleHashtagOnly() {
		assertThat(UtilityColor.toColor("#"), is(BLACK));
	}

	@Test
	public void canEncodeRed() {
		assertThat(UtilityColor.toString(RED), is("#FF0000"));
	}

}
