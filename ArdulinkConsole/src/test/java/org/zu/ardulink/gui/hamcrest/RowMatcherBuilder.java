package org.zu.ardulink.gui.hamcrest;

import static java.util.Arrays.asList;

import java.awt.Component;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JPanel;

public class RowMatcherBuilder {

	public static List<? extends Component> componentsOf(JPanel panel) {
		return asList(panel.getComponents());
	}

	public static Object[] items(JComboBox jComboBox) {
		int itemCount = jComboBox.getItemCount();
		Object[] objects = new Object[itemCount];
		for (int i = 0; i < itemCount; i++) {
			objects[i] = jComboBox.getItemAt(i);
		}
		return objects;
	}

	public static BaseBuilder row(int row) {
		return new BaseBuilder(row);

	}

}
