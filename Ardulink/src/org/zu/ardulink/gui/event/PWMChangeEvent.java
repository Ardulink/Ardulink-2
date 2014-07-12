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

package org.zu.ardulink.gui.event;

import org.zu.ardulink.gui.PWMController;

/**
 * [ardulinktitle] [ardulinkversion]
 * It implements an event from PWMController
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 * @see PWMController, PWMControllerListener
 */
public class PWMChangeEvent {
	
	private int pwmValue = 0;
	private PWMController source;
	
	public PWMChangeEvent(PWMController source, int pwmValue) {
		super();
		this.pwmValue = pwmValue;
		this.source = source;
	}

	public int getPwmValue() {
		return pwmValue;
	}

	public PWMController getSource() {
		return source;
	}
}
