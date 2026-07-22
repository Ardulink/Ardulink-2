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
import static java.lang.String.format;
import static java.util.Comparator.reverseOrder;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.joining;
import static org.ardulink.util.Throwables.propagate;
import static org.testcontainers.images.PullPolicy.defaultPolicy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.ardulink.testsupport.junit5.UseVirtualAvr.VirtualAvrExtension;
import org.ardulink.util.Throwables;
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

	interface FirmwareLoader {
		List<String> supportedProtocols();

		File loadFirmware(String uri, Path rootDir);
	}

	class ClasspathFirmwareLoader implements FirmwareLoader {

		@Override
		public List<String> supportedProtocols() {
			return List.of("classpath://");
		}

		@Override
		public File loadFirmware(String uri, Path rootDir) {
			String resourcePath = uri.substring("classpath://".length());
			String normalizedPath = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;
			try {
				URL resource = UseVirtualAvr.class.getResource(normalizedPath);
				if (resource == null) {
					throw new ExtensionConfigurationException("Classpath resource not found: " + normalizedPath);
				}
				String fileName = normalizedPath.substring(normalizedPath.lastIndexOf('/') + 1);
				File target = rootDir.resolve(fileName).toFile();
				try (InputStream in = resource.openStream()) {
					Files.write(target.toPath(), in.readAllBytes());
				}
				return target;
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

	}

	class HttpFirmwareLoader implements FirmwareLoader {

		@Override
		public List<String> supportedProtocols() {
			return List.of("http://", "https://");
		}

		@Override
		public File loadFirmware(String uri, Path rootDir) {
			try {
				URL url = new URI(uri).toURL();
				File target = rootDir.resolve(filename(url)).toFile();
				return downloadTo(url, target);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			} catch (URISyntaxException e) {
				throw propagate(e);
			}
		}

	}

	static class VirtualAvrExtension
			implements BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback, ParameterResolver {

		private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace
				.create(VirtualAvrExtension.class);

		static class FirmwareManager implements AutoCloseable {

			private final Path rootDir;
			private final ConcurrentHashMap<String, File> cache = new ConcurrentHashMap<>();
			private final List<FirmwareLoader> loaders = List.of(new HttpFirmwareLoader(),
					new ClasspathFirmwareLoader());

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
				return loaders.stream() //
						.filter(l -> l.supportedProtocols().stream().anyMatch(s -> uri.startsWith(s))) //
						.findFirst() //
						.map(l -> l.loadFirmware(uri, rootDir)) //
						.orElseGet(() -> {
							String supported = loaders.stream().flatMap(l -> l.supportedProtocols().stream())
									.collect(joining(", "));
							throw new ExtensionConfigurationException(
									format("Unsupported firmware URI scheme: %s (supported: %s)", uri, supported));
						});
			}

			@Override
			public void close() throws Exception {
				if (rootDir != null && Files.exists(rootDir)) {
					try (Stream<Path> walk = Files.walk(rootDir)) {
						walk.sorted(reverseOrder()).forEach(path -> {
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
			return context.getRoot().getStore(NAMESPACE) //
					.getOrComputeIfAbsent("firmwareManager", __ -> new FirmwareManager(), FirmwareManager.class);
		}

		@Override
		public void beforeAll(ExtensionContext context) {
			Class<?> testClass = context.getRequiredTestClass();
			validateConfiguration(testClass);
			if (needsSharedContainer(testClass)) {
				UseVirtualAvr classAnn = testClass.getAnnotation(UseVirtualAvr.class);
				File firmware = getOrCreateFirmwareManager(context).resolveFirmware(classAnn.firmware());
				VirtualAvrContainer<?> container = createContainer(classAnn.deviceName(), firmware);
				container.start();
				context.getStore(NAMESPACE).put("container", container);
			}
		}

		private void validateConfiguration(Class<?> testClass) {
			UseVirtualAvr classAnn = testClass.getAnnotation(UseVirtualAvr.class);
			if (classAnn != null && !classAnn.isolated() && hasIsolatedMethod(testClass)) {
				throw new ExtensionConfigurationException(
						format("Cannot mix class-level shared @%s with isolated methods",
								UseVirtualAvr.class.getSimpleName()));
			}
		}

		private boolean hasIsolatedMethod(Class<?> testClass) {
			return Arrays.stream(testClass.getDeclaredMethods()) //
					.map(m -> m.getAnnotation(UseVirtualAvr.class)) //
					.filter(Objects::nonNull) //
					.anyMatch(UseVirtualAvr::isolated);
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
			UseVirtualAvr config = findConfig(context).orElseThrow(() -> new ExtensionConfigurationException(
					format("@%s not found", UseVirtualAvr.class.getSimpleName())));
			if (config.isolated()) {
				File firmware = getOrCreateFirmwareManager(context).resolveFirmware(config.firmware());
				VirtualAvrContainer<?> container = createContainer(config.deviceName(), firmware);
				container.start();
				context.getStore(NAMESPACE).put("container", container);
			}
		}

		private static VirtualAvrContainer<?> createContainer(String deviceName, File firmware) {
			return virtualAvrContainer(firmware).withImagePullPolicy(defaultPolicy()).withDeviceName(deviceName);
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
			return context.getElement().flatMap(e -> Optional.ofNullable(e.getAnnotation(UseVirtualAvr.class)))
					.or(() -> Optional.ofNullable(context.getRequiredTestClass().getAnnotation(UseVirtualAvr.class)));
		}

		private boolean needsSharedContainer(Class<?> testClass) {
			UseVirtualAvr classAnnotation = testClass.getAnnotation(UseVirtualAvr.class);
			return (classAnnotation != null && !classAnnotation.isolated())
					|| (Arrays.stream(testClass.getDeclaredMethods()) //
							.map(m -> m.getAnnotation(UseVirtualAvr.class)) //
							.filter(Objects::nonNull) //
							.anyMatch(not(UseVirtualAvr::isolated)));
		}

		@Override
		public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
			return parameterContext.getParameter().getType().equals(VirtualAvrContainer.class);
		}

		@Override
		public Object resolveParameter(ParameterContext pc, ExtensionContext ctx) {
			ExtensionContext.Namespace classNamespace = ExtensionContext.Namespace.create(ctx.getRequiredTestClass(),
					NAMESPACE);
			Store store = ctx.getStore(classNamespace);
			VirtualAvrContainer<?> container = store.get("container", VirtualAvrContainer.class);
			if (container == null) {
				Store methodStore = ctx.getStore(NAMESPACE);
				container = methodStore.get("container", VirtualAvrContainer.class);
			}
			if (container == null) {
				throw new ExtensionConfigurationException(
						format("No VirtualAvrContainer available. Ensure @%s is properly configured.",
								UseVirtualAvr.class.getSimpleName()));
			}
			return container;
		}

	}

}
