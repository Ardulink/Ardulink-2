package org.ardulink.core.linkmanager.providers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;

import org.ardulink.core.Link;
import org.ardulink.core.Pin;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.Tone;
import org.ardulink.core.events.CustomListener;
import org.ardulink.core.events.EventListener;
import org.ardulink.core.events.RplyListener;
import org.ardulink.core.linkmanager.LinkConfig;
import org.ardulink.core.linkmanager.providers.FactoriesViaMetaInfArdulink.GenericLinkFactory;
import org.junit.jupiter.api.Test;

public class FactoriesViaMetaInfArdulinkTest {

	private static final class TestLinkConfig implements LinkConfig {
	}

	private static class TestLinkWithoutConstructor implements Link {

		@Override
		public void close() throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public long switchDigitalPin(DigitalPin digitalPin, boolean value) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public long switchAnalogPin(AnalogPin analogPin, int value) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public long stopListening(Pin pin) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public long startListening(Pin pin) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public long sendTone(Tone tone) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public long sendNoTone(AnalogPin analogPin) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public long sendKeyPressEvent(char keychar, int keycode, int keylocation, int keymodifiers, int keymodifiersex)
				throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public long sendCustomMessage(String... messages) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public Link removeRplyListener(RplyListener listener) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public Link removeListener(EventListener listener) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public Link removeCustomListener(CustomListener listener) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public Link addRplyListener(RplyListener listener) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public Link addListener(EventListener listener) throws IOException {
			throw new UnsupportedOperationException();
		}

		@Override
		public Link addCustomListener(CustomListener listener) throws IOException {
			throw new UnsupportedOperationException();
		}
	}

	private static class TestLinkWithConstructor extends TestLinkWithoutConstructor {

		public TestLinkWithConstructor(TestLinkConfig config) {
			super();
		}

	}

	@Test
	void configClassNameNotOfTypeLinkConfig() {
		String configClassName = String.class.getName();
		assertThatThrownBy(() -> sut(configClassName, "SomeNotExistingClassName")).isInstanceOf(RuntimeException.class)
				.hasMessage(configClassName + " not of type " + LinkConfig.class.getName());
	}

	@Test
	void linkClassDoesNotExist() throws Exception {
		LinkConfig linkConfig = new LinkConfig() {
		};
		String configClassName = linkConfig.getClass().getName();
		GenericLinkFactory genericLinkFactory = sut(configClassName, "SomeNotExistingClassName");
		assertThatThrownBy(() -> genericLinkFactory.newLink(linkConfig)).isInstanceOf(ClassNotFoundException.class)
				.hasMessage("SomeNotExistingClassName");
	}

	@Test
	void linkClassHasNoConstructorWithArgumentOfTypeLinkConfig() throws Exception {
		TestLinkConfig config = new TestLinkConfig();
		String configClassName = config.getClass().getName();
		String linkClassName = TestLinkWithoutConstructor.class.getName();
		GenericLinkFactory genericLinkFactory = sut(configClassName, linkClassName);
		assertThatThrownBy(() -> genericLinkFactory.newLink(config)).isInstanceOf(RuntimeException.class)
				.hasMessage(linkClassName + " has no public constructor with argument of type " + configClassName);
	}

	@Test
	void ok() throws Exception {
		TestLinkConfig config = new TestLinkConfig();
		assertThat(sut(config.getClass().getName(), TestLinkWithConstructor.class.getName()).newLink(config))
				.isNotNull();
	}

	GenericLinkFactory sut(String configClassName, String linkClassName) throws ClassNotFoundException {
		return new GenericLinkFactory(getClass().getClassLoader(), "anyName", configClassName, linkClassName);
	}

}
