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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
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
class OptionalsTest {

	String presentValue = "present";
	Optional<String> presentOptional = Optional.of(presentValue);
	Optional<String> emptyOptional = Optional.empty();

	String supplierValue = "other";
	Supplier<Optional<String>> supplingOther = () -> Optional.of(supplierValue);

	@Test
	void whenPresentAlwaysThePresentValueIsReturned() {
		assertThat(Optionals.or(presentOptional, supplingOther)).hasValue(presentValue);
		assertThat(Optionals.or(presentOptional, () -> emptyOptional)).hasValue(presentValue);
	}

	@Test
	void whenEmptyTheValueOfTheSupplierIsReturned() {
		assertThat(Optionals.or(emptyOptional, supplingOther)).hasValue(supplierValue);
		assertThat(Optionals.or(emptyOptional, () -> emptyOptional)).isEmpty();
	}

}
