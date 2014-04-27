/**
Copyright 2013 Luciano Zu project Ardulink http://www.ardulink.org/

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

@author Luciano Zu
*/
package org.zu.ardulink.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class JAXBReaderWriter<T> {
	
	private Class<T> tClass;
	
	public JAXBReaderWriter(Class<T> tClass) {
		this.tClass = tClass;
	}

	public T read(String file) throws ReadingException {
		return read(new File(file));
	} 
	
	public T read(File file) throws ReadingException {
		try {
		    FileInputStream is = new FileInputStream(file);
			T retvalue = read(is);
		    
		    return retvalue;
		}
		catch (Exception e) {
			throw new ReadingException(e);
		}
	} 

	public T read(InputStream is) throws ReadingException {
		try {
		    // create JAXB context and instantiate Unmarshaller
		    JAXBContext context = JAXBContext.newInstance(tClass);

		    InputStreamReader reader = new InputStreamReader(is, "UTF-8");
		    
		    // get variables from our xml file
		    Unmarshaller um = context.createUnmarshaller();
		    @SuppressWarnings("unchecked")
			T retvalue = (T)um.unmarshal(reader);
		    
		    return retvalue;
		}
		catch (Exception e) {
			throw new ReadingException(e);
		}
	} 

	public void write(T t, String file) throws WritingException {
		write(t, new File(file));
	} 
	
	public void write(T t, File file) throws WritingException {
		try {

		    FileOutputStream os = new FileOutputStream(file);
		    write(t, os);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new WritingException(e);
		}
	}
	
	public void write(T t, OutputStream os) throws WritingException {
		try {
		    // create JAXB context and instantiate marshaller
		    JAXBContext context = JAXBContext.newInstance(t.getClass());
		    Marshaller m = context.createMarshaller();
		    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		    OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");
		    
		    // Write to File
		    m.marshal(t, writer);
		    
		    writer.close();
		    os.close();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new WritingException(e);
		}
	}
}
