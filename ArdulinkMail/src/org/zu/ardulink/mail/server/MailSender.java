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

import static org.zu.ardulink.mail.server.ArdulinkMailConstants.MAIL_PASSWORD_KEY;
import static org.zu.ardulink.mail.server.ArdulinkMailConstants.MAIL_USER_KEY;
import static org.zu.ardulink.util.Preconditions.checkNotNull;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 *         [adsense]
 *
 */
public class MailSender {

	private static final Properties mailConfig = MailListener.getMailConfig();
	private static final Session session = Session.getDefaultInstance(
			mailConfig, new Authenticator() {

				private final PasswordAuthentication passwordAuthentication = new PasswordAuthentication(
						getUser(), getPassword());

				protected PasswordAuthentication getPasswordAuthentication() {
					return passwordAuthentication;
				}

			});

	public static void sendMail(Address[] to, String subject, String body)
			throws AddressException, MessagingException {

		Message message = new MimeMessage(session);
		message.setFrom(new InternetAddress(getUser()));
		message.setRecipients(Message.RecipientType.TO, to);
		message.setSubject(subject);
		message.setText(body);

		Transport.send(message);
	}

	private static String getUser() {
		return getNonNull(MAIL_USER_KEY);
	}

	private static String getPassword() {
		// TODO Luciano what to return if unset? null? empty String? Throw RTE?
		// For RTE see #getUser()
		return mailConfig.getProperty(MAIL_PASSWORD_KEY);
	}

	private static String getNonNull(String key) {
		return checkNotNull(mailConfig.getProperty(key), "%s must not be null",
				key);
	}

}
