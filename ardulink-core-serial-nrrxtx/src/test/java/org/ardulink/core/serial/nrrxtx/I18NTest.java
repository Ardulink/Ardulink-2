package org.ardulink.core.serial.nrrxtx;

import static org.ardulink.testsupport.i18n.I18NTestSupport.assertAllAttributesHaveDescriptions;

import org.junit.Test;

public class I18NTest {

	@Test
	public void allAttributesHaveAdescription() {
		assertAllAttributesHaveDescriptions("ardulink://serial-nrrxtx");
	}

}
