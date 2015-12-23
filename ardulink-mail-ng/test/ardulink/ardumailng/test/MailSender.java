package ardulink.ardumailng.test;

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
