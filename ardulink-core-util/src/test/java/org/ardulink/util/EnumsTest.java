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

import static org.ardulink.util.Strings.swapUpperLower;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class EnumsTest {

	enum TestEnum {
		A, B, C;
	}

	String cName = TestEnum.C.name();
	String swappedCName = swapUpperLower(cName);

	@Test
	void testEnumWithName() {
		assertThat(Enums.enumWithName(TestEnum.class, cName)).hasValue(TestEnum.C);
		assertThat(Enums.enumWithName(TestEnum.class, swappedCName)).isEmpty();
	}

	@Test
	void testEnumWithNameIgnoreCase() {
		assertThat(Enums.enumWithNameIgnoreCase(TestEnum.class, cName)).hasValue(TestEnum.C);
		assertThat(Enums.enumWithNameIgnoreCase(TestEnum.class, swappedCName)).hasValue(TestEnum.C);
	}

}
