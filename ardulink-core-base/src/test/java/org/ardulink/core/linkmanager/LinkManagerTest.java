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

package org.ardulink.core.linkmanager;

import static org.ardulink.util.URIs.newURI;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.ardulink.core.Link;
import org.ardulink.core.linkmanager.LinkManager.Configurer;
import org.ardulink.core.linkmanager.viaservices.AlLinkWithoutArealLinkFactoryWithConfig;
import org.ardulink.core.linkmanager.viaservices.AlLinkWithoutArealLinkFactoryWithoutConfig;
import org.junit.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class LinkManagerTest {

	LinkManager sut = LinkManager.getInstance();

	@Test
	public void onceQueriedChoiceValuesStayValid() throws Exception {
		Configurer configurer = sut
				.getConfigurer(newURI("ardulink://dummyLink"));

		choiceValuesOfDNowAre("x", "y");

		// let's query the possible values
		assertThat(configurer.getAttribute("d").getChoiceValues(),
				is(new Object[] { "x", "y" }));

		// now the possible values change from x and y to 1 and 2
		choiceValuesOfDNowAre("1", "2");

		// but because the client queried for x and y those two values should
		// stay valid beside 1 and 2 now are the valid choices
		configurer.getAttribute("d").setValue("y");

		// but when querying the choice values again the changes are reflected
		assertThat(configurer.getAttribute("d").getChoiceValues(),
				is(new Object[] { "1", "2" }));
	}

	private void choiceValuesOfDNowAre(String... values) {
		DummyLinkConfig.choiceValuesOfD.set(values);
	}

	@Test
	public void canLoadViaMetaInfServicesArdulinkLinkfactoryWithoutConfig() {
		Link link = sut.getConfigurer(
				newURI("ardulink://aLinkWithoutArealLinkFactoryWithoutConfig"))
				.newLink();
		assertThat(
				link,
				is(instanceOf(AlLinkWithoutArealLinkFactoryWithoutConfig.class)));
	}

	@Test
	public void canLoadViaMetaInfServicesArdulinkLinkfactoryWithConfig() {
		Link link = sut.getConfigurer(
				newURI("ardulink://aLinkWithoutArealLinkFactoryWithConfig"))
				.newLink();
		assertThat(link,
				is(instanceOf(AlLinkWithoutArealLinkFactoryWithConfig.class)));
	}

}
