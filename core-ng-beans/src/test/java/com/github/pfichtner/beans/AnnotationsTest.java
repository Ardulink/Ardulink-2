package com.github.pfichtner.beans;

import static com.github.pfichtner.beans.finder.impl.FindByFieldAccess.directFieldAccess;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;

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

	class PrivateAnnotatedFieldWithGetterAndSetter {
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
	public void testAnnoOnField() throws Exception {
		assertHasBothAnnotations(BeanProperties
				.builder(new AnotherAnnoOnTheField())
				.using(directFieldAccess()).build());
	}

	@Test
	public void privateAnnotatedFieldWithGetterAndSetter() throws Exception {
		assertHasBothAnnotations(BeanProperties
				.builder(new PrivateAnnotatedFieldWithGetterAndSetter())
				.using(directFieldAccess()).build());
//		assertHasBothAnnotations(BeanProperties
//				.forBean(new PrivateAnnotatedFieldWithGetterAndSetter()));
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
		hasAnnotations(beanProperties.getAttribute("string"), SomeAnno.class,
				AnotherAnno.class);
	}

	private void hasAnnotations(Attribute attribute,
			final Class<? extends Annotation>... annoClasses) {
		assertThat(attribute.getAnnotations().length, is(annoClasses.length));
		for (int i = 0; i < annoClasses.length; i++) {
			assertThat(
					attribute.getAnnotations()[i].annotationType().getName(),
					is(annoClasses[i].getName()));
		}
	}

}
