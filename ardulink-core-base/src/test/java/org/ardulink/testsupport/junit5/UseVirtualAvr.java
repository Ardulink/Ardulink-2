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
import static java.util.function.Predicate.not;
import static org.testcontainers.images.PullPolicy.defaultPolicy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

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

	String firmware();

	static class VirtualAvrExtension
			implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback, ParameterResolver {

		private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace
				.create(VirtualAvrExtension.class);

		static class FirmwareManager implements ExtensionContext.Store.CloseableResource {

			private final Path rootDir;
			private final ConcurrentHashMap<String, File> cache = new ConcurrentHashMap<>();

			FirmwareManager() {
				try {
					this.rootDir = Files.createTempDirectory("ardulink-firmware");
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}

			File resolveFirmware(String firmwareUri) {
				return cache.computeIfAbsent(firmwareUri, this::loadFirmware);
			}

			private File loadFirmware(String uri) {
				if (uri.startsWith("https://") || uri.startsWith("http://")) {
					return downloadFromUrl(uri);
				} else if (uri.startsWith("classpath://")) {
					String resourcePath = uri.substring("classpath://".length());
					return loadFromClasspath(resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath);
				}
				throw new ExtensionConfigurationException(
						"Unsupported firmware URI scheme: " + uri + " (supported: https://, http://, classpath://)");
			}

			private File downloadFromUrl(String urlString) {
				try {
					URL url = new URL(urlString);
					File target = rootDir.resolve(filename(url)).toFile();
					return downloadTo(url, target);
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}

			private File loadFromClasspath(String resourcePath) {
				try {
					URL resource = UseVirtualAvr.class.getResource(resourcePath);
					if (resource == null) {
						throw new ExtensionConfigurationException("Classpath resource not found: " + resourcePath);
					}
					String fileName = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
					File target = rootDir.resolve(fileName).toFile();
					try (InputStream in = resource.openStream()) {
						Files.write(target.toPath(), in.readAllBytes());
					}
					return target;
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}

			@Override
			public void close() throws Exception {
				if (rootDir != null && Files.exists(rootDir)) {
					try (Stream<Path> walk = Files.walk(rootDir)) {
						walk.sorted(Comparator.reverseOrder())
								.forEach(path -> {
									try {
										Files.deleteIfExists(path);
									} catch (IOException e) {
										// best effort cleanup
									}
								});
					}
				}
			}

		}

		private static FirmwareManager getOrCreateFirmwareManager(ExtensionContext context) {
			ExtensionContext.Store rootStore = context.getRoot().getStore(NAMESPACE);
			synchronized (FirmwareManager.class) {
				FirmwareManager fm = rootStore.get("firmwareManager", FirmwareManager.class);
				if (fm == null) {
					fm = new FirmwareManager();
					rootStore.put("firmwareManager", fm);
				}
				return fm;
			}
		}

		@Override
		public void beforeAll(ExtensionContext context) {
			Class<?> testClass = context.getRequiredTestClass();
			validateConfiguration(testClass);
			if (needsSharedContainer(testClass)) {
				UseVirtualAvr classAnn = testClass.getAnnotation(UseVirtualAvr.class);
				FirmwareManager fm = getOrCreateFirmwareManager(context);
				File firmware = fm.resolveFirmware(classAnn.firmware());
				VirtualAvrContainer<?> container = createContainer(classAnn.deviceName(), firmware);
				container.start();
				context.getStore(NAMESPACE).put("container", container);
			}
		}

		private void validateConfiguration(Class<?> testClass) {
			UseVirtualAvr classAnn = testClass.getAnnotation(UseVirtualAvr.class);
			if (classAnn != null && !classAnn.isolated()) {
				boolean hasIsolatedMethod = Arrays.stream(testClass.getDeclaredMethods())
						.map(m -> m.getAnnotation(UseVirtualAvr.class))
						.filter(Objects::nonNull)
						.anyMatch(UseVirtualAvr::isolated);

				if (hasIsolatedMethod) {
					throw new ExtensionConfigurationException(
							"Cannot mix class-level shared @UseVirtualAvr with isolated methods");
				}
			}
		}

		@Override
		public void afterAll(ExtensionContext context) {
			Class<?> testClass = context.getRequiredTestClass();
			ExtensionContext.Namespace classNamespace = ExtensionContext.Namespace.create(testClass, NAMESPACE);
			Store store = context.getStore(classNamespace);
			VirtualAvrContainer<?> container = store.remove("container", VirtualAvrContainer.class);
			if (container != null) {
				container.stop();
			}
		}

		@Override
		public void beforeEach(ExtensionContext context) {
			UseVirtualAvr config = findConfig(context)
					.orElseThrow(() -> new ExtensionConfigurationException("@UseVirtualAvr not found"));
			if (config.isolated()) {
				FirmwareManager fm = getOrCreateFirmwareManager(context);
				File firmware = fm.resolveFirmware(config.firmware());
				VirtualAvrContainer<?> container = createContainer(config.deviceName(), firmware);
				container.start();
				context.getStore(NAMESPACE).put("container", container);
			}
		}

		private static VirtualAvrContainer<?> createContainer(String deviceName, File firmware) {
			return virtualAvrContainer(firmware)
					.withImagePullPolicy(defaultPolicy())
					.withDeviceName(deviceName);
		}

		@Override
		public void afterEach(ExtensionContext context) {
			Store store = context.getStore(NAMESPACE);
			VirtualAvrContainer<?> container = store.remove("container", VirtualAvrContainer.class);
			if (container != null) {
				container.stop();
			}
		}

		private Optional<UseVirtualAvr> findConfig(ExtensionContext context) {
			return context.getElement()
					.flatMap(e -> Optional.ofNullable(e.getAnnotation(UseVirtualAvr.class)))
					.or(() -> Optional.ofNullable(context.getRequiredTestClass().getAnnotation(UseVirtualAvr.class)));
		}

		private boolean needsSharedContainer(Class<?> testClass) {
			UseVirtualAvr classAnnotation = testClass.getAnnotation(UseVirtualAvr.class);
			if (classAnnotation != null && !classAnnotation.isolated()) {
				return true;
			}

			return Arrays.stream(testClass.getDeclaredMethods())
					.map(m -> m.getAnnotation(UseVirtualAvr.class))
					.filter(Objects::nonNull)
					.anyMatch(not(UseVirtualAvr::isolated));
		}

		@Override
		public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
			return parameterContext.getParameter().getType().equals(VirtualAvrContainer.class);
		}

		@Override
		public Object resolveParameter(ParameterContext pc, ExtensionContext ctx) {
			ExtensionContext.Namespace classNamespace = ExtensionContext.Namespace
					.create(ctx.getRequiredTestClass(), NAMESPACE);
			Store store = ctx.getStore(classNamespace);
			VirtualAvrContainer<?> container = store.get("container", VirtualAvrContainer.class);
			if (container == null) {
				Store methodStore = ctx.getStore(NAMESPACE);
				container = methodStore.get("container", VirtualAvrContainer.class);
			}
			if (container == null) {
				throw new ExtensionConfigurationException(
						"No VirtualAvrContainer available. Ensure @UseVirtualAvr is properly configured.");
			}
			return container;
		}

	}

}
