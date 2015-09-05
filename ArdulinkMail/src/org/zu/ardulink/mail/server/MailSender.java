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

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MailSender implements ArdulinkMailConstants {
	
	private static Properties mailConfig;
	private static boolean isSMTPinitiated = false;
	private static Session session;

	static {
		
		mailConfig = MailListener.getMailConfig();
		initSMTP();
		
	}

	public static void sendMail(Address[] to, String subject, String body) throws AddressException, MessagingException {

		if(isSMTPinitiated) {
			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(mailConfig.getProperty(MAIL_USER_KEY)));
			message.setRecipients(Message.RecipientType.TO, to);
			message.setSubject(subject);
			message.setText(body);

			Transport.send(message);		
		}
	}

	
	
	
	private static void initSMTP() {
		session = Session.getDefaultInstance(mailConfig,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(mailConfig.getProperty(MAIL_USER_KEY), mailConfig.getProperty(MAIL_PASSWORD_KEY));
					}
				});
		
		
		isSMTPinitiated = true;
	}

}
