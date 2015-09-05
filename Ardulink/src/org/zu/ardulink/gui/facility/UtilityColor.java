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

import java.awt.Color;

/**
 * [ardulinktitle] [ardulinkversion]
 * This class has some usefull methods to Color (color conversion and so on)
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class UtilityColor {
	
	public static Color toColor(String hexString) {
		int hexValue = 0;
		if(hexString != null && hexString.startsWith("#")) {
			hexString = hexString.substring(1);
		}
		
		try {
			hexValue = Integer.parseInt(hexString, 16);
		}
		catch(NumberFormatException e) {
			hexValue = 0;
		}
		
		Color retvalue = new Color(hexValue);
		
		return retvalue;
	}
	
	public static String toString(Color color) {
		int red = color.getRed();
		int green = color.getGreen();
		int blue = color.getBlue();
		StringBuilder builder = new StringBuilder(7);
		builder.append('#');
		builder.append(String.format("%02X", red));
		builder.append(String.format("%02X", green));
		builder.append(String.format("%02X", blue));
		return builder.toString();
	}

}
