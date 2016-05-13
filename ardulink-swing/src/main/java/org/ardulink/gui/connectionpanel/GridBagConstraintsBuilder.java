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
package org.ardulink.gui.connectionpanel;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.LINE_START;
import static java.awt.GridBagConstraints.VERTICAL;

import java.awt.GridBagConstraints;
import java.awt.Insets;

public final class GridBagConstraintsBuilder {

	private final GridBagConstraints c = new GridBagConstraints();

	private GridBagConstraintsBuilder() {
		super();
	}

	public static GridBagConstraintsBuilder constraints(int row, int column) {
		return new GridBagConstraintsBuilder().row(row).column(column)
				.anchor(LINE_START).insets(4, 4, 4, 4);
	}

	public GridBagConstraintsBuilder row(int row) {
		c.gridy = row;
		return this;
	}

	public GridBagConstraintsBuilder column(int column) {
		c.gridx = column;
		return this;
	}

	public GridBagConstraintsBuilder insets(int top, int left, int bottom,
			int right) {
		c.insets = new Insets(top, left, bottom, right);
		return this;
	}

	public GridBagConstraintsBuilder anchor(int anchor) {
		c.anchor = anchor;
		return this;
	}

	public GridBagConstraintsBuilder fillBoth() {
		return fillHorizontal().fillVertical().fill(BOTH);
	}

	public GridBagConstraintsBuilder fillHorizontal() {
		return weightX(1).fill(HORIZONTAL);
	}

	public GridBagConstraintsBuilder fillVertical() {
		return weightY(1).fill(VERTICAL);
	}

	public GridBagConstraintsBuilder weightX(int weight) {
		c.weightx = weight;
		return this;
	}

	public GridBagConstraintsBuilder weightY(int weight) {
		c.weighty = weight;
		return this;
	}

	private GridBagConstraintsBuilder fill(int type) {
		c.fill = type;
		return this;
	}

	public GridBagConstraintsBuilder gridwidth(int width) {
		c.gridwidth = width;
		return this;
	}

	public GridBagConstraintsBuilder gridheight(int height) {
		c.gridheight = height;
		return this;
	}

	public GridBagConstraints build() {
		return c;
	}

}