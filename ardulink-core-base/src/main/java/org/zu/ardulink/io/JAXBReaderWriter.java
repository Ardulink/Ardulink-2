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
package org.zu.ardulink.io;

import static java.lang.Boolean.TRUE;
import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
@Deprecated
// Obsolete with ardulink-mail based on camel
public class JAXBReaderWriter<T> {

	private final Class<T> type;

	public JAXBReaderWriter(Class<T> type) {
		this.type = type;
	}

	public T read(String file) throws ReadingException {
		return read(new File(file));
	}

	public T read(File file) throws ReadingException {
		try {
			FileInputStream is = new FileInputStream(file);
			try {
				return read(is);
			} finally {
				is.close();
			}
		} catch (Exception e) {
			throw new ReadingException(e);
		}
	}

	public T read(InputStream is) throws ReadingException {
		try {
			InputStreamReader reader = new InputStreamReader(is, "UTF-8");
			try {
				return type.cast(createUnmarshaller().unmarshal(reader));
			} finally {
				reader.close();
			}
		} catch (Exception e) {
			throw new ReadingException(e);
		}
	}

	public void write(T t, String file) throws WritingException {
		write(t, new File(file));
	}

	public void write(T t, File file) throws WritingException {
		try {
			FileOutputStream os = new FileOutputStream(file);
			try {
				write(t, os);
			} finally {
				os.close();
			}
		} catch (Exception e) {
			throw new WritingException(e);
		}
	}

	public void write(T t, OutputStream os) throws WritingException {
		try {
			OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
			try {
				createMarshaller().marshal(t, writer);
			} finally {
				writer.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new WritingException(e);
		}
	}

	private Marshaller createMarshaller() throws JAXBException {
		Marshaller marshaller = createContext().createMarshaller();
		marshaller.setProperty(JAXB_FORMATTED_OUTPUT, TRUE);
		return marshaller;
	}

	private Unmarshaller createUnmarshaller() throws JAXBException {
		return createContext().createUnmarshaller();
	}

	private JAXBContext createContext() throws JAXBException {
		return JAXBContext.newInstance(type);
	}
}
