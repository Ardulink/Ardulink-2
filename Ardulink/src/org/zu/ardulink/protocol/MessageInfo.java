/**
Copyright 2013 Luciano Zu project Ardulink http://www.ardulink.org/

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

@author Luciano Zu
*/

package org.zu.ardulink.protocol;

/**
 * [ardulinktitle]
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class MessageInfo {
	private boolean sent = false;
	private long messageID = IProtocol.UNDEFINED_ID;
	private String messageSent = null;
	private String messageReceived = null;
	private int reply = IProtocol.UNDEFINED_REPLY;
	private ReplyMessageCallback callback = null;
	
	public MessageInfo() {
	}
	
	/**
	 * 
	 * @param result
	 * @param messageID
	 */
	public MessageInfo(boolean result, long messageID) {
		this.sent = result;
		this.messageID = messageID;
	}

	public MessageInfo(boolean result) {
		this.sent = result;
	}

	public boolean isSent() {
		return sent;
	}
	public void setSent(boolean result) {
		this.sent = result;
	}
	public long getMessageID() {
		return messageID;
	}
	public void setMessageID(long messageID) {
		this.messageID = messageID;
	}

	public String getMessageSent() {
		return messageSent;
	}

	public void setMessageSent(String messageSent) {
		this.messageSent = messageSent;
	}

	public int getReply() {
		return reply;
	}

	public void setReply(int reply) {
		this.reply = reply;
	}

	public ReplyMessageCallback getCallback() {
		return callback;
	}

	public void setCallback(ReplyMessageCallback callback) {
		this.callback = callback;
	}

	public String getMessageReceived() {
		return messageReceived;
	}

	public void setMessageReceived(String messageReceived) {
		this.messageReceived = messageReceived;
	}
	
}
