package org.ardulink.testsupport.junit5;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.atomic.AtomicBoolean;

import org.ardulink.testsupport.junit5.UseVirtualAvr.VirtualAvrExtension;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

import com.github.pfichtner.testcontainers.virtualavr.VirtualAvrContainer;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(VirtualAvrExtension.class)
public @interface UseVirtualAvr {

	static final String TTY_USB0 = "ttyUSB0";

	static class VirtualAvrExtension implements BeforeAllCallback, AfterAllCallback, ParameterResolver {

		private static final AtomicBoolean started = new AtomicBoolean(false);

		@SuppressWarnings("resource")
		private static final VirtualAvrContainer<?> container = new VirtualAvrContainer<>()
				.withDeviceName("ttyUSB0").withDeviceGroup("root").withDeviceMode(666);

		@Override
		public void beforeAll(ExtensionContext context) {
			if (started.compareAndSet(false, true)) {
				container.start();
			}
		}

		@Override
		public void afterAll(ExtensionContext context) {
			container.stop();
		}

		@Override
		public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
			return parameterContext.getParameter().getType().equals(VirtualAvrContainer.class);
		}

		@Override
		public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
			return container;
		}

	}

}
