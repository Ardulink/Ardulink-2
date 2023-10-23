package org.ardulink.core.linkmanager.providers;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static java.util.stream.IntStream.rangeClosed;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.stream.Stream;

import org.ardulink.core.convenience.LinkDelegate;
import org.ardulink.core.linkmanager.LinkConfig;
import org.ardulink.core.linkmanager.LinkFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class FactoriesViaMetaInfArdulinkTest {

	static final class TestLinkConfig implements LinkConfig {
	}

	static class TestLinkWithoutLinkConfigConstructor extends LinkDelegate {
		public TestLinkWithoutLinkConfigConstructor() {
			super(null);
		}
	}

	static class TestLinkWithConfigConstructor extends LinkDelegate {
		public TestLinkWithConfigConstructor(TestLinkConfig config) {
			super(null);
		}
	}

	@ParameterizedTest
	@MethodSource("zeroToTwentyWordsExcludingThreeWords")
	void throwsExceptionIfNotThreeArgs(List<String> words) {
		String row = row(words);
		assertThatThrownBy(() -> sut(row)).isInstanceOf(RuntimeException.class)
				.hasMessage("Could not split " + row + " into name:configclass:linkclass");
	}

	static Stream<List<String>> zeroToTwentyWordsExcludingThreeWords() {
		return rangeClosed(0, 20).filter(i -> i != 3)
				.mapToObj(i -> range(0, i).mapToObj(String::valueOf).collect(toList()));
	}

	@Test
	void configClassNameNotOfTypeLinkConfig() {
		String configClassName = String.class.getName();
		assertThatThrownBy(() -> sut(makeRow(configClassName, "SomeNotExistingClassName")))
				.isInstanceOf(RuntimeException.class)
				.hasMessage(configClassName + " not of type " + LinkConfig.class.getName());
	}

	@Test
	void linkClassDoesNotExist() {
		TestLinkConfig config = new TestLinkConfig();
		String configClassName = config.getClass().getName();
		assertThatThrownBy(() -> sut(makeRow(configClassName, "SomeNotExistingClassName")))
				.isInstanceOf(RuntimeException.class).hasCauseInstanceOf(ClassNotFoundException.class)
				.hasMessageContaining("SomeNotExistingClassName");
	}

	@Test
	void linkClassHasNoConstructorWithArgumentOfTypeLinkConfig() throws ClassNotFoundException {
		TestLinkConfig config = new TestLinkConfig();
		String configClassName = config.getClass().getName();
		String linkClassName = TestLinkWithoutLinkConfigConstructor.class.getName();
		assertThatThrownBy(() -> sut(makeRow(configClassName, linkClassName))).isInstanceOf(RuntimeException.class)
				.hasMessage(linkClassName + " has no public constructor with argument of type " + configClassName);
	}

	@Test
	void ok() throws Exception {
		TestLinkConfig config = new TestLinkConfig();
		assertThat(sut(makeRow(config.getClass().getName(), TestLinkWithConfigConstructor.class.getName()))
				.newLink(config)).isInstanceOf(TestLinkWithConfigConstructor.class);
	}

	@ParameterizedTest
	@MethodSource("stringsRepresentingNull")
	void okWithoutConfig(String configClassName) throws Exception {
		String linkClassName = TestLinkWithoutLinkConfigConstructor.class.getName();
		TestLinkConfig config = new TestLinkConfig();
		assertThat(sut(makeRow(configClassName, linkClassName)).newLink(config))
				.isInstanceOf(TestLinkWithoutLinkConfigConstructor.class);
	}

	@ParameterizedTest
	@MethodSource("stringsRepresentingNull")
	void ifTheConfigClassIsNullThereHasToBePublicZeroArgConstructor(String configClassName) throws Exception {
		String linkClassName = TestLinkWithConfigConstructor.class.getName();
		assertThatThrownBy(() -> sut(makeRow(configClassName, linkClassName))).isInstanceOf(RuntimeException.class)
				.hasMessage(linkClassName + " has no public zero arg constructor");
	}

	static Stream<String> stringsRepresentingNull() {
		return Stream.of("null", "NULL", "Null", "nUlL", null);
	}

	static String makeRow(String configClassName, String linkClassName) {
		return row(asList("anyName", configClassName, linkClassName));
	}

	private static String row(List<String> row) {
		return row.stream().collect(joining(":"));
	}

	LinkFactory<LinkConfig> sut(String line) {
		return new FactoriesViaMetaInfArdulink.LineProcessor(getClass().getClassLoader()).processLine(line);
	}

}
