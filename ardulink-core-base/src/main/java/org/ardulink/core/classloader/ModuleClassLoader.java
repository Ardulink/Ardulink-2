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

import static org.ardulink.util.Preconditions.checkState;
import static org.ardulink.util.Throwables.propagate;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.ardulink.util.Lists;

public class ModuleClassLoader extends URLClassLoader {

	public ModuleClassLoader(String moduleDir) {
		this(new File(moduleDir));
	}

	public ModuleClassLoader(File moduleDir) {
		this(Thread.currentThread().getContextClassLoader(), moduleDir);
	}

	public ModuleClassLoader(ClassLoader parent, String moduleDir) {
		this(parent, new File(moduleDir));
	}

	public ModuleClassLoader(ClassLoader parent, File moduleDir) {
		super(toUrls(list(moduleDir)), parent);
	}

	private static URL[] toUrls(List<File> files) {
		List<URL> urls = Lists.newArrayList();
		for (File file : files) {
			urls.add(toURL(file));
		}
		return urls.toArray(new URL[urls.size()]);
	}

	private static URL toURL(File file) {
		try {
			checkState(file.exists(), "File %s not found", file);
			return file.toURI().normalize().toURL();
		} catch (MalformedURLException e) {
			throw propagate(e);
		}
	}

	private static List<File> list(File dir) {
		checkState(dir.exists(), "Directory %s not found", dir);
		List<File> files = Lists.newArrayList();
		for (String filename : dir.list(jarFiler())) {
			files.add(new File(dir, filename));
		}
		return files;
	}

	private static FilenameFilter jarFiler() {
		return new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(".jar");
			}
		};
	}

}
