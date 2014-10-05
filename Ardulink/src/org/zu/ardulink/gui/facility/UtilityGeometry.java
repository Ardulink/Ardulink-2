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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.SwingUtilities;

/**
 * [ardulinktitle] [ardulinkversion]
 * This class has utility methods for GUI components
 * @author Luciano Zu project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class UtilityGeometry {

	public static void setAlignmentCentered(Component component, Component referredComponent) {
		if(referredComponent == null) {
			referredComponent = SwingUtilities.getRoot(component);
		}
		Point rootLocation = referredComponent.getLocation();
		Dimension rootDimension = referredComponent.getSize();
		Dimension componentDimension = component.getSize();
		
		Point componentLocation = new Point(rootLocation);
		int dx = (rootDimension.width - componentDimension.width) / 2;
		int dy = (rootDimension.height - componentDimension.height) / 2;
		componentLocation.translate(dx, dy);
		
		component.setLocation(componentLocation);
		
	}
	public static void setAlignmentCentered(Component component) {
		setAlignmentCentered(component, null);
	}
	
}
