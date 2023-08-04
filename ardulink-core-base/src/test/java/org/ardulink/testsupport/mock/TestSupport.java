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

package org.ardulink.testsupport.mock;

import static java.util.UUID.randomUUID;
import static org.ardulink.testsupport.mock.MockLinkFactory.MockLinkConfig.NAME_ATTRIBUTE;

import org.ardulink.core.AbstractListenerLink;
import org.ardulink.core.Link;
import org.ardulink.core.convenience.LinkDelegate;
import org.ardulink.core.events.AnalogPinValueChangedEvent;
import org.ardulink.core.events.DigitalPinValueChangedEvent;
import org.mockito.internal.util.MockUtil;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public final class TestSupport {

	private TestSupport() {
		super();
	}

	public static String uniqueMockUri() {
		return mockUriWithName(randomUUID().toString());
	}

	public static String mockUriWithName(String name) {
		return String.format("ardulink://mock?%s=%s", NAME_ATTRIBUTE, name);
	}

	public static Link getMock(Link link) {
		return link == null || isMock(link) ? link : getMock(extractDelegated(link));
	}

	private static AbstractListenerLink getDummy(Link link) {
		return (AbstractListenerLink) getMock(link);
	}

	private static boolean isMock(Link link) {
		return MockUtil.isMock(link);
	}

	public static Link extractDelegated(Link link) {
		return ((LinkDelegate) link).getDelegate();
	}

	public static void fireEvent(Link link, AnalogPinValueChangedEvent event) {
		getDummy(link).fireStateChanged(event);
	}

	public static void fireEvent(Link link, DigitalPinValueChangedEvent event) {
		getDummy(link).fireStateChanged(event);
	}

}
