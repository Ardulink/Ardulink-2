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

package org.ardulink.core.linkmanager;

import static java.lang.String.format;
import static java.net.URI.create;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.toList;
import static org.ardulink.core.beans.finder.impl.FindByAnnotation.propertyAnnotated;
import static org.ardulink.core.linkmanager.Classloaders.moduleClassloader;
import static org.ardulink.util.Numbers.convertTo;
import static org.ardulink.util.Preconditions.checkArgument;
import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Preconditions.checkState;
import static org.ardulink.util.Predicates.attribute;
import static org.ardulink.util.Primitives.findPrimitiveFor;
import static org.ardulink.util.Primitives.wrap;
import static org.ardulink.util.ServiceLoaders.services;
import static org.ardulink.util.Strings.nullOrEmpty;
import static org.ardulink.util.Throwables.propagate;
import static org.ardulink.util.anno.LapsedWith.JDK14;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Negative;
import javax.validation.constraints.NegativeOrZero;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

import org.ardulink.core.Link;
import org.ardulink.core.beans.Attribute;
import org.ardulink.core.beans.Attribute.AttributeReader;
import org.ardulink.core.beans.BeanProperties;
import org.ardulink.core.beans.BeanProperties.DefaultAttribute;
import org.ardulink.core.linkmanager.LinkConfig.ChoiceFor;
import org.ardulink.core.linkmanager.LinkConfig.I18n;
import org.ardulink.core.linkmanager.LinkConfig.Named;
import org.ardulink.core.linkmanager.LinkFactory.Alias;
import org.ardulink.core.linkmanager.providers.LinkFactoriesProvider;
import org.ardulink.util.Numbers;
import org.ardulink.util.anno.LapsedWith;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
@SuppressWarnings("rawtypes")
public abstract class LinkManager {

	public interface NumberValidationInfo extends ValidationInfo {
		double min();

		double max();
	}

	public interface ValidationInfo {

		ValidationInfo NULL = new ValidationInfo() {
		};

	}

	private static class ValidationInfoCreator {

		private final Attribute attribute;
		private final Class<?> wrappedType;

		ValidationInfoCreator(Attribute attribute) {
			this.attribute = attribute;
			this.wrappedType = wrap(attribute.getType());
		}

		private Optional<Long> minValue() {
			Annotation[] annotations = attribute.getAnnotations();
			Optional<Long> min = tryFind(annotations, Min.class).map(Min::value);
			return min.isPresent() //
					? min //
					: contains(annotations, PositiveOrZero.class) //
							? value(0) //
							: contains(annotations, Positive.class) ? value(+1) : empty();
		}

		private Optional<Long> maxValue() {
			Annotation[] annotations = attribute.getAnnotations();
			Optional<Long> max = tryFind(annotations, Max.class).map(Max::value);
			return max.isPresent() //
					? max //
					: contains(annotations, NegativeOrZero.class) //
							? value(0) //
							: contains(annotations, Negative.class) ? value(-1) : empty();
		}

		private static Optional<Long> value(long value) {
			return Optional.of(value);
		}

		private <S extends Annotation> Optional<S> tryFind(Annotation[] annotations, Class<S> annoClass) {
			return stream(annotations) //
					.filter(attribute(Annotation::annotationType, isEqual(annoClass))) //
					.findFirst() //
					.map(annoClass::cast);
		}

		private <S extends Annotation> boolean contains(Annotation[] annotations, Class<S> annoClass) {
			return tryFind(annotations, annoClass).isPresent();
		}

		ValidationInfo create() {
			Optional<Long> minValue = minValue();
			Optional<Long> maxValue = maxValue();
			// TODO What to define as min/max for fps? MAX_VALUE/-MAX_VALUE?
			if (typeIs(Character.class)) {
				return newNumberValidationInfo(minValue.orElse((long) Character.MIN_VALUE),
						maxValue.orElse((long) Character.MAX_VALUE));
			} else if (typeIs(Double.class)) {
				return newNumberValidationInfo(minValue.map(Number::doubleValue).orElse(Double.NaN),
						maxValue.map(Number::doubleValue).orElse(Double.NaN));
			} else if (typeIs(Float.class)) {
				return newNumberValidationInfo(minValue.map(Number::floatValue).orElse(Float.NaN),
						maxValue.map(Number::floatValue).orElse(Float.NaN));
			} else if (typeIs(Number.class)) {
				@SuppressWarnings("unchecked")
				Numbers numberType = Numbers.numberType((Class<Number>) wrappedType);
				return newNumberValidationInfo(minValue.orElse(convertTo(numberType.min(), Long.class)),
						maxValue.orElse(convertTo(numberType.max(), Long.class)));
			}
			return ValidationInfo.NULL;
		}

		private boolean typeIs(Class<?> type) {
			return type.isAssignableFrom(wrappedType);
		}

		NumberValidationInfo newNumberValidationInfo(double min, double max) {
			return new NumberValidationInfo() {

				@Override
				public double min() {
					return min;
				}

				@Override
				public double max() {
					return max;
				}
			};
		}

	}

	public interface ConfigAttribute {

		/**
		 * Returns the name of this attribute. If there is a localized name available
		 * the localized named is returned.
		 * 
		 * @return name of this attribute
		 */
		String getName();

		/**
		 * Returns the localized description of this attribute. Returns
		 * <code>null</code> if there is no localized description available.
		 * 
		 * @return description of this attribute
		 */
		String getDescription();

		/**
		 * Returns the type of this attribute.
		 * 
		 * @return type
		 */
		Class<?> getType();

		/**
		 * Returns the current value of this attribute.
		 * 
		 * @return current value
		 */
		Object getValue();

		/**
		 * Sets the new value of this attribute. If this attribute hasChoiceValues the
		 * value is <b>not</b> checked here for validity!
		 */
		void setValue(Object value);

		/**
		 * Returns <code>true</code> if this attribute has predefined choice values.
		 * 
		 * @return <code>true</code> if this attribute has predefined choice values
		 * @see #getChoiceValues()
		 */
		boolean hasChoiceValues();

		/**
		 * If the attribute's choice depends on other attribute (the choice can not be
		 * determined before those attributes are filled) those attributes are returned
		 * otherwise an empty array.
		 * 
		 * @return dependencies or empty array
		 */
		ConfigAttribute[] choiceDependsOn();

		/**
		 * Returns the choice values (if any) of this attribute.
		 * 
		 * @return the available choice values
		 * @see #hasChoiceValues()
		 */
		Object[] getChoiceValues();

		ValidationInfo getValidationInfo();

		String getChoiceDescription(Object value);

	}

	public interface Configurer {

		Collection<String> getAttributes();

		ConfigAttribute getAttribute(String key);

		Link newLink();

		/**
		 * Creates an object that identifies the Configurer in its current state and
		 * thus the Link it would create at that moment.
		 * 
		 * @return identifier for the Configurer and its state.
		 */
		Object uniqueIdentifier();

	}

	private static final class HardCodedValues implements AttributeReader {

		private final String name;
		private final Class<?> type;
		private final Object value;

		public HardCodedValues(String name, Class<?> type, Object value) {
			this.name = name;
			this.type = type;
			this.value = value;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public Class<?> getType() {
			return type;
		}

		@Override
		public Object getValue() throws Exception {
			return value;
		}

	}

	static class DefaultConfigurer<T extends LinkConfig> implements Configurer {

		class ConfigAttributeAdapter<C extends LinkConfig> implements ConfigAttribute {

			private final Attribute attribute;
			private final Attribute getChoicesFor;
			private final List<ConfigAttribute> dependsOn;
			private List<Object> cachedChoiceValues;
			private final ResourceBundle nls;

			public ConfigAttributeAdapter(C linkConfig, BeanProperties beanProperties, String key) {
				this.attribute = beanProperties.getAttribute(key);
				checkArgument(attribute != null, "Could not determine attribute %s. Available attributes are %s", key,
						beanProperties.attributeNames());
				this.getChoicesFor = choicesFor(linkConfig);
				this.dependsOn = this.getChoicesFor == null ? emptyList() : resolveDeps(this.getChoicesFor);
				Class<?> linkConfigClass = linkConfig.getClass();
				I18n nls = linkConfigClass.getAnnotation(I18n.class);
				this.nls = nls == null ? null : resourceBundle(linkConfigClass, nls);
			}

			private ResourceBundle resourceBundle(Class<?> linkConfigClass, I18n nls) {
				String baseName = nullOrEmpty(nls.value()) ? useClassname(linkConfigClass)
						: usePackageAndName(linkConfigClass, nls);
				return ResourceBundle.getBundle(baseName, Locale.getDefault(), linkConfigClass.getClassLoader());
			}

			private String useClassname(Class<?> clazz) {
				return clazz.getName();
			}

			private String usePackageAndName(Class<?> clazz, I18n nls) {
				return clazz.getPackage().getName() + "." + nls.value();
			}

			private Attribute choicesFor(C linkConfig) {
				Attribute choiceFor = BeanProperties.builder(linkConfig).using(propertyAnnotated(ChoiceFor.class))
						.build().getAttribute(attribute.getName());
				return choiceFor == null && attribute.getType().isEnum()
						? new DefaultAttribute(attribute.getName(), attribute.getType(), hardCodedValues(), null)
						: choiceFor;
			}

			private AttributeReader hardCodedValues() {
				return new HardCodedValues(attribute.getName(), attribute.getType(),
						attribute.getType().getEnumConstants());
			}

			private List<ConfigAttribute> resolveDeps(Attribute choiceFor) {
				ChoiceFor cfa = choiceFor.getAnnotation(ChoiceFor.class);
				return cfa == null ? emptyList()
						: stream(cfa.dependsOn()).map(name -> getAttribute(name)).collect(toList());
			}

			@Override
			public String getName() {
				String name = this.attribute.getName();
				return getFromBundle(name, name);
			}

			@Override
			public String getDescription() {
				return getFromBundle(this.attribute.getName() + ".description", null);
			}

			@Override
			public String getChoiceDescription(Object value) {
				return getFromBundle(this.attribute.getName() + "." + value, null);
			}

			private String getFromBundle(String bundleKey, String defaultValue) {
				return nls == null || !nls.containsKey(bundleKey) ? defaultValue : nls.getString(bundleKey);
			}

			@Override
			public Class<?> getType() {
				return attribute.getType();
			}

			@Override
			public Object getValue() {
				try {
					return this.attribute.readValue();
				} catch (Exception e) {
					throw new IllegalStateException("Error reading attribute " + this.attribute.getName(), e);
				}
			}

			@Override
			public void setValue(Object value) {
				try {
					this.attribute.writeValue(value);
					changed = true;
				} catch (Exception e) {
					throw new IllegalStateException("Error writing attribute " + this.attribute.getName(), e);
				}
			}

			@Override
			public boolean hasChoiceValues() {
				return this.getChoicesFor != null;
			}

			@Override
			public ConfigAttribute[] choiceDependsOn() {
				return this.dependsOn.toArray(new ConfigAttribute[this.dependsOn.size()]);
			}

			@Override
			public Object[] getChoiceValues() {
				checkState(hasChoiceValues(), "attribute does not have choiceValues");
				try {
					if (this.cachedChoiceValues == null || changed) {
						this.cachedChoiceValues = asList(loadChoiceValues());
						changed = false;
					}
					return this.cachedChoiceValues.toArray(new Object[this.cachedChoiceValues.size()]);
				} catch (Exception e) {
					throw propagate(e);
				}
			}

			private Object[] loadChoiceValues() throws Exception {
				Object value = checkNotNull(this.getChoicesFor.readValue(),
						"returntype for choice of %s was null (should be empty Collection, empty Stream or an empty Object[])",
						getName());
				if (value instanceof Collection<?>) {
					Collection<?> collection = (Collection<?>) value;
					value = collection.toArray(new Object[collection.size()]);
				}
				if (value instanceof Stream<?>) {
					try (Stream<?> stream = (Stream<?>) value) {
						value = stream.toArray();
					}
				}
				checkState(value instanceof Object[], "returntype is not a Collection, Stream or Object[] but %s",
						value == null ? null : value.getClass());
				return (Object[]) value;
			}

			@Override
			public ValidationInfo getValidationInfo() {
				return new ValidationInfoCreator(attribute).create();
			}

		}

		private final LinkFactory<T> linkFactory;
		private final T linkConfig;
		private final BeanProperties beanProperties;
		private final Map<String, ConfigAttributeAdapter<T>> cache = new HashMap<>();
		private boolean changed = true;

		public DefaultConfigurer(LinkFactory<T> linkFactory) {
			this.linkFactory = linkFactory;
			this.linkConfig = linkFactory.newLinkConfig();
			this.beanProperties = BeanProperties.builder(linkConfig).using(propertyAnnotated(Named.class)).build();
		}

		@LapsedWith(value = JDK14, module = "records")
		final class CacheKey {

			private final Class<? extends LinkFactory> factoryType;

			private final Map<String, Object> values;

			public CacheKey() throws Exception {
				this.factoryType = DefaultConfigurer.this.linkFactory.getClass();
				this.values = Collections.unmodifiableMap(extractData());
			}

			private Map<String, Object> extractData() {
				// values can be null, https://bugs.openjdk.java.net/browse/JDK-8148463
				return DefaultConfigurer.this.getAttributes().stream().collect(HashMap::new,
						(m, v) -> m.put(v, getAttribute(v).getValue()), HashMap::putAll);
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((factoryType == null) ? 0 : factoryType.hashCode());
				result = prime * result + ((values == null) ? 0 : values.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj) {
					return true;
				}
				if ((obj == null) || (getClass() != obj.getClass())) {
					return false;
				}
				@SuppressWarnings("unchecked")
				CacheKey other = (CacheKey) obj;
				if (factoryType == null) {
					if (other.factoryType != null) {
						return false;
					}
				} else if (!factoryType.equals(other.factoryType)) {
					return false;
				}
				return values == null ? other.values == null : values.equals(other.values);
			}

			@Override
			public String toString() {
				return "CacheKey [factoryType=" + factoryType + ", values=" + values + "]";
			}

		}

		@Override
		public Object uniqueIdentifier() {
			try {
				return new CacheKey();
			} catch (Exception e) {
				throw propagate(e);
			}
		}

		@Override
		public Collection<String> getAttributes() {
			return beanProperties.attributeNames();
		}

		@Override
		public ConfigAttribute getAttribute(String key) {
			return cache.computeIfAbsent(key, k -> new ConfigAttributeAdapter<>(linkConfig, beanProperties, k));

		}

		@Override
		public Link newLink() {
			validate();
			try {
				return this.linkFactory.newLink(this.linkConfig);
			} catch (Exception e) {
				throw propagate(e);
			}
		}

		private void validate() {
			getAttributes().stream().map(this::getAttribute).filter(ConfigAttribute::hasChoiceValues)
					.forEach(this::checkIfValid);
		}

		private void checkIfValid(ConfigAttribute attribute) {
			Object value = attribute.getValue();
			if (value != null) {
				List<Object> validValues = asList(attribute.getChoiceValues());
				checkArgument(validValues.contains(value), "'%s' is not a valid value for %s, valid values are %s",
						value, attribute.getName(), validValues);
			}
		}

	}

	public static final String ARDULINK_SCHEME = "ardulink";

	private static final LinkManager instance = new LinkManager() {

		@Override
		public List<URI> listURIs() {
			return getConnectionFactories().map(f -> create(format("%s://%s", ARDULINK_SCHEME, f.getName())))
					.collect(toList());
		}

		private Optional<LinkFactory> getConnectionFactory(String name) {
			List<LinkFactory> connectionFactories = getConnectionFactories().collect(toList());
			BiFunction<String, List<LinkFactory>, Optional<LinkFactory>> function1 = (t, u) -> getByName(t, u);
			BiFunction<String, List<LinkFactory>, Optional<LinkFactory>> function2 = (t, u) -> getByAlias(t, u);
			return Stream.of(function1, function2).map(f -> f.apply(name, connectionFactories))
					.filter(Optional::isPresent).map(Optional::get).findFirst();
		}

		private Optional<LinkFactory> getByName(String name, List<LinkFactory> connectionFactories) {
			return connectionFactories.stream().filter(attribute(LinkFactory::getName, isEqual(name))).findFirst();
		}

		private Optional<LinkFactory> getByAlias(String name, List<LinkFactory> connectionFactories) {
			return connectionFactories.stream().filter(f -> hasAliasNamed(name, f)).findFirst();
		}

		private boolean hasAliasNamed(String name, LinkFactory factory) {
			Alias alias = factory.getClass().getAnnotation(LinkFactory.Alias.class);
			return alias != null && asList(alias.value()).contains(name);
		}

		private Stream<LinkFactory> getConnectionFactories() {
			return services(LinkFactoriesProvider.class, moduleClassloader()).stream()
					.map(LinkFactoriesProvider::loadLinkFactories).flatMap(Collection::stream);
		}

		@Override
		public Configurer getConfigurer(URI uri) {
			String name = checkNotNull(extractNameFromURI(uri), "%s not a valid URI: Unable not extract name", uri);
			LinkFactory connectionFactory = getConnectionFactory(name).orElseThrow(() -> new IllegalArgumentException(
					format("No factory registered for '%s', available names are %s", name, listURIs())));
			@SuppressWarnings("unchecked")
			Configurer configurer = new DefaultConfigurer(connectionFactory);
			return uri.getQuery() == null ? configurer : configure(configurer, uri.getQuery().split("\\&"));
		}

		private Configurer configure(Configurer configurer, String[] params) {
			stream(params).map(p -> p.split("\\=", 2)).filter(s -> s.length == 2).forEach(s -> {
				ConfigAttribute attribute = configurer.getAttribute(s[0]);
				attribute.setValue(nullEmptyString(convert(s[1], attribute.getType())));
			});
			return configurer;
		}

		private Object nullEmptyString(Object value) {
			return "".equals(value) ? null : value;
		}

		private Object convert(String value, Class<?> targetType) {
			if (targetType.isInstance(value)) {
				return value;
			} else if (targetType.isEnum()) {
				@SuppressWarnings("unchecked")
				Class<Enum<?>> enumClass = (Class<Enum<?>>) targetType;
				return enumWithName(enumClass, value);
			} else {
				return findPrimitiveFor(targetType).map(p -> nullOrEmpty(value) ? p.defaultValue() : p.parse(value))
						.orElse(value);
			}
		}

		private Object enumWithName(Class<Enum<?>> targetType, String value) {
			return stream(targetType.getEnumConstants()).filter(attribute(Enum::name, isEqual(value))).findFirst()
					.orElse(null);
		}

	};

	public static LinkManager getInstance() {
		return instance;
	}

	public static String extractNameFromURI(URI uri) {
		return checkSchema(uri).getHost();
	}

	public static URI replaceName(URI uri, String name) {
		try {
			return new URI(uri.getScheme(), uri.getUserInfo(), name, uri.getPort(), uri.getPath(), uri.getQuery(),
					uri.getFragment());
		} catch (URISyntaxException e) {
			throw propagate(e);
		}
	}

	private static URI checkSchema(URI uri) {
		String scheme = uri.getScheme();
		checkArgument(ARDULINK_SCHEME.equalsIgnoreCase(scheme), "scheme not %s (was %s)", ARDULINK_SCHEME, scheme);
		return uri;
	}

	/**
	 * Returns a newly created {@link Configurer} for the passed {@link URI}.
	 * Configurers should <b>not</b> be shared amongst threads since there is no
	 * guarantee that they are threadsafe. Beside that their values are retrieved to
	 * calculate cache keys for sharing Link instances which should not be done in
	 * parallel, too.
	 * 
	 * @param uri the URI to create the new Configurer for
	 * @return newly created Configurer for the passed URI
	 */
	public abstract Configurer getConfigurer(URI uri);

	/**
	 * List all available (registered) URIs. Can be empty if no factory is
	 * registered but never is <code>null</code>.
	 * 
	 * @return all available URIs.
	 */
	public abstract List<URI> listURIs();

}
