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

/**
 * [ardulinktitle]
 * This class has utility methods for GUI components
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class UtilityModel {
	
	/**
	 * Generate a String array containing all string between a given range.
	 * i.e.
	 * 
	 * generateModelForCombo(2,4) generates {"2", "3", "4"}
	 * 
	 * @param minValue
	 * @param maxValue
	 * @return the string array
	 */
	public static String[] generateModelForCombo(int minValue, int maxValue) {
		String[] retvalue = null;

		if(minValue < maxValue) {
			retvalue = new String[maxValue - minValue + 1];
			for(int i=0; i < retvalue.length; i++) {
				retvalue[i] = "" + (i+minValue);
			}
		} else {
			retvalue = new String[] {""+maxValue};
		}
		
		return retvalue;
	}


}
