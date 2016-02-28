package com.github.pfichtner.beans;

import static com.github.pfichtner.beans.finder.impl.FindByFieldAccess.directFieldAccess;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.zu.ardulink.util.Preconditions.checkNotNull;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;

import org.junit.Ignore;
import org.junit.Test;

public class AnnotationsTest {

	@Retention(RUNTIME)
	@interface AnotherAnno {
	}

	@Retention(RUNTIME)
	@interface SomeAnno {
	}

	class AnotherAnnoOnTheField {
		@SomeAnno
		@AnotherAnno
		public String string;
	}

	class AnotherAnnoOnTheFieldWithGetterAndSetter {
		@SomeAnno
		@AnotherAnno
		private String string;

		public String getString() {
			return string;
		}

		public void setString(String string) {
			this.string = string;
		}
	}

	class AnotherAnnoOnTheGetter {
		private String string;

		@SomeAnno
		@AnotherAnno
		public String getString() {
			return string;
		}

		public void setString(String string) {
			this.string = string;
		}
	}

	class AnotherAnnoOnTheSetter {
		private String string;

		public String getString() {
			return string;
		}

		@SomeAnno
		@AnotherAnno
		public void setString(String string) {
			this.string = string;
		}
	}

	class AnotherAnnoOnTheGetterAndSetter {
		private String string;

		@SomeAnno
		@AnotherAnno
		public String getString() {
			return string;
		}

		@SomeAnno
		@AnotherAnno
		public void setString(String string) {
			this.string = string;
		}
	}

	@Test
	public void anotherAnnoOnTheField() throws Exception {
		assertHasBothAnnotations(BeanProperties
				.builder(new AnotherAnnoOnTheField())
				.using(directFieldAccess()).build());
	}

	@Test
	@Ignore
	// not yet working
	public void anotherAnnoOnTheFieldWithGetterAndSetter() throws Exception {
		assertHasBothAnnotations(BeanProperties
				.builder(new AnotherAnnoOnTheFieldWithGetterAndSetter())
				.using(directFieldAccess()).build());
	}

	@Test
	public void testAnnoOnGetter() throws Exception {
		assertHasBothAnnotations(BeanProperties
				.forBean(new AnotherAnnoOnTheGetter()));
	}

	@Test
	public void testAnnoOnSetter() throws Exception {
		assertHasBothAnnotations(BeanProperties
				.forBean(new AnotherAnnoOnTheSetter()));
	}

	@Test
	public void testAnnoOnGetterAndSetter() throws Exception {
		assertHasBothAnnotations(BeanProperties
				.forBean(new AnotherAnnoOnTheGetterAndSetter()));
	}

	@SuppressWarnings("unchecked")
	private void assertHasBothAnnotations(BeanProperties beanProperties) {
		hasAnnotations(
				checkNotNull(beanProperties.getAttribute("string"),
						"no attribute named \"string\" found in %s",
						beanProperties), SomeAnno.class, AnotherAnno.class);
	}

	private void hasAnnotations(Attribute attribute,
			final Class<? extends Annotation>... annoClasses) {
		for (int i = 0; i < annoClasses.length; i++) {
			assertThat(attribute.getAnnotation(annoClasses[i]),
					is(notNullValue()));
		}
	}

}
