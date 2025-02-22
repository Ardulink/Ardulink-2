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
package org.ardulink.console;

import static org.ardulink.core.Pin.analogPin;
import static org.ardulink.core.Pin.digitalPin;

import javax.swing.JComboBox;
import javax.swing.JSlider;
import javax.swing.JToggleButton;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;

/**
 * [ardulinktitle] [ardulinkversion] This is the ready ardulink console a
 * complete SWING application to manage an Arduino board. Console has several
 * tabs with all ready arduino components. Each tab is able to do a specific
 * action sending or listening for messages to arduino or from arduino board.
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class ConsoleIT {

	private static final String IS_HEADLESS = "java.awt.GraphicsEnvironment#isHeadless";

	@Test
	@DisabledIf(IS_HEADLESS)
	void canConnectToVirtualRandom() {
		String connection = "ardulink://virtual-console";

		ConsolePage page = new ConsolePage(new Console());
		page.useConnection(connection);

		page.connect();
		JToggleButton toggle = page.digitalSwitch(digitalPin(12));
		toggle.doClick(); // on
		toggle.doClick(); // off

		JSlider slider = page.analogSlider(analogPin(11));
		slider.setValue(42);
		slider.setValue(0);

		page.disconnect();

		// TODO verify the link interaction (console output)
	}

	@Test
	@DisabledIf(IS_HEADLESS)
	@Disabled("we need to spawn up the virtualavr first (prefered using testcontainers)")
	void canConnectToVirtualAvr() {
		String connection = "ardulink://serial-jssc";
		String virtualAvrDevice = "/dev/ttyUSB0";

		ConsolePage page = new ConsolePage(new Console());
		page.useConnection(connection);
		JComboBox<?> port = page.attributeChooser("port", JComboBox.class);
		port.setSelectedItem(virtualAvrDevice);

		page.connect();
		JToggleButton toggle = page.digitalSwitch(digitalPin(12));
		toggle.doClick(); // on
		toggle.doClick(); // off

		JSlider slider = page.analogSlider(analogPin(11));
		slider.setValue(42);
		slider.setValue(0);

		// TODO disconnect fails on jssc
		page.disconnect();
	}

}
