package org.ardulink.gui.facility;

import static java.awt.Color.BLACK;
import static java.awt.Color.BLUE;
import static java.awt.Color.GREEN;
import static java.awt.Color.RED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.awt.Color;

import org.junit.jupiter.api.Test;

class UtilityColorTest {

	@Test
	void testToColor() {
		assertColorIs("000000", BLACK);
		assertColorIs("FF0000", RED);
		assertColorIs("00FF00", GREEN);
		assertColorIs("0000FF", BLUE);
	}

	private void assertColorIs(String hex, Color color) {
		assertSoftly(s -> {
			s.assertThat(UtilityColor.toColor(hex)).isEqualTo(color);
			s.assertThat(UtilityColor.toColor("#" + hex)).isEqualTo(color);
		});
	}

	@Test
	void emptyIsBlack() {
		assertThat(UtilityColor.toColor(null)).isEqualTo(BLACK);
		assertThat(UtilityColor.toColor("")).isEqualTo(BLACK);
		assertThat(UtilityColor.toColor("#")).isEqualTo(BLACK);
	}

	@Test
	void testToString() throws Exception {
		assertThat(UtilityColor.toString(BLACK)).isEqualTo("#000000");
		assertThat(UtilityColor.toString(RED)).isEqualTo("#FF0000");
		assertThat(UtilityColor.toString(GREEN)).isEqualTo("#00FF00");
		assertThat(UtilityColor.toString(BLUE)).isEqualTo("#0000FF");
	}

}
