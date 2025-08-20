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
package org.ardulink.core.classloader;

import static java.util.stream.Collectors.toList;
import static org.ardulink.util.Preconditions.checkState;
import static org.ardulink.util.Throwables.propagate;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ModuleClassLoader extends URLClassLoader {

	public ModuleClassLoader(String moduleDir) {
		this(Path.of(moduleDir));
	}

	public ModuleClassLoader(Path moduleDir) {
		this(contextClassLoader(), moduleDir);
	}

	public ModuleClassLoader(ClassLoader parent, String moduleDir) {
		this(parent, Path.of(moduleDir));
	}

	public ModuleClassLoader(ClassLoader parent, Path moduleDir) {
		super(toUrls(list(moduleDir)), parent);
	}

	private static ClassLoader contextClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}

	private static URL[] toUrls(List<Path> files) {
		return files.stream().map(ModuleClassLoader::toURL).toArray(URL[]::new);
	}

	private static URL toURL(Path path) {
		try {
			checkState(Files.exists(path), "File %s not found", path);
			return path.toUri().normalize().toURL();
		} catch (MalformedURLException e) {
			throw propagate(e);
		}
	}

	private static List<Path> list(Path dir) {
		checkState(Files.exists(dir), "Directory %s not found", dir);
		try {
			return Files.list(dir).filter(ModuleClassLoader::isJar).collect(toList());
		} catch (IOException e) {
			throw propagate(e);
		}
	}

	private static boolean isJar(Path path) {
		return path.getFileName().toString().toLowerCase().endsWith(".jar");
	}
}
