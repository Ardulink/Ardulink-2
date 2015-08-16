package org.zu.ardulink.gui.facility;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

public class IntMinMaxModel extends AbstractListModel<Integer> implements
		ComboBoxModel<Integer> {

	private static final long serialVersionUID = -6314940179491347446L;

	private int low;
	private int high;

	private Integer selectedItem;

	public IntMinMaxModel(int low, int high) {
		this.low = low;
		this.high = high;
	}

	@Override
	public int getSize() {
		return high - low + 1;
	}

	@Override
	public Integer getElementAt(int index) {
		return Integer.valueOf(index + low);
	}

	@Override
	public void setSelectedItem(Object selectedItem) {
		this.selectedItem = (Integer) selectedItem;
	}

	@Override
	public Integer getSelectedItem() {
		return selectedItem;
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