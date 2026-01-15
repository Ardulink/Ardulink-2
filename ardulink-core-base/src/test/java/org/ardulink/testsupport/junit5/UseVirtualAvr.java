/**
Copyright 2013 project Ardulink http://www.ardulink.org/
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package org.ardulink.testsupport.junit5;

import static com.github.pfichtner.testcontainers.virtualavr.IOUtil.downloadTo;
import static com.github.pfichtner.testcontainers.virtualavr.IOUtil.filename;
import static com.github.pfichtner.testcontainers.virtualavr.TestcontainerSupport.virtualAvrContainer;
import static java.nio.file.Files.createTempDirectory;
import static java.util.function.Predicate.not;
import static org.testcontainers.images.PullPolicy.defaultPolicy;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URL;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.ardulink.testsupport.junit5.UseVirtualAvr.VirtualAvrExtension;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;

import com.github.pfichtner.testcontainers.virtualavr.VirtualAvrContainer;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(VirtualAvrExtension.class)
public @interface UseVirtualAvr {

	public static final String TTY_USB0 = "ttyUSB0";

	String deviceName() default TTY_USB0;

	boolean isolated() default false;

	static class VirtualAvrExtension
			implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback, ParameterResolver {

		private static final String REMOTE_INO_FILE = "https://github.com/Ardulink/Firmware/releases/download/v1.2.0/ArdulinkProtocol.ino.hex";
		private static final File inoFile = loadFromNet();

		static File loadFromNet() {
			try {
				URL inoUrl = new URL(REMOTE_INO_FILE);
				File target = Path
						.of(createTempDirectory("ardulink-firmware").toFile().getAbsolutePath(), filename(inoUrl))
						.toFile();
				return downloadTo(inoUrl, target);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		private static final AtomicBoolean started = new AtomicBoolean(false);

		private static final VirtualAvrContainer<?> sharedContainer = createContainer(TTY_USB0);
		static {
			Runtime.getRuntime().addShutdownHook(new Thread(sharedContainer::stop));
		}

		@Override
		public void beforeAll(ExtensionContext context) {
			Class<?> testClass = context.getRequiredTestClass();
			validateConfiguration(testClass);
			if (needsSharedContainer(testClass) && started.compareAndSet(false, true)) {
				sharedContainer.start();
			}
		}

		private void validateConfiguration(Class<?> testClass) {
			UseVirtualAvr classAnn = testClass.getAnnotation(UseVirtualAvr.class);
			if (classAnn != null && !classAnn.isolated()) {
				boolean hasIsolatedMethod = Arrays.stream(testClass.getDeclaredMethods())
						.map(m -> m.getAnnotation(UseVirtualAvr.class)).filter(Objects::nonNull)
						.anyMatch(UseVirtualAvr::isolated);

				if (hasIsolatedMethod) {
					throw new ExtensionConfigurationException(
							"Cannot mix class-level shared @UseVirtualAvr with isolated methods");
				}
			}
		}

		@Override
		public void afterAll(ExtensionContext context) {
			// DO NOTHING
			// Let the JVM shut it down, or use a shutdown hook if desired
		}

		@Override
		public void beforeEach(ExtensionContext context) {
			UseVirtualAvr config = findConfig(context)
					.orElseThrow(() -> new ExtensionConfigurationException("@UseVirtualAvr not found"));
			if (config.isolated()) {
				VirtualAvrContainer<?> container = createContainer(config.deviceName());
				container.start();
				context.getStore(ExtensionContext.Namespace.create(getClass(), context)).put("container", container);
			}
		}

		private static VirtualAvrContainer<?> createContainer(String deviceName) {
			return virtualAvrContainer(inoFile) //
					.withImagePullPolicy(defaultPolicy()) //
					.withDeviceName(deviceName);
		}

		@Override
		public void afterEach(ExtensionContext context) {
			Store store = context.getStore(ExtensionContext.Namespace.create(getClass(), context));
			VirtualAvrContainer<?> container = store.remove("container", VirtualAvrContainer.class);
			if (container != null) {
				container.stop();
			}
		}

		private Optional<UseVirtualAvr> findConfig(ExtensionContext context) {
			return context.getElement() //
					.flatMap(e -> Optional.ofNullable(e.getAnnotation(UseVirtualAvr.class))) //
					.or(() -> Optional.ofNullable(context.getRequiredTestClass().getAnnotation(UseVirtualAvr.class) //
					));
		}

		private boolean needsSharedContainer(Class<?> testClass) {
			// Case 1: annotation on class → shared container
			UseVirtualAvr classAnnotation = testClass.getAnnotation(UseVirtualAvr.class);
			if (classAnnotation != null && !classAnnotation.isolated()) {
				return true;
			}

			// Case 2: scan methods
			return Arrays.stream(testClass.getDeclaredMethods()).map(m -> m.getAnnotation(UseVirtualAvr.class))
					.filter(Objects::nonNull).anyMatch(not(UseVirtualAvr::isolated));
		}

		@Override
		public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
			return parameterContext.getParameter().getType().equals(VirtualAvrContainer.class);
		}

		@Override
		public Object resolveParameter(ParameterContext pc, ExtensionContext ctx) {
			Store store = ctx.getStore(ExtensionContext.Namespace.create(getClass(), ctx));
			VirtualAvrContainer<?> isolated = store.get("container", VirtualAvrContainer.class);
			return isolated == null ? sharedContainer : isolated;
		}

	}

}
