package org.zu.ardulink.mail.server;

public class MailSenderManualTest {

	public static void main(String[] args) {
		
		MailSenderTest test = new MailSenderTest();
		
		test.setUp();
		test.sendMail();
		test.cleanUp();

	}

}
