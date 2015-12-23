package org.zu.ardulink.protocol.parser;

import org.zu.ardulink.protocol.ALProtocol;
import org.zu.ardulink.util.Strings;

public class ALProtocolMessageStore implements IProtocolMessageStore {

	// implementation can be better than a simple String
	private String buffer = "";
	
	
	@Override
	public void addMessageChunck(String chunck) {
		buffer += chunck;
	}

	@Override
	public boolean isMessageComplete() {
		boolean retvalue = false;
		
		int messageEnd = buffer.indexOf(Strings.bytes2String(ALProtocol.DEFAULT_OUTGOING_MESSAGE_DIVIDER)); 
		if(messageEnd != -1) {
			retvalue = true;
		}
		return retvalue;
	}

	@Override
	public String getNextMessage() {
		String retvalue = null;
		int messageEnd = buffer.indexOf(Strings.bytes2String(ALProtocol.DEFAULT_OUTGOING_MESSAGE_DIVIDER));
		if(messageEnd != -1) {
			retvalue = buffer.substring(0, messageEnd + 1);
			buffer = buffer.substring(messageEnd + 1);
		}
		return retvalue;
	}

}
