package org.zu.ardulink.protocol.custommessages;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SimpleCustomMessageMakerTest {

	private SimpleCustomMessageMaker maker;

    @Before  
    public void setUp() {  
    	maker = new SimpleCustomMessageMaker();
    }  

    @After  
    public void cleanUp() {  
    	maker = null;
    }  
	
    @Test
	public void simpleCustomMessageMakerTest() {
		
		assertEquals("ID1/5", maker.getCustomMessage("ID1", String.valueOf(5)));
		assertEquals("ID1/TEXT1", maker.getCustomMessage("ID1", "TEXT1"));
		assertEquals("ID1/5/TEXT1", maker.getCustomMessage("ID1", String.valueOf(5), "TEXT1"));
	}

}
