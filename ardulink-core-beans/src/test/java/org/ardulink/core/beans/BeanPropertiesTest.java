/**
Copyright 2013 project Ardulink http://www.ardulink.org/
 
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ardulink.core.beans;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.ardulink.core.beans.finder.impl.FindByAnnotation.propertyAnnotated;
import static org.ardulink.core.beans.finder.impl.FindByFieldAccess.directFieldAccess;
import static org.ardulink.core.beans.finder.impl.FindByIntrospection.beanAttributes;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
class BeanPropertiesTest {

	@Retention(RUNTIME)
	public @interface ThisAnnotationHasNoValueAttribute {
		// no attributes
	}

	@Retention(RUNTIME)
	public @interface ThisAnnotationHasAnAttributeThatIsNotAstring {
		boolean value();
	}

	@Retention(RUNTIME)
	public @interface OurOwnTestAnno {
		String value();

		String someOtherAttribute() default "";
	}

	public static class BeanWithReadMethod {
		public String getFoo() {
			throw new UnsupportedOperationException();
		}
	}

	public static class BeanWithWriteMethod {
		public void setFoo(String foo) {
			throw new UnsupportedOperationException();
		}
	}

	public static class BeanWithReadAndWriteMethod {

		private String foo;

		public String getFoo() {
			return foo;
		}

		public void setFoo(String foo) {
			this.foo = foo;
		}
	}

	public static class BeanWithAnnotatedReadMethod {
		@OurOwnTestAnno("foo")
		public String xxxxxxxxxxxxxxx() {
			throw new UnsupportedOperationException();
		}
	}

	public static class BeanWithPublicField {
		public String foo;
	}

	public static class BeanWithAnnotatedWriteMethod {
		@OurOwnTestAnno("foo")
		public void xxxxxxxxxxxxxxx(String xxxxxxxxxxxxxxx) {
			throw new UnsupportedOperationException();
		}
	}

	public static class BeanWithDifferentTypes {
		private List<String> values;

		@OurOwnTestAnno("weHaveToUseAnnotationsSinceThisWontWorkWithBeans")
		public Collection<String> getValues() {
			return values;
		}

		@OurOwnTestAnno("weHaveToUseAnnotationsSinceThisWontWorkWithBeans")
		public void setValues(List<String> values) {
			this.values = values;
		}
	}

	public static class BeanWithMultipleAttributes {
		public String getA() {
			throw new UnsupportedOperationException();
		}

		public int getB() {
			throw new UnsupportedOperationException();
		}
	}

	public static class BeanWithSettersAndGettersAndAnnoOnPrivateField {
		@OurOwnTestAnno("foo")
		private List<String> values;

		public List<String> getValues() {
			return values;
		}

		public void setValues(List<String> values) {
			this.values = values;
		}
	}

	public static class BeanWithAnnoOnPublicField {
		@OurOwnTestAnno("foo")
		public List<String> values;
	}

	public static class BeanWithAnnoOnPublicFieldButUsingNotValueButSomeOtherAttribute {
		@OurOwnTestAnno(value = "notFoo", someOtherAttribute = "foo")
		public List<String> values;
	}

	@Test
	void nonExistingProperty() {
		BeanProperties bp = BeanProperties.forBean(new BeanWithReadMethod());
		Attribute attribute = bp.getAttribute("aNonExisitingAttribute");
		assertThat(attribute, is(nullValue()));
	}

	@Test
	void canFindPropertyByReadMethod() {
		BeanProperties bp = BeanProperties.forBean(new BeanWithReadMethod());
		Attribute attribute = bp.getAttribute("foo");
		assertThat(attribute.getName(), is("foo"));
		assertThat(attribute.getType().getName(), is(String.class.getName()));
	}

	@Test
	void canFindPropertyByWriteMethod() {
		BeanProperties bp = BeanProperties.forBean(new BeanWithWriteMethod());
		Attribute attribute = bp.getAttribute("foo");
		assertThat(attribute.getName(), is("foo"));
		assertThat(attribute.getType().getName(), is(String.class.getName()));
	}

	@Test
	void canReadValue() throws Exception {
		String value = "bar";
		BeanWithReadAndWriteMethod bean = new BeanWithReadAndWriteMethod();
		bean.setFoo(value);
		BeanProperties bp = BeanProperties.forBean(bean);
		Attribute attribute = bp.getAttribute("foo");
		assertThat(attribute.getName(), is("foo"));
		assertThat(attribute.readValue(), is(value));
	}

	@Test
	void canWriteValue() throws Exception {
		String value = "bar";
		BeanProperties bp = BeanProperties.forBean(new BeanWithReadAndWriteMethod());
		Attribute attribute = bp.getAttribute("foo");
		attribute.writeValue("bar");
		assertThat(attribute.getName(), is("foo"));
		assertThat(attribute.readValue(), is(value));
	}

	@Test
	void canFindPropertyByAnnotatedReadMethod() {
		BeanProperties bp = BeanProperties.builder(new BeanWithAnnotatedReadMethod())
				.using(beanAttributes(), propertyAnnotated(OurOwnTestAnno.class)).build();
		Attribute attribute = bp.getAttribute("foo");
		assertThat(attribute.getName(), is("foo"));
		assertThat(attribute.getType().getName(), is(String.class.getName()));
	}

	@Test
	void canFindPropertyByAnnotatedWriteMethod() {
		BeanProperties bp = BeanProperties.builder(new BeanWithAnnotatedWriteMethod())
				.using(beanAttributes(), propertyAnnotated(OurOwnTestAnno.class)).build();
		Attribute attribute = bp.getAttribute("foo");
		assertThat(attribute.getName(), is("foo"));
		assertThat(attribute.getType().getName(), is(String.class.getName()));
	}

	@Test
	void throwsExceptionsIfAnnotationHasNoValueAttribute() {
		Class<ThisAnnotationHasNoValueAttribute> clazz = ThisAnnotationHasNoValueAttribute.class;
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> propertyAnnotated(clazz));
		assertThat(exception.getMessage(),
				allOf(containsString(clazz.getName()), containsString("has no attribute named value")));
	}

	@Test
	void throwsExceptionsIfAnnotationHasValueAttributeWithWrongType() {
		Class<ThisAnnotationHasAnAttributeThatIsNotAstring> clazz = ThisAnnotationHasAnAttributeThatIsNotAstring.class;
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> propertyAnnotated(clazz));
		assertThat(exception.getMessage(), allOf(containsString(clazz.getName()),
				containsString(String.class.getName()), containsString(boolean.class.getName())));
	}

	@Test
	void canDirectlyAccessFields() throws Exception {
		String value = "bar";
		BeanWithPublicField bean = new BeanWithPublicField();
		BeanProperties bp = BeanProperties.builder(bean).using(directFieldAccess()).build();
		Attribute attribute = bp.getAttribute("foo");
		assertThat(attribute.getName(), is("foo"));
		assertThat(attribute.getType().getName(), is(String.class.getName()));
		attribute.writeValue(value);
		assertThat(bean.foo, is((Object) value));
	}

	@Test
	void canMergeDifferentTypes() throws Exception {
		BeanProperties bp = BeanProperties.builder(new BeanWithDifferentTypes())
				.using(propertyAnnotated(OurOwnTestAnno.class)).build();
		Attribute attribute = bp.getAttribute("weHaveToUseAnnotationsSinceThisWontWorkWithBeans");
		assertThat(attribute.getType().getName(), is(Collection.class.getName()));
	}

	@Test
	void canlistAttributes() {
		BeanProperties bp = BeanProperties.forBean(new BeanWithMultipleAttributes());
		assertThat(new ArrayList<>(bp.attributeNames()), is(Arrays.asList("a", "b")));
	}

	@Test
	void canFindPropertyByAnnotatedField() throws Exception {
		BeanWithSettersAndGettersAndAnnoOnPrivateField bean = new BeanWithSettersAndGettersAndAnnoOnPrivateField();
		BeanProperties bp = BeanProperties.builder(bean).using(propertyAnnotated(OurOwnTestAnno.class)).build();
		Attribute attribute = bp.getAttribute("foo");
		assertThat(attribute.getName(), is("foo"));
		assertThat(attribute.getType().getName(), is(List.class.getName()));
		attribute.writeValue(Arrays.asList("1", "2", "3"));
		assertThat(bean.getValues(), is(Arrays.asList("1", "2", "3")));
		bean.setValues(Arrays.asList("3", "2", "1"));
		assertThat(attribute.readValue(), is(Arrays.asList("3", "2", "1")));
	}

	@Test
	void canFindPropertyByAnnotatedPublicField() throws Exception {
		BeanWithAnnoOnPublicField bean = new BeanWithAnnoOnPublicField();
		BeanProperties bp = BeanProperties.builder(bean)
				.using(beanAttributes(), propertyAnnotated(OurOwnTestAnno.class)).build();
		Attribute attribute = bp.getAttribute("foo");
		assertThat(attribute.getName(), is("foo"));
		assertThat(attribute.getType().getName(), is(List.class.getName()));
		attribute.writeValue(Arrays.asList("1", "2", "3"));
		assertThat(bean.values, is(Arrays.asList("1", "2", "3")));
		bean.values = Arrays.asList("3", "2", "1");
		assertThat(attribute.readValue(), is(Arrays.asList("3", "2", "1")));
	}

	@Test
	void canFindPropertyByAnnotatedPublicFieldBitNotUsingValueButSomeOtherAttribute() throws Exception {
		BeanWithAnnoOnPublicFieldButUsingNotValueButSomeOtherAttribute bean = new BeanWithAnnoOnPublicFieldButUsingNotValueButSomeOtherAttribute();
		BeanProperties bp = BeanProperties.builder(bean)
				.using(beanAttributes(), propertyAnnotated(OurOwnTestAnno.class, "someOtherAttribute")).build();
		Attribute attribute = bp.getAttribute("foo");
		assertThat(attribute.getName(), is("foo"));
		assertThat(attribute.getType().getName(), is(List.class.getName()));
		// rest remains
	}

}
