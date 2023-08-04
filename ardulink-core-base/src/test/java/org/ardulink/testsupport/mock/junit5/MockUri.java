package org.ardulink.testsupport.mock.junit5;

import static java.lang.String.format;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.ardulink.testsupport.mock.TestSupport.mockUriWithName;
import static org.ardulink.testsupport.mock.TestSupport.uniqueMockUri;
import static org.assertj.core.util.Strings.isNullOrEmpty;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotatedFields;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;
import static org.junit.platform.commons.util.ReflectionUtils.makeAccessible;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.function.Predicate;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.ReflectionUtils;

@Retention(RUNTIME)
@Target({ ANNOTATION_TYPE, FIELD, PARAMETER })
@ExtendWith(MockUriProvider.class)
public @interface MockUri {
	String name() default "";
}

class MockUriProvider implements BeforeEachCallback, BeforeAllCallback, ParameterResolver {

	static final Class<MockUri> ANNO_TYPE = MockUri.class;
	static final Namespace NAMESPACE = Namespace.create(MockUriProvider.class);

	@Override
	public void beforeAll(ExtensionContext context) {
		injectStaticFields(context, context.getRequiredTestClass());
	}

	@Override
	public void beforeEach(ExtensionContext context) {
		context.getRequiredTestInstances().getAllInstances() //
				.forEach(instance -> injectInstanceFields(context, instance));
	}

	private void injectStaticFields(ExtensionContext context, Class<?> testClass) {
		injectFields(context, null, testClass, ReflectionUtils::isStatic);
	}

	private void injectInstanceFields(ExtensionContext context, Object instance) {
		injectFields(context, instance, instance.getClass(), ReflectionUtils::isNotStatic);
	}

	private void injectFields(ExtensionContext context, Object testInstance, Class<?> testClass,
			Predicate<Field> predicate) {
		findAnnotatedFields(testClass, ANNO_TYPE, predicate).forEach(field -> injectField(testInstance, field));
	}

	private void injectField(Object testInstance, Field field) {
		assertNonFinalField(field);
		assertSupportedType("field", field.getType());
		try {
			makeAccessible(field).set(testInstance, getUri(getAnnotation(field, ANNO_TYPE)));
		} catch (Throwable t) {
			ExceptionUtils.throwAsUncheckedException(t);
		}
	}

	private <T extends Annotation> T getAnnotation(Field field, Class<T> anno) {
		return findAnnotation(field, anno).orElseThrow(
				() -> new JUnitException(format("Field %s must be annotated with @%s", field, anno.getSimpleName())));
	}

	private <T extends Annotation> T getAnnotation(ParameterContext parameterContext, Class<T> anno) {
		return parameterContext.findAnnotation(anno)
				.orElseThrow(() -> new JUnitException(format("Parameter %s must be annotated with @%s",
						parameterContext.getParameter(), anno.getSimpleName())));
	}

	private void assertNonFinalField(Field field) {
		if (ReflectionUtils.isFinal(field)) {
			throw new ExtensionConfigurationException(
					format("@%s field [%s] must not be declared as final.", ANNO_TYPE.getSimpleName(), field));
		}
	}

	private void assertSupportedType(String target, Class<?> type) {
		if (type != String.class) {
			throw new ExtensionConfigurationException(format("Can only resolve @%s %s of type %s but was: %s",
					ANNO_TYPE.getSimpleName(), target, String.class.getName(), type.getName()));
		}
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		boolean annotated = parameterContext.isAnnotated(ANNO_TYPE);
		if (annotated && parameterContext.getDeclaringExecutable() instanceof Constructor) {
			throw new ParameterResolutionException(
					format("@%s is not supported on constructor parameters. Please use field injection instead.",
							ANNO_TYPE.getSimpleName()));
		}
		return annotated;
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		Class<?> parameterType = parameterContext.getParameter().getType();
		assertSupportedType("parameter", parameterType);
		return getUri(getAnnotation(parameterContext, ANNO_TYPE));
	}

	private String getUri(MockUri mockUri) {
		String name = mockUri.name();
		return isNullOrEmpty(name) ? uniqueMockUri() : mockUriWithName(name);
	}

}
