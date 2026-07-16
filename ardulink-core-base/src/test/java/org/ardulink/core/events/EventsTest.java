package org.ardulink.core.events;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import nl.jqno.equalsverifier.EqualsVerifier;

class EventsTest {

	@ParameterizedTest
	@ValueSource(classes = { //
			DefaultAnalogPinValueChangedEvent.class, //
			DefaultDigitalPinValueChangedEvent.class, //
			DefaultCustomEvent.class, //
	})
	void equalsContract(Class<?> clazz) {
		assertDoesNotThrow(EqualsVerifier.forClass(clazz)::verify);
	}

}
