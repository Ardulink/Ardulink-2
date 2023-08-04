package org.ardulink.testsupport.mock.junit5;

import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.ardulink.testsupport.mock.TestSupport.mockUriWithName;
import static org.ardulink.testsupport.mock.TestSupport.uniqueMockUri;
import static org.assertj.core.util.Strings.isNullOrEmpty;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotatedFields;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;
import static org.junit.platform.commons.util.ReflectionUtils.makeAccessible;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
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
@ExtendWith(MockUriProvider.class)
public @interface MockUri {
	String name() default "";
}

class MockUriProvider implements BeforeEachCallback, BeforeAllCallback, ParameterResolver {

	private static final Class<MockUri> ANNO_TYPE = MockUri.class;
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

		findAnnotatedFields(testClass, ANNO_TYPE, predicate).forEach(field -> {
			assertNonFinalField(field);
			assertSupportedType("field", field.getType());
			try {
				makeAccessible(field).set(testInstance, getUri(getAnnotation(field, ANNO_TYPE)));
			} catch (Throwable t) {
				ExceptionUtils.throwAsUncheckedException(t);
			}
		});
	}

	private <T extends Annotation> T getAnnotation(Field field, Class<T> anno) {
		return findAnnotation(field, anno).orElseThrow(
				() -> new JUnitException("Field " + field + " must be annotated with @" + anno.getSimpleName()));
	}

	private <T extends Annotation> T getAnnotation(ParameterContext parameterContext, Class<T> anno) {
		return parameterContext.findAnnotation(anno).orElseThrow(() -> new JUnitException(
				"Parameter " + parameterContext.getParameter() + " must be annotated with @" + anno.getSimpleName()));
	}

	private void assertNonFinalField(Field field) {
		if (ReflectionUtils.isFinal(field)) {
			throw new ExtensionConfigurationException(String
					.format("@" + ANNO_TYPE.getSimpleName() + " field [%s] must not be declared as final.", field));
		}
	}

	private void assertSupportedType(String target, Class<?> type) {
		if (type != String.class) {
			throw new ExtensionConfigurationException(
					String.format("Can only resolve @" + ANNO_TYPE.getSimpleName() + " %s of type %s but was: %s",
							target, String.class.getName(), type.getName()));
		}
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		boolean annotated = parameterContext.isAnnotated(ANNO_TYPE);
		if (annotated && parameterContext.getDeclaringExecutable() instanceof Constructor) {
			throw new ParameterResolutionException("@" + ANNO_TYPE.getSimpleName()
					+ " is not supported on constructor parameters. Please use field injection instead.");
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
