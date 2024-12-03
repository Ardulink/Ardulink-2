package org.ardulink.gui.facility;

import static java.awt.Color.BLACK;
import static java.awt.Color.BLUE;
import static java.awt.Color.GREEN;
import static java.awt.Color.RED;
import static net.jqwik.api.Arbitraries.integers;
import static net.jqwik.api.Combinators.combine;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.awt.Color;

import org.junit.jupiter.api.Test;

import net.jqwik.api.Arbitrary;
import net.jqwik.api.ArbitrarySupplier;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;

class UtilityColorTest {

	private static class ColorSupplier implements ArbitrarySupplier<Color> {
		@Override
		public Arbitrary<Color> get() {
			return combine( //
					integers().between(0, 255), //
					integers().between(0, 255), //
					integers().between(0, 255) //
			).as(Color::new);
		}

	}

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

	@Test
	void testInvert() throws Exception {
		assertThat(UtilityColor.invert(BLACK)).isEqualTo(Color.WHITE);
		assertThat(UtilityColor.invert(RED)).isEqualTo(Color.decode("#00FFFF"));
		assertThat(UtilityColor.invert(GREEN)).isEqualTo(Color.decode("#FF00FF"));
		assertThat(UtilityColor.invert(BLUE)).isEqualTo(Color.decode("#FFFF00"));
	}

	@Property
	void invertingAnInvertedColorResultsInOriginalColor(@ForAll(supplier = ColorSupplier.class) Color color) {
		Color inverted = UtilityColor.invert(color);
		assertThat(UtilityColor.invert(inverted)).isEqualTo(color);
	}

}
