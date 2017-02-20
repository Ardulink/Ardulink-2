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

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
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
		fireContentsChanged(this, -1, -1);
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