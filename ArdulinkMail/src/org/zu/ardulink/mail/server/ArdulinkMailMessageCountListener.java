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
import static org.zu.ardulink.mail.server.ArdulinkMailConstants.MAIL_CONTENT_PASSWORD_KEY;
import static org.zu.ardulink.mail.server.ArdulinkMailConstants.MAIL_FROM_ADDRESSES_KEY;
import static org.zu.ardulink.mail.server.ArdulinkMailConstants.MAIL_VALIDATE_CONTENT_PASSWORD_KEY;
import static org.zu.ardulink.mail.server.ArdulinkMailConstants.MAIL_VALIDATE_FROM_KEY;
import static org.zu.ardulink.mail.server.ArdulinkMailConstants.TRUE;

import java.io.IOException;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.event.MessageCountAdapter;
import javax.mail.event.MessageCountEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class ArdulinkMailMessageCountListener extends MessageCountAdapter {

	private static Logger logger = LoggerFactory.getLogger(ArdulinkMailMessageCountListener.class);
	
	public void messagesAdded(MessageCountEvent ev) {
	    Message[] msgs = ev.getMessages();
		logger.info("Got {} new messages", msgs.length);

	    for (int i = 0; i < msgs.length; i++) {
			try {
				manageMessage(msgs[i]);
			} catch (IOException ioex) { 
			    ioex.printStackTrace();	
			} catch (MessagingException mex) {
			    mex.printStackTrace();
			}
	    }
	}

	private void manageMessage(Message message) throws MessagingException, IOException {
		logger.info("*****************************************************************************************************");
		logger.info("Message {}:", message.getMessageNumber());
	    
	    validateContentType(message);
	    validateFrom(message.getFrom());
	    String content = getContent(message);
	    validateContentPassword(content);
	    
	    String reply = execute(content);
	    if(reply != null) {
	    	
	    //	sendMail(message.getFrom(), "Re: " + message.getSubject(), reply);
	    }
	}

	private void sendMail(Address[] to, String subject, String body) throws MessagingException {
		
		MailSender.sendMail(to, subject, body);
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
						logger.info(
								"From Address: {} is not valid. Mail is not validated.",
								from[i]);
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
				logger.info("Content password not found. Password validation skipped.");
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
		
		ArdulinkExecutor executor = new ArdulinkExecutor();
		return executor.execute(content);
	}

}
