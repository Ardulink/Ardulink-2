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

package org.ardulink.gui.event;

import static org.ardulink.util.anno.LapsedWith.JDK14;

import org.ardulink.util.anno.LapsedWith;

/**
 * [ardulinktitle] [ardulinkversion] project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 */
@LapsedWith(value = JDK14, module = "records")
public class PositionEvent {

	@LapsedWith(value = JDK14, module = "records")
	public static class Point {
		public final int x, y;

		public Point(int x, int y) {
			this.x = x;
			this.y = y;
		}

	}

	private final Point position;
	private final int maxSize;
	private final String id;

	public PositionEvent(Point position, int maxSize, String id) {
		this.position = position;
		this.maxSize = maxSize;
		this.id = id;
	}

	public Point position() {
		return position;
	}

	public int maxSize() {
		return maxSize;
	}

	public String id() {
		return id;
	}
}
