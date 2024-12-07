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

import static org.ardulink.util.Preconditions.checkState;
import static org.ardulink.util.Suppliers.memoize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatRuntimeException;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class SuppliersTest {

	Supplier<Object> throwingSupplier = new Supplier<Object>() {

		private boolean called;

		@Override
		public Object get() {
			checkState(!called, "Only one call allowed");
			called = true;
			return new Object();
		}
	};

	@Test
	void throwingSupplierDoesThrowException() {
		assertThatNoException().isThrownBy(throwingSupplier::get);
		assertThatRuntimeException().isThrownBy(throwingSupplier::get);
	}

	@Test
	void canMemoize() {
		Supplier<Object> sut = memoize(throwingSupplier);
		assertThat(sut.get()).isSameAs(sut.get());
	}

	@Test
	void aMemoizeInstanceGetsNotDecoratedTwice() {
		Supplier<Object> sut = memoize(throwingSupplier);
		assertThat(sut).isSameAs(memoize(sut));
	}

}
