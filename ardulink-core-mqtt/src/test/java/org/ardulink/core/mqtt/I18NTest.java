package org.ardulink.core.mqtt;

import static org.ardulink.testsupport.i18n.I18NTestSupport.assertAllAttributesHaveDescriptions;

import org.junit.jupiter.api.Test;

class I18NTest {

	@Test
	void allAttributesHaveAdescription() {
		assertAllAttributesHaveDescriptions("ardulink://mqtt");
	}

}
