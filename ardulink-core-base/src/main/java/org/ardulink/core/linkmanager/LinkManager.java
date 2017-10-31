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

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import static java.lang.String.format;
import static org.ardulink.core.beans.finder.impl.FindByAnnotation.propertyAnnotated;
import static org.ardulink.util.Preconditions.checkArgument;
import static org.ardulink.util.Preconditions.checkNotNull;
import static org.ardulink.util.Preconditions.checkState;
import static org.ardulink.util.Strings.nullOrEmpty;
import static org.ardulink.util.Throwables.propagate;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.ardulink.core.Link;
import org.ardulink.core.beans.Attribute;
import org.ardulink.core.beans.Attribute.AttributeReader;
import org.ardulink.core.beans.BeanProperties;
import org.ardulink.core.beans.BeanProperties.DefaultAttribute;
import org.ardulink.core.linkmanager.LinkConfig.ChoiceFor;
import org.ardulink.core.linkmanager.LinkConfig.I18n;
import org.ardulink.core.linkmanager.LinkConfig.Named;
import org.ardulink.util.Lists;
import org.ardulink.util.Optional;
import org.ardulink.util.Primitive;
import org.ardulink.util.Throwables;
import org.ardulink.util.URIs;

/**
 * [ardulinktitle] [ardulinkversion]
 * 
 * project Ardulink http://www.ardulink.org/
 * 
 * [adsense]
 *
 */
public abstract class LinkManager {

	public interface NumberValidationInfo extends ValidationInfo {
		double min();

		double max();
	}

	public interface ValidationInfo {

		ValidationInfo NULL = new ValidationInfo() {
		};

	}

	public interface ConfigAttribute {

		ConfigAttribute[] EMPTY_ARRAY = new ConfigAttribute[0];

		/**
		 * Returns the name of this attribute. If there is a localized name
		 * available the localized named is returned.
		 * 
		 * @return name of this attribute
		 */
		String getName();

		/**
		 * Returns the description of this attribute. If there is a localized
		 * description available <code>null</code> is returned.
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
		 * Sets the new value of this attribute. If this attribute
		 * hasChoiceValues only one of those values can be set!
		 */
		void setValue(Object value);

		/**
		 * Returns <code>true</code> if this attribute has predefined choice
		 * values.
		 * 
		 * @return <code>true</code> if this attribute has predefined choice
		 *         values
		 * @see #getChoiceValues()
		 */
		boolean hasChoiceValues();

		/**
		 * If the attribute's choice depends on other attribute (the choice can
		 * not be determined before those attributes are filled) those
		 * attributes are returned otherwise an empty array.
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

	}

	public interface Configurer {

		Collection<String> getAttributes();

		ConfigAttribute getAttribute(String key);

		Link newLink();

		/**
		 * Creates an object that identifies the Configurer in its current state
		 * and thus the Link it would create at that moment.
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

		@Override
		public void addAnnotations(Collection<Annotation> annotations) {
			// since this class has no reference to a method or field there are
			// no annos to add
		}
	}

	private static class DefaultConfigurer<T extends LinkConfig> implements
			Configurer {

		public class ConfigAttributeAdapter<T extends LinkConfig> implements
				ConfigAttribute {

			private final Attribute attribute;
			private final Attribute getChoicesFor;
			private final List<ConfigAttribute> dependsOn;
			private List<Object> cachedChoiceValues;
			private final ResourceBundle nls;

			private final Min minValueProvider = annotationProxy(Min.class,
					"value", MIN_VALUE);
			private final Max maxValueProvider = annotationProxy(Max.class,
					"value", MAX_VALUE);

			public ConfigAttributeAdapter(T linkConfig,
					BeanProperties beanProperties, String key) {
				this.attribute = beanProperties.getAttribute(key);
				checkArgument(
						attribute != null,
						"Could not determine attribute %s. Available attributes are %s",
						key, beanProperties.attributeNames());
				this.getChoicesFor = choicesFor(linkConfig);
				this.dependsOn = this.getChoicesFor == null ? Collections
						.<ConfigAttribute> emptyList()
						: resolveDeps(this.getChoicesFor);
				Class<?> linkConfigClass = linkConfig.getClass();
				I18n nls = linkConfigClass.getAnnotation(I18n.class);
				this.nls = nls == null ? null : resourceBundle(linkConfigClass,
						nls);
			}

			private ResourceBundle resourceBundle(Class<?> linkConfigClass,
					I18n nls) {
				String baseName = nullOrEmpty(nls.value()) ? useClassname(linkConfigClass)
						: usePackageAndName(linkConfigClass, nls);
				return ResourceBundle.getBundle(baseName, Locale.getDefault(),
						linkConfigClass.getClassLoader());
			}

			private String useClassname(Class<?> clazz) {
				return clazz.getName();
			}

			private String usePackageAndName(Class<?> clazz, I18n nls) {
				return clazz.getPackage().getName() + "." + nls.value();
			}

			private Attribute choicesFor(T linkConfig) {
				Attribute choiceFor = BeanProperties.builder(linkConfig)
						.using(propertyAnnotated(ChoiceFor.class)).build()
						.getAttribute(attribute.getName());
				if (choiceFor == null && attribute.getType().isEnum()) {
					HardCodedValues reader = new HardCodedValues(
							attribute.getName(), attribute.getType(), attribute
									.getType().getEnumConstants());
					return new DefaultAttribute(attribute.getName(),
							attribute.getType(), reader, null);
				}
				return choiceFor;
			}

			private List<ConfigAttribute> resolveDeps(Attribute choiceFor) {
				ChoiceFor cfa = choiceFor.getAnnotation(ChoiceFor.class);
				if (cfa == null) {
					return Collections.emptyList();
				}
				List<ConfigAttribute> deps = new ArrayList<ConfigAttribute>(
						cfa.dependsOn().length);
				for (String name : cfa.dependsOn()) {
					deps.add(getAttribute(name));
				}
				return deps;
			}

			@Override
			public String getName() {
				String name = this.attribute.getName();
				return getFromBundle(name, name);
			}

			@Override
			public String getDescription() {
				return getFromBundle(this.attribute.getName() + ".description",
						null);
			}

			private String getFromBundle(String bundleKey, String defaultValue) {
				return nls == null || !nls.containsKey(bundleKey) ? defaultValue
						: nls.getString(bundleKey);
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
					throw propagate(e);
				}
			}

			@Override
			public void setValue(Object value) {
				try {
					this.attribute.writeValue(value);
					changed = true;
				} catch (Exception e) {
					throw propagate(e);
				}
			}

			@Override
			public boolean hasChoiceValues() {
				return this.getChoicesFor != null;
			}

			@Override
			public ConfigAttribute[] choiceDependsOn() {
				return this.dependsOn
						.toArray(new ConfigAttribute[this.dependsOn.size()]);
			}

			@Override
			public Object[] getChoiceValues() {
				checkState(hasChoiceValues(),
						"attribute does not have choiceValues");
				try {
					if (this.cachedChoiceValues == null || changed) {
						Object[] value = loadChoiceValues();
						this.cachedChoiceValues = Arrays.asList(value);
						changed = false;
					}
					return this.cachedChoiceValues
							.toArray(new Object[this.cachedChoiceValues.size()]);
				} catch (Exception e) {
					throw propagate(e);
				}
			}

			private Object[] loadChoiceValues() throws Exception {
				Object value = checkNotNull(
						this.getChoicesFor.readValue(),
						"returntype for choice of %s was null (should be an empty Object[] or empty Collection)",
						getName());
				if (value instanceof Collection<?>) {
					Collection<?> collection = (Collection<?>) value;
					value = collection.toArray(new Object[collection.size()]);
				}
				checkState(value instanceof Object[],
						"returntype is not an Object[] but %s",
						value == null ? null : value.getClass());
				return (Object[]) value;
			}

			@Override
			public ValidationInfo getValidationInfo() {
				if (Integer.class.isAssignableFrom(Primitive.wrap(getType()))) {
					Annotation[] annotations = attribute.getAnnotations();
					return newNumberValidationInfo(find(annotations, Min.class)
							.or(minValueProvider).value(),
							find(annotations, Max.class).or(maxValueProvider)
									.value());
				}
				return ValidationInfo.NULL;
			}

			private <S> S annotationProxy(Class<S> clazz,
					final String methodName, final long value) {
				return clazz.cast(Proxy.newProxyInstance(getClass()
						.getClassLoader(), new Class<?>[] { clazz },
						new InvocationHandler() {
							@Override
							public Object invoke(Object proxy, Method method,
									Object[] args) throws Throwable {
								if (methodName.equals(method.getName())) {
									return value;
								}
								throw new UnsupportedOperationException(
										"Method " + method + " not supported");
							}
						}));
			}

			private <S extends Annotation> Optional<S> find(
					Annotation[] annotations, Class<S> annoClass) {
				for (Annotation annotation : annotations) {
					if (annotation.annotationType().equals(annoClass)) {
						return Optional.of(annoClass.cast(annotation));
					}
				}
				return Optional.absent();
			}

			private NumberValidationInfo newNumberValidationInfo(
					final long min, final long max) {
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

		private final LinkFactory<T> linkFactory;
		private final T linkConfig;
		private BeanProperties beanProperties;
		private final Map<String, ConfigAttributeAdapter<T>> cache = new HashMap<String, ConfigAttributeAdapter<T>>();
		private boolean changed = true;

		public DefaultConfigurer(LinkFactory<T> linkFactory) {
			this.linkFactory = linkFactory;
			this.linkConfig = linkFactory.newLinkConfig();
			this.beanProperties = BeanProperties.builder(linkConfig)
					.using(propertyAnnotated(Named.class)).build();
		}

		class CacheKey {

			@SuppressWarnings("rawtypes")
			private final Class<? extends LinkFactory> factoryType;

			private final Map<String, Object> values;

			public CacheKey() throws Exception {
				this.factoryType = DefaultConfigurer.this.linkFactory
						.getClass();
				this.values = Collections.unmodifiableMap(extractData());
			}

			private Map<String, Object> extractData() {
				Map<String, Object> values = new HashMap<String, Object>();
				for (String attribute : DefaultConfigurer.this.getAttributes()) {
					values.put(attribute,
							DefaultConfigurer.this.getAttribute(attribute)
									.getValue());
				}
				return values;
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result
						+ ((factoryType == null) ? 0 : factoryType.hashCode());
				result = prime * result
						+ ((values == null) ? 0 : values.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				CacheKey other = (CacheKey) obj;
				if (factoryType == null) {
					if (other.factoryType != null)
						return false;
				} else if (!factoryType.equals(other.factoryType))
					return false;
				if (values == null) {
					if (other.values != null)
						return false;
				} else if (!values.equals(other.values))
					return false;
				return true;
			}

			@Override
			public String toString() {
				return "CacheKey [factoryType=" + factoryType + ", values="
						+ values + "]";
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
			try {
				return beanProperties.attributeNames();
			} catch (Exception e) {
				throw propagate(e);
			}
		}

		public ConfigAttribute getAttribute(String key) {
			ConfigAttributeAdapter<T> configAttributeAdapter = cache.get(key);
			if (configAttributeAdapter == null) {
				cache.put(key,
						configAttributeAdapter = new ConfigAttributeAdapter<T>(
								linkConfig, beanProperties, key));
			}
			return configAttributeAdapter;
		}

		@Override
		public Link newLink() {
			validate();
			try {
				return this.linkFactory.newLink(this.linkConfig);
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}
		}

		private void validate() {
			for (String name : getAttributes()) {
				ConfigAttribute attribute = getAttribute(name);
				if (attribute.hasChoiceValues()) {
					checkIfValid(attribute);
				}
			}
		}

		private void checkIfValid(ConfigAttribute attribute) {
			Object value = attribute.getValue();
			if (value != null) {
				List<Object> validValues = Arrays.asList(attribute
						.getChoiceValues());
				checkArgument(validValues.contains(value),
						"%s is not a valid value for %s, valid values are %s",
						value, attribute.getName(), validValues);
			}
		}

	}

	private static final String SCHEMA = "ardulink";

	public static LinkManager getInstance() {
		return new LinkManager() {

			@Override
			public List<URI> listURIs() {
				List<LinkFactory> factories = getConnectionFactories();
				List<URI> result = new ArrayList<URI>(factories.size());
				for (LinkFactory<?> factory : factories) {
					result.add(URIs.newURI(format("%s://%s", SCHEMA,
							factory.getName())));
				}
				return result;
			}

			private Optional<LinkFactory<?>> getConnectionFactory(String name) {
				for (LinkFactory<?> connectionFactory : getConnectionFactories()) {
					if (connectionFactory.getName().equals(name)) {
						return Optional.<LinkFactory<?>> of(connectionFactory);
					}
				}
				return Optional.<LinkFactory<?>> absent();
			}

			// of course we also could load the FactoryFactories via
			// serviceloader to enable additional FactoryFactories
			private List<LinkFactory> getConnectionFactories() {
				List<LinkFactory> factories = Lists.newArrayList();
				factories.addAll(new FactoriesViaServiceLoader()
						.loadLinkFactories());
				factories.addAll(new FactoriesViaMetaInfArdulink()
						.loadLinkFactories());
				return factories;
			}

			@Override
			public Configurer getConfigurer(URI uri) {
				String name = checkNotNull(extractNameFromURI(uri), uri
						+ " not a valid URI: Unable not extract name");
				LinkFactory connectionFactory = getConnectionFactory(name)
						.getOrThrow(
								IllegalArgumentException.class,
								"No factory registered for \"%s\", available names are %s",
								name, listURIs());
				DefaultConfigurer defaultConfigurer = new DefaultConfigurer(
						connectionFactory);
				return configure(defaultConfigurer,
						uri.getQuery() == null ? new String[0] : uri.getQuery()
								.split("\\&"));
			}

			private Configurer configure(Configurer configurer, String[] params) {
				for (String param : params) {
					String[] split = param.split("\\=");
					if (split.length == 2) {
						ConfigAttribute attribute = configurer
								.getAttribute(split[0]);
						attribute.setValue(convert(split[1],
								attribute.getType()));
					}
				}
				return configurer;
			}

			private Object convert(String value,
					Class<? extends Object> targetType) {
				if (targetType.isInstance(value)) {
					return value;
				} else if (targetType.isEnum()) {
					@SuppressWarnings("unchecked")
					Class<Enum<?>> enumClass = (Class<Enum<?>>) targetType;
					return enumWithName(enumClass, value);
				} else {
					return Primitive.parseAs(targetType, value);
				}
			}

			private Object enumWithName(Class<Enum<?>> targetType, String value) {
				for (Enum<?> enumConstant : targetType.getEnumConstants()) {
					if (enumConstant.name().equals(value)) {
						return enumConstant;
					}
				}
				return null;
			}

		};
	}

	public static String extractNameFromURI(URI uri) {
		return checkSchema(uri).getHost();
	}

	private static URI checkSchema(URI uri) {
		checkArgument(SCHEMA.equalsIgnoreCase(uri.getScheme()),
				"schema not %s (was %s)", SCHEMA, uri.getScheme());
		return uri;
	}

	/**
	 * Returns a newly created {@link Configurer} for the passed {@link URI}.
	 * Configurers should <b>not</b> be shared amongst threads since there is no
	 * guarantee that they are threadsafe. Beside that their values are
	 * retrieved to calculate cache keys for sharing Link instances which should
	 * not be done in parallel, too.
	 * 
	 * @param uri
	 *            the URI to create the new Configurer for
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