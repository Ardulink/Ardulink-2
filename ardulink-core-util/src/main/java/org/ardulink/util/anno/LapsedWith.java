package org.ardulink.util.anno;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;

/**
 * Elements marked with this annotation can be replaced by more efficient or
 * easier to read alternatives. To make search as easy as possible when
 * upgrading, value <b>should</b> be one of the constant values.
 */
@Retention(SOURCE)
public @interface LapsedWith {

	String JDK11 = "JDK11";
	String JDK14 = "JDK14";

	String value();

	String module() default "";

}
