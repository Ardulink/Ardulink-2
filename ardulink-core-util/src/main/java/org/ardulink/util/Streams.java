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

import static org.ardulink.util.anno.LapsedWith.JDK8;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.ardulink.util.anno.LapsedWith;

@LapsedWith(value = JDK8, module = "BufferedReader#lines/Collectors#joining")
public final class Streams {

	private static final int BUFFER_SIZE = 1024;

	private Streams() {
		super();
	}

	public static String toString(InputStream inputStream) throws IOException {
		byte[] buffer = new byte[BUFFER_SIZE];
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(BUFFER_SIZE);
		try {
				int bytesRead;
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}
			return outputStream.toString();
		} finally {
			outputStream.close();
		}
	}

}
