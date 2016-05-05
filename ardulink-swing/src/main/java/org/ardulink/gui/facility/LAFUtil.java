package org.ardulink.gui.facility;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.UIManager.LookAndFeelInfo;

public final class LAFUtil {

	private LAFUtil() {
		super();
	}

	public static void setLookAndFeel(String lafName) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			UnsupportedLookAndFeelException {
		for (LookAndFeelInfo laf : UIManager.getInstalledLookAndFeels()) {
			if (lafName.equals(laf.getName())) {
				UIManager.setLookAndFeel(laf.getClassName());
			}
		}
	}

}
