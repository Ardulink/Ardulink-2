package org.ardulink.core.serial.jssc;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.ardulink.core.linkmanager.LinkManager;
import org.ardulink.core.linkmanager.LinkManager.Configurer;
import org.ardulink.util.URIs;
import org.junit.Test;

public class I18NTest {

	@Test
	public void allAttributesHaveAdescription() {
		Configurer configurer = LinkManager.getInstance().getConfigurer(
				URIs.newURI("ardulink://serial-jssc"));
		for (String name : configurer.getAttributes()) {
			String description = configurer.getAttribute(name).getDescription();
			assertThat(name + " has no description", description,
					is(notNullValue()));
		}

	}

}
