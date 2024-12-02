package org.ardulink.gui.facility;

import java.util.Arrays;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

public final class LAFUtil {

	private LAFUtil() {
		super();
	}

	public static void setLookAndFeel(String lafName) {
		Arrays.stream(UIManager.getInstalledLookAndFeels()).filter(l -> lafName.equals(l.getName())).findFirst()
				.ifPresent(LAFUtil::use);
	}

	private static void use(LookAndFeelInfo lookAndFeelInfo) {
		try {
			UIManager.setLookAndFeel(lookAndFeelInfo.getClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			throw new RuntimeException(e);
		}
	}

}
