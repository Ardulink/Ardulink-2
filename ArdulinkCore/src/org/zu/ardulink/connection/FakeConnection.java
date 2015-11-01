package org.zu.ardulink.connection;

import java.util.Arrays;
import java.util.List;

import org.zu.ardulink.ConnectionContact;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * This is a fake connection used to create a Link without an Arduino. It hasn't a thread to manage messages coming from Arduino.
 * It manages only link created with ALProtocol and it manages call backs with all ok messages. Since it hasn't a thread for
 * incoming messages be careful with call backs that are done with the same caller thread.
 * 
 * @author Luciano Zu 
 * [adsense]
 */
public class FakeConnection implements Connection {
	
	private boolean connected = false;
	private ConnectionContact contact;

	@Override
	public List<String> getPortList() {
		return Arrays.asList(new String[]{"COM999"});
	}

	@Override
	public boolean connect(Object... params) {
		connected = true;
		return isConnected();
	}

	@Override
	public boolean disconnect() {
		connected = false;
		return !isConnected();
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public boolean writeSerial(String message) {

		String id = null;
		int idPosition = message.indexOf("?id=");
		if (idPosition != -1) {
			id = message.substring(idPosition + 4);
		}

		sendReply(id);
		return true;
	}

	@Override
	public boolean writeSerial(int numBytes, int[] message) {
		throw new RuntimeException("binary writeSerial should not be called for this connection");
	}

	/*
	 * Sends a reply in ALProtocol
	 */
	private void sendReply(String id) {
		if(id != null) {
			byte[] bytes = ("alp://rply/ok?id=" + id).getBytes();
			int[]  retmessage = new int[bytes.length];
			for (int i = 0; i < retmessage.length; i++) {
				retmessage[i] = bytes[i];
			}
			contact.parseInput("FAKE", retmessage.length, retmessage);
		}
	}

	@Override
	public void setConnectionContact(ConnectionContact contact) {
		this.contact = contact;
	}
}
