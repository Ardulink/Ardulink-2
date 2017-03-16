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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.ardulink.util.Throwables.propagate;

public final class LoadStream {

	private LoadStream() {
		super();
	}

	public static String asString(InputStream is) {
		byte[] buffer = new byte[1024];
		ByteArrayOutputStream os = new ByteArrayOutputStream(buffer.length);
		try {
			try {
				int bytesRead;
				while ((bytesRead = is.read(buffer)) != -1) {
					os.write(buffer, 0, bytesRead);
				}
			} finally {
				os.close();
			}
			return os.toString();
		} catch (IOException e) {
			throw propagate(e);
		}
	}

}
