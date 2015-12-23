package org.zu.ardulink.connection.pi;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.zu.ardulink.Link;
import org.zu.ardulink.protocol.IProtocol;
import org.zu.ardulink.protocol.LoggerReplyMessageCallback;
import org.zu.ardulink.protocol.MessageInfo;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RaspberryPIConnectionTest {
	
	private Link link;
	
	private boolean isInitialized = false;
	private boolean isLastTest = false;
	
    @Before  
    public void setUp() {
    	if(!isInitialized) {
        	RaspberryPIConnection connection = new RaspberryPIConnection();
        	link = Link.createInstance("Rasp Link", connection);
        	isInitialized = true;
    	}
    }  

    @After  
    public void cleanUp() {  
    	if(link != null && isLastTest) {
    		link.disconnect();
    		link = null;
    	}
    }  
	
    @Test
    public void t01GetPortList() {
    	Object[] portlist = link.getPortList().toArray();
    	assertArrayEquals(new Object[]{"Raspberry PI"}, portlist);
    }
    
    @Test
	public void t02Connect() {
    	assertTrue(link.connect());
    }

    @Test
    public void t03SendPowerPinSwitch() {
    	MessageInfo info = link.sendPowerPinSwitch(0, IProtocol.HIGH);
    	assertTrue(info.isSent());
    }

    @Test
    public void t04SendPowerPinSwitch() {
    	MessageInfo info = link.sendPowerPinSwitch(0, IProtocol.HIGH, new LoggerReplyMessageCallback());
    	isLastTest = true;
    	assertTrue(info.isSent());
    }

}
