package org.zu.ardulink.mail.server.contentmanagement;

public class ProtocolContentManagerManualTest {

	public static void main(String[] args) {
		ProtocolContentManagerTest test = new ProtocolContentManagerTest();
		
		test.setUp();
		
		test.isForContentOk();
		test.isForContentKo();
		
		test.execute();
		
		test.cleanUp();
		
	}

}
