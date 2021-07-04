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

package org.ardulink.core.raspi;

import static org.ardulink.util.anno.LapsedWith.JDK8;
import static org.junit.Assert.assertThrows;

import org.ardulink.core.convenience.Links;
import org.ardulink.util.anno.LapsedWith;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class PiLinkTest {

	@Test
	// TODO should do a Assume if we are on a raspi or not
	public void creatingInstanceWillFailOnX86withUnsatisfiedLinkError() {
		@LapsedWith(module = JDK8, value = "Lambda")
		ThrowingRunnable runnable = new ThrowingRunnable() {
			@Override
			public void run() throws Throwable {
				Links.getLink("ardulink://raspberry");
			}
		};
		assertThrows(UnsatisfiedLinkError.class, runnable);
	}

}
