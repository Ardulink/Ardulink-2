package org.ardulink.core.beans;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.ardulink.core.beans.finder.impl.FindByAnnotation.propertyAnnotated;
import static org.ardulink.core.beans.finder.impl.FindByFieldAccess.directFieldAccess;
import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.anno.LapsedWith.JDK8;
import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;

import org.ardulink.util.anno.LapsedWith;
import org.junit.jupiter.api.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class AnnotationsTest {

	@Retention(RUNTIME)
	@interface AnotherAnno {
	}

	@Retention(RUNTIME)
	public @interface SomeAnno {
		String value() default "";
	}

	class AnotherAnnoOnTheField {
		@SomeAnno
		@AnotherAnno
		public String string;
	}

	class AnotherAnnoOnTheFieldWithGetterAndSetter {
		@SomeAnno("string")
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
	void anotherAnnoOnTheField() throws Exception {
		assertHasBothAnnotations(
				BeanProperties.builder(new AnotherAnnoOnTheField()).using(directFieldAccess()).build());
	}

	@Test
	void anotherAnnoOnTheFieldWithGetterAndSetter() throws Exception {
		// this will only work if the property was found using propertyAnnotated
		// since when looking up via findByIntrospection there is no relation
		// between the reader/setter and the private field!
		assertHasBothAnnotations(BeanProperties.builder(new AnotherAnnoOnTheFieldWithGetterAndSetter())
				.using(propertyAnnotated(SomeAnno.class)).build());
	}

	@Test
	void testAnnoOnGetter() throws Exception {
		assertHasBothAnnotations(BeanProperties.forBean(new AnotherAnnoOnTheGetter()));
	}

	@Test
	void testAnnoOnSetter() throws Exception {
		assertHasBothAnnotations(BeanProperties.forBean(new AnotherAnnoOnTheSetter()));
	}

	@Test
	void testAnnoOnGetterAndSetter() throws Exception {
		assertHasBothAnnotations(BeanProperties.forBean(new AnotherAnnoOnTheGetterAndSetter()));
	}

	@SuppressWarnings("unchecked")
	private void assertHasBothAnnotations(BeanProperties beanProperties) {
		hasAnnotations(checkNotNull(beanProperties.getAttribute("string"), "no attribute named \"string\" found in %s",
				beanProperties), SomeAnno.class, AnotherAnno.class);
	}

	@LapsedWith(module = JDK8, value = "Streams")
	private void hasAnnotations(Attribute attribute, Class<? extends Annotation>... annoClasses) {
		for (Class<? extends Annotation> element : annoClasses) {
			assertThat(attribute.getAnnotation(element)).isNotNull()
					.withFailMessage(element.getSimpleName() + " not found");
		}
	}

}
