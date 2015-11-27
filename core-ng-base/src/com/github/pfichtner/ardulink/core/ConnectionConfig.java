package com.github.pfichtner.ardulink.core;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

public interface ConnectionConfig {

	@Retention(RUNTIME)
	public @interface Named {

		String value();

	}

	@Retention(RUNTIME)
	public @interface PossibleValueFor {

		String value();

	}

}
