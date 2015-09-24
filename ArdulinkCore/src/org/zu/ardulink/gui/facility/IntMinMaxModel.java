package org.zu.ardulink.gui.facility;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

public class IntMinMaxModel extends AbstractListModel implements
		ComboBoxModel {

	private static final long serialVersionUID = -6314940179491347446L;

	private final int low;
	private final int size;

	private Integer selectedItem;

	public IntMinMaxModel(int low, int high) {
		this.low = low;
		this.size = high - low + 1;
	}

	@Override
	public int getSize() {
		return this.size;
	}

	@Override
	public Integer getElementAt(int index) {
		return Integer.valueOf(index + this.low);
	}

	@Override
	public void setSelectedItem(Object selectedItem) {
		this.selectedItem = (Integer) selectedItem;
	}

	@Override
	public Integer getSelectedItem() {
		return this.selectedItem;
	}

	public IntMinMaxModel withSelectedItem(int selectedItem) {
		setSelectedItem(Integer.valueOf(selectedItem));
		return this;
	}

	public IntMinMaxModel withFirstItemSelected() {
		selectIndex(0);
		return this;
	}

	public IntMinMaxModel withLastItemSelected() {
		selectIndex(getSize() - 1);
		return this;
	}

	private void selectIndex(int index) {
		int size = getSize();
		if (size != 0 && index >= 0 && index < size) {
			withSelectedItem(getElementAt(index));
		}
	}

}