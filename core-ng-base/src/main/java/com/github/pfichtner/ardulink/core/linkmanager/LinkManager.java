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

package com.github.pfichtner.ardulink.core.linkmanager;

import static com.github.pfichtner.beans.finder.impl.FindByAnnotation.propertyAnnotated;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import static java.lang.String.format;
import static org.zu.ardulink.util.Preconditions.checkArgument;
import static org.zu.ardulink.util.Preconditions.checkNotNull;
import static org.zu.ardulink.util.Preconditions.checkState;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.ServiceLoader;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.zu.ardulink.util.Lists;
import org.zu.ardulink.util.Optional;
import org.zu.ardulink.util.Primitive;

import com.github.pfichtner.ardulink.core.Link;
import com.github.pfichtner.ardulink.core.linkmanager.LinkConfig.ChoiceFor;
import com.github.pfichtner.ardulink.core.linkmanager.LinkConfig.I18n;
import com.github.pfichtner.ardulink.core.linkmanager.LinkConfig.Named;
import com.github.pfichtner.beans.Attribute;
import com.github.pfichtner.beans.BeanProperties;

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

		Link newLink() throws Exception;

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

			public ConfigAttributeAdapter(T linkConfig,
					BeanProperties beanProperties, String key) {
				this.attribute = beanProperties.getAttribute(key);
				checkArgument(attribute != null,
						"Could not determine attribute %s", key);
				this.getChoicesFor = BeanProperties.builder(linkConfig)
						.using(propertyAnnotated(ChoiceFor.class)).build()
						.getAttribute(attribute.getName());
				this.dependsOn = this.getChoicesFor == null ? Collections
						.<ConfigAttribute> emptyList()
						: resolveDeps(this.getChoicesFor);
				I18n nls = linkConfig.getClass().getAnnotation(I18n.class);
				this.nls = nls == null ? null : ResourceBundle.getBundle(nls
						.value());
			}

			private List<ConfigAttribute> resolveDeps(Attribute choiceFor) {
				ChoiceFor cfa = choiceFor.getAnnotation(ChoiceFor.class);
				List<ConfigAttribute> deps = new ArrayList<ConfigAttribute>(
						cfa.dependsOn().length);
				for (String name : cfa.dependsOn()) {
					deps.add(getAttribute(name));
				}
				return deps;
			}

			@Override
			public String getName() {
				return nls == null
						|| !nls.containsKey(this.attribute.getName()) ? this.attribute
						.getName() : nls.getString(this.attribute.getName());
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
					throw new RuntimeException(e);
				}
			}

			@Override
			public void setValue(Object value) {
				try {
					this.attribute.writeValue(value);
					changed = true;
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public boolean hasChoiceValues() {
				return this.getChoicesFor != null;
			}

			@Override
			public ConfigAttribute[] choiceDependsOn() {
				return this.dependsOn.toArray(new ConfigAttribute[0]);
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
					throw new RuntimeException(e);
				}
			}

			private Object[] loadChoiceValues() throws Exception {
				Object value = checkNotNull(
						this.getChoicesFor.readValue(),
						"returntype for choice of %s was null (should be an empty Object[] or empty Collection)",
						getName());
				if (value instanceof Collection<?>) {
					value = ((Collection<?>) value).toArray(new Object[0]);
				}
				checkState(value instanceof Object[],
						"returntype is not an Object[] but %s",
						value == null ? null : value.getClass());
				return (Object[]) value;
			}

			@Override
			public ValidationInfo getValidationInfo() {
				if (Integer.class.isAssignableFrom(Primitive.wrap(getType()))) {
					Optional<Min> min = find(attribute.getAnnotations(),
							Min.class);
					Optional<Max> max = find(attribute.getAnnotations(),
							Max.class);
					return newNumberValidationInfo(min.isPresent() ? min.get()
							.value() : MIN_VALUE, max.isPresent() ? max.get()
							.value() : MAX_VALUE);
				}
				return ValidationInfo.NULL;
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

		public DefaultConfigurer(LinkFactory<T> connectionFactory) {
			this.linkFactory = connectionFactory;
			this.linkConfig = connectionFactory.newLinkConfig();
			this.beanProperties = BeanProperties.builder(linkConfig)
					.using(propertyAnnotated(Named.class)).build();
		}

		@Override
		public Collection<String> getAttributes() {
			try {
				return beanProperties.attributeNames();
			} catch (Exception e) {
				throw new RuntimeException(e);
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
		public Link newLink() throws Exception {
			validate();
			return this.linkFactory.newLink(this.linkConfig);
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
					String name = factory.getName();
					try {
						result.add(new URI(format("%s://%s", SCHEMA, name)));
					} catch (URISyntaxException e) {
						throw new RuntimeException(e);
					}
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

			private List<LinkFactory> getConnectionFactories() {
				return Lists.newArrayList(ServiceLoader.load(LinkFactory.class)
						.iterator());
			}

			@Override
			public Configurer getConfigurer(URI uri) {
				String name = extractNameFromURI(uri);
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

			private Object convert(String value, Class<?> targetType) {
				return targetType.isInstance(value) ? value : Primitive
						.parseAs(targetType, value);
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