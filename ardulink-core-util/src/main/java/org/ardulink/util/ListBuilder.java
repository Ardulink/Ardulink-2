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
package org.ardulink.util;

import static org.ardulink.util.anno.LapsedWith.JDK9;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.ardulink.util.anno.LapsedWith;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
@LapsedWith(value = JDK9, module = "List#of")
public class ListBuilder<T> {

	private final List<T> data = new ArrayList<>();

	private ListBuilder() {
		super();
	}

	public static <T> ListBuilder<T> newBuilder() {
		return new ListBuilder<>();
	}

	public ListBuilder<T> addAll(T... ts) {
		return addAll(Arrays.asList(ts));
	}

	public ListBuilder<T> addAll(Collection<T> ts) {
		this.data.addAll(ts);
		return this;
	}

	public ListBuilder<T> add(T t) {
		this.data.add(t);
		return this;
	}

	public List<T> build() {
		return new ArrayList<>(this.data);
	}

}
