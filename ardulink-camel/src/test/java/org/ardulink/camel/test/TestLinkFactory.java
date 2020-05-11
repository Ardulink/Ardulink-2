package org.ardulink.camel.test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.ardulink.camel.test.TestLinkFactory.TestLinkConfig;
import org.ardulink.core.Link;
import org.ardulink.core.Pin;
import org.ardulink.core.Pin.AnalogPin;
import org.ardulink.core.Pin.DigitalPin;
import org.ardulink.core.Tone;
import org.ardulink.core.events.CustomListener;
import org.ardulink.core.events.EventListener;
import org.ardulink.core.events.RplyListener;
import org.ardulink.core.linkmanager.LinkConfig;
import org.ardulink.core.linkmanager.LinkFactory;

public class TestLinkFactory implements LinkFactory<TestLinkConfig> {
	
	public class TestLinkConfig implements LinkConfig {

		@Named("a")
		private String a;

		@Named("b")
		private TimeUnit b;

		public String getA() {
			return a;
		}

		public void setA(String a) {
			this.a = a;
		}

		public TimeUnit getB() {
			return b;
		}

		public void setB(TimeUnit b) {
			this.b = b;
		}

	}

	public class TestLink implements Link {

		private String a;
		private TimeUnit b;

		public TestLink(TestLinkConfig config) {
			a = config.a;
			b = config.b;
		}
		
		public String getA() {
			return a;
		}
		
		public TimeUnit getB() {
			return b;
		}
		
		@Override
		public void close() throws IOException {
		}

		@Override
		public Link addListener(EventListener listener) throws IOException {
			return null;
		}

		@Override
		public Link removeListener(EventListener listener) throws IOException {
			return null;
		}

		@Override
		public Link addRplyListener(RplyListener listener) throws IOException {
			return null;
		}

		@Override
		public Link removeRplyListener(RplyListener listener) throws IOException {
			return null;
		}

		@Override
		public Link addCustomListener(CustomListener listener) throws IOException {
			return null;
		}

		@Override
		public Link removeCustomListener(CustomListener listener)
				throws IOException {
			return null;
		}

		@Override
		public long startListening(Pin pin) throws IOException {
			return 0;
		}

		@Override
		public long stopListening(Pin pin) throws IOException {
			return 0;
		}

		@Override
		public long switchAnalogPin(AnalogPin analogPin, int value)
				throws IOException {
			return 0;
		}

		@Override
		public long switchDigitalPin(DigitalPin digitalPin, boolean value)
				throws IOException {
			return 0;
		}

		@Override
		public long sendKeyPressEvent(char keychar, int keycode, int keylocation,
				int keymodifiers, int keymodifiersex) throws IOException {
			return 0;
		}

		@Override
		public long sendTone(Tone tone) throws IOException {
			return 0;
		}

		@Override
		public long sendNoTone(AnalogPin analogPin) throws IOException {
			return 0;
		}

		@Override
		public long sendCustomMessage(String... messages) throws IOException {
			return 0;
		}

	}
	@Override
	public String getName() {
		return "testlink";
	}

	@Override
	public Link newLink(TestLinkConfig config) throws Exception {
		return new TestLink(config);
	}

	@Override
	public TestLinkConfig newLinkConfig() {
		return new TestLinkConfig();
	}

}
