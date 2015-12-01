package com.github.pfichtner.ardulink.core.linkmanager;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

public interface LinkConfig {

	@Retention(RUNTIME)
	public @interface Named {
		String value();
	}

	@Retention(RUNTIME)
	public @interface ChoiceFor {
		String value();
	}

}
