package org.zu.ardulink.protocol.custommessages;

public interface CustomMessageSender {
	public void setCustomMessageMaker(CustomMessageMaker customMessageMaker);
	public CustomMessageMaker getCustomMessageMaker();
}
