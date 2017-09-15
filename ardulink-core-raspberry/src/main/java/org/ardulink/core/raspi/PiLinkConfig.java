/**
Copyright 2013 project Ardulink http://www.ardulink.org/
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package org.ardulink.core.raspi;

import org.ardulink.core.linkmanager.LinkConfig;

import com.pi4j.io.gpio.PinPullResistance;

public class PiLinkConfig implements LinkConfig {

	@Named("pinPullResistance")
	private PinPullResistance pinPullResistance = PinPullResistance.PULL_DOWN;

	public PinPullResistance getPinPullResistance() {
		return pinPullResistance;
	}

	public void setPinPullResistance(PinPullResistance pinPullResistance) {
		this.pinPullResistance = pinPullResistance;
	}

}
