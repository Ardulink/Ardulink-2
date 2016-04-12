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
package org.ardulink.gui;

import static java.awt.GridBagConstraints.BOTH;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.LINE_START;

import java.awt.GridBagConstraints;
import java.awt.Insets;

public final class GridBagConstraintsBuilder {

	private final GridBagConstraints c;

	private GridBagConstraintsBuilder(int row, int column) {
		c = new GridBagConstraints();
		c.anchor = LINE_START;
		c.gridx = column;
		c.gridy = row;
		c.insets = new Insets(4, 4, 4, 4);
	}

	public static GridBagConstraintsBuilder constraints(int row, int column) {
		return new GridBagConstraintsBuilder(row, column);
	}

	public GridBagConstraintsBuilder fillHorizontal() {
		c.weightx = 1;
		c.fill = HORIZONTAL;
		return this;
	}

	public GridBagConstraintsBuilder fillBoth() {
		c.weightx = 1;
		c.weighty = 1;
		c.fill = BOTH;
		return this;
	}

	public GridBagConstraintsBuilder gridwidth(int width) {
		c.gridwidth = width;
		return this;
	}

	public GridBagConstraints build() {
		return c;
	}

}