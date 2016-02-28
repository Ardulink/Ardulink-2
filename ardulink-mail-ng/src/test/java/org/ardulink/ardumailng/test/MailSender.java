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

package org.ardulink.ardumailng.test;

import static com.icegreen.greenmail.util.GreenMailUtil.sendTextEmailTest;

public class MailSender {

	private String to;
	private String from;
	private String subject;

	private MailSender(String to) {
		this.to = to;
	}

	public static MailSender sendMailTo(String to) {
		return new MailSender(to);
	}

	public MailSender from(String from) {
		this.from = from;
		return this;
	}

	public MailSender withSubject(String subject) {
		this.subject = subject;
		return this;
	}

	public void andText(String body) {
		sendTextEmailTest(to, from, subject, body);
	}

}
