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

import static org.ardulink.util.Preconditions.checkArgument;
import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Preconditions.checkState;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class PreconditionsTest {

	private static final String MESSAGE = "the message";
	private static final String MESSAGE_FORMAT = "the %d message";
	private static final String FORMATTED_MESSAGE = "the 42 message";

	@Test
	void testCheckArgumentOk() {
		assertDoesNotThrow(() -> checkArgument(true, MESSAGE));
	}

	@Test
	void testCheckArgumentNotOk() {
		assertThat(assertThatIllegalArgumentException().isThrownBy(() -> checkArgument(false, MESSAGE))
				.withMessage(MESSAGE));
	}

	@Test
	void testCheckArgumentNotOkWihArgsToFormat() {
		assertThat(assertThatIllegalArgumentException().isThrownBy(() -> checkArgument(false, MESSAGE_FORMAT, 42))
				.withMessage(FORMATTED_MESSAGE));
	}

	@Test
	void testCheckNotNullOk() {
		assertSame(this, checkNotNull(this, MESSAGE));
	}

	@Test
	void testCheckNotNullNotOk() {
		assertThat(assertThatNullPointerException().isThrownBy(() -> checkNotNull(null, MESSAGE)).withMessage(MESSAGE));
	}

	@Test
	void testCheckNotNullNotOkWihArgsToFormat() {
		assertThat(assertThatNullPointerException().isThrownBy(() -> checkNotNull(null, MESSAGE_FORMAT, 42))
				.withMessage(FORMATTED_MESSAGE));
	}

	@Test
	void testCheckStateOk() {
		assertDoesNotThrow(() -> checkState(true, MESSAGE));
	}

	@Test
	void testCheckStateNotOk() {
		assertThat(assertThatIllegalStateException().isThrownBy(() -> checkState(false, MESSAGE)).withMessage(MESSAGE));
	}

	@Test
	void testCheckStateNotOkWihArgsToFormat() {
		assertThat(assertThatIllegalStateException().isThrownBy(() -> checkState(false, MESSAGE_FORMAT, 42))
				.withMessage(FORMATTED_MESSAGE));
	}

}
