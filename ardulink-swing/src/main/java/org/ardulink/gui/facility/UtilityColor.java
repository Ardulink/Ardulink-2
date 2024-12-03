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

package org.ardulink.gui.facility;

import static java.awt.Color.BLACK;
import static java.lang.Integer.toHexString;
import static org.ardulink.util.Strings.nullOrEmpty;

import java.awt.Color;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public final class UtilityColor {

	private UtilityColor() {
		super();
	}

	public static Color toColor(String hexString) {
		hexString = normalize(hexString);
		return hexString.isEmpty() ? BLACK : Color.decode(hexString);
	}

	private static String normalize(String hexString) {
		if (hexString != null && hexString.startsWith("#")) {
			hexString = hexString.substring(1);
		}
		return nullOrEmpty(hexString) ? "" : "#" + hexString;
	}

	public static String toString(Color color) {
		return "#" + toHexString(color.getRGB()).substring(2).toUpperCase();
	}

	public static int invert(int value) {
		return 255 - value;
	}

	public static Color invert(Color color) {
		return new Color(invert(color.getRed()), invert(color.getGreen()), invert(color.getBlue()));
	}

}
