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

import static java.util.function.Predicate.isEqual;
import static org.ardulink.util.Predicates.attribute;

import java.util.Arrays;
import java.util.Optional;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public final class LAFUtil {

	private LAFUtil() {
		super();
	}

	public static Optional<LookAndFeelInfo> findLookAndFeel(String lafName) {
		return Arrays.stream(UIManager.getInstalledLookAndFeels()) //
				.filter(attribute(LookAndFeelInfo::getName, isEqual(lafName))) //
				.findFirst();
	}

	public static void setLookAndFeel(String lafName) {
		findLookAndFeel(lafName).ifPresent(LAFUtil::setLookAndFeel);
	}

	private static void setLookAndFeel(LookAndFeelInfo lookAndFeelInfo) {
		try {
			UIManager.setLookAndFeel(lookAndFeelInfo.getClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			throw new RuntimeException(e);
		}
	}

}
