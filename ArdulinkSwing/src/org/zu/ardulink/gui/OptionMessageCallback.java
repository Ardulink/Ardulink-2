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

package org.zu.ardulink.gui;

import java.awt.Component;

import javax.swing.JOptionPane;

import org.zu.ardulink.protocol.IProtocol;
import org.zu.ardulink.protocol.MessageInfo;
import org.zu.ardulink.protocol.ReplyMessageCallback;

/**
 * [ardulinktitle] [ardulinkversion]
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 */
public class OptionMessageCallback implements ReplyMessageCallback {

	private Component component = null;
	
	public OptionMessageCallback(Component component) {
		super();
		this.component = component;
	}

	@Override
	public void replyInfo(MessageInfo messageInfo) {
		if(messageInfo.getReply() == IProtocol.REPLY_OK) {
			JOptionPane.showMessageDialog(component, "Done", "Ok", JOptionPane.INFORMATION_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(component, "Error", "Ko", JOptionPane.ERROR_MESSAGE);
		}
	}

}
