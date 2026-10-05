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

package org.ardulink.core.proto.api;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.ardulink.core.linkmanager.Classloaders.MODULE_DIR_PROPERTY;
import static org.ardulink.core.proto.api.Protocols.protocolNames;
import static org.ardulink.core.proto.api.Protocols.tryProtoByName;
import static org.ardulink.core.proto.moduledir.ModuleDirOnlyProtocol.NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.ardulink.core.proto.moduledir.ModuleDirOnlyProtocol;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 * Protocols have to be discovered via the module directory just like links are,
 * otherwise a protocol that is only shipped next to an application stays
 * invisible. The binary distribution relies on exactly that:
 * {@code ardulink-core-firmata-proto.jar} sits in {@code lib} but is not on the
 * application classpath of the jars that are started with {@code java -jar}.
 */
class ProtocolsFromModuleDirTest {

	private static final String SERVICE_RESOURCE = "META-INF/services/" + Protocol.class.getName();

	@TempDir
	Path moduleDir;

	@AfterEach
	void clearModuleDir() {
		System.clearProperty(MODULE_DIR_PROPERTY);
	}

	@Test
	void protocolRegisteredByAJarOfTheModuleDirIsRegistered() throws IOException {
		createJar();
		useModuleDir();
		assertSoftly(s -> {
			s.assertThat(protocolNames()).contains(NAME);
			s.assertThat(tryProtoByName(NAME)).hasValueSatisfying( //
					proto -> assertThat(proto).isExactlyInstanceOf(ModuleDirOnlyProtocol.class));
		});
	}

	@Test
	void whenModuleDirIsNotSetThenTheJarIsNotFound() throws IOException {
		createJar();
		assertThat(tryProtoByName(NAME)).isEmpty();
	}

	@Test
	void protocolIsNotRegisteredWithoutAJarRegisteringIt() {
		useModuleDir();
		assertThat(tryProtoByName(NAME)).isEmpty();
	}

	private void createJar() throws IOException {
		Path jar = moduleDir.resolve("module-dir-only-protocol.jar");
		try (OutputStream out = Files.newOutputStream(jar); //
				JarOutputStream jarOut = new JarOutputStream(out)) {
			jarOut.putNextEntry(new JarEntry(SERVICE_RESOURCE));
			jarOut.write((ModuleDirOnlyProtocol.class.getName() + "\n").getBytes(UTF_8));
			jarOut.closeEntry();
		}
	}

	private void useModuleDir() {
		System.setProperty(MODULE_DIR_PROPERTY, moduleDir.toAbsolutePath().toString());
	}

}