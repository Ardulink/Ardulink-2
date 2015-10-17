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

package org.zu.ardulink.mail.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

import com.sun.mail.imap.IMAPFolder;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class MailListener implements ArdulinkMailConstants {

	private static Properties mailConfig;
	private static Folder inbox;
	
	public static void main(String[] args) throws MessagingException {
		initConfiguration();
		initInbox();
		System.out.println("Messaged in inbox: " + inbox.getMessageCount());
		
		// Add messageCountListener to listen for new messages
		inbox.addMessageCountListener(new ArdulinkMailMessageCountAdapter());
		
		// TODO pezzo da rivedere. Perché casto a IMAP se l'ho messo in configurazione? Perché
		// casto ad una classe proprietaria della SUN?
		// NON esiste codice più standard?
		// Se ho un listener non posso semplicemente ciclare questo thread con una sleep?
		// int freq = 1000;
		while(true) {
			IMAPFolder f = (IMAPFolder)inbox;
			f.idle();
			System.out.println("IDLE done");
		}
	}

	private static void initInbox() {
		try {
			System.out.println("Inbox initiating...");

	        Properties props = new Properties();
	        props.setProperty(MAIL_STORE_PROTOCOL_KEY, mailConfig.getProperty(MAIL_STORE_PROTOCOL_KEY));

	        // Used gmail with lesser secure authentication (https://www.google.com/settings/security/lesssecureapps)
	        // For a full access I should implement OAuth2 for ArdulinkMail (https://java.net/projects/javamail/pages/OAuth2)
	        
	        Session session = Session.getInstance(props, null);
			Store store = session.getStore();
			System.out.println(mailConfig.getProperty(MAIL_HOST_KEY) + " " + mailConfig.getProperty(MAIL_USER_KEY) + " " + mailConfig.getProperty(MAIL_PASSWORD_KEY));
	        store.connect(mailConfig.getProperty(MAIL_HOST_KEY), mailConfig.getProperty(MAIL_USER_KEY), mailConfig.getProperty(MAIL_PASSWORD_KEY));
	        
	        // clearDefault(store.getDefaultFolder()); // With GMAIL it doesn't work since "all messages" cannot be cleared.
	        clearMainFolder(store.getFolder("INBOX"));
	        
	        inbox = store.getFolder("INBOX");
	        inbox.open(Folder.READ_WRITE);
			System.out.println("Inbox initiated");
		} catch (Exception e) {
			throw new IllegalStateException("Error initiating inbox. Exiting...");
		}
	}

	/**
	 * Init the MailListener with the a config properties file like this:
	 * 
	 * mail.store.protocol=imaps
	 * 
	 * host=imap.gmail.com
	 * user=spazzolauzer@gmail.com
	 * password=pisello01
	 */
	private static void initConfiguration() {
		System.out.println("Searching for config file...");
		ClassLoader classLoader = MailListener.class.getClassLoader();
		InputStream is = classLoader.getResourceAsStream(MAIL_CONF_PROPERTIES_FILENAME);
		if(is == null) {
			System.out.println("config file not found in MailListener classpath");
			is = ClassLoader.getSystemResourceAsStream(MAIL_CONF_PROPERTIES_FILENAME);
			if(is == null) {
				System.out.println("config file not found in System classpath");
				File configFile = new File("./" + MAIL_CONF_PROPERTIES_FILENAME);
				if(configFile.exists()) {
					try {
						is = new FileInputStream(configFile);
						System.out.println("config file found in the working dir");
					} catch (FileNotFoundException e) {
						System.out.println("config file not found in the working dir");
					}
				} else {
					throw new IllegalStateException("config file not found in the working dir");
				}
			} else {
				throw new IllegalStateException("config file not found in System classpath");
			}
		} else {
			System.out.println("config file found in MailListener classpath");
		}
		
		loadConfiguration(is);
		System.out.println("Config file loaded.");
		try {
			is.close();
		} catch (IOException e) {
			// Not clear closed
		}
	}

	private static void loadConfiguration(InputStream is) {
		mailConfig = new Properties();
		try {
			mailConfig.load(is);
		} catch (IOException e) {
			throw new IllegalStateException("Error reading config file", e);
		}
	}

	private static void printFolders(Folder folder) throws MessagingException {
		System.out.println(folder.getFullName() + " - " + folder.getName());
		Folder[] childs = folder.list();
		System.out.println("************childs***************");
		for (int i = 0; i < childs.length; i++) {
			printFolders(childs[i]);
		}
		

//		Folder[] personalNamespaces = store.getPersonalNamespaces();
//		System.out.println("************personalNamespaces***************");
//		for (int i = 0; i < personalNamespaces.length; i++) {
//			System.out.println(personalNamespaces[i].getFullName() + " - " + personalNamespaces[i].getName());
//		}
//		
//		Folder[] sharedNamespaces = store.getSharedNamespaces();
//		System.out.println("************sharedNamespaces***************");
//		for (int i = 0; i < sharedNamespaces.length; i++) {
//			System.out.println(sharedNamespaces[i].getFullName() + " - " + sharedNamespaces[i].getName());
//		}
	}

	private static void clearMainFolder(Folder folder) throws MessagingException {
		System.out.println("Deleting old messages...");
		Folder[] childs = folder.list();
		for (int i = 0; i < childs.length; i++) {
			clear(childs[i]);
		}
		clear(folder);
		System.out.println("Messages deleted.");
	}

	private static void clear(Folder folder) throws MessagingException {
		if(folder.exists() && (folder.getType() & Folder.HOLDS_MESSAGES) == Folder.HOLDS_MESSAGES) {
			int totMessages = folder.getMessageCount();
			System.out.println(totMessages + " messages from folder: " + folder.getFullName());
			folder.open(Folder.READ_WRITE);
			Message[] messages = folder.getMessages();
			for (int i = 0; i < messages.length; i++) {
				System.out.println("Deleting message: " + (i+1) + " of " + totMessages);
				messages[i].setFlag(Flags.Flag.DELETED, true);
			}
			folder.close(true);
		}
		Folder[] childs = folder.list();
		for (int i = 0; i < childs.length; i++) {
			clear(childs[i]);
		}
	}

	public static Properties getMailConfig() {
		if(mailConfig == null) {
			initConfiguration();
		}
		return mailConfig;
	}
}
