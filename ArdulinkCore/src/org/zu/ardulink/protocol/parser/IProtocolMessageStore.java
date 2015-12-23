package org.zu.ardulink.protocol.parser;

public interface IProtocolMessageStore {
	
	public void addMessageChunck(String chunck);
	
	public boolean isMessageComplete();
	
	public String getNextMessage();
}
