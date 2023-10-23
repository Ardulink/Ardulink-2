package org.ardulink.core.linkmanager.providers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;

import org.ardulink.core.convenience.LinkDelegate;
import org.ardulink.core.linkmanager.LinkConfig;
import org.ardulink.core.linkmanager.LinkFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

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
	@ValueSource(strings = { "one:two", "one:two:three:four" })
	void throwsExceptionIfNotThreeArgs(String line) {
		assertThatThrownBy(
				() -> new FactoriesViaMetaInfArdulink.LineProcessor(getClass().getClassLoader()).processLine(line))
				.isInstanceOf(RuntimeException.class)
				.hasMessage("Could not split " + line + " into name:configclass:linkclass");
	}

	@Test
	void configClassNameNotOfTypeLinkConfig() {
		String configClassName = String.class.getName();
		assertThatThrownBy(() -> sut(configClassName, "SomeNotExistingClassName")).isInstanceOf(RuntimeException.class)
				.hasMessage(configClassName + " not of type " + LinkConfig.class.getName());
	}

	@Test
	void linkClassDoesNotExist() {
		TestLinkConfig config = new TestLinkConfig();
		String configClassName = config.getClass().getName();
		assertThatThrownBy(() -> sut(configClassName, "SomeNotExistingClassName")).isInstanceOf(RuntimeException.class)
				.hasCauseInstanceOf(ClassNotFoundException.class).hasMessageContaining("SomeNotExistingClassName");
	}

	@Test
	void linkClassHasNoConstructorWithArgumentOfTypeLinkConfig() throws ClassNotFoundException {
		TestLinkConfig config = new TestLinkConfig();
		String configClassName = config.getClass().getName();
		String linkClassName = TestLinkWithoutLinkConfigConstructor.class.getName();
		assertThatThrownBy(() -> sut(configClassName, linkClassName)).isInstanceOf(RuntimeException.class)
				.hasMessage(linkClassName + " has no public constructor with argument of type " + configClassName);
	}

	@Test
	void ok() throws Exception {
		TestLinkConfig config = new TestLinkConfig();
		assertThat(sut(config.getClass().getName(), TestLinkWithConfigConstructor.class.getName()).newLink(config))
				.isInstanceOf(TestLinkWithConfigConstructor.class);
	}

	@ParameterizedTest
	@MethodSource("stringsRepresentingNull")
	void okWithoutConfig(String configClassName) throws Exception {
		String linkClassName = TestLinkWithoutLinkConfigConstructor.class.getName();
		TestLinkConfig config = new TestLinkConfig();
		assertThat(sut(configClassName, linkClassName).newLink(config))
				.isInstanceOf(TestLinkWithoutLinkConfigConstructor.class);
	}

	@ParameterizedTest
	@MethodSource("stringsRepresentingNull")
	void ifTheConfigClassIsNullThereHasToBePublicZeroArgConstructor(String configClassName) throws Exception {
		String linkClassName = TestLinkWithConfigConstructor.class.getName();
		assertThatThrownBy(() -> sut(configClassName, linkClassName)).isInstanceOf(RuntimeException.class)
				.hasMessage(linkClassName + " has no public zero arg constructor");
	}

	static Stream<String> stringsRepresentingNull() {
		return Stream.of("null", "NULL", "Null", "nUlL", null);
	}

	LinkFactory<LinkConfig> sut(String configClassName, String linkClassName) {
		return new FactoriesViaMetaInfArdulink.LineProcessor(getClass().getClassLoader())
				.processLine("anyName:" + configClassName + ":" + linkClassName);
	}

}
