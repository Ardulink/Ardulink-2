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
import static java.util.Arrays.asList;
import static org.ardulink.core.beans.finder.api.AttributeFinders.beanAttributes;
import static org.ardulink.core.beans.finder.api.AttributeFinders.directFieldAccess;
import static org.ardulink.core.beans.finder.api.AttributeFinders.propertyAnnotated;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
		Class<?> value();
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

	public static class BeanThatUsesEnumWithNonStringReturnType {
		@ThisAnnotationHasAnAttributeThatIsNotAstring(AtomicInteger.class)
		public List<String> values;
	}

	public static class BeanWithAnnoOnPublicFieldButUsingNotValueButSomeOtherAttribute {
		@OurOwnTestAnno(value = "notFoo", someOtherAttribute = "foo")
		public List<String> values;
	}

	public static enum MyEnum {
		A, B, C;
	}

	@Retention(RUNTIME)
	public @interface OurOwnTestAnnoWithEnumType {
		MyEnum value();

		MyEnum someOtherAttribute() default MyEnum.A;
	}

	public static class BeanWithAnnoOnPublicFieldButUsingNotValueButSomeOtherAttributeThatIsNotStringType {
		@OurOwnTestAnnoWithEnumType(value = MyEnum.B, someOtherAttribute = MyEnum.C)
		public List<String> values;
	}

	@Test
	void nonExistingProperty() {
		BeanProperties bp = BeanProperties.forBean(new BeanWithReadMethod());
		Attribute attribute = bp.getAttribute("aNonExisitingAttribute");
		assertThat(attribute).isNull();
	}

	@Test
	void canFindPropertyByReadMethod() {
		BeanProperties bp = BeanProperties.forBean(new BeanWithReadMethod());
		Attribute attribute = bp.getAttribute("foo");
		assertThat(attribute.getName()).isEqualTo("foo");
		assertThat(attribute.getType()).isEqualTo(String.class);
	}

	@Test
	void canFindPropertyByWriteMethod() {
		BeanProperties bp = BeanProperties.forBean(new BeanWithWriteMethod());
		Attribute attribute = bp.getAttribute("foo");
		assertThat(attribute.getName()).isEqualTo("foo");
		assertThat(attribute.getType()).isEqualTo(String.class);
	}

	@Test
	void canReadValue() throws Exception {
		String value = "bar";
		BeanWithReadAndWriteMethod bean = new BeanWithReadAndWriteMethod();
		bean.setFoo(value);
		BeanProperties bp = BeanProperties.forBean(bean);
		Attribute attribute = bp.getAttribute("foo");
		assertThat(attribute.getName()).isEqualTo("foo");
		assertThat(attribute.readValue()).isEqualTo(value);
	}

	@Test
	void canWriteValue() throws Exception {
		String value = "bar";
		BeanProperties bp = BeanProperties.forBean(new BeanWithReadAndWriteMethod());
		Attribute attribute = bp.getAttribute("foo");
		attribute.writeValue(value);
		assertThat(attribute.getName()).isEqualTo("foo");
		assertThat(attribute.readValue()).isEqualTo(value);
	}

	@Test
	void canFindPropertyByAnnotatedReadMethod() {
		BeanProperties bp = BeanProperties.builder(new BeanWithAnnotatedReadMethod())
				.using(beanAttributes(), propertyAnnotated(OurOwnTestAnno.class)).build();
		Attribute attribute = bp.getAttribute("foo");
		assertThat(attribute.getName()).isEqualTo("foo");
		assertThat(attribute.getType()).isEqualTo(String.class);
	}

	@Test
	void canFindPropertyByAnnotatedWriteMethod() {
		BeanProperties bp = BeanProperties.builder(new BeanWithAnnotatedWriteMethod())
				.using(beanAttributes(), propertyAnnotated(OurOwnTestAnno.class)).build();
		Attribute attribute = bp.getAttribute("foo");
		assertThat(attribute.getName()).isEqualTo("foo");
		assertThat(attribute.getType()).isEqualTo(String.class);
	}

	@Test
	void throwsExceptionsIfAnnotationHasNoValueAttribute() {
		Class<ThisAnnotationHasNoValueAttribute> clazz = ThisAnnotationHasNoValueAttribute.class;
		assertThatIllegalArgumentException().isThrownBy(() -> propertyAnnotated(clazz))
				.withMessageContainingAll(clazz.getName(), "has no attribute named value");
	}

	@Test
	void throwsExceptionsIfAnnotationHasNoAttributeWithThatName() {
		Class<ThisAnnotationHasNoValueAttribute> clazz = ThisAnnotationHasNoValueAttribute.class;
		String attributeName = "XXX-attribute-does-not-exist-XXX";
		assertThatIllegalArgumentException().isThrownBy(() -> propertyAnnotated(clazz, attributeName))
				.withMessageContainingAll(clazz.getName(), "has no attribute named " + attributeName);
	}

	@Test
	void worksWithAnnotationsThatAreNonStrings() {
		BeanProperties bp = BeanProperties.builder(new BeanThatUsesEnumWithNonStringReturnType())
				.using(beanAttributes(), propertyAnnotated(ThisAnnotationHasAnAttributeThatIsNotAstring.class)).build();
		Attribute attribute = bp.getAttribute(AtomicInteger.class);
		assertThat(attribute.getName()).isEqualTo(AtomicInteger.class.toString());
		assertThat(attribute.getType()).isEqualTo(List.class);
	}

	@Test
	void canDirectlyAccessFields() throws Exception {
		String value = "bar";
		BeanWithPublicField bean = new BeanWithPublicField();
		BeanProperties bp = BeanProperties.builder(bean).using(directFieldAccess()).build();
		Attribute attribute = bp.getAttribute("foo");
		assertThat(attribute.getName()).isEqualTo("foo");
		assertThat(attribute.getType()).isEqualTo(String.class);
		attribute.writeValue(value);
		assertThat(bean.foo).isEqualTo((Object) value);
	}

	@Test
	void canMergeDifferentTypes() throws Exception {
		BeanProperties bp = BeanProperties.builder(new BeanWithDifferentTypes())
				.using(propertyAnnotated(OurOwnTestAnno.class)).build();
		Attribute attribute = bp.getAttribute("weHaveToUseAnnotationsSinceThisWontWorkWithBeans");
		assertThat(attribute.getType()).isEqualTo(Collection.class);
	}

	@Test
	void canlistAttributes() {
		BeanProperties bp = BeanProperties.forBean(new BeanWithMultipleAttributes());
		assertThat(new ArrayList<>(bp.attributeNames())).containsExactly("a", "b");
	}

	@Test
	void canFindPropertyByAnnotatedField() throws Exception {
		BeanWithSettersAndGettersAndAnnoOnPrivateField bean = new BeanWithSettersAndGettersAndAnnoOnPrivateField();
		BeanProperties bp = BeanProperties.builder(bean).using(propertyAnnotated(OurOwnTestAnno.class)).build();
		Attribute attribute = bp.getAttribute("foo");
		assertThat(attribute.getName()).isEqualTo("foo");
		assertThat(attribute.getType()).isEqualTo(List.class);
		List<String> values1 = asList("1", "2", "3");
		attribute.writeValue(values1);
		assertThat(bean.getValues()).isEqualTo(values1);
		List<String> values2 = asList("3", "2", "1");
		bean.setValues(values2);
		assertThat(attribute.readValue()).isEqualTo(values2);
	}

	@Test
	void canFindPropertyByAnnotatedPublicField() throws Exception {
		BeanWithAnnoOnPublicField bean = new BeanWithAnnoOnPublicField();
		BeanProperties bp = BeanProperties.builder(bean)
				.using(beanAttributes(), propertyAnnotated(OurOwnTestAnno.class)).build();
		Attribute attribute = bp.getAttribute("foo");
		assertThat(attribute.getName()).isEqualTo("foo");
		assertThat(attribute.getType()).isEqualTo(List.class);
		List<String> values1 = asList("1", "2", "3");
		attribute.writeValue(values1);
		assertThat(bean.values).isEqualTo(values1);
		List<String> values2 = asList("3", "2", "1");
		bean.values = values2;
		assertThat(attribute.readValue()).isEqualTo(values2);
	}

	@Test
	void canFindPropertyByAnnotatedPublicFieldBitNotUsingValueButSomeOtherAttribute() throws Exception {
		BeanWithAnnoOnPublicFieldButUsingNotValueButSomeOtherAttribute bean = new BeanWithAnnoOnPublicFieldButUsingNotValueButSomeOtherAttribute();
		BeanProperties bp = BeanProperties.builder(bean)
				.using(beanAttributes(), propertyAnnotated(OurOwnTestAnno.class, "someOtherAttribute")).build();
		Attribute attribute = bp.getAttribute("foo");
		assertThat(attribute.getName()).isEqualTo("foo");
		assertThat(attribute.getType()).isEqualTo(List.class);
		// rest remains
	}

	@Test
	void canFindPropertyByAnnotatedPublicFieldBitNotUsingValueButSomeOtherAttributeThatIsNotStringType()
			throws Exception {
		BeanWithAnnoOnPublicFieldButUsingNotValueButSomeOtherAttributeThatIsNotStringType bean = new BeanWithAnnoOnPublicFieldButUsingNotValueButSomeOtherAttributeThatIsNotStringType();
		BeanProperties bp = BeanProperties.builder(bean)
				.using(beanAttributes(), propertyAnnotated(OurOwnTestAnnoWithEnumType.class, "someOtherAttribute"))
				.build();
		Attribute attribute = bp.getAttribute("C");
		assertThat(attribute.getName()).isEqualTo("C");
		assertThat(attribute.getType()).isEqualTo(List.class);
		List<String> values1 = asList("1", "2", "3");
		attribute.writeValue(values1);
		assertThat(bean.values).isEqualTo(values1);
		List<String> values2 = asList("3", "2", "1");
		bean.values = values2;
		assertThat(attribute.readValue()).isEqualTo(values2);
	}

}
