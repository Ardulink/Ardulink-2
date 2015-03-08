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

import java.awt.Point;

import org.zu.ardulink.Link;
import org.zu.ardulink.gui.customcomponents.joystick.PositionEvent;
import org.zu.ardulink.gui.customcomponents.joystick.PositionListener;
import org.zu.ardulink.protocol.ReplyMessageCallback;

public class MotorDriver implements PositionListener, Linkable {

	private Link link = null;
	private ReplyMessageCallback replyMessageCallback;
	
	private int maxSize = 255;
	private int x = 0;
	private int y = 0;
	private String id = "none";
	
	private int rightPower = 0;
	private int leftPower  =  0;
	private String rightDirection = "F";
	private String leftDirection = "F";
	
	@Override
	public void setLink(Link link) {
		this.link = link;
	}

	@Override
	public ReplyMessageCallback getReplyMessageCallback() {
		return replyMessageCallback;
	}

	@Override
	public void setReplyMessageCallback(ReplyMessageCallback replyMessageCallback) {
		this.replyMessageCallback = replyMessageCallback;
	}

	@Override
	public void positionChanged(PositionEvent e) {
		synchronized (this) {
			maxSize = e.getMaxSize();
			Point p = e.getPosition();
			x = p.x;
			y = p.y;
			id = e.getId();

			computeMotorPower();
			sendMessage();
		}
	}

	private void computeMotorPower() {
		// Motor power is computed with a simple Linear Transformation with this matrix
		// -1 1
		//  1 1
		
		rightPower = -x + y;
		leftPower  =  x + y;
		rightDirection = "F";
		if(rightPower < 0) {
			rightDirection = "B";
			rightPower = -rightPower;
		}
		leftDirection = "F";
		if(leftPower < 0) {
			leftDirection = "B";
			leftPower = -leftPower;
		}
		if(rightPower > 255) {
			rightPower = 255;
		}
		if(leftPower > 255) {
			leftPower = 255;
		}
	}

	private void sendMessage() {
		if(link != null) {
			String message = id + "(" + leftDirection + leftPower + ")[" + rightDirection + rightPower + "]";
			System.out.println(message);
			link.sendCustomMessage(message, replyMessageCallback);
		}
		

	}
}
