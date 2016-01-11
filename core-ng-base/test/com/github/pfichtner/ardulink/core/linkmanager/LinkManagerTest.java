package com.github.pfichtner.ardulink.core.linkmanager;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.net.URI;

import org.junit.Test;

import com.github.pfichtner.ardulink.core.linkmanager.LinkManager.Configurer;

public class LinkManagerTest {

	@Test
	public void onceQueriedChoiceValuesStayValid() throws Exception {
		LinkManager linkManager = LinkManager.getInstance();
		Configurer configurer = linkManager.getConfigurer(new URI(
				"ardulink://dummyLink"));

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

}
