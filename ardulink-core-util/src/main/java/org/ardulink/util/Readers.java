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
package org.ardulink.util;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.stream.Stream;

public final class Readers {

	private static final String lineSeparator = System.getProperty("line.separator");

	private Readers() {
		super();
	}

	public static String toString(Reader reader) throws IOException {
		return lines(reader).collect(joining(lineSeparator));
	}

	public static Stream<String> lines(Reader reader) throws IOException {
		try (BufferedReader bufferedReader = new BufferedReader(reader)) {
			// have to consume the stream because otherwise it's closed outside try/catch
			return bufferedReader.lines().collect(toList()).stream();
		}
	}

}
