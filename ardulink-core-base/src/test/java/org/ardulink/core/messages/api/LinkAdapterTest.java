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

package org.ardulink.core.messages.api;

import static org.hamcrest.CoreMatchers.containsString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.ardulink.core.Link;
import org.ardulink.core.messages.impl.DefaultToDeviceMessageCustom;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class LinkAdapterTest {

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Test
	public void canSendCustomMessage() throws IOException {
		Link link = mock(Link.class);
		LinkMessageAdapter linkMessageAdapter = new LinkMessageAdapter(link);
		linkMessageAdapter
				.sendMessage(new DefaultToDeviceMessageCustom("one", "two"));
		verify(link).sendCustomMessage("one", "two");
	}

	@Test
	public void unknownMessageWillResultInRTE() throws IOException {
		LinkMessageAdapter linkMessageAdapter = new LinkMessageAdapter(
				mock(Link.class));
		expectedException.expect(IllegalStateException.class);
		expectedException
				.expectMessage(containsString("OutMessage type not supported"));
		linkMessageAdapter.sendMessage(unknownOutMessage());
	}

	private ToDeviceMessage unknownOutMessage() {
		return new ToDeviceMessage() {
		};
	}

}
