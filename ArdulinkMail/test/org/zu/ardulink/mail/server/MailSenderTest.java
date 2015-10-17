package org.zu.ardulink.mail.server;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MailSenderTest {
	
	private InternetAddress address = null;
	private String subject = null;
	private String body = null;
	
    @Before  
    public void setUp() {  
    	try {
			address = new InternetAddress("luciano.zu@gmail.com");
		} catch (AddressException e) {
			e.printStackTrace();
		}
    	assertNotNull(address);
    	subject = "test message";
		body = "this is a test message body";
    }  

    @After  
    public void cleanUp() {  
    	address = null;
    	subject = null;
		body = null;
    }  
	
    @Test
	public void sendMail() {
		Address[] to = {address};
		try {
			MailSender.sendMail(to, subject, body);
		} catch (AddressException e) {
			e.printStackTrace();
			fail("ADDRESS:" + address.getAddress() + " - " + e.getMessage());
		} catch (MessagingException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

}
