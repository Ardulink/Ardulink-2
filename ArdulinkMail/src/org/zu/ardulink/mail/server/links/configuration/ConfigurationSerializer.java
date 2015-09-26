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

package org.zu.ardulink.mail.server.links.configuration;

import java.io.File;

import org.zu.ardulink.io.JAXBReaderWriter;
import org.zu.ardulink.io.ReadingException;
import org.zu.ardulink.io.WritingException;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ConfigurationSerializer {
	
	public static final String CONFIGURATION_FILE_NAME = "ArdulinkMailConfiguration.xml";

	public static AConfiguration read(String file) throws ReadingException {
		return read(new File(file));
	} 
	
	public static AConfiguration read(File file) throws ReadingException {
		JAXBReaderWriter<AConfiguration> reader = new JAXBReaderWriter<AConfiguration>(AConfiguration.class);
		return reader.read(file);
	} 
	
	public static void write(AConfiguration configuration, String file) throws WritingException {
		write(configuration, new File(file));
	} 
	
	public static void write(AConfiguration configuration, File file) throws WritingException {
		JAXBReaderWriter<AConfiguration> writer = new JAXBReaderWriter<AConfiguration>(AConfiguration.class);
		writer.write(configuration, file);
	} 

}
