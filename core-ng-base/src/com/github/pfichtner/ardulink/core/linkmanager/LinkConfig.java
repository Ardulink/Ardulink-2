package com.github.pfichtner.ardulink.core.linkmanager;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

public interface LinkConfig {

	LinkConfig NO_ATTRIBUTES = new LinkConfig() {
		// no attributes
	};

	@Retention(RUNTIME)
	public @interface Named {
		String value();
	}

	@Retention(RUNTIME)
	public @interface ChoiceFor {
		String value();
	}

	@Retention(RUNTIME)
	public @interface I18n {
		String value();
	}

}
