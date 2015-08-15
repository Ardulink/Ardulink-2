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

package org.zu.ardulink.gui.facility;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

/**
 * [ardulinktitle] [ardulinkversion] This class has utility methods for GUI
 * components
 * 
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 *         [adsense]
 *
 */
public final class UtilityModel {

	private UtilityModel() {
		super();
	}

	/**
	 * Generate a ComboBoxModel containing all ints between a given range. i.e.
	 * 
	 * generateModelForCombo(2,4) generates a ComboBoxModel containing {2,3,4}
	 * 
	 * @param minValue
	 *            start value
	 * @param maxValue
	 *            end value
	 * @return new ComboBoxModel
	 */
	public static ComboBoxModel<Integer> generateModelForCombo(int minValue,
			int maxValue) {
		return new DefaultComboBoxModel<Integer>(values(minValue, maxValue));
	}

	private static Integer[] values(int minValue, int maxValue) {
		if (minValue >= maxValue) {
			return new Integer[] { maxValue };
		}
		Integer[] values = new Integer[maxValue - minValue + 1];
		for (int i = 0; i < values.length; i++) {
			values[i] = i + minValue;
		}
		return values;
	}

}
