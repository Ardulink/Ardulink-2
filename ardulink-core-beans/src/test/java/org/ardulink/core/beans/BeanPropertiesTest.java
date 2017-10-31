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

import static org.ardulink.core.beans.finder.impl.FindByAnnotation.propertyAnnotated;
import static org.ardulink.core.beans.finder.impl.FindByFieldAccess.directFieldAccess;
import static org.ardulink.core.beans.finder.impl.FindByIntrospection.beanAttributes;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;

import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public class BeanPropertiesTest {

	@Rule
	public ExpectedException exception = ExpectedException.none();

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

	public static class BeanWithJustAprivateField {
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

	public static class BeanWithMultpleAttributes {
		public String getA() {
			throw new UnsupportedOperationException();
		}

		public int getB() {
			throw new UnsupportedOperationException();
		}
	}

	public static class BeanWithAnnoOnField {
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

	@Test
	public void canFindPropertyByReadMethod() {
		BeanProperties bp = BeanProperties.forBean(new BeanWithReadMethod());
		Attribute attribute = bp.getAttribute("foo");
		assertThat(attribute.getName(), is("foo"));
		assertThat(attribute.getType().getName(), is(String.class.getName()));
	}

	@Test
	public void canFindPropertyByWriteMethod() {
		BeanProperties bp = BeanProperties.forBean(new BeanWithWriteMethod());
		Attribute attribute = bp.getAttribute("foo");
		assertThat(attribute.getName(), is("foo"));
		assertThat(attribute.getType().getName(), is(String.class.getName()));
	}

	@Test
	public void canReadValue() throws Exception {
		String value = "bar";
		BeanWithReadAndWriteMethod bean = new BeanWithReadAndWriteMethod();
		bean.setFoo(value);
		BeanProperties bp = BeanProperties.forBean(bean);
		Attribute attribute = bp.getAttribute("foo");
		assertThat(attribute.getName(), is("foo"));
		assertThat(attribute.readValue(), is((Object) value));
	}

	@Test
	public void canWriteValue() throws Exception {
		String value = "bar";
		BeanProperties bp = BeanProperties
				.forBean(new BeanWithReadAndWriteMethod());
		Attribute attribute = bp.getAttribute("foo");
		attribute.writeValue("bar");
		assertThat(attribute.getName(), is("foo"));
		assertThat(attribute.readValue(), is((Object) value));
	}

	@Test
	public void canFindPropertyByAnnotatedReadMethod() {
		BeanProperties bp = BeanProperties
				.builder(new BeanWithAnnotatedReadMethod())
				.using(beanAttributes(),
						propertyAnnotated(OurOwnTestAnno.class)).build();
		Attribute attribute = bp.getAttribute("foo");
		assertThat(attribute.getName(), is("foo"));
		assertThat(attribute.getType().getName(), is(String.class.getName()));
	}

	@Test
	public void canFindPropertyByAnnotatedWriteMethod() {
		BeanProperties bp = BeanProperties
				.builder(new BeanWithAnnotatedWriteMethod())
				.using(beanAttributes(),
						propertyAnnotated(OurOwnTestAnno.class)).build();
		Attribute attribute = bp.getAttribute("foo");
		assertThat(attribute.getName(), is("foo"));
		assertThat(attribute.getType().getName(), is(String.class.getName()));
	}

	@Test
	public void throwsExceptionsIfAnnotationHasNoValueAttribute() {
		Class<ThisAnnotationHasNoValueAttribute> clazz = ThisAnnotationHasNoValueAttribute.class;
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(allOf(containsString(clazz.getName()),
				containsString("has no attribute named value")));
		propertyAnnotated(clazz);
	}

	@Test
	public void throwsExceptionsIfAnnotationHasValueAttributeWithWrongType() {
		Class<ThisAnnotationHasAnAttributeThatIsNotAstring> clazz = ThisAnnotationHasAnAttributeThatIsNotAstring.class;
		exception.expect(IllegalArgumentException.class);
		exception.expectMessage(allOf(containsString(clazz.getName()),
				containsString(String.class.getName()),
				containsString(boolean.class.getName())));
		propertyAnnotated(clazz);
	}

	@Test
	public void canDirectlyAccessFields() throws Exception {
		String value = "bar";
		BeanWithJustAprivateField bean = new BeanWithJustAprivateField();
		BeanProperties bp = BeanProperties.builder(bean)
				.using(directFieldAccess()).build();
		Attribute attribute = bp.getAttribute("foo");
		assertThat(attribute.getName(), is("foo"));
		assertThat(attribute.getType().getName(), is(String.class.getName()));
		attribute.writeValue(value);
		assertThat(bean.foo, is((Object) value));

	}

	@Test
	public void canMergeDifferentTypes() throws Exception {
		BeanProperties bp = BeanProperties
				.builder(new BeanWithDifferentTypes())
				.using(propertyAnnotated(OurOwnTestAnno.class)).build();
		Attribute attribute = bp
				.getAttribute("weHaveToUseAnnotationsSinceThisWontWorkWithBeans");
		assertThat(attribute.getType().getName(),
				is(Collection.class.getName()));
	}

	@Test
	public void canlistAttributes() {
		BeanProperties bp = BeanProperties
				.forBean(new BeanWithMultpleAttributes());
		assertThat(new ArrayList<String>(bp.attributeNames()),
				is(Arrays.asList("a", "b")));
	}

	@Test
	public void canFindPropertyByAnnotatedField() throws Exception {
		BeanWithAnnoOnField bean = new BeanWithAnnoOnField();
		BeanProperties bp = BeanProperties.builder(bean)
				.using(propertyAnnotated(OurOwnTestAnno.class)).build();
		Attribute attribute = bp.getAttribute("foo");
		assertThat(attribute.getName(), is("foo"));
		assertThat(attribute.getType().getName(), is(List.class.getName()));
		attribute.writeValue(Arrays.asList("1", "2", "3"));
		assertThat(bean.getValues(), is(Arrays.asList("1", "2", "3")));
		bean.setValues(Arrays.asList("3", "2", "1"));
		assertThat(attribute.readValue(),
				is((Object) Arrays.asList("3", "2", "1")));

	}

	@Test
	public void canFindPropertyByAnnotatedPublicField() throws Exception {
		BeanWithAnnoOnPublicField bean = new BeanWithAnnoOnPublicField();
		BeanProperties bp = BeanProperties
				.builder(bean)
				.using(beanAttributes(),
						propertyAnnotated(OurOwnTestAnno.class)).build();
		Attribute attribute = bp.getAttribute("foo");
		assertThat(attribute.getName(), is("foo"));
		assertThat(attribute.getType().getName(), is(List.class.getName()));
		attribute.writeValue(Arrays.asList("1", "2", "3"));
		assertThat(bean.values, is(Arrays.asList("1", "2", "3")));
		bean.values = Arrays.asList("3", "2", "1");
		assertThat(attribute.readValue(),
				is((Object) Arrays.asList("3", "2", "1")));

	}

}
