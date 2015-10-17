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

import java.io.IOException;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ArdulinkMailMessageCountAdapter extends MessageCountAdapter implements ArdulinkMailConstants {

	public void messagesAdded(MessageCountEvent ev) {
	    Message[] msgs = ev.getMessages();
	    System.out.println("Got " + msgs.length + " new messages");

	    for (int i = 0; i < msgs.length; i++) {
			try {
				manageMessage(msgs[i]);
			} catch (IOException ioex) { 
			    ioex.printStackTrace();	
			} catch (MessagingException mex) {
			    mex.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
	    }
	}

	private void manageMessage(Message message) throws MessagingException, IOException {
	    System.out.println("-----");
	    System.out.println("Message " + message.getMessageNumber() + ":");
	    
	    validateContentType(message);
	    validateFrom(message.getFrom());
	    String content = getContent(message);
	    validateContentPassword(content);
	    
	    String reply = execute(content);
	    if(reply != null) {
		    System.out.println("Sending reply to caller...");
	    	MailSender.sendMail(message.getFrom(), "Re:" + message.getSubject(), reply + "\n\n YOU WROTE: \n\n" + content);
		    System.out.println("Mail sent.");
	    }
	}

	private void validateContentType(Message message) throws MessagingException, IOException {
		boolean isValid = false;
		if(message.isMimeType("text/plain")) { // TODO validare anche text/html
			isValid = true;
		} else if(message.isMimeType("multipart/*")) {
			isValid = isMultipartValid(message);
		}
		
		if(!isValid) {
			throw new MessagingException("MimeType " + message.getContentType() + " is not recognized");
		}
	}

	private boolean isMultipartValid(Message message) throws MessagingException, IOException {
		boolean retvalue = false;
		Multipart multipart = (Multipart)message.getContent();
		int count = multipart.getCount();
		for(int i = 0; i < count; i++) {
			BodyPart part = multipart.getBodyPart(i);
			if(part.isMimeType("text/plain")) {
				retvalue = true;
			}
		}
		
		return retvalue;
	}

	private void validateFrom(Address[] from) throws MessagingException {
		
		if(TRUE.equalsIgnoreCase(MailListener.getMailConfig().getProperty(MAIL_VALIDATE_FROM_KEY))) {
			String fromAddressesString = MailListener.getMailConfig().getProperty(MAIL_FROM_ADDRESSES_KEY);
			if(fromAddressesString != null) {
				String[] fromAddresses = fromAddressesString.split(";");
				boolean foundAll = true;
				for(int i = 0; i < from.length; i++) {
					boolean found = false;
					for(int j = 0; j < fromAddresses.length; j++) {
						if(from[i].toString().contains(fromAddresses[j])) {
							found = true;
						}
					}
					if(!found) {
						foundAll = false;
						System.out.println("From Address: " + from[i].toString() + " is not valid. Mail is not validated.");
					}
				}
				if(!foundAll) {
					throw new MessagingException("Mail is not validated. The from email address is not valid.");
				}
			} else {
				throw new MessagingException("Key " + MAIL_VALIDATE_FROM_KEY + " not found in mail-conf.properties file.");
			}
		}
		
	}	

	private void validateContentPassword(String content) throws IOException, MessagingException {
		if(TRUE.equalsIgnoreCase(MailListener.getMailConfig().getProperty(MAIL_VALIDATE_CONTENT_PASSWORD_KEY))) {
			String contentPassword = MailListener.getMailConfig().getProperty(MAIL_CONTENT_PASSWORD_KEY);
			if(contentPassword != null) {
				if(!content.contains(contentPassword)) {
					throw new MessagingException("Content password validation failed.");
				}
			} else {
				System.out.println("Content password not found. Password validation skipped.");
			}
		}
	}

	private String getContent(Message message) throws IOException, MessagingException {

		String retvalue = "";
		
		Object msgContent = message.getContent();
		if(msgContent instanceof Multipart) {
			Multipart multipart = (Multipart)message.getContent();
			int count = multipart.getCount();
			for(int i = 0; i < count; i++) {
				BodyPart part = multipart.getBodyPart(i);
				if(part.isMimeType("text/plain")) {
					retvalue += "Part" + i + ": " + part.getContent().toString();
				}
			}
		} else {
			retvalue = msgContent.toString();
		}
		
		return retvalue;
	}

	private String execute(String content) throws MessagingException {
		return new ArdulinkExecutor().execute(content);
	}

}
