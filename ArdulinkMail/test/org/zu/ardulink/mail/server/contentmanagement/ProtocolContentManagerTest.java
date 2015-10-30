package org.zu.ardulink.mail.server.contentmanagement;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ProtocolContentManagerTest {
	
	private ProtocolContentManager protocolContentManager;
	private String content4ProtocolContentManager;
	private String contentNot4ProtocolContentManager;
	private List<String> mailContentHooks;
	private List<String> values;
	private List<String> aLinkNames;
	
    @Before  
    public void setUp() { 
    	protocolContentManager = new ProtocolContentManager();
    	content4ProtocolContentManager = "hi this is a content good for hooks";
    	contentNot4ProtocolContentManager = "hello this is not a content good for hooks";
    	mailContentHooks = Arrays.asList(new String[] {"is a"});
    	values = Arrays.asList(new String[] {"sendPowerPinSwitch(32, 1)"});
    	aLinkNames = Arrays.asList(new String[] {"FAKE LINK"});
    }  

    @After  
    public void cleanUp() {  
    	protocolContentManager = null;
    	content4ProtocolContentManager = null;
    	contentNot4ProtocolContentManager = null;
    	mailContentHooks = null;
    	values = null;
    	aLinkNames = null;
    }  
	
    @Test
	public void isForContentOk() {
    	
    	assertTrue(protocolContentManager.isForContent(content4ProtocolContentManager, mailContentHooks));
	}	

    @Test
	public void isForContentKo() {
    	
    	assertFalse(protocolContentManager.isForContent(contentNot4ProtocolContentManager, mailContentHooks));
	}	
    
    @Test
    public void execute() {
    	String message = protocolContentManager.execute(content4ProtocolContentManager, values, mailContentHooks, aLinkNames);
    	assertEquals("message sendPowerPinSwitch(32, 1) sent for link: FAKE LINK with this result: OK\n", message);
    }

}
