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

package org.ardulink.mail.test;

import static com.icegreen.greenmail.util.GreenMailUtil.sendTextEmailTest;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class MailSender {

	private String to;
	private String from;
	private String subject;
	private String body;

	public static void send(MailSender mailSender) {
		sendTextEmailTest(mailSender.to, mailSender.from, mailSender.subject, mailSender.body);
	}

	public static MailSender mailFrom(String from) {
		return new MailSender().from(from);
	}

	public static MailSender mailTo(String to) {
		return new MailSender().to(to);
	}

	public MailSender from(String from) {
		this.from = from;
		return this;
	}

	public MailSender to(String to) {
		this.to = to;
		return this;
	}

	public MailSender withSubject(String subject) {
		this.subject = subject;
		return this;
	}

	public MailSender withText(String body) {
		this.body = body;
		return this;
	}

}
